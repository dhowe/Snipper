package snipper;

import java.util.ArrayList;

import com.jsyn.data.FloatSample;

import util.AudioUtils;

public class Snip {

	public static float SILENCE_THRESHOLD = .01f;
	public static float SOUND_THRESHOLD = .1f;
	
	public static float[] originalFrames;
	public static float silenceThreshold, soundThreshold;
	
	public float[] frames;
	public int start, length, max;

	public Snip(float[] framesI, int startI, int lengthI, int maxI) {
		
		frames = cloneArray(framesI); // clone or no?
		start = startI;
		length = lengthI;
		max = maxI;
	}

	static float[] cloneArray(float[] src) {
		float[] dest = new float[src.length];
		System.arraycopy(src, 0, dest, 0, src.length );
		return dest;
	}
	
	public FloatSample toFloatSample(boolean rev) {
		float[] f = cloneArray(frames, start, length);
		if (rev) {
	    for (int j = 0; j < f.length; j++)
	      f[j] = f[f.length-1-j];
		}
		FloatSample fs = new FloatSample(f); // cache?
		fs.setFrameRate(AudioUtils.SAMPLE_RATE);
		return fs;
	}
	
	static float[] cloneArray(float[] src, int startIdx, int length) {
		float[] dest = new float[length];
		System.arraycopy(src, startIdx, dest, 0, length);
		return dest;
	}

	public int stop() {

		return (start + length) - 1;
	}
	
	public String toString() {
		
		return "{ start: " + start + ", max: " + max + ", stop: " + stop() + ", length: " + length + " }";
	}

	public static Snip[] getBursts(float[] frames) {
		
		return getBursts(frames, SOUND_THRESHOLD, SILENCE_THRESHOLD);
	}
	
	public static Snip[] getBursts(float[] frames, float soundThresh, float silenceThresh) {
		
		originalFrames = frames; // refactor
		silenceThreshold = silenceThresh;
		soundThreshold = soundThresh;
		
		ArrayList<Snip> result = new ArrayList<Snip>();
		_getBursts(0, frames.length, result);
		return result.toArray(new Snip[0]);
	}

	private static void _getBursts(int startIdx, int length, ArrayList<Snip> result) {

		boolean dbug = false;
		
		if (dbug) System.out.print("Burst.getBursts(" + result.size() 
				+ ") :: "+startIdx+"-"+(startIdx+length-1)+" ");
				
		Snip burst = _getMaxBurst(startIdx, length);
		
		if (dbug)System.out.println(burst!=null);
		
		if (burst != null) {
			
			result.add(burst);
			if (dbug) System.out.println("ADDED[" + result.size() + "] " + burst + " ");
			
			int minSize = AudioUtils.SAMPLE_RATE / 10;  // at least 1/10 of sec
			
			int leftStartIdx = startIdx;
			int leftEndIdx = burst.start-1;
			int leftLength = (leftEndIdx - leftStartIdx) + 1;
			
			if (leftStartIdx > leftEndIdx)
				throw new RuntimeException("Invalid (left) start="+leftStartIdx+" end="+leftEndIdx);

			if (leftLength > minSize) { 
			
				if (dbug) System.out.println("Left: "+leftStartIdx+"-" + leftEndIdx);
				_getBursts(leftStartIdx, leftLength, result);
			}
						
			int rightStartIdx = burst.start + burst.length;
			int rightEndIdx = startIdx + length; // end of clip
			int rightLength = rightEndIdx - rightStartIdx + 1;
			
			if (rightStartIdx > rightEndIdx)
				throw new RuntimeException("Invalid (right) start="+rightStartIdx+" end="+rightEndIdx);
			
			if (rightLength > minSize) {
				
				if (dbug) System.out.println("Right: "+rightStartIdx+"-" + rightEndIdx);
				_getBursts(rightStartIdx, rightLength, result);
			}
		}
	}

	public static Snip _getMaxBurst(int startIdx, int length) {
		
		return _getMaxBurst(startIdx, length, 100);
	}
	
	public static Snip _getMaxBurst(int startIdx, int length, int numRequiredForStartStop) {

		int maxIdx = AudioUtils.absMaxIndex(startIdx, length, originalFrames, soundThreshold);

		if (maxIdx < 0) return null; // no loud enough sound
	
		int endIdx = startIdx + length;
		int snipStopIdx = getBurstStop(maxIdx, endIdx-maxIdx, silenceThreshold, numRequiredForStartStop);
		int snipStartIdx = getBurstStart(maxIdx, maxIdx-startIdx, silenceThreshold, numRequiredForStartStop);

		// either no stop or no start point found
		if (snipStopIdx < 0) {
			snipStopIdx = endIdx - 1;
			return null;
		}
	
		if (snipStartIdx < 0)
			return null;
		
		return new Snip(originalFrames, snipStartIdx, (snipStopIdx-snipStartIdx) + 1, maxIdx);
	}

	public static int getBurstStop(int start, int length, float threshold, int numRequiredForStop)  {

		int numUnder = 0;
		int stopIndex = -1;
		
		//System.out.println("getBurstStop() checking frames "+start+"-"+(start+length-1));

		for (int i = start; i < Math.min(originalFrames.length, start + length); i++) {
			
			if (Math.abs(originalFrames[i]) < threshold) {
				
				//System.out.println("HIT: "+i);
				if (++numUnder == numRequiredForStop) {
					//System.out.println("STOP-HIT: "+i);
					stopIndex = i;
					break;
				}
			}
			else {
				numUnder = 0;
			}
		}
		
		if (stopIndex < 0) {
			//stopIndex = -1; // (start + length) - 1;
			//System.out.print("[WARN] No stop point found: num="+numUnder+"/"+numRequiredForStop);
		}


		return stopIndex;
	} 
	
	public static int getBurstStart(int start, int length, float threshold, int numRequiredForStart) {

		int numUnder = 0;
		int startIndex = -1;

		for (int i = start; i > Math.max(0, start - length); i--) {
			if (Math.abs(originalFrames[i]) < threshold) {
				if (++numUnder == numRequiredForStart) {
					// System.out.println("START-HIT: "+i);
					startIndex = i;
					break;
				}
			} else {
				numUnder = 0;
			}
		}

		if (startIndex < 0) {
			//startIndex = -1;//(start - length);
			//System.out.print("[WARN] No start point found "+numUnder+"/"+numRequiredForStart);
		}

		return startIndex;
	}

	 public float maxValue() {
		return frames[max];
	}

}
