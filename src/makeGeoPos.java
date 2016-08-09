

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;
import processing.xml.XMLElement;
import codeanticode.glgraphics.GLConstants;
import de.fhpotsdam.unfolding.Map;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.examples.marker.PlaceMarker;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.Yahoo;
import de.fhpotsdam.unfolding.utils.DebugDisplay;
import de.fhpotsdam.unfolding.utils.MapUtils;

import toxi.geom.*;
import toxi.geom.mesh.TriangleMesh;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;

import proxml.*;



// MIT RECHTSKLICK PUNKTE ANLEGEN
// MIT S WIRD EINE XML IN bin/data/test.xml gespeichert
// wichtig: die proxml library muss drin sein
// falls ihr in eine andere datei speichern wollt muss diese zuvor im filesystem angelegt sein z.b. mit terminal: touch bla.xml


@SuppressWarnings("serial")
public class makeGeoPos extends PApplet {

	public static Logger log = Logger.getLogger(makeGeoPos.class);

	Map map;
	EventDispatcher eventDispatcher;

	List<Location> rssGeoLocations = new ArrayList<Location>();
	
	// Mesh
	TriangleMesh mesh=new TriangleMesh("fluid");
	
	// TOXI
	int NUM_PARTICLES = 3000;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;
	
	List<AttractionBehavior> geoAttractor = new ArrayList<AttractionBehavior>();

	Vec2D mousePos;
	
	DebugDisplay debugDisplay;
	
	List<Location> locations = new ArrayList<Location>();
	
	XMLElement channel;
	XMLInOut xmlInOut;

	public void setup() {
		size(800, 600, GLConstants.GLGRAPHICS);
		//smooth();

		// Creates default mapDisplay
		//map = new Map(this, "map", 50, 50, 700, 500);
		map = new Map(this, 0, 0, width, height, new Microsoft.RoadProvider());
		//map.setTweening(true);
		map.zoomToLevel(12);
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 12);

		// Creates default dispatcher
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);

		
		debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250, 150);
		
		//MarkerManager markerManager = new MarkerManager(map, markers);
		//map.mapDisplay.setMarkerManager(markerManager);
		fill(255,0,255);
		
		xmlInOut = new XMLInOut(this);
		//xmlEvent(new XMLElement("ellipses"));
	}
	
	


	public void draw() {
		background(0);
		
		map.draw();

		for (Location l : locations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(l);
			ellipse(xy[0],xy[1],10,10);
		}
		
	}

	public void keyPressed() {
		PVector rotateCenter = new PVector(mouseX, mouseY);
		map.mapDisplay.setTransformationCenter(rotateCenter);
		
		if (key == 'r') {
			map.rotate(-PI/15);
		} else if (key == 'l') {
			map.rotate(PI/15);
		} else if (key == 'a') {
			//isoThreshold++;
		} else if (key == 's') {
			proxml.XMLElement channel = new proxml.XMLElement("channel");
			
			for (Location l : locations) {
				
				proxml.XMLElement item = new proxml.XMLElement("item");
				item.addAttribute("lat",l.getLat());
				item.addAttribute("lng",l.getLon());
				channel.addChild(item);
			}
			
			
			xmlInOut.saveElement(channel,"test.xml");
			
		}
		
		//println(isoThreshold);
		
		
	}
	
	
	public void mousePressed() {
		if (mouseButton == RIGHT) {
			println(map.mapDisplay.getLocationFromScreenPosition(mouseX, mouseY));

			locations.add(map.mapDisplay.getLocationFromScreenPosition(mouseX, mouseY));
		}
	}


}
