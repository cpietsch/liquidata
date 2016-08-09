

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;
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




@SuppressWarnings("serial")
public class RSSFeedMarkerApp extends PApplet {

	public static Logger log = Logger.getLogger(RSSFeedMarkerApp.class);

	Map map;
	EventDispatcher eventDispatcher;

	List<Location> rssGeoLocations = new ArrayList<Location>();
	
	
	// TOXI
	int NUM_PARTICLES = 100;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;
	
	List<AttractionBehavior> geoAttractor = new ArrayList<AttractionBehavior>();

	Vec2D mousePos;
	
	DebugDisplay debugDisplay;

	public void setup() {
		size(800, 600, GLConstants.GLGRAPHICS);
		//smooth();

		// Creates default mapDisplay
		//map = new Map(this, "map", 50, 50, 700, 500);
		map = new Map(this, 0, 0, width, height, new Microsoft.RoadProvider());
		//map.setTweening(true);
		map.zoomToLevel(12);
		map.zoomAndPanTo(new Location(47.192104f,8.854508f), 9);

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
		
		//debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250, 150);
	}
	
	public void addParticle() {
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			
			VerletParticle2D p = new VerletParticle2D(new Vec2D(xy[0],xy[1]));
			AttractionBehavior attr = new AttractionBehavior(new Vec2D(xy[0], xy[1]),2000, 1.0f,0.0f);
			
			p.addBehavior(attr);
			geoAttractor.add(attr);
			physics.addParticle(p);
			// add a negative attraction force field around the new particle
			//physics.addBehavior(new AttractionBehavior(p, 20, -1.2f, 0.01f));
		}
	  
	}

	private void loadRSSGeoLocations() {
		// Load RSS feed
		String url = "WifiSpots.xml";
		XMLElement rss = new XMLElement(this, url);
		// Get all items
		XMLElement[] itemXMLElements = rss.getChildren("channel/item");
		for (int i = 0; i < itemXMLElements.length; i++) {
			// Adds lat,lon as locations for each item
			XMLElement latXML = itemXMLElements[i].getChild("geo:lat");
			XMLElement lonXML = itemXMLElements[i].getChild("geo:long");
			float lat = Float.valueOf(latXML.getContent());
			float lon = Float.valueOf(lonXML.getContent());

			rssGeoLocations.add(new Location(lat, lon));
		}
		println("particles:"+rssGeoLocations.size());
	}

	public void draw() {
		background(0);
		map.draw();

		// draw the locations
		int i=0;
		
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			
			geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));
			//mousePos = new Vec2D(xy[0], xy[1]);
			// create a new positive attraction force field around the mouse position (radius=250px)
			  //mouseAttractor = new AttractionBehavior(mousePos, 250, 0.9f);
			//physics.addBehavior(mouseAttractor);
			 //physics.particles.get(i).set(xy[0], xy[1]).jitter(2, 2);
			//physics.particles.get(i).addBehavior(new AttractionBehavior(new Vec2D(xy[0], xy[1]),10, 0.9f));
			//physics.particles.get(i).
			i++;
		}
		
		
		/*
		if (physics.particles.size() < NUM_PARTICLES) {
			addParticle();
		}*/
		physics.update();
		for (VerletParticle2D p : physics.particles) {
			if(p.x==0 || p.y==0 || p.x==width || p.y==height){
				
			} else {
				ellipse(p.x, p.y, 15, 15);
			}
			
		}
		
		//debugDisplay.draw();
		//println(frameRate);
	}

	public void keyPressed() {
		PVector rotateCenter = new PVector(mouseX, mouseY);
		map.mapDisplay.setTransformationCenter(rotateCenter);
		
		if (key == 'r') {
			map.rotate(-PI/15);
		} else if (key == 'l') {
			map.rotate(PI/15);
		}
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
