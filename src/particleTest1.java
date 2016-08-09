

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.xml.XMLElement;
import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import de.fhpotsdam.unfolding.Map;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Yahoo;
import de.fhpotsdam.unfolding.utils.DebugDisplay;
import de.fhpotsdam.unfolding.utils.MapUtils;

import toxi.geom.*;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;

import toxi.sim.fluids.*;

import SearchGeometry.*;


@SuppressWarnings("serial")
public class particleTest1 extends PApplet {

	public static Logger log = Logger.getLogger(isoTest.class);

	Map map;
	EventDispatcher eventDispatcher;

	List<Location> rssGeoLocations = new ArrayList<Location>();
	
	// ISO
	IsoContour iso;
	int isoThreshold=100;
	
	// TOXI
	int NUM_PARTICLES = 8000;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;
	
	List<AttractionBehavior> geoAttractor = new ArrayList<AttractionBehavior>();
	
	ArrayList<NodeAttr> nodes = new ArrayList<NodeAttr>();
	
	//Map<Location, AttractionBehavior> attractionMap = new HashMap();

	Vec2D mousePos;
	
	DebugDisplay debugDisplay;
	
	GLGraphicsOffScreen layer;
	GLTexture srcTex, bloomMask, destTex;
	GLTexture tex0, tex2, tex4, tex8, tex16;
	GLTexture tmp2, tmp4, tmp8, tmp16;
	GLTextureFilter extractBloom, blur, blend4, toneMap;
	
	PVector lastPos;

	public void setup() {
		// Padding in Abstrac
		size(1024, 768, GLConstants.GLGRAPHICS);
		//smooth();
		frameRate(700);
		// Creates default mapDisplay
		//map = new Map(this, "map", 50, 50, 700, 500);
		map = new Map(this, 0, 0, width, height, new Microsoft.RoadProvider());
		//map.setTweening(true);
		//map.zoomToLevel(12);
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 11);
		map.mapDisplay.grid_padding = 1;

		// Creates default dispatcher
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);

		loadRSSGeoLocations();
		
		// TOXI
		physics = new VerletPhysics2D();
		physics.setDrag(0.1f);
		physics.setWorldBounds(new Rect(0, 0, width, height));
		// the NEW way to add gravity to the simulation, using behaviors
		//physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		addParticle();
		noStroke();
		fill(0,0,0,100);
		
		// BUFFER
		layer = new GLGraphicsOffScreen(this, width, height);  
		
		
		iso = new IsoContour(this, new PVector(0,0), new PVector(width,height), 40,40);
		debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250, 150);
		
		// Loading required filters.
		 extractBloom = new GLTextureFilter(this, "ExtractBloom.xml");
		 blur = new GLTextureFilter(this, "Blur.xml");
		 blend4 = new GLTextureFilter(this, "Blend4.xml");  
		 toneMap = new GLTextureFilter(this, "ToneMap.xml");
		   
		 destTex = new GLTexture(this, width, height);
		 
		 // Initializing bloom mask and blur textures.
		 bloomMask = new GLTexture(this, width, height, GLTexture.FLOAT);
		 tex0 = new GLTexture(this, width, height, GLTexture.FLOAT);
		 tex2 = new GLTexture(this, width / 2, height / 2, GLTexture.FLOAT);
		 tmp2 = new GLTexture(this, width / 2, height / 2, GLTexture.FLOAT); 
		 tex4 = new GLTexture(this, width / 4, height / 4, GLTexture.FLOAT);
		 tmp4 = new GLTexture(this, width / 4, height / 4, GLTexture.FLOAT);
		 tex8 = new GLTexture(this, width / 8, height / 8, GLTexture.FLOAT);
		 tmp8 = new GLTexture(this, width / 8, height / 8, GLTexture.FLOAT); 
		 tex16 = new GLTexture(this, width / 16, height / 16, GLTexture.FLOAT);
		 tmp16 = new GLTexture(this, width / 16, height / 16, GLTexture.FLOAT);
		

		
	}
	
	public void addParticle() {
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			

			NodeAttr n = new NodeAttr(new Vec2D(xy[0],xy[1]),physics);
			nodes.add(n);
			
			
		}
		println("particles:"+nodes.size());
	}

	private void loadRSSGeoLocations() {
		// Load RSS feed

		String url = "test.xml";
		
		XMLElement rss = new XMLElement(this, url);
		// Get all items
		XMLElement[] itemXMLElements = rss.getChildren("item");
		//println(itemXMLElements);
		//for (int i = 0; i < itemXMLElements.length; i++) {
		for (int i = 0; i < itemXMLElements.length; i++) {
		
			// Adds lat,lon as locations for each item
			float lat = itemXMLElements[i].getFloat("lat");
			float lng = itemXMLElements[i].getFloat("lng");
			println(itemXMLElements[i]);
			
//			XMLElement lonXML = itemXMLElements[i].getChild("geo:long");
//			float lat = Float.valueOf(latXML.getContent());
//			float lon = Float.valueOf(lonXML.getContent());

			rssGeoLocations.add(new Location(lat, lng));
		}
		println("loc:"+rssGeoLocations.size());
	}

	public void draw() {
		background(0);
		//iso.clear();
		map.draw();
		
		
		
		if(!map.getTopLeftBorder().equals(lastPos)){
			fill(255,255,0);
			int i=0;
			
			for (Location location : rssGeoLocations) {
				float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
				nodes.get(i).setSpring(xy);
				//geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));

				//VerletParticle2D p = physics.particles.get(i);
				//p.removeAllBehaviors();
				//geoAttractor.get(i).getAttractor().set(xy[0], xy[1]);
				//println(p.behaviors.size());
				
				//p.removeAllBehaviors();
				//p.addBehavior(new AttractionBehavior(new Vec2D(xy[0], xy[1]),2000, 1.0f, 0.00000000001f));
			//	if(xy[0]<0 || xy[1]<0 || xy[0]>width || xy[1]>height){
					
//					if(!p.isLocked()){
//						p.lock();
//					}
					
			//	} else {
					//geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));
					//physics.particles.get(i).unlock();
//					if(p.isLocked()){
//						p.unlock();
//					}
					//geoAttractor.get(i).getAttractor().set(xy[0], xy[1]).jitter(0.00001f);
					//p.removeAllBehaviors();
					//p.addBehavior(new AttractionBehavior(new Vec2D(xy[0], xy[1]),2000, 1.0f, 0.00001f));
					//float pxy[]=p.toArray();
					//triangle(pxy[0], pxy[1], pxy[0]+5,pxy[1], pxy[0],pxy[1]+5);
		//		}
				//mousePos = new Vec2D(xy[0], xy[1]);
				// create a new positive attraction force field around the mouse position (radius=250px)
				  //mouseAttractor = new AttractionBehavior(mousePos, 250, 0.9f);
				//physics.addBehavior(mouseAttractor);
				//physics.particles.get(i).set(xy[0], xy[1]);
				//physics.particles.get(i).addBehavior(new AttractionBehavior(new Vec2D(xy[0], xy[1]),10, 0.9f));
				//physics.particles.get(i).
				i++;
			}
			
			lastPos=map.getTopLeftBorder();
		} else {
			fill(0);
		}
		// draw the locations
		
		
		//Map<Location, Particle> particleMap = new HashMap();
		//Particle particle = particleMap.get(location)
		
		
		/*
		if (physics.particles.size() < NUM_PARTICLES) {
			addParticle();
		}*/
		physics.update();
		srcTex = layer.getTexture();
		
		layer.beginDraw();
		layer.background(0);
		layer.beginGL();
		//

		for(NodeAttr n : nodes) {
			
			n.draw(layer);
			
			//iso.addPoint( new PVector( p.x, p.y, 0 ));
		}
		layer.endGL();
		layer.endDraw();
		
		float fx = constrain((mouseX) / width, 0.01f, 1);
		 float fy = (mouseY) / height;
		
		//extractBloom.setParameterValue("bright_threshold", fx);
		// extractBloom.apply(srcTex, tex0);

		 // Downsampling with blur
		 srcTex.filter(blur, tex2);
		 tex2.filter(blur, tmp2);        
		 tmp2.filter(blur, tex2);
		    
		 tex2.filter(blur, tex4);        
		 tex4.filter(blur, tmp4);
		 tmp4.filter(blur, tex4);            
		 tex4.filter(blur, tmp4);
		 tmp4.filter(blur, tex4);            
		    
		 tex4.filter(blur, tex8);        
		 tex8.filter(blur, tmp8);
		 tmp8.filter(blur, tex8);        
		 tex8.filter(blur, tmp8);
		 tmp8.filter(blur, tex8);        
		 tex8.filter(blur, tmp8);
		 tmp8.filter(blur, tex8);

		 tex8.filter(blur, tex16);     
		 tex16.filter(blur, tmp16);
		 tmp16.filter(blur, tex16);        
		 tex16.filter(blur, tmp16);
		 tmp16.filter(blur, tex16);        
		 tex16.filter(blur, tmp16);
		 tmp16.filter(blur, tex16);
		 tex16.filter(blur, tmp16);
		 tmp16.filter(blur, tex16);  
		 
		 // Blending downsampled textures.
		 blend4.apply(new GLTexture[]{tex2, tex4, tex8, tex16}, new GLTexture[]{bloomMask});
		 
		 // Final tone mapping into destination texture.
		 toneMap.setParameterValue("exposure", fy);
		 toneMap.setParameterValue("bright", fx);
		 toneMap.apply(new GLTexture[]{srcTex, bloomMask}, new GLTexture[]{destTex});
		
		 image(bloomMask, 0, 0, width, height);
		/*
		noFill();
		stroke(0,50);
		iso.plotGrid();
		//iso.lookUpAndDraw(width, height, 5, 5, 1.0f);
		*/
		
		
		// draw the iso
		//fill(255,0,0,100);
		//noStroke();
		//iso.plot( isoThreshold/10000.0f );
		//iso.
		//iso.lookUpAndDraw(100, 100, 20, 20, isoThreshold/10000.0f);
		
		
		
		
		//debugDisplay.draw();
		// LOG4J
		if(frameCount % 20==0){
			println(frameRate);
		}
		//(frameRate);
	}

	public void keyPressed() {
		PVector rotateCenter = new PVector(mouseX, mouseY);
		map.mapDisplay.setTransformationCenter(rotateCenter);
		
		if (key == 'r') {
			map.rotate(-PI/15);
		} else if (key == 'l') {
			map.rotate(PI/15);
		} else if (key == 'a') {
			isoThreshold+=20;
		} else if (key == 'y') {
			isoThreshold-=20;
		}
		
		println(isoThreshold);
		
		
	}
	
	/*
	public void mousePressed() {
	  mousePos = new Vec2D(mouseX, mouseY);
	  // create a new positive attraction force field around the mouse position (radius=250px)
	  mouseAttractor = new AttractionBehavior(mousePos, 250, 0.9f);
	  physics.addBehavior(mouseAttractor);
	}
*/

}
