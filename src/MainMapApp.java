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
import de.fhpotsdam.unfolding.interactions.TuioCursorHandler;
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

@SuppressWarnings("serial")
public class MainMapApp extends PApplet {

	public static Logger log = Logger.getLogger(MainMapApp.class);

	Map map;
	EventDispatcher eventDispatcher;

	Vec2D mousePos;

	DebugDisplay debugDisplay;

	
	Fluid fluid;
	
	// TUIO
	TuioCursorHandler tuioCursorHandler;
	
	//public static final String JDBC_CONN_STRING_MAC = "jdbc:sqlite:data/52mb.mbtiles";
	public static final String JDBC_CONN_STRING_MAC = "jdbc:sqlite:data/test_2aa8ed.mbtiles";

	public static void main(String[] args) {

		PApplet.main(new String[] { "MainMapApp" });

	}

	public void setup() {
		// Padding in Abstrac
		size(800, 600, GLConstants.GLGRAPHICS);



		/*
		 * hint(DISABLE_OPENGL_2X_SMOOTH); hint(ENABLE_OPENGL_4X_SMOOTH);
		 * hint(ENABLE_NATIVE_FONTS); hint(ENABLE_ACCURATE_TEXTURES);
		 * hint(DISABLE_DEPTH_TEST); hint(DISABLE_OPENGL_ERROR_REPORT);
		 */

		frameRate(700);
		// Creates default mapDisplay
		// map = new Map(this, "map", 50, 50, 700, 500);
		map = new Map(this, 0, 0, width, height, new
			MBTilesMapProvider(JDBC_CONN_STRING_MAC));
		// map = new Map(this, 0, 0, width, height, new
		// Microsoft.RoadProvider());

		/* map = new Map(this, "map", 0, 0, width, height, true, false,
				new OpenStreetMap.CloudmadeProvider(
						MapDisplayFactory.OSM_API_KEY, 41009));
		*/
		//map = new Map(this, 0, 0, width, height, new MBTilesMapProvider(JDBC_CONN_STRING_MAC));
		
		map.setTweening(false);
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 14);
		map.setZoomRange(9, 19);
		//map.mapDisplay.grid_padding = 4; // wieviele Tiles preaload
		
		
		
		debugDisplay = new DebugDisplay(this, map.mapDisplay, 600, 200, 250, 150);

		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				mouseWheel(evt.getWheelRotation());
			}
		});
		
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);
		//eventDispatcher = new EventDispatcher();
		
		noStroke();
		noFill();
		
		// TUIO
		tuioCursorHandler = new TuioCursorHandler(this, map);
		eventDispatcher.addBroadcaster(tuioCursorHandler);

		eventDispatcher.register(map, "pan");
		
		fluid = new Fluid(this,map);
		
		
		
	}


	public void draw() {
		
		map.draw();

		//debugDisplay.draw();
		fluid.draw();
		
		

		// LOG4J
		if (frameCount % 10 == 0) {
			println(frameRate);
		}


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
			fluid.addParticle();
		} 

	}

	/*
	 * public void mousePressed() { mousePos = new Vec2D(mouseX, mouseY);
	 * //create a new positive attraction force field around the mouse
	 * position(radius=250px) mouseAttractor = new AttractionBehavior(mousePos,
	 * 250, 0.9f); physics.addBehavior(mouseAttractor);
	 * 
	 * }
	 */

}
