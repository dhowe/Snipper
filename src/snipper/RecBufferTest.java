package snipper;

import processing.core.PApplet;
import processing.core.PGraphics;
import util.SoniaUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;

public class RecBufferTest extends PApplet {
	
	PGraphics waveform;
	RecBuffer buffer;
	Snip[] bursts;
	
	static Synthesizer synth;
	static LineOut lineOut;
	
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

	  waveform = createGraphics(width-20, height-20);
	  buffer = new RecBuffer(synth, 4f);
	} 
	 
	public void draw() {

		background(0, 50, 0);
	  
		if (buffer.enabled())
			SoniaUtils.computeWaveForm(waveform, buffer.getCurrentFrames(), false);
  	
    image(waveform, 10, 10);
    
    fill(255,0,0,127); // draw bursts
    if (bursts != null) {
	    	for (int i = 0; i < bursts.length; i++) {
		    	int s1 = (int) PApplet.lerp(0, waveform.width,  bursts[i].start / (float) buffer.size());
		    	int s2 = (int) PApplet.lerp(0, waveform.width,  bursts[i].stop() / (float) buffer.size());
		    	rect(10 + s1, 10, s2-s1, waveform.height);
	    	}
    }
	} 
	
	@Override
	public void keyPressed() {
		
		if (key == ' ') {
			
			if (buffer.enabled()) {
				bursts = Snip.getBursts(buffer.getCurrentFrames());
				System.out.println("found "+bursts.length+" bursts :: "+buffer.size());
				buffer.stop();
			}
			else {
				bursts = null;
				buffer.start();
			}
		}
	}
	
	@Override
	public void mouseClicked() {

	}

	public static void main(String[] args) {
		synth = JSyn.createSynthesizer();
		lineOut = new LineOut();
		PApplet.main(new String[] { RecBufferTest.class.getName() });
	}
}
