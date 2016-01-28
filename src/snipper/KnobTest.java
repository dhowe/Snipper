package snipper;

import processing.core.PApplet;
import g4p_controls.GEvent;
import g4p_controls.GValueControl;

public class KnobTest extends PApplet {

	Control a, b, c, d;

	public void settings() {
		size(300, 300);
	}
	public void setup() {
		a = new Control(this, "Delay", 50, 100);
		b = new Control(this, "Feedback", 100, 100);
		c = new Control(this, "Level", 150, 100);
		d = new Control(this, "Time", 200, 100);
	}

	public void handleKnobEvents(GValueControl knob, GEvent event) {
		if (a.knob == knob)
			a.value.setText(knob.getValueS());
		if (b.knob == knob)
			b.value.setText(knob.getValueS());
		if (c.knob == knob)
			c.value.setText(knob.getValueS());
		if (d.knob == knob)
			d.value.setText(knob.getValueS());
	}

	public void draw() {
		background(200);
	}
	
	public static void main(String[] args) {
		PApplet.main(new String[] {KnobTest.class.getName()});
	}

}
