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



public class NodeAttr {

	VerletParticle2D particle;
	
	AttractionBehavior attr;
	Random random = new Random();
	int size;
	boolean locked=false;
	private PApplet p;
	

	
	public NodeAttr(PApplet p,VerletPhysics2D physics) {
		this.p=p;
		
		this.attr= new AttractionBehavior(new Vec2D(200, 890),2000, 1.0f,0.01f);
		this.particle = new VerletParticle2D(new Vec2D(200, 890));
		this.particle.addBehavior(this.attr);
			
		
		physics.addParticle(this.particle);
		
		
		size=5+(int)random.nextInt(10);
		

	}
	
	public void draw(GLGraphicsOffScreen pg, float zoom) {
		//top.println(particle.toString());
		//this.randBehave();
		
		PApplet.println(PApplet.HEIGHT);
		
		if(particle.x<=0 || particle.y<=0 || particle.x>=PApplet.WIDTH || particle.y>=PApplet.HEIGHT){
			
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
		
		attr.setAttractor(new Vec2D(xy[0], xy[1]));
				
		//size=10+(int)random.nextInt(40);
	
	}
	public void setSpring(float xy[],float force) {
		attr.setAttractor(new Vec2D(xy[0], xy[1]));
	}
	

	

	
}
