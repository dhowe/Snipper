package snipper;


import java.util.ArrayList;

import pitaru.sonia.*;
import processing.core.*;
import util.AudioUtils;
import util.SoniaUtils;

public class SampleRecordLoop extends PApplet {
	
	int SAMPLE_RATE = 44100;
	int SPECTRUM_LENGTH = 256;
	float LEVEL_GAIN_SCALE = 50f;
	float SILENCE_THRESHOLD = .005f;
	float SOUND_THRESHOLD = .01f;
	
  float fftMax = -1000;
	PGraphics waveform;
	Sample sample;
	PImage meterImg;
	
	int startIdx, stopIdx, maxIdx;
	
	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

		meterImg = loadImage("meter125x12.png"); 

	  Sonia.start(this); 
	  
	  LiveInput.start();	  
	  //sample = new Sample(44100 * 1);  
	  sample = new Sample("piano1.wav");
	  //sample.repeat();
	} 
	 
	public void draw() {
		
	  background(0,50,0);
	  
	  if (true || sample.isPlaying()) {
	  	
	  	if (waveform == null) 
	  		computeWaveForm(sample, width-20, height-20);
	  	
	    image(waveform, 10, 10);
	    
	    fill(255);
	    int sliderX = (int) PApplet.lerp(0, waveform.width,  sample.getCurrentFrame() / (float) sample.getNumFrames());
	    rect(10+sliderX, (height-waveform.height)/2, 2, waveform.height);
	    
	    int s1 = (int) PApplet.lerp(0, waveform.width,  startIdx / (float) sample.getNumFrames());
	    int s2 = (int) PApplet.lerp(0, waveform.width,  stopIdx / (float) sample.getNumFrames());
	    fill(255,0,0,64);
	    rect(10+s1, (height-waveform.height)/2, s2-s1, waveform.height);
	    
//	    
//	    rect(10+s2, (height-waveform.height)/2, 2, waveform.height);
//	    
//	    fill(255,0,0);
//	    int s3 = (int) PApplet.lerp(0, waveform.width,  maxIdx / (float) sample.getNumFrames());
//	    rect(10+s3, (height-waveform.height)/2, 2, waveform.height);
	    
	  }
	  else {
	  	SoniaUtils.drawInputMeter(this, 10, 10, width-20, height-20);
	  }
	} 

  public void computeWaveForm(Sample s, int w, int h) 
  {
  	rectMode(CENTER);

  	System.out.println("SampleRecordLoop.computeWaveForm("+w+","+h+")");
    float[] pts = wavePoints(s, w);
    
    waveform = createGraphics(w, h);
    float center = waveform.height/2f;
    
    waveform.beginDraw();
    
    waveform.noStroke();
    waveform.fill(0);
    waveform.rect(0,0, waveform.width-1, waveform.height-1);
    
    waveform.smooth();
    waveform.noFill();
    
    for (int i = 0; i < pts.length-1; i++) {
    	
      float y1 = center+(pts[i]   * h/2f);
      float y2 = center+(pts[i+1] * h/2f);

      waveform.stroke(220);
      waveform.line(i, y1, i+1, y2);
      
      waveform.stroke(165);
      
      if (y1>center && y2>center)
        waveform.line(i, center+1, i, y1-1);
      if (y1<center && y2<center) 
        waveform.line(i, center-1, i, y1+1);
    }

    waveform.endDraw();
    
    rectMode(CORNER);
  }

  Burst[] b;
  public void keyReleased() {
  	if (key==' ') {
  	  b = getBursts(sample);
  		System.out.println("Found "+b.length+" bursts");
  		for (int i = 0; i < b.length; i++) {
				System.out.println(i+") "+b[i]);
			}
  	}
  	if (key=='1') {
  		startIdx = b[0].start;
  		stopIdx = b[0].stop;
  		maxIdx = b[0].max;
  	}
  	else if (key=='2') {
  		startIdx = b[1].start;
  		stopIdx = b[1].stop;
  		maxIdx = b[1].max;
  	}
  	if (key=='s') {
  		Burst b = getMaxBurst(sample, SOUND_THRESHOLD, SILENCE_THRESHOLD);
  		startIdx = b.start;
  		stopIdx = b.stop;
  		maxIdx = b.max;
  		sample.stop();
  		//waveform = null;
  		sample.repeat(startIdx, stopIdx);
  	}
  }
   
	private Burst[] getBursts(Sample sample) {
		
		ArrayList<Burst> bursts = new ArrayList<Burst>();
		getBursts(frames(sample), bursts);
		return bursts.toArray(new Burst[0]);
	}
	
	private void getBursts(float[] frames, ArrayList<Burst> result) {
		
		System.out.println("SampleRecordLoop.getBursts("+result.size()+") :: "+frames.length);
		Burst burst = getMaxBurst(frames, SOUND_THRESHOLD, SILENCE_THRESHOLD);
		
		if (burst == null) return;
		
		result.add(burst);
		System.out.println("ADDED["+result.size()+"] "+burst+" ");

		System.out.println("CHECKING: 0-"+(burst.start-1));
		if (burst.start > SAMPLE_RATE/10) { // at least 1/10 of sec 
			float[] beg = subset(burst.frames, 0, burst.start-1);
			getBursts(beg, result);
	  }
		
		System.out.println("CHECKING: "+(burst.stop+1)+"-"+frames.length);
		if (burst.stop < frames.length-SAMPLE_RATE/10) { // at least 1/10 of sec
			float[] end = subset(burst.frames, burst.stop+1, burst.frames.length-(burst.stop+1));
			getBursts(end, result);
		}
	}
		
	private Burst getMaxBurst(Sample sample, float soundThresh, float silenceThresh) {
		
		return getMaxBurst(frames(sample), soundThresh, silenceThresh);
	}
		
	private Burst getMaxBurst(float[] frames, float soundThresh, float silenceThresh) {

		int maxIdx = maxIndex(frames, soundThresh);
		if (maxIdx < 0) return null;
		int stopIdx = getBurstStop(frames, maxIdx, SILENCE_THRESHOLD);
		int startIdx = getBurstStart(frames, maxIdx, SILENCE_THRESHOLD);
		
		if (0==1) {
			System.out.println("Length: "+frames.length);
			System.out.println("MaxIdx: "+maxIdx);
			System.out.println("MaxVal: "+frames[maxIdx]+"\n");
			System.out.println("StartIdx: "+startIdx);
			System.out.println("StopIdx: "+stopIdx);
			System.out.println("Total-Length: "+toPercent( (stopIdx-startIdx), frames.length));
		}
		
		return new Burst(frames, startIdx, stopIdx, maxIdx);
	}

	private String toPercent(float val, float total) {
		return Math.round((val/total) * 100) + "%";
	}

	private int getBurstStop(float[] frames, int maxIdx, float threshold) {
		
		int numUnder = 0;
		int stopIndex = frames.length-1;
		
		for (int i = maxIdx; i < frames.length; i++) {
			if (frames[i] < threshold) {
				if (++numUnder == 1000) {
					//System.out.println("STOP-HIT: "+i);
					stopIndex = i;
					break;
				}
			}
			else {
				numUnder = 0;
			}
		}
		
		if (stopIndex == frames.length-1)
			System.out.println("[WARN] No stop point found");
		
		return stopIndex;
	}
	
	private int getBurstStart(float[] frames, int maxIdx, float threshold) {
		
		int numUnder = 0;
		int startIndex = 0;
		
		for (int i = maxIdx; i >= 0; i--) {
			if (frames[i] < threshold) {
				if (++numUnder == 1000) {
					//System.out.println("START-HIT: "+i);
					startIndex = i;
					break;
				}
			}
			else {
				numUnder = 0;
			}
		}
		
		if (startIndex == 0)
			System.out.println("[WARN] No start point found");
		
		return startIndex;
	}

	public void mousePressed() {
		
	  LiveInput.startRecLoop(sample); // Record LiveInput data into the Sample object. 
	  // The recording will automatically end when all of the Sample's frames are filled with data. 
	  println("REC");
	} 

	public void mouseReleased(){
		
	  LiveInput.stopRec(sample); 
	  sample.repeat();
	  println("PLAY");
	} 
	
	private float[] wavePoints(Sample s, int w) {
		
		float[] pts = new float[w]; // 1 pt per pixel
		int numFrames = s.getNumFrames();
		
    int mod = s.getNumFrames() / w;
    
    float[] frames = new float[numFrames];
    s.read(frames);
    
    float max = maxValue(frames);

		for (int i = 1; i < pts.length; i++) { // should calculate max over idx-mod/2 -> idx+mod/2
			int idx = i * mod;
			pts[i] = (frames[idx] / max);
		}
    
    pts[0] = pts[pts.length-1]; // first = last
    
		return pts;
	}

	public static float[] frames(Sample s) {

		float[] frames = new float[s.getNumFrames()];
  	s.read(frames);
  	return frames;
	}
    
	public static int maxIndex(float[] frames) {
		return maxIndex(frames, 0);
	}
	
	public static int maxIndex(float[] frames, float minThreshold) {
			
		int maxIdx = -1;
		float max = minThreshold;
		for (int i = 0; i < frames.length; i++) {
			float f = Math.abs(frames[i]);
			if (f > max) {
				max = f;
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	public static float maxValue(float[] frames) {
		return maxValue(frames, 0);
	}
	
	public static float maxValue(float[] frames, float minThreshold) {
		return frames[maxIndex(frames, minThreshold)];
	}
	
	public static void main(String[] args) {
		
		PApplet.main(new String[] { SampleRecordLoop.class.getName() });
	}

}
