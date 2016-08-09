
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

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
import de.fhpotsdam.unfolding.mapdisplay.MapDisplayFactory;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
//import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Yahoo;
import de.fhpotsdam.unfolding.utils.DebugDisplay;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;

import toxi.geom.*;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;

//import toxi.sim.fluids.*;

import SearchGeometry.*;

@SuppressWarnings("serial")
public class particleTest1mbtiles extends PApplet {

	public static Logger log = Logger.getLogger(isoTest.class);

	Map map;
	EventDispatcher eventDispatcher;

	List<Location> rssGeoLocations = new ArrayList<Location>();

	// ISO
	IsoContour iso;
	int isoThreshold = 100;

	// TOXI
	int NUM_PARTICLES = 8000;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;

	List<AttractionBehavior> geoAttractor = new ArrayList<AttractionBehavior>();

	ArrayList<NodeAttr> nodes = new ArrayList<NodeAttr>();

	// Map<Location, AttractionBehavior> attractionMap = new HashMap();

	Vec2D mousePos;

	DebugDisplay debugDisplay;

	GLGraphicsOffScreen layer;
	GLTexture srcTex, bloomMask, destTex, darkLayer;
	GLTexture tex0, tex2, tex4, tex8, tex16;
	GLTexture tmp2, tmp4, tmp8, tmp16;
	GLTextureFilter extractBloom, blur,blur2, blend4, toneMap, maskFilter;

	PVector lastPos;
	
	GL gl;
	PGraphicsOpenGL pgl;
	
	float maskFactor=0f;
	float bloomFactor=1.5f;
	float zoom, lastZoom,particleZoom, bloomZoom;

	boolean zoomChange=false;

	public static final String JDBC_CONN_STRING_MAC = "jdbc:sqlite:data/test.mbtiles";

	public static void main(String[] args) {

		PApplet.main(new String[] { "particleTest1mbtiles" }); // Der letzte
																// String muss
																// der Pfad zu
																// Deiner Klasse
																// sein

	}

	public void setup() {
		// Padding in Abstrac
		size(1024, 768, GLConstants.GLGRAPHICS);
		
		pgl = (PGraphicsOpenGL) g;
		gl = pgl.gl;
		
		/*
		hint(DISABLE_OPENGL_2X_SMOOTH);
		hint(ENABLE_OPENGL_4X_SMOOTH);
		hint(ENABLE_NATIVE_FONTS);
		hint(ENABLE_ACCURATE_TEXTURES);
		hint(DISABLE_DEPTH_TEST);
		hint(DISABLE_OPENGL_ERROR_REPORT);
		*/
		
		// smooth();
		frameRate(700);
		// Creates default mapDisplay
		// map = new Map(this, "map", 50, 50, 700, 500);
		// map = new Map(this, 0, 0, width, height, new MBTilesMapProvider(JDBC_CONN_STRING_MAC));
		//map = new Map(this, 0, 0, width, height, new Microsoft.RoadProvider());
		
		map = new Map(this, "map", 10, 10, width, height, true, false,
				new OpenStreetMap.CloudmadeProvider(MapDisplayFactory.OSM_API_KEY, 41009));
		map.setTweening(false);
		
		//41009
		//41010
		
		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				mouseWheel(evt.getWheelRotation());
			}
		});
		
		//41010

		map.setTweening(false);
		// map.zoomToLevel(12);
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 14);
		map.mapDisplay.grid_padding = 1;

		// Creates default dispatcher
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);

		loadRSSGeoLocations();

		// TOXI
		physics = new VerletPhysics2D();
		physics.setDrag(0.05f);
		physics.setWorldBounds(new Rect(0, 0, width, height));
		// the NEW way to add gravity to the simulation, using behaviors
		// physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		//addParticle();
		noStroke();
		fill(0, 0, 0, 100);

		// BUFFER
		layer = new GLGraphicsOffScreen(this, width, height);

		iso = new IsoContour(this, new PVector(0, 0),
				new PVector(width, height), 40, 40);
		debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250,
				150);

		// Loading required filters.
		extractBloom = new GLTextureFilter(this, "ExtractBloomBG.xml");
		blur = new GLTextureFilter(this, "Blur.xml");
		blur2 = new GLTextureFilter(this, "Blur1D.xml");
		blend4 = new GLTextureFilter(this, "Blend4.xml");
		toneMap = new GLTextureFilter(this, "ToneMap.xml");

		destTex = new GLTexture(this, width, height);
		darkLayer = new GLTexture(this, width, height);

		maskFilter = new GLTextureFilter(this, "Mask.xml");

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
		
		
	
		
		//gl.glDisable(GLConstants.GL_DEPTH_TEST);
	}

	public void addParticle() {
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);

			NodeAttr n = new NodeAttr(this);
			nodes.add(n);

		}
		println("particles:" + nodes.size());
	}
	void addParticleFly() {
		float xy[] = map.mapDisplay.getScreenPositionFromLocation(rssGeoLocations.get(nodes.size()));
		  NodeAttr n = new NodeAttr(this);
		  n.setSpring(xy,true);
		  nodes.add(n);
		  
		}

	private void loadRSSGeoLocations() {
		// Load RSS feed

		//String url = "test.xml";
		String url = "02_fake_Consoli.xml";
		
		XMLElement rss = new XMLElement(this, url);
		// Get all items
		XMLElement[] itemXMLElements = rss.getChildren("item");
		// println(itemXMLElements);
		// for (int i = 0; i < itemXMLElements.length; i++) {
		for (int i = 0; i < itemXMLElements.length; i++) {

			// Adds lat,lon as locations for each item
			float lat = itemXMLElements[i].getFloat("lat");
			float lng = itemXMLElements[i].getFloat("lng");
			//println(itemXMLElements[i]);

			// XMLElement lonXML = itemXMLElements[i].getChild("geo:long");
			// float lat = Float.valueOf(latXML.getContent());
			// float lon = Float.valueOf(lonXML.getContent());

			rssGeoLocations.add(new Location(lat, lng));
		}
		println("loc:" + rssGeoLocations.size());
	}

	public void draw() {
		if(rssGeoLocations.size()!=nodes.size()){
			addParticleFly();
		}
		
	
		
		zoomChange=false;
		// background(0);
		// iso.clear();
		map.draw();
		darkLayer.paint(30,200);
		
		zoom = map.getZoomLevel();
		if(zoom!=lastZoom){
			lastZoom=zoom;
			zoomChange=true;
		}
		
		// image(darkLayer,0,0);

		if (!map.getTopLeftBorder().equals(lastPos)) {
			fill(255, 255, 0);
			int i = 0;

			for (NodeAttr node : nodes) {
				float xy[] = map.mapDisplay
						.getScreenPositionFromLocation(rssGeoLocations.get(i));
				node.setSpring(xy,zoomChange);

				// WENN PAN DANN FEST
				// WENN ZOOM DANN LIQUID

				// geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));

				// VerletParticle2D p = physics.particles.get(i);
				// p.removeAllBehaviors();
				// geoAttractor.get(i).getAttractor().set(xy[0], xy[1]);
				// println(p.behaviors.size());

				// p.removeAllBehaviors();
				// p.addBehavior(new AttractionBehavior(new Vec2D(xy[0],
				// xy[1]),2000, 1.0f, 0.00000000001f));
				// if(xy[0]<0 || xy[1]<0 || xy[0]>width || xy[1]>height){

				// if(!p.isLocked()){
				// p.lock();
				// }

				// } else {
				// geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));
				// physics.particles.get(i).unlock();
				// if(p.isLocked()){
				// p.unlock();
				// }
				// geoAttractor.get(i).getAttractor().set(xy[0],
				// xy[1]).jitter(0.00001f);
				// p.removeAllBehaviors();
				// p.addBehavior(new AttractionBehavior(new Vec2D(xy[0],
				// xy[1]),2000, 1.0f, 0.00001f));
				// float pxy[]=p.toArray();
				// triangle(pxy[0], pxy[1], pxy[0]+5,pxy[1], pxy[0],pxy[1]+5);
				// }
				// mousePos = new Vec2D(xy[0], xy[1]);
				// create a new positive attraction force field around the mouse
				// position (radius=250px)
				// mouseAttractor = new AttractionBehavior(mousePos, 250, 0.9f);
				// physics.addBehavior(mouseAttractor);
				// physics.particles.get(i).set(xy[0], xy[1]);
				// physics.particles.get(i).addBehavior(new
				// AttractionBehavior(new Vec2D(xy[0], xy[1]),10, 0.9f));
				// physics.particles.get(i).
				i++;
			}

			lastPos = map.getTopLeftBorder();
		} else {
			fill(0);
		}
		// draw the locations

		// Map<Location, Particle> particleMap = new HashMap();
		// Particle particle = particleMap.get(location)

		/*
		 * if (physics.particles.size() < NUM_PARTICLES) { addParticle(); }
		 */
		//physics.particles.get(0).
		//physics.
		
		zoom = map.getZoomLevel();
		particleZoom = map(zoom,5,18, 0.9f,4);
		physics.update();
		srcTex = layer.getTexture();

		// geo pos zeichnen
		layer.beginDraw();
		layer.background(50);
		layer.beginGL();
		layer.gl.glColor3f(255, 255, 255);

		for (NodeAttr n : nodes) {

			n.draw(layer, particleZoom);

		}
		layer.endGL();
		layer.endDraw();
		
		//println(zoom);
		// float fx = constrain((mouseX) / width, 0.01f, 1);
		// float fy = (mouseY) / height;

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
		//blend4.apply(new GLTexture[] { tex4,tex8,tex16, tex16, tex16, tex16, tex16, tex16, tex16, tex16, tex16,tex16 }, new GLTexture[] { bloomMask });
		
		blend4.apply(new GLTexture[] {  tex8,tex8,tex8,tex16 }, new GLTexture[] { bloomMask });
	
		// white spots rausfiltern und kanten machen
		bloomZoom=map(zoom,5,17, 1.8f,0.6f);
		extractBloom.setParameterValue("bright_threshold", bloomZoom);
		extractBloom.setParameterValue("bg", 0.1f);
		extractBloom.apply(bloomMask, srcTex);
		
		// maske erstellen
		//maskFilter.setParameterValue("mask_factor", maskFactor);
		//maskFilter.apply(new GLTexture[] { darkLayer, srcTex }, destTex);
		
		
		
		//destTex.filter(blur, srcTex);

		gl.glBlendFunc(gl.GL_DST_COLOR,gl.GL_ZERO);
		image(srcTex, 0, 0, width, height);
		//gl.glBlendFunc(gl.GL_ZERO, gl.GL_ONE);
	

	
		
		//

		
		/*
		 blend4.apply(new GLTexture[]{tex2, tex4, tex4, tex4, tex8, tex16, tex16, tex16, tex16, tex16}, new GLTexture[]{bloomMask});
		
		extractBloom.setParameterValue("bright_threshold", bloomFactor);
		extractBloom.apply(bloomMask, srcTex);
		
		//blend4.apply(new GLTexture[]{srcTex, srcTex, srcTex}, new GLTexture[]{bloomMask});

		maskFilter.setParameterValue("mask_factor",maskFactor);
		maskFilter.apply(new GLTexture[]{darkLayer, srcTex}, destTex);
		image(destTex, 0, 0, width, height);
		*/
		//srcTex.filter(blur, tex2);
		
		//darkLayer.paint(255);
		
		//maskFilter.setParameterValue("mask_factor",0.9f);
		//maskFilter.apply(new GLTexture[] { darkLayer, srcTex }, destTex);

		// maske mit flŸssigkeit Ÿber die karte zeichnen
	
		

		// SLIDER BAUEN


		// LOG4J
		if (frameCount % 10 == 0) {
			println(frameRate);
		}
		// (frameRate);
		
		debugDisplay.draw();

	}
	
	public void mouseWheel(float delta) {
		PVector itc = new PVector(mouseX, mouseY);
		map.mapDisplay.setInnerTransformationCenter(itc);
		
		if (delta < 0) {
			map.zoomIn();
		} else if (delta > 0) {
			map.zoomOut();
		}
	}

	public void keyPressed() {
		PVector rotateCenter = new PVector(mouseX, mouseY);
		map.mapDisplay.setTransformationCenter(rotateCenter);

		if (key == 'r') {
			map.rotate(-PI / 15);
		} else if (key == 'l') {
			map.rotate(PI / 15);
		} else if (key == 'a') {
			maskFactor += 0.1f;
		} else if (key == 'y') {
			maskFactor -= 0.1f;
		} else if (key == 's') {
			bloomFactor += 0.1f;
		} else if (key == 'x') {
			bloomFactor -= 0.1f;
		}

		println("maskfactor:"+maskFactor+" bloomfactor:"+bloomFactor);

	}

	/*
	 public void mousePressed() { 
		 mousePos = new Vec2D(mouseX, mouseY); //create a new positive attraction force field around the mouse position(radius=250px)
		 mouseAttractor = new AttractionBehavior(mousePos, 250, 0.9f);
		 physics.addBehavior(mouseAttractor);
	
	 }
	 */

}
