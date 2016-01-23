package snipper;

import processing.core.PApplet;
import processing.core.PGraphics;
import util.SoniaUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.LineOut;

public class RecBufferTest extends PApplet {
	
	PGraphics waveform;
	RecBuffer buffer;
	Snip[] bursts;
	int off = 10, waveWidth=380, selected = -1;
		
	static Synthesizer synth;
	static LineOut lineOut;
	
	double sleepP = 0.1f, triggerP = 0.1f;
	 
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

	  waveform = createGraphics(waveWidth, height-off*2);
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

		background(0, 50, 0);
	  
		if (buffer.enabled()) {
			SoniaUtils.computeWaveForm(waveform, buffer.getCurrentFrames(), false);
		}
		
		image(waveform, off, off);
		
		drawThreshold(Snip.SOUND_THRESHOLD);
		
		
		drawBursts();
	}

	private void drawThreshold(float thresh) {
		float s1 = (off + waveform.height/2f) - (waveform.height * thresh);
		float s2 = (off + waveform.height/2f) + (waveform.height * thresh);
		stroke(0,255,0);
		line(off, s1, width-off, s1);
		line(off, s2, width-off, s2);
	}

	private void drawBursts() {
    if (bursts == null) return;
    
		float centerY = off + waveform.height/2f;
		for (int i = 0; i < bursts.length; i++) {
			float s1 = PApplet.lerp(0, waveform.width,  bursts[i].start / (float) buffer.size());
			float s2 = PApplet.lerp(0, waveform.width,  bursts[i].stop() / (float) buffer.size());
			float z =  Math.min(1, bursts[i].maxValue()) * waveform.height;
			fill(0,0,255,127);
			if (i == selected)
				fill(255,0,0,127); 
			rect(off + s1, centerY-z/2f, s2-s1, z);
		}
	} 
	
	@Override
	public void keyPressed() {
		//if (key == ' ') {trigger();}
	}

	private synchronized void trigger() {
		
		//if (buffer == null) return;
		
		if (buffer.enabled()) {
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
		PApplet.main(new String[] { RecBufferTest.class.getName() });
	}
}
