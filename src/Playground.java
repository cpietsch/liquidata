import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import codeanticode.glgraphics.GLConstants;
import processing.core.PApplet;

import toxi.geom.*;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;

public class Playground extends PApplet {
	
	VerletPhysics2D physics;
	AttractionBehavior attr;
	List<VerletParticle2D> particles = new ArrayList<VerletParticle2D>();

	
	public static void main(String[] args) {

		PApplet.main(new String[] { "Playground" });

	}

	public void setup() {
		// Padding in Abstrac
		size(800, 600, GLConstants.GLGRAPHICS);
		frameRate(700);
		strokeWeight(1);
		stroke(255,255,255,30);
		rectMode(CENTER);
		
		physics = new VerletPhysics2D();
		physics.setDrag(0.05f);
		physics.setWorldBounds(new Rect(0, 0, width, height));
		attr = new AttractionBehavior(new Vec2D(width/2,height/2), 300, 0.08f);
		physics.addBehavior(attr);
		//physics.addBehavior(new GravityBehavior(new Vec2D(0, 0.15f)));
		
	}
	
	
	public void draw(){
		background(0);
		physics.update();
		
		for(VerletParticle2D p1 : physics.particles){
			for(VerletParticle2D p2 : physics.particles){
				if(p1.distanceTo(p2.getAbs())<50){
					//line(p1.x,p1.y,p2.x,p2.y);
				}
			}
			//rect(p1.x,p1.y,2,2);
		
		}
		
		if (frameCount % 100 == 0) {
			println(frameRate);
		}
	}
	
	public void mouseDragged() {
		
		attr.setAttractor(new Vec2D(mouseX,mouseY));
	}
	
	public void mousePressed(){
		
	}
	
	public void keyPressed() {
	    if (key == 'r') {
	    	for (int i = 0; i < 20; i++) {
	    		VerletParticle2D p = new VerletParticle2D(new Vec2D(mouseX, mouseY));
				physics.addParticle(p);
				physics.addBehavior(new AttractionBehavior(p, 40, -0.2f, 0.0001f));
			}
	    	
			println("size "+physics.particles.size());
	    }
	}
}