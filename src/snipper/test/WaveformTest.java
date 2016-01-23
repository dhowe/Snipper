package snipper.test;

import processing.core.PApplet;
import processing.core.PGraphics;
import util.SoniaUtils;

public class WaveformTest extends PApplet {
	
	PGraphics waveform;
	private float[] frames;
	
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

		waveform = createGraphics(width-20, height-20);		
	}

	private void loadFrames() {
		frames = new float[44100];
		for (int i = 0; i < frames.length; i++) {
			int j =  i % frames.length;
			frames[i] = ((i+frameCount)%frames.length)/(float)frames.length;
			//System.out.println(frmea);
		}
		System.out.println(frames[frames.length/2]);
	} 
	 
	public void draw() {
		
	  background(0,50,0);
	  loadFrames();
  	SoniaUtils.computeWaveForm(waveform, frames);
    image(waveform, 10, 10);
	} 

	public static void main(String[] args) {

		PApplet.main(new String[] { WaveformTest.class.getName() });
	}
}
