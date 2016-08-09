import processing.core.PVector;
import toxi.geom.*;
import toxi.physics2d.*;
import toxi.physics2d.behaviors.*;
import de.fhpotsdam.unfolding.geo.Location;

public class MapParticle extends VerletParticle2D {
	
	Location location;
	float _x;
	float _y;
	boolean wasOutside=false;
	boolean hide=false;
	
	//AttractionBehavior attr;

	public MapParticle(float x, float y, Location loc) {
		super(x, y);
		this.location=loc;
		
		//this.attr=  new AttractionBehavior(this,1000, 0.3f, 0.01f);
		//this.addBehavior(attr);
		// TODO Auto-generated constructor stub
	}
	
	public void set(float[] loc){
		//attr.setAttractor(new Vec2D(loc[0],loc[1]));
		this.set(loc[0],loc[1]);
		hide=false;
		_x=x;
		_y=y;
	}
	
	public void rem() {
		//x=0;
		//y=0;
		hide=true;
	}
	

	
	

}
