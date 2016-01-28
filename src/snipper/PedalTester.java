package snipper;

import g4p_controls.GEvent;
import g4p_controls.GValueControl;
import processing.core.*;
import util.SoniaUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.LineOut;

public class PedalTester extends PApplet {
	
	PGraphics waveform;
	RecBuffer buffer;
	Snip[] bursts;

	double sleepP = 0.1f, triggerP = 0.1f;
	int border = 100, controlY = 35, waveY = 130, selected = -1;
	
	static Synthesizer synth;
	static LineOut lineOut;
	
	String[] cnames = { "Mode", "Feedback", "Level", "TimeMs" };
	Control[]  controls = new Control[cnames.length];
	PImage bg;
	
	public void settings() { 
		
	  size(800, 480);
	}
	
	public void setup() {
		
		bg = loadImage("pedal800w.png");

	  waveform = createGraphics(width - border*2, 255);
	  
	  for (int i = 0; i < controls.length; i++) {
	  	controls[i] = new Control(this, cnames[i], 
	  			10+border+Control.SIZE/2f+(i*((width-border*2)/(float)cnames.length)), controlY);
		}
		
	  buffer = new RecBuffer(synth, 5f);
		buffer.start();
	
	  new Thread() {
	  	
			public void run() {
	  		while (true) {
		  		try {
		  			synth.sleepFor(sleepP);
					} catch (InterruptedException e) {}
		  		if (Math.random()< triggerP) {
		  			bursts = Snip.getBursts(buffer.getCurrentFrames());
		  			trigger();
		  		}
	  		}
	  	};
	  }.start();
	} 
	 
	public void draw() {
	  
		background(bg);
		strokeWeight(1);
		if (buffer.enabled()) {
			SoniaUtils.computeWaveForm(waveform, buffer.getCurrentFrames(), false);
		}
		image(waveform, border, waveY);
		//stroke(255,206,64); // yellow
		
		drawThreshold(waveY + waveform.height/2f, Snip.SOUND_THRESHOLD);
		drawBursts();
		
		noFill();
		stroke(0);
		strokeWeight(5);
		rect(border-2, waveY-2, waveform.width+4, waveform.height+4,10);
	}

	public void handleKnobEvents(GValueControl knob, GEvent event) {
		for (int i = 0; i < controls.length; i++) {
			if (controls[i].knob == knob)
				controls[i].value.setText(knob.getValueS());
		}
	}
	
	private void drawThreshold(float centerY, float thresh) {
		float s1 = centerY - (waveform.height * thresh);
		float s2 = centerY + (waveform.height * thresh);
		stroke(0,255,0,127);
		line(border, s1, width-border, s1);
		line(border, s2, width-border, s2);
	}

	private void drawBursts() {
		
    if (bursts == null) return;
    
		float centerY = waveY + waveform.height/2f;
		
		for (int i = 0; i < bursts.length; i++) {
			float s1 = PApplet.lerp(0, waveform.width,  bursts[i].start / (float) buffer.size());
			float s2 = PApplet.lerp(0, waveform.width,  bursts[i].stop() / (float) buffer.size());
			float z =  Math.min(1, bursts[i].maxValue() * waveform.height *.9f);
			fill(255,206,64,127);
			//if (i == selected) fill(255,0,0,127); 
			rect(border + s1, centerY-z/2f, s2-s1, z);
		}
	} 
	
	@Override
	public void keyPressed() {
		//if (key == ' ') {trigger();}
	}

	private synchronized void trigger() {
		
		//if (buffer == null) return;
		
		if (false && buffer.enabled()) {
			//bursts = Snip.getBursts(buffer.getCurrentFrames());
			boolean pickOne = bursts.length > 0 && true; 

			if (pickOne) {
				selected = (int) (Math.random()*bursts.length);
				System.out.println("Burst.Idx="+selected+"/"+bursts.length);
				FloatSample floatSample = bursts[selected].toFloatSample(true);
				Player.playSample(synth, lineOut, floatSample, false);
			}
			else {
				for (int i = 0; i < bursts.length; i++) {
					FloatSample floatSample = bursts[i].toFloatSample(true);
					Player.playSample(synth, lineOut, floatSample, false);
				}
			}
		}
	}
	
	@Override
	public void mouseClicked() {

	}

	public static void main(String[] args) {
		synth = JSyn.createSynthesizer();
		lineOut = new LineOut();
		PApplet.main(new String[] { PedalTester.class.getName() });
	}
}
