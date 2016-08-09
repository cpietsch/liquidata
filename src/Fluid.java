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

public class Fluid {
	
	private PApplet p;
	private Map map;

	GL gl;
	PGraphicsOpenGL pgl;
	
	List<Location> rssGeoLocations = new ArrayList<Location>();
	

	// TOXI
	int NUM_PARTICLES = 4000;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;

	List<NodeSpring> nodes = new ArrayList<NodeSpring>();

	// Map<Location, AttractionBehavior> attractionMap = new HashMap();

	// GLGRAPHICS
	GLGraphicsOffScreen layer;
	GLTexture srcTex, bloomMask, destTex, darkLayer;
	GLTexture tex0, tex2, tex4, tex8, tex16;
	GLTexture tmp2, tmp4, tmp8, tmp16;
	GLTextureFilter extractBloom, blur, blur2, blend4, toneMap, maskFilter;

	float maskFactor = 0f;
	float bloomFactor = 1.5f;
	float particleZoom, bloomZoom;
	
	PVector lastPos;
	float zoom, lastZoom;
	boolean zoomChange = false;
	
	


	public Fluid(PApplet p, Map map) {

		this.p = p;
		this.map = map;
		
		// GL
		pgl = (PGraphicsOpenGL) p.g;
		gl = pgl.gl;
		
		// TOXI
		physics = new VerletPhysics2D();
		physics.setDrag(0.05f);
		physics.setWorldBounds(new Rect(0, 0, p.width, p.height));
		// the NEW way to add gravity to the simulation, using behaviors
		// physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		
		// BUFFER
		layer = new GLGraphicsOffScreen(p, p.width, p.height);
		// Loading required filters.
		extractBloom = new GLTextureFilter(p, "ExtractBloomBG.xml");
		blur = new GLTextureFilter(p, "Blur.xml");
		blur2 = new GLTextureFilter(p, "Blur1D.xml");
		blend4 = new GLTextureFilter(p, "Blend4.xml");
		toneMap = new GLTextureFilter(p, "ToneMap.xml");
		maskFilter = new GLTextureFilter(p, "Mask.xml");


		// Initializing bloom mask and blur textures.
		destTex = new GLTexture(p, p.width, p.height);
		bloomMask = new GLTexture(p, p.width, p.height, GLTexture.FLOAT);
		tex0 = new GLTexture(p, p.width, p.height, GLTexture.FLOAT);
		tex2 = new GLTexture(p, p.width / 2, p.height / 2, GLTexture.FLOAT);
		tmp2 = new GLTexture(p, p.width / 2, p.height / 2, GLTexture.FLOAT);
		tex4 = new GLTexture(p, p.width / 4, p.height / 4, GLTexture.FLOAT);
		tmp4 = new GLTexture(p, p.width / 4, p.height / 4, GLTexture.FLOAT);
		tex8 = new GLTexture(p, p.width / 8, p.height / 8, GLTexture.FLOAT);
		tmp8 = new GLTexture(p, p.width / 8, p.height / 8, GLTexture.FLOAT);
		tex16 = new GLTexture(p, p.width / 16, p.height / 16, GLTexture.FLOAT);
		tmp16 = new GLTexture(p, p.width / 16, p.height / 16, GLTexture.FLOAT);

		// gl.glDisable(GLConstants.GL_DEPTH_TEST);
		
		loadRSSGeoLocations("testL.xml");
		
	}
	
	public void addParticle() {
		int i=0;
		NodeSpring old;
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			
				NodeSpring n = new NodeSpring(p, new Vec2D(xy[0],xy[1]),physics);
				n.setSpring(xy, true);
				nodes.add(n);
	
			//physics.particles.
			i++;
		}
		
		p.println("particles:" + nodes.size());
	}

	void addParticleFly() {
		/*
		float xy[] = map.mapDisplay
				.getScreenPositionFromLocation(rssGeoLocations.get(nodes.size()));
		NodeSpring n = new NodeSpring(p,new Vec2D(xy[0],xy[1]),physics);
		n.setSpring(xy, false);
		nodes.add(n);
*/
	}

	private void loadRSSGeoLocations(String url) {

		XMLElement rss = new XMLElement(p, url);
		XMLElement[] itemXMLElements = rss.getChildren("item");
		
		for (int i = 0; i < itemXMLElements.length; i++) {

			float lat = itemXMLElements[i].getFloat("lat");
			float lng = itemXMLElements[i].getFloat("lng");

			rssGeoLocations.add(new Location(lat, lng));
		}
		p.println("loc:" + rssGeoLocations.size());
	}
	
	public void draw() {
		
		if (rssGeoLocations.size() != nodes.size()) {
			addParticleFly();
		}
		
	
		
		zoomChange = false;
		zoom = map.getZoomLevel();
		if (zoom != lastZoom) {
			lastZoom = zoom;
			zoomChange = true;
		}

		
		if (!map.getTopLeftBorder().equals(lastPos)) {
			int i = 0;

			for (NodeSpring node : nodes) {
				float xy[] = map.mapDisplay
						.getScreenPositionFromLocation(rssGeoLocations.get(i));
				node.setSpring(xy, zoomChange);
				i++;
			}

			lastPos = map.getTopLeftBorder();
		} 
		
		physics.update();
	
		
		
		srcTex = layer.getTexture();
		
		// Raw Image zeichnen
		particleZoom = PApplet.map(zoom, 14, 19, 0.5f, 4);
		particleZoom = PApplet.constrain(particleZoom,0.5f,4);
		
		layer.beginDraw();
		layer.background(50);
		layer.beginGL();
		layer.gl.glColor3f(255, 255, 255);

		for (NodeSpring n : nodes) {
			n.draw(layer, particleZoom);
		}
		
		layer.endGL();
		layer.endDraw();

		
		
		// Downsampling with blur
		srcTex.filter(blur, tex8);

		
		/*
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
		blend4.apply(new GLTexture[] { tex8, tex8, tex8, tex16 },
				new GLTexture[] { bloomMask });

		// white spots rausfiltern und kanten machen
		bloomZoom = p.map(zoom, 9, 19, 1.8f, 0.8f);
		extractBloom.setParameterValue("bright_threshold", bloomZoom);
		extractBloom.setParameterValue("bg", 0.1f); //transparent fŸr die maske
		extractBloom.apply(bloomMask, srcTex);

		// mit blend funktion flŸssigkeit zeichnen
		gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ZERO);
		p.image(srcTex, 0, 0, p.width, p.height);
		// gl.glBlendFunc(gl.GL_ZERO, gl.GL_ONE);
*/
		p.image(tex8, 0, 0, p.width, p.height);
		
		
	}

}
