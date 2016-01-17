package snipper;

import java.util.ArrayList;

import util.AudioUtils;

public class Snip {

	public static float SILENCE_THRESHOLD = .005f;
	public static float SOUND_THRESHOLD = .1f;
	
	public static float[] originalFrames;
	public static float silenceThreshold, soundThreshold;
	
	float[] frames; // should this be a clone of the array?
	int start, length, max;

	public Snip(float[] framesI, int startI, int lengthI, int maxI) {
		frames = cloneArray(framesI);
		start = startI;
		length = lengthI;
		max = maxI;
	}

	static float[] cloneArray(float[] src) {
		float[] dest = new float[src.length];
		System.arraycopy(src, 0, dest, 0, src.length );
		return dest;
	}

	public int stop() {

		return (start + length) - 1;
	}
	
	public String toString() {
		
		return "{ start: " + start + ", stop: " + (start+length) + ", length: " + length + " }";
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
		
		if (dbug) System.out.println("Burst.getBursts(" + result.size() + ")");
		
		Snip burst = _getMaxBurst(startIdx, length);
		
		if (burst != null) {
		
			result.add(burst);
			if (dbug) System.out.println("ADDED[" + result.size() + "] " + burst + " ");
			
			int minSize = AudioUtils.SAMPLE_RATE / 10;  // at least 1/10 of sec
			
			int leftStartIdx = startIdx;
			int leftEndIdx = burst.start;
			int leftLength = leftEndIdx - leftStartIdx;
			
			int rightStartIdx = burst.start + burst.length + 1;
			int rightEndIdx = startIdx + length; // end of clip
			int rightLength = rightEndIdx - rightStartIdx;
			
			if (leftLength > minSize) { 
			
				if (dbug) System.out.println("Left: "+leftStartIdx+"-" + leftEndIdx);
				_getBursts(leftStartIdx, leftLength, result);
			}
	
			if (rightLength > minSize) {
				
				if (dbug) System.out.println("Right: "+rightStartIdx+"-" + rightEndIdx);
				_getBursts(rightStartIdx, rightLength, result);
			}
		}
	}

	private static Snip _getMaxBurst(int startIdx, int length) {

		int maxIdx = AudioUtils.absMaxIndex(originalFrames, soundThreshold);
		
		if (maxIdx < 0) return null;
		
		int endIdx = startIdx + length;
		int snipStopIdx = getBurstStop(maxIdx, endIdx-maxIdx, silenceThreshold);
		int snipStartIdx = getBurstStart(maxIdx, maxIdx-startIdx, silenceThreshold);

		return new Snip(originalFrames, snipStartIdx, snipStopIdx-snipStartIdx, maxIdx);
	}

	public static int getBurstStop(int start, int length, float threshold) {

		int numUnder = 0;
		int stopIndex = -1;
		
		System.out.println("getBurstStop() checking frames "+start+"-"+(start+length-1));

		for (int i = start; i < start + length; i++) {
			
			if (originalFrames[i] < threshold) {
				
				//System.out.println("HIT: "+i);
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
		
		if (stopIndex < 0) {
			stopIndex = (start + length) - 1;
			System.out.println("[WARN] No stop point found");
		}


		return stopIndex;
	}

	public static int getBurstStart(int start, int length, float threshold) {

		int numUnder = 0;
		int startIndex = -1;

		for (int i = start; i > start - length; i--) {
			if (originalFrames[i] < threshold) {
				if (++numUnder == 1000) {
					// System.out.println("START-HIT: "+i);
					startIndex = i;
					break;
				}
			} else {
				numUnder = 0;
			}
		}

		if (startIndex < 0) {
			startIndex = (start - length);
			System.out.println("[WARN] No start point found");
		}

		return startIndex;
	}

}
