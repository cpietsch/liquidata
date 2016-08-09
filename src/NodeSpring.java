import java.util.Random;

import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import toxi.geom.*;
import toxi.physics.VerletConstrainedSpring;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;
import toxi.test.PhysTest;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;
import javax.media.opengl.*;



public class NodeSpring {
	VerletSpring2D spring;
	VerletParticle2D particle;
	VerletParticle2D lockedSpring;
	Random random = new Random();
	int size;
	boolean locked=false;
	PApplet p;
	
	public NodeSpring(PApplet p, Vec2D start, VerletPhysics2D physics) {
		this.p=p;

		this.particle = new VerletParticle2D(10,10);
		//this.particle.addForce(new Vec2D(10,1));
		//this.particle.addBehavior(new AttractionBehavior(start,100, 1.0f,0.01f));
		
		this.lockedSpring = new VerletParticle2D(start);
		this.lockedSpring.lock();
		this.spring = new VerletSpring2D(this.particle,this.lockedSpring,0.2f,0.001f);
		
		physics.addParticle(this.particle);
		physics.addSpring(this.spring);

		size=5+(int)random.nextInt(10);
		//size=10;
	}
	
	public void draw(GLGraphicsOffScreen pg, float zoom) {
		
		if(particle.x<=0 || particle.y<=0 || particle.x>=p.width || particle.y>=p.height){
			
		} else {
			
			pg.gl.glPushMatrix();
			pg.gl.glColor3f(1.0f, 1.0f, 1.0f);
			//pg.gl.triangle(particle.x, particle.y, particle.x+10,particle.y, particle.x, particle.y+10);
			pg.gl.glBegin(GL.GL_POLYGON);
				pg.gl.glVertex2f(particle.x, particle.y);
				pg.gl.glVertex2f(particle.x-(size*zoom), particle.y);
				pg.gl.glVertex2f(particle.x-(size*zoom), particle.y+(size*zoom));
				pg.gl.glVertex2f(particle.x, particle.y+(size*zoom));
			pg.gl.glEnd();
			
			pg.gl.glColor3f(1.0f, 0.0f, 1.0f);
			pg.gl.glLineWidth(2.0f);
			pg.gl.glBegin(GL.GL_LINES);
				pg.gl.glVertex2f(lockedSpring.x, lockedSpring.y);
				pg.gl.glVertex2f(particle.x, particle.y);
			pg.gl.glEnd();
			
			pg.gl.glPopMatrix();
		}
	
		
	}
	
	public void setSpring(float xy[],boolean zoomChange) {
		lockedSpring.set(xy[0], xy[1]);
		
		/*
		if(zoomChange){
			spring.setStrength(0.001f);
		} else {
			spring.setStrength(0.01f);
		}
		*/
		
		//size=10+(int)random.nextInt(40);
	
	}
	

	
}
