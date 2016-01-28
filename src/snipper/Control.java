package snipper;

import g4p_controls.*;
import processing.core.PApplet;

public class Control {

	static float SIZE = 60;

	GKnob knob;
	GLabel value, name;

	Control(PApplet p, String knobName, float x, float y) {

		name = new GLabel(p, x - 20, y - 20, 100, 20);
		name.setText(knobName);

		value = new GLabel(p, x+10, y + SIZE, 40, 20);
		value.setText("0.50");

		knob = new GKnob(p, x, y, SIZE, SIZE, 0.6f);
		knob.setTurnRange(120, 60);
		knob.setLocalColorScheme(6);
		knob.setOpaque(false);
		knob.setValue(0.5f);
		knob.setNbrTicks(11);
		knob.setShowTicks(true);
		knob.setShowTrack(true);
		knob.setShowArcOnly(false);
		knob.setStickToTicks(false);
		knob.setTurnMode(1283);
		knob.setIncludeOverBezel(true);
		knob.setOverArcOnly(false);
		knob.setSensitivity(1);
		knob.setEasing(1);
	}
}
