package pdelay.test;
import pitaru.sonia.Sonia;
import processing.core.PApplet;
import util.TapTempo;

public class TapTempoTest extends PApplet {

	TapTempo tapTempo;

	public void settings() {
		size(400, 200);
	}

	public void setup() {
		Sonia.start(this);
		tapTempo = new TapTempo();
	}

	public void draw() {
		background(0);
		text("BPM: "+tapTempo.bpm(), 20, 20);
	}

	public void keyReleased() {
		if (key == ' ') 
			tapTempo.addBeat(Sonia.currentTime());
	}

	public static void main(String[] args) {
		
		PApplet.main(new String[] { TapTempoTest.class.getName() });
	}
}
