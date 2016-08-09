import java.util.Random;

import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import toxi.geom.*;
import toxi.physics.VerletConstrainedSpring;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;
import javax.media.opengl.*;



public class Node {
	VerletSpring2D spring;
	VerletParticle2D particle;
	VerletParticle2D lockedSpring;
	AttractionBehavior attr;
	Random random = new Random();
	int size;
	boolean locked=false;
	

	
	public Node(VerletPhysics2D physics) {
		super();

		this.particle = new VerletParticle2D(10,10);
		
		this.lockedSpring = new VerletParticle2D(10,10);
		this.lockedSpring.lock();
		this.spring = new VerletSpring2D(this.particle,this.lockedSpring,0.9f,0.0001f);
		
		
		physics.addParticle(this.particle);
		physics.addSpring(this.spring);
		
		size=5+(int)random.nextInt(10);
		

	}
	
	public void draw(GLGraphicsOffScreen pg, float zoom) {
		//top.println(particle.toString());
		this.randBehave();
		if(particle.x<=0 || particle.y<=0 || particle.x>=1900 || particle.y>=1000){
			
		} else {
			pg.gl.glPushMatrix();
			//pg.gl.triangle(particle.x, particle.y, particle.x+10,particle.y, particle.x, particle.y+10);
			pg.gl.glBegin(GL.GL_POLYGON);
				pg.gl.glVertex2f(particle.x, particle.y);
				pg.gl.glVertex2f(particle.x-(size*zoom), particle.y);
				pg.gl.glVertex2f(particle.x-(size*zoom), particle.y+(size*zoom));
				pg.gl.glVertex2f(particle.x, particle.y+(size*zoom));
			pg.gl.glEnd();
			
			pg.gl.glPopMatrix();
		}
		
	}
	
	public void setSpring(float xy[],boolean zoomChange) {
		lockedSpring.set(xy[0], xy[1]);
		if(zoomChange){
			spring.setStrength(0.0001f);
		} else {
			spring.setStrength(0.01f);
		}
		
		//size=10+(int)random.nextInt(40);
	
	}
	public void setSpring(float xy[],float force) {
		lockedSpring.set(xy[0], xy[1]);
		spring.setStrength(force);
	}
	
	public void randBehave() {
		int rand=(int)random.nextInt(100);
		
		if(rand==89){
			lockedSpring.jitter(20f);
		} else if(rand<50){
			lockedSpring.jitter(0.001f);
			//lockedSpring.ji
			
		}
	}
	

	
}
