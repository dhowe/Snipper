package snipper.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import snipper.Snip;
import util.AudioUtils;

public class CreateBurstsTest {

	public CreateBurstsTest() {
		Snip.silenceThreshold = .001f;
		Snip.soundThreshold = .09f;
	}
	
//		for (int i = 0; i < data.length; i++)
//			frames[dataOffset + i] = data[i];
	
  @Test
  public void testGetMaxBurst() {
  	
  	float[] frames = new float[] {
  			0,    0,   0,  .7f,  .8f, .9f, .6f, .5f, .4f, .2f, 
  			.1f,  0,   0,   0,   0,    0,   0,  .7f, .8f, .91f, 
  			.6f, .5f, .4f, .2f, .1f,   0,   0,   0
  	};
  	Snip.originalFrames = frames;
  	System.out.println(frames.length);
    	
  	Snip max = Snip._getMaxBurst(0, frames.length, 2);
  	assertEquals(19, max.max);
  	assertEquals(15, max.start); 
  	assertEquals(12, max.length);
  	assertEquals(26, max.stop());

  	// nothing above silenceThreshold
  	for (int i = 0; i < frames.length; i++)
  		frames[i] = (float) (Math.random() * Snip.SILENCE_THRESHOLD);
  	Snip empty = Snip._getMaxBurst(0, frames.length, 2);
  	assertEquals(null, empty);
  	
  	// nothing above soundThreshold 
  	for (int i = 0; i < frames.length; i++) 
  		frames[i] = (float) (Snip.SILENCE_THRESHOLD+.01);
  	Snip tooQuiet = Snip._getMaxBurst(0, frames.length, 2);
  	assertEquals(null, tooQuiet);
  	
  	// nothing below soundThreshold 
  	for (int i = 0; i < frames.length; i++) 
  		frames[i] = (float) (Snip.SILENCE_THRESHOLD+Math.random());
  	Snip tooLoud = Snip._getMaxBurst(0, frames.length, 2);
  	System.out.println(tooLoud);

  	assertEquals(true, tooLoud.max >= 0 && tooLoud.max < frames.length);
  	assertEquals(0, tooLoud.start); 
  	assertEquals(frames.length, tooLoud.length);
  	assertEquals(frames.length-1, tooLoud.stop());

  }
  
  @Test
  public void testGetBurstStartStop() {

  	float[] frames = new float[] { 0, 0, 0, .7f, .8f, .9f, .6f, .5f, .4f, .2f, .1f, 0, 0, 0 };
  	
		Snip.originalFrames = frames;

		
		int startIdx = 0;
		int length = frames.length;
		int maxId = AudioUtils.absMaxIndex(frames);
		
		//System.out.println(maxId);
		//assertEquals(AudioUtils.absMaxIndex(data) + dataOffset, maxId);
		assertEquals(5, maxId);
		
		
		int checkLength = length - (maxId+1);// compute length to last-frame
		
		//System.out.println("length: "+length);
		//System.out.println("maxId: "+maxId);
		//System.out.println("checkLength: "+checkLength);
		
		assertEquals(frames.length, (maxId+1) + checkLength);

  	int burstStop = Snip.getBurstStop(maxId+1, checkLength, Snip.silenceThreshold, 2);
  	assertEquals(12, burstStop);

  	checkLength = (maxId - 1) - startIdx; // compute length to first-frame
  	//System.out.println("checkLength: " + checkLength);
  	int burstStart = Snip.getBurstStart(maxId-1, checkLength, Snip.silenceThreshold, 2);
  	assertEquals(1, burstStart);
  }
}
