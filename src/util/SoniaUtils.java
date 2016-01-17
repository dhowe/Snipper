package util;

import pitaru.sonia.LiveInput;
import pitaru.sonia.Sample;
import processing.core.*;

import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.softsynth.jsyn.Synth;

public class SoniaUtils
{
	static float NOISE_THRESHOLD = 0.001f;
	
	static final int SAMPLE_RATE = 44100;
	static final float INPUT_SENSITIVITY = 1.5f;
  static final float BASE_MIDI_C = 8.1758224f;
  static final int NUM_SMOOTHING_FRAMES = (int)(SAMPLE_RATE/1000f * 3f); // 3 MS

	public static float[] frames(Sample s) {

		float[] frames = new float[s.getNumFrames()];
  	s.read(frames);
  	return frames;
	}
	
	
  public static void listAudioDevices() {
  	
      AudioDeviceManager audioManager = AudioDeviceFactory.createAudioDeviceManager();

      int numDevices = audioManager.getDeviceCount();
      for (int i = 0; i < numDevices; i++) {
          String deviceName = audioManager.getDeviceName(i);
          int maxInputs = audioManager.getMaxInputChannels(i);
          int maxOutputs = audioManager.getMaxInputChannels(i);
          boolean isDefaultInput = (i == audioManager.getDefaultInputDeviceID());
          boolean isDefaultOutput = (i == audioManager.getDefaultOutputDeviceID());
          System.out.println("#" + i + " : " + deviceName);
          System.out.println("  max inputs : " + maxInputs
                  + (isDefaultInput ? "   (default)" : ""));
          System.out.println("  max outputs: " + maxOutputs
                  + (isDefaultOutput ? "   (default)" : ""));
      }
  }
  
  public static void drawInputMeterVertical(PApplet p, int x, int y, int w, int h)
  {
    float meterData = LiveInput.getLevel();
    p.fill(0, 100, 0);
    p.rect(x - w/2, y, w , Math.min(h, meterData * -h * INPUT_SENSITIVITY));
 }
  
  public static void drawInputMeter(PApplet p, int x, int y, int w, int h)
  {
    p.fill(0, 100, 0);
    p.rect(x, y, Math.min(w, (LiveInput.getLevel() * w) * INPUT_SENSITIVITY), h);
 }
  
  public static String getCpuPercentage()
  {
    double usage = Synth.getUsage();
    //System.out.println("AudioUtils.getCpuPercentage() -> "+usage);
    int percent = (int) (usage * 100.0); /* Chop trailing digits. */
    return Integer.toString(percent) + "%";
  }

  public static void declickifyEnds(Sample s)
  {
    if (s == null) return;
    declickifyEnds(s, 0, s.getNumFrames());
  }
  
  public static void declickifyEnds(Sample s, int startFrame, int endFrame)
  {
    if (s == null) return;
    //System.out.println("SamplerFi.declickifyEnds(s, "+startFrame+","+endFrame+")");
    
    // write sample data to 'frames
    float[] tmp = new float[s.getNumFrames()];
    s.read(tmp);
    
    // write float[] data to back to the sample
    AudioUtils.declickifyEnds(tmp, true, startFrame, endFrame);
    s.write(tmp);
  }
 	
	public static void computeWaveForm(PApplet p, Sample s, int w, int h, float noiseThreshold)
	{
		computeWaveForm(p.createGraphics(w, h), SoniaUtils.frames(s), noiseThreshold);
	}
	 
	public static void computeWaveForm(PGraphics waveform, float[] frames) {
		computeWaveForm(waveform, frames, true);
	}
	
	public static void computeWaveForm(PGraphics waveform, float[] frames, boolean scaled) {
		computeWaveForm(waveform, frames, NOISE_THRESHOLD, scaled);
	}
	
	public static void computeWaveForm(PGraphics waveform, float[] frames, float noiseThreshold) {
		computeWaveForm(waveform, frames, noiseThreshold, true);
	}
	
	/**
	 * Draws a waveForm into the PGraphics object 
	 */
 	public static void computeWaveFormY(PGraphics waveform, float[] frames, float noiseThreshold, boolean scaled) 
  {	
    float center = waveform.height/2f;
    float[] pts = AudioUtils.wavePoints(frames, waveform.width, noiseThreshold, scaled);
    
    waveform.beginDraw();
    waveform.background(0);
    waveform.stroke(220);
    
    for (int i = 0; i < pts.length; i++) {
    	float y1 = center + (pts[i]   * waveform.height/2f);
    	float y2 = center - (pts[i]   * waveform.height/2f);
      waveform.line(i, y1, i, y2);
    }
    waveform.endDraw();   
  }
 	
 	public static void computeWaveForm(PGraphics waveform, float[] frames, float noiseThreshold, boolean scaled) 
  {	
    float center = waveform.height/2f;
    float[] pts = AudioUtils.wavePoints(frames, waveform.width, noiseThreshold, scaled);
    
    waveform.beginDraw();
    waveform.background(0);
    //\waveform.stroke(220);
    
	  for (int i = 0; i < pts.length-1; i++) {
	  	
	    float y1 = center + (pts[i]   * center);
	    float y2 = center - (pts[i+1] * center);

	    waveform.stroke(220);
	    waveform.line(i, y1, i+1, y2);
	    
	    if (y1>center && y2>center) {
	      waveform.stroke(165);
	      waveform.line(i, center+1, i, y1 - 1);
	    }
	    if (y1<center && y2<center) {
	      waveform.stroke(165);
	      waveform.line(i, center-1, i, y1 + 1);
	    }
	  }
	  
    waveform.endDraw();   
  }
  
	/**
	 * Draws a waveForm into the PGraphics object 
   */
 	public static void computeWaveFormX(PGraphics waveform, float[] frames, float noiseThreshold, boolean scaled) 
  {	
    float center = waveform.height/2f;
    float[] pts = AudioUtils.wavePoints(frames, waveform.width, noiseThreshold, scaled);
    
    waveform.beginDraw();
    waveform.background(0);
    waveform.stroke(220);
    for (int i = 0; i < pts.length-1; i++) {
      float y1 = center + (pts[i]   * waveform.height/2f);
      float y2 = center - (pts[i+1]   * waveform.height/2f);
      waveform.line(i, y1, i+1, y2);
    }

    waveform.endDraw();   
  }

}// end
