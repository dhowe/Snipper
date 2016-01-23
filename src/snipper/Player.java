package snipper;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.*;

public class Player {

	private static boolean dbug;

	static FloatSample createSample(float[] frames) {
		FloatSample sample = new FloatSample(frames);
		sample.setFrameRate(44100f);
		return sample;
	}

	static float[] createFrames() {
		float[] f = new float[5 * 44100];
		float value = 0f;
		for (int i = 0; i < f.length; i++) {
			f[i] = (float) (Math.random() * 2f - 1);// value;
			value += 0.01;
			if (value >= 1.0) {
				value -= 2.0;
			}
		}
		return f;
	}
	
	public static void playSample(Synthesizer synth, LineOut lineOut, FloatSample sample) {
		playSample(synth, lineOut, sample, 0, false);
	}
	
	public static void playSample(Synthesizer synth, LineOut lineOut, 
			FloatSample sample, boolean blocking) {
		playSample(synth, lineOut, sample, 0, blocking);
	}

	public static void playSample(Synthesizer synth, LineOut lineOut, 
			FloatSample sample,  int startFrame, boolean blocking) {
		playSample(synth, lineOut, sample, 0,  sample.getNumFrames(), blocking);
	}
	
	public static void playSample(Synthesizer synth, LineOut lineOut, 
			FloatSample sample,  int startFrame, int numFrames, boolean blocking) {
		
		
		try {
			
			if (dbug) {
				System.out.println("Sample has: channels  = "+ sample.getChannelsPerFrame());
				System.out.println("            frames    = " + sample.getNumFrames());
				System.out.println("            rate      = " + sample.getFrameRate());
				System.out.println("            loopStart = " + sample.getSustainBegin());
				System.out.println("            loopEnd   = " + sample.getSustainEnd());
			}
			
			if (lineOut.getSynthesisEngine() == null) {
				synth.add(lineOut);
				lineOut.start();
			}
			
			VariableRateDataReader samplePlayer;
			
			synth.add(samplePlayer = new VariableRateMonoReader());
			
			samplePlayer.output.connect(0, lineOut.input, 0);
			samplePlayer.rate.set(sample.getFrameRate());
			samplePlayer.dataQueue.queue(sample, startFrame, numFrames);
			
			if (blocking) {
				do {
					synth.sleepFor(.1);
				} while (samplePlayer.dataQueue.hasMore());
			}
 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LineOut lineOut = new LineOut();
		Synthesizer synth = JSyn.createSynthesizer();
		synth.add(lineOut);
		lineOut.start();
		synth.start();
		playSample(synth, lineOut, createSample(createFrames()), false);
	}
}
