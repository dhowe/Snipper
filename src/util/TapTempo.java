package util;

public class TapTempo {
	
	static float resetIntervalFactor = 3;
	
	float averageInterval = Float.MAX_VALUE;
	float[] beatTimes;
	
	public TapTempo() {
		beatTimes = new float[3];
	}
	
	public void reset() {
		for (int i = 0; i < beatTimes.length; i++)
			beatTimes[i] = 0;
	}
	
	public float bpm() {
		
		for (int i = 0; i < beatTimes.length; i++) {
			if (beatTimes[i] == 0)
				return 0;
		}
		
		float invl1 = beatTimes[0] - beatTimes[1];
		float intv2 = beatTimes[1] - beatTimes[2];
		averageInterval = (invl1+intv2) / 2f;
		
		return 60 / averageInterval;
	}
	
	public void addBeat(float time) {
		
		if (time - beatTimes[0] > resetIntervalFactor * averageInterval) {
			reset();
		}
			
		for (int i = beatTimes.length-1; i > 0; i--)
			beatTimes[i] = beatTimes[i-1];
		beatTimes[0] = time;	
	}
	
	public String toString() {
		
		String s = "[ ";
		for (int i = beatTimes.length-1; i >= 0; i--)
			s += beatTimes[i]+"  ";
		return s + "]";
	}

	
}
