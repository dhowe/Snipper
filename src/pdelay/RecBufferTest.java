package pdelay;

import processing.core.PApplet;
import processing.core.PGraphics;
import util.SoniaUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;

public class RecBufferTest extends PApplet {
	
	PGraphics waveform;
	RecBuffer buffer;
	boolean paused;
	static Synthesizer synth;
	
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

	  waveform = createGraphics(width-20, height-20);
	  buffer = new RecBuffer(synth, 4f);
	  buffer.start();
	} 
	 
	public void draw() {

		if (paused) return;
		
		background(0, 50, 0);
	  
  	SoniaUtils.computeWaveForm(waveform, buffer.getCurrentFrames(), false);
  	
    image(waveform, 10, 10);
	} 
	
	@Override
	public void keyPressed() {
		if (key == ' ') {
			buffer.toggleEnabled();
		}
	}
	
	@Override
	public void mouseClicked() {
		paused = !paused;
		if (paused) {
			Player.playSample(synth, buffer.lineOut, Player.createSample(buffer.getCurrentFrames()), false);
			Burst[] bursts = Burst.getBursts(buffer.getCurrentFrames(), 0.1f, .01f);
			System.out.println("found "+bursts.length+" bursts");
		}
	}

	public static void main(String[] args) {
		synth = JSyn.createSynthesizer();
		PApplet.main(new String[] { RecBufferTest.class.getName() });
	}
}
