import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.physics.behaviors.AttractionBehavior;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractMarker;


public class ParticleMarker extends AbstractMarker {
	
	Location location;
	Particle particle;
	AttractionBehavior attractionBehavior;
	
	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void draw(PGraphics pg, float x, float y) {
		attractionBehavior.setAttractor(new Vec2D(x, y));
		//particle.setAttractor(attractionBehavior);
		pg.triangle(x, y, x+5, y, x, y+5);
	}

	@Override
	public void drawOuter(PGraphics pg, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean isInside(float checkX, float checkY, float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

}
