import de.fhpotsdam.unfolding.Map;
import processing.core.PApplet;


public class HelloWorld extends PApplet {
	
	Map map;
	
	public void setup() {
		size(600, 600);
		
		map = new Map(this);
	}
	
	public void draw() {
		background(255);
		
		map.draw();
		
		ellipse(mouseX, mouseY, 20, 20);
	}
	
}
