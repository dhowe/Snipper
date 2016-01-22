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
	int off = 10;
	
	public static int maxId;
	
	static Synthesizer synth;
	static LineOut lineOut;
	
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

	  waveform = createGraphics(width-off*2, height-off*2);
	  buffer = new RecBuffer(synth, 4f);
	} 
	 
	public void draw() {

		background(0, 50, 0);
	  
		if (buffer.enabled()) {
			SoniaUtils.computeWaveForm(waveform, buffer.getCurrentFrames(), false);
		}
		
		image(waveform, off, off);
		stroke(0,255,0);

		float centerY = off + waveform.height/2f;
		float s1 = centerY - (waveform.height * Snip.SOUND_THRESHOLD);
		float s2 = centerY + (waveform.height * Snip.SOUND_THRESHOLD);
		line(off, s1, width-off, s1);
		line(off, s2, width-off, s2);
		
		float z = PApplet.lerp(0, waveform.width, maxId/buffer.length());
		line(z, off, z, height-off);
		
    if (bursts != null) {
    	fill(255,0,0,127); // draw bursts
    	for (int i = 0; i < bursts.length; i++) {
	    	s1 = PApplet.lerp(0, waveform.width,  bursts[i].start / (float) buffer.size());
	    	s2 = PApplet.lerp(0, waveform.width,  bursts[i].stop() / (float) buffer.size());
	  		z =  Math.min(1, bursts[i].maxValue()) * waveform.height;
	    	rect(off + s1, centerY-z/2f, s2-s1, z);
    	}
    }
	} 
	
	@Override
	public void keyPressed() {
		
		if (key == ' ') {
			
			if (buffer.enabled()) {
				bursts = Snip.getBursts(buffer.getCurrentFrames());
				System.out.println("Found "+bursts.length+" burst(s) :: "+buffer.size());
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
