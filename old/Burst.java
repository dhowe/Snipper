package snipper;

import java.util.ArrayList;

import pitaru.sonia.Sample;
import util.AudioUtils;
import util.SoniaUtils;

public class Burst {

	static float SILENCE_THRESHOLD = .005f;
	static float SOUND_THRESHOLD = .1f;
	
	Sample computed;
	float[] frames; // should this be a clone of the array?
	int start, stop, max;

	public Burst(float[] framesI, int startI, int endI, int maxI) {
		frames = framesI;
		start = startI;
		stop = endI;
		max = maxI;
	}

	Sample sample() {
		return sample(false);
	}

	Sample sample(boolean recompute) {
		if (computed == null || recompute) {
			float[] subset = AudioUtils.subset(frames, start, stop - start);
			computed = new Sample(subset.length);
			computed.write(subset);
		}
		return computed;
	}

	public String toString() {
		return "{ start: " + start + ", stop: " + stop + ", size: "
				+ (stop - start) + " }";
	}

	public static Burst[] getBursts(float[] frames) {
		
		return getBursts(frames, SOUND_THRESHOLD, SILENCE_THRESHOLD);
	}
	
	public static Burst[] getBursts(float[] frames, float soundThresh, float silenceThresh) {
		
		ArrayList<Burst> result = new ArrayList<Burst>();
		getBursts(frames, soundThresh, silenceThresh, result);
		return result.toArray(new Burst[0]);
	}

	public static void getBursts(float[] frames, float soundThresh,
			float silenceThresh, ArrayList<Burst> result) {
		_getBursts(0, frames.length-1, frames, soundThresh, silenceThresh, result);
	}

	private static void _getBursts(int startIdx, int length, float[] frames, 
			float soundThresh, float silenceThresh, ArrayList<Burst> result) {

		boolean dbug = true;
		
		if (dbug) System.out.println("Burst.getBursts(" + result.size() + ") :: "+ frames.length);
		
		Burst burst = _getMaxBurst(startIdx, length, frames, soundThresh, silenceThresh);

		if (burst != null) {
		
			result.add(burst);
			
			if (dbug) System.out.println("ADDED[" + result.size() + "] " + burst + " ");
	
			if (burst.start > AudioUtils.SAMPLE_RATE / 10) { // at least 1/10 of sec
			
				if (dbug) System.out.println("Trying: 0-" + (burst.start - 1));
				
				//float[] beg = AudioUtils.subset(burst.frames, 0, burst.start - 1);
				
				getBursts(0, frames, soundThresh, silenceThresh, result);
			}
	
			if (burst.stop < frames.length - (AudioUtils.SAMPLE_RATE / 10)) { // >= 1/10 sec
				
				if (dbug) System.out.println("Trying: " + (burst.stop + 1) + "-" + frames.length);
			
				//float[] end = AudioUtils.subset(burst.frames, burst.stop + 1, burst.frames.length - (burst.stop + 1));
				
				getBursts(end, soundThresh, silenceThresh, result);
			}
		}
	}

//	static Burst getMaxBurst(Sample sample, float soundThresh, float silenceThresh) {
//
//		return getMaxBurst(SoniaUtils.frames(sample), soundThresh, silenceThresh);
//	}

	private static Burst _getMaxBurst(int start, int end, float[] frames, float soundThresh, float silenceThresh) {

		int maxIdx = AudioUtils.absMaxIndex(frames, soundThresh);
		
		if (maxIdx < 0) return null;
		
		int stopIdx = getBurstStop(frames, maxIdx, silenceThresh);
		int startIdx = getBurstStart(frames, maxIdx, silenceThresh);

		return new Burst(frames, startIdx, stopIdx, maxIdx);
	}

	static String toPercent(float val, float total) {
		return Math.round((val / total) * 100) + "%";
	}

	static int getBurstStop(float[] frames, int maxIdx, float threshold) {

		int numUnder = 0;
		int stopIndex = frames.length - 1;

		for (int i = maxIdx; i < frames.length; i++) {
			if (frames[i] < threshold) {
				if (++numUnder == 1000) {
					// System.out.println("STOP-HIT: "+i);
					stopIndex = i;
					break;
				}
			} else {
				numUnder = 0;
			}
		}

		//if (stopIndex == frames.length - 1)
			//System.out.println("[WARN] No stop point found");

		return stopIndex;
	}

	static int getBurstStart(float[] frames, int maxIdx, float threshold) {

		int numUnder = 0;
		int startIndex = 0;

		for (int i = maxIdx; i >= 0; i--) {
			if (frames[i] < threshold) {
				if (++numUnder == 1000) {
					// System.out.println("START-HIT: "+i);
					startIndex = i;
					break;
				}
			} else {
				numUnder = 0;
			}
		}

		if (startIndex == 0)
			System.out.println("[WARN] No start point found");

		return startIndex;
	}
}
