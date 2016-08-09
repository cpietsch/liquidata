

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.xml.XMLElement;
import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLModel;
import codeanticode.glgraphics.GLTexture;
import de.fhpotsdam.unfolding.Map;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.mapdisplay.GLGraphicsMapDisplay;
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
import toxi.processing.ToxiclibsSupport;

import toxi.sim.fluids.*;

import SearchGeometry.*;


@SuppressWarnings("serial")
public class isoTest extends PApplet {

	public static Logger log = Logger.getLogger(isoTest.class);

	Map map;
	EventDispatcher eventDispatcher;

	List<Location> rssGeoLocations = new ArrayList<Location>();
	
	
	List<MapParticle> visible = new ArrayList<MapParticle>();
	List<MapParticle> visible2 = new ArrayList<MapParticle>();
	List<MapParticle> all = new ArrayList<MapParticle>();
	
	// ISO
	IsoContour iso;
	int isoThreshold=100;
	int MAX_NUM_PARTICLES 	= 200;
	float POINT_WEIGHT 	= 5.f;
	float ISO 				= 0.03f;
	int NUM_VERTICES = 20000;
	float isoWIDTH;
	float isoHEIGHT;
	float[][] isoParticlesData = new float[MAX_NUM_PARTICLES][3];
	int particleCount;
	float centerOffsetX, centerOffsetY;
	
	// TOXI
	int NUM_PARTICLES = 8000;
	VerletPhysics2D physics;
	AttractionBehavior mouseAttractor;
	
	List<AttractionBehavior> geoAttractor = new ArrayList<AttractionBehavior>();
	
	//Map<Location, AttractionBehavior> attractionMap = new HashMap();

	Vec2D mousePos;
	
	DebugDisplay debugDisplay;
	GLGraphicsMapDisplay MapDisplay;
	
	
	PVector lastPos;
	
	GLGraphicsOffScreen layer;
	GLTexture srcTex;
	PGraphics PG;
	
	GL gl;
	PGraphicsOpenGL pgl;
	GLModel fluid;
	GLGraphics renderer;
	
	TriangleMesh mesh;
	ToxiclibsSupport gfx;
	int numTiles;
	float tilesRatio;
	
	boolean animate;

	public void setup() {
		// Padding in Abstrac
		size(1024, 600, GLConstants.GLGRAPHICS);
		
		//layer = new GLGraphicsOffScreen(this, width, height);
		srcTex = new GLTexture(this, width, height);
		//smooth();
		frameRate(700);
		// Creates default mapDisplay
		//map = new Map(this, "map", 50, 50, 700, 500);
		//map = new Map(this, "map", 0, 0, width, height, false, false, new Microsoft.RoadProvider());
		map = new Map(this, 0, 0, width, height, new Microsoft.RoadProvider());
		//map.
		map.setTweening(true);
		//map.zoomToLevel(12);
		map.zoomAndPanTo(new Location(52.52317f, 13.4116f), 13);
		map.mapDisplay.grid_padding = 1;
		//layer = (GLGraphicsOffScreen) map.mapDisplay.getPG();
		//MapDisplay = 
		

		// Creates default dispatcher
		eventDispatcher = MapUtils.createDefaultEventDispatcher(this, map);

		
		
		// TOXI
		physics = new VerletPhysics2D();
		physics.setDrag(0.5f);
		//physics.setWorldBounds(new Rect(0, 0, width, height));
		// the NEW way to add gravity to the simulation, using behaviors
		//physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		loadRSSGeoLocations();
		addParticle();
		//noStroke();
		fill(0,0,0,100);
		
		pgl = (PGraphicsOpenGL) this.g;
		gl = pgl.gl;
		renderer = (GLGraphics)g;
		
		animate=false;
		
		
		//iso = new IsoContour(this, new PVector(0,0), new PVector(width,height), 50,50);
		
		// Creating the Isocontour
		isoWIDTH 	= width;
		isoHEIGHT 	= height;
		numTiles = 70;
		tilesRatio = isoWIDTH / isoHEIGHT;
		iso = new IsoContour( this, new PVector(0, 0), new PVector(isoWIDTH, isoHEIGHT), numTiles, (int)(numTiles/tilesRatio) );
		
		centerOffsetX = (isoWIDTH-width)/2.f;
		centerOffsetY = (isoHEIGHT-height)/2.f;
		
		fluid = new GLModel( this, NUM_VERTICES, GLGraphics.TRIANGLES , GLModel.STREAM );
		fluid.initColors();
		//fluid.beginUpdateColors();
		//for (int i = 0; i < NUM_VERTICES; i++) fluid.updateColor(i, 100, 100, 100, 255);
		//fluid.endUpdateColors();
		fluid.setColors(10);
		//fluid.setTint(100);
		//fluid.setBlendMode(BLUR);
		
		
		
		mesh=new TriangleMesh();
		gfx=new ToxiclibsSupport(this);
		
		
		
	}
	
	public void addParticle() {
		for (Location location : rssGeoLocations) {
			float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
			
			//VerletParticle2D p = new VerletParticle2D(new Vec2D(xy[0],xy[1])).lock();
			MapParticle p = new MapParticle(xy[0],xy[1], location);
			all.add(p);
			
			//AttractionBehavior attr = new AttractionBehavior(p,100, 0.9f, 0.01f);
			
			//p.addBehavior(attr);
			//geoAttractor.add(attr);
			physics.addParticle(p);
			physics.addBehavior(new AttractionBehavior(p, 20, -0.2f, 0.0001f));
			// add a negative attraction force field around the new particle
			//physics.addBehavior(new AttractionBehavior(p, 20, -1.2f, 0.01f));
		}
		println("particles:"+physics.particles.size());
	  
	}

	private void loadRSSGeoLocations() {
		// Load RSS feed
		//String url = "WifiSpots.xml";
		String url = "test.xml";
		
		XMLElement rss = new XMLElement(this, url);
		XMLElement[] itemXMLElements = rss.getChildren("item");
		
		for (int i = 0; i < itemXMLElements.length; i++) {

			float lat = itemXMLElements[i].getFloat("lat");
			float lng = itemXMLElements[i].getFloat("lng");

			rssGeoLocations.add(new Location(lat, lng));
		}
		println("locs:"+rssGeoLocations.size());
	}

	public void draw() {
		//map.mapDisplay.draw();
		//srcTex = layer.getTexture();
		
		background(255);

		map.draw();
		//layer.endDraw();
		
		
		//image(srcTex, 0, 0);
		
		
		if((!map.getTopLeftBorder().equals(lastPos) || (animate)) && frameCount%2==0 ){
			iso.clear();
			physics.update();
			//println("update");
			
			fill(255,255,0);
			/*
			for(int i=0;i<physics.particles.size();i++){
				MapParticle p=(MapParticle) physics.particles.get(i);
				p.set(map.mapDisplay.getScreenPositionFromLocation(p.location));
				//AttractionBehavior attr = (AttractionBehavior) p.behaviors.get(0);
			}
			*/
			if(!animate){
				visible.clear();
				for (MapParticle p1 : all) {
					p1.set(map.mapDisplay.getScreenPositionFromLocation(p1.location));
					if((p1.x>0 && p1.y>0 && p1.x<width && p1.y<height)){
						visible.add(p1);
					} else {
						p1.wasOutside=true;
					}
				}
			}
			
	
			float dist;
			
			for(MapParticle p1 : visible){
				if(!p1.hide){
					for(MapParticle p2 : visible){
						dist=p1.distanceTo(p2);
						if(dist<10 && dist>0){
							p2.rem();
						}
					}
				}
			}
			
			visible2.clear();
			for (MapParticle p1 : visible) {
				if(!p1.hide){
					visible2.add(p1);
				}
			}
			
			POINT_WEIGHT = map(map.getZoom(),16384 , 262144, 2, 27);
			POINT_WEIGHT = constrain(POINT_WEIGHT,2,27);
			
		
			for(MapParticle p1 : visible2){
				if(p1.wasOutside){
					fill(255,0,0);
					p1.wasOutside=false;

				} else {
					fill(255,255,0);
				}
				//triangle(p1.x, p1.y, p1.x+20,p1.y, p1.x, p1.y+20);
				iso.addPoint( new PVector(p1.x,p1.y), POINT_WEIGHT );
				//iso.addPoint(new PVector(p1.x,p1.y));
			}
			
			iso.plot( ISO );
			
			fluid.beginUpdateVertices();
			for ( int i = 0; i < iso.numVertices; i++) {
				PVector pos = (PVector)iso.vertexPositions.get(i);
				fluid.updateVertex( i, pos.x, pos.y, 0 );
			}
			fluid.endUpdateVertices();
			
			
			//for (Location location : rssGeoLocations) {
				//float xy[] = map.mapDisplay.getScreenPositionFromLocation(location);
								
				//geoAttractor.get(i).setAttractor(new Vec2D(xy[0],xy[1]));

				//VerletParticle2D p = physics.particles.get(i);
				
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
				//i++;
			//}
			
			lastPos=map.getTopLeftBorder();
		} else {
			fill(0);
		}
		
		
		pushMatrix();
		translate( -centerOffsetX, -centerOffsetY );
		
		/*
		beginShape( TRIANGLES );
		for ( int i = 0; i < iso.numVertices; i++) {
			PVector pos = (PVector)iso.vertexPositions.get(i);
			vertex( pos.x, pos.y );
		}
		endShape();
		*/
		
		/*
		mesh=new TriangleMesh();
		for ( int i = 0; i < iso.numVertices-3; i++) {
			PVector a = (PVector)iso.vertexPositions.get(i);
		    PVector b = (PVector)iso.vertexPositions.get(i+1);
		    PVector c = (PVector)iso.vertexPositions.get(i+2);
		    
			mesh.addFace(new Vec3D(a.x,a.y,0),new Vec3D(b.x,b.y,0),new Vec3D(c.x,c.y,0));
		}
		mesh.computeVertexNormals();
		fill(255,160,0);
		stroke(100);
		gfx.mesh(mesh,true);
		*/
		
		// TEST
		
	
		
		/*
		fluid.beginUpdateVertices();
	    for (int i = 0; i < iso.numVertices; i++) {
	    	fluid.displaceVertex(i, sin((i+frameCount)/100)/50, cos((i+frameCount)/100)/50, 0);
	    	//fluid.updateColor(i, 255, 255, 255, (sin((frameCount)/100)+1)*255);
	    }
	    fluid.endUpdateVertices();
		*/
		
		//gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ONE);
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_FILL );
		// Draw fluid

		renderer.beginGL();
		//renderer.model(fluid);
		fluid.render( 0, iso.numVertices, null );
		renderer.endGL();
		
		//gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ONE);
		//gl.glBlendFunc(gl.GL_ZERO, gl.GL_ONE);
		
		popMatrix();
		
		//gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ZERO);
		/*
		for (int i = 0; i < 10; i++) {
			fill(i*20);
			rect(0+i*10,0+i*10,100+i*2,100+i*10);
		}
		*/
		
		
		//gl.glBlendFunc(gl.GL_ZERO, gl.GL_ONE);
		// draw the locations
		
		
		//Map<Location, Particle> particleMap = new HashMap();
		//Particle particle = particleMap.get(location)
		
		
		/*
		if (physics.particles.size() < NUM_PARTICLES) {
			addParticle();
		}*/
		//physics.update();
		
		/*
		for(int i=0;i<physics.particles.size();i++){
			MapParticle p=(MapParticle) physics.particles.get(i);
			triangle(p.x, p.y, p.x+5,p.y, p.x, p.y+5);
		}
		*/

		//println(visible.size()+" " +visible2.size());
		
		
		
		//gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ZERO);
		//fill(0);
		//iso.plot( isoThreshold/10000.0f );
		//gl.glBlendFunc(gl.GL_ZERO, gl.GL_ONE);
		
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
		if(frameCount % 2==0){
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
			POINT_WEIGHT+=0.5;
			
			println(map.getZoom()+ " " + POINT_WEIGHT);
		} else if (key == 'y') {
			POINT_WEIGHT-=0.5;
			println(map.getZoom()+ " " + POINT_WEIGHT);
		} else if (key == 'q') {
			animate=!animate;
			println((animate)?"animate":"stop");
		}
		
		if (key == 's') {
			numTiles+=10;
			iso = new IsoContour( this, new PVector(0, 0), new PVector(isoWIDTH, isoHEIGHT), numTiles, (int)(numTiles/tilesRatio) );
			
			for(MapParticle p1 : visible2){
				if(p1.wasOutside){
					fill(255,0,0);
					p1.wasOutside=false;

				} else {
					fill(255,255,0);
				}
				//triangle(p1.x, p1.y, p1.x+20,p1.y, p1.x, p1.y+20);
				iso.addPoint( new PVector(p1.x,p1.y), POINT_WEIGHT );
				//iso.addPoint(new PVector(p1.x,p1.y));
			}
			
			iso.plot( ISO );
			
			fluid.beginUpdateVertices();
			for ( int i = 0; i < iso.numVertices; i++) {
				PVector pos = (PVector)iso.vertexPositions.get(i);
				fluid.updateVertex( i, pos.x, pos.y, 0 );
			}
			fluid.endUpdateVertices();
			
		}
		if (key == 'x') {
			numTiles-=10;
			iso = new IsoContour( this, new PVector(0, 0), new PVector(isoWIDTH, isoHEIGHT), numTiles, (int)(numTiles/tilesRatio) );
			
			for(MapParticle p1 : visible2){
				if(p1.wasOutside){
					fill(255,0,0);
					p1.wasOutside=false;

				} else {
					fill(255,255,0);
				}
				//triangle(p1.x, p1.y, p1.x+20,p1.y, p1.x, p1.y+20);
				iso.addPoint( new PVector(p1.x,p1.y), POINT_WEIGHT );
				//iso.addPoint(new PVector(p1.x,p1.y));
			}
			
			iso.plot( ISO );
			
			fluid.beginUpdateVertices();
			for ( int i = 0; i < iso.numVertices; i++) {
				PVector pos = (PVector)iso.vertexPositions.get(i);
				fluid.updateVertex( i, pos.x, pos.y, 0 );
			}
			fluid.endUpdateVertices();
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
