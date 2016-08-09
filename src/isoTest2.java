

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.xml.XMLElement;
import codeanticode.glgraphics.GLConstants;
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
public class isoTest2 extends PApplet {

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
	
	//Map<Location, AttractionBehavior> attractionMap = new HashMap();

	Vec2D mousePos;
	
	DebugDisplay debugDisplay;
	
	PGraphicsOpenGL layer;
	
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
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 9);
		map.mapDisplay.grid_padding = 1;

		// Creates default dispatcher
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);

		loadRSSGeoLocations();
		
		// TOXI
		physics = new VerletPhysics2D();
		physics.setDrag(0.05f);
		physics.setWorldBounds(new Rect(0, 0, width, height));
		// the NEW way to add gravity to the simulation, using behaviors
		//physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		addParticle();
		noStroke();
		fill(0,0,0,100);
		
		// BUFFER
		layer = (PGraphicsOpenGL) g;
		
		
		iso = new IsoContour(this, new PVector(0,0), new PVector(width,height), 40,40);
		debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250, 150);
		
		
		
	}
	
	public void addParticle() {
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			
			VerletParticle2D p = new VerletParticle2D(new Vec2D(xy[0],xy[1]));
			AttractionBehavior attr = new AttractionBehavior(new Vec2D(xy[0], xy[1]),2000, 0.9f, 0.0000001f);
			//VerletSpring2D spring = new VerletSpring2D()
			
			p.addBehavior(attr);
			geoAttractor.add(attr);
			physics.addParticle(p);
			// add a negative attraction force field around the new particle
			//physics.addBehavior(new AttractionBehavior(p, 20, -1.2f, 0.01f));
		}
	  
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
		println("particles:"+rssGeoLocations.size());
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
				
				//geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));

				VerletParticle2D p = physics.particles.get(i);
				//p.removeAllBehaviors();
				geoAttractor.get(i).getAttractor().set(xy[0], xy[1]);
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
		
		for (VerletParticle2D p : physics.particles) {
			if(p.x==0 || p.y==0 || p.x==width || p.y==height){
				
			} else {
				//ellipse(p.x, p.y, 15, 15);
				triangle(p.x, p.y, p.x+10,p.y, p.x, p.y+10);
				// Add points to the iso class
				//iso.addPoint( new PVector( p.x, p.y, 0 ));
			}
			
		}
		
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
