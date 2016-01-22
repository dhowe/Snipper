package snipper;

import java.util.ArrayList;

import pitaru.sonia.Sample;
import util.AudioUtils;
import util.SoniaUtils;

public class Burst {

	static float SILENCE_THRESHOLD = .005f;
	static float SOUND_THRESHOLD = .01f;
	
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

		System.out.println("SampleRecordLoop.getBursts(" + result.size() + ") :: "+ frames.length);
		
		Burst burst = getMaxBurst(frames, soundThresh, silenceThresh);

		if (burst == null) {
			System.out.println("null burst");
			return;
		}

		result.add(burst);
		//System.out.println("ADDED[" + result.size() + "] " + burst + " ");

		//System.out.println("CHECKING: 0-" + (burst.start - 1));
		if (burst.start > AudioUtils.SAMPLE_RATE / 10) { // at least 1/10 of sec
			float[] beg = AudioUtils.subset(burst.frames, 0, burst.start - 1);
			getBursts(beg, soundThresh, silenceThresh, result);
		}

		//System.out.println("CHECKING: " + (burst.stop + 1) + "-" + frames.length);
		if (burst.stop < frames.length - AudioUtils.SAMPLE_RATE / 10) { // at least
																																		// 1/10 of
																																		// sec
			float[] end = AudioUtils.subset(burst.frames, burst.stop + 1,
					burst.frames.length - (burst.stop + 1));
			getBursts(end, soundThresh, silenceThresh, result);
		}
	}

	static Burst getMaxBurst(Sample sample, float soundThresh, float silenceThresh) {

		return getMaxBurst(SoniaUtils.frames(sample), soundThresh, silenceThresh);
	}

	static Burst getMaxBurst(float[] frames, float soundThresh, float silenceThresh) {

		int maxIdx = AudioUtils.absMaxIndex(frames, soundThresh);
		
		System.out.print(" maxId="+maxIdx);
		
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

		if (stopIndex == frames.length - 1)
			System.out.println("[WARN] No stop point found");

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
