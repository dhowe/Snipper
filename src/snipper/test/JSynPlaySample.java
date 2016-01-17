package snipper.test;

import java.net.URL;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.*;
import com.jsyn.util.SampleLoader;

public class JSynPlaySample {

	private Synthesizer synth;
	private VariableRateDataReader samplePlayer;
	private LineOut lineOut;

	private void test() {

		FloatSample sample = null;
		if (1 == 2) {
			try {
				URL sampleFile = new URL(
						"http://www.softsynth.com/samples/Clarinet.wav");
				sampleFile = new URL("http://localhost/dhowe/Desktop/piano1.wav");
				sample = SampleLoader.loadFloatSample(sampleFile);
				// sampleFile = new
				// URL("http://www.softsynth.com/samples/NotHereNow22K.wav");
			} catch (Exception e2) {
				e2.printStackTrace();
				return;
			}
		} else {

			float[] f = new float[5 * 44100];
			float value = 0f;
			for (int i = 0; i < f.length; i++) {
				f[i] = (float) (Math.random() * 2f - 1);// value;
				value += 0.01;
				if (value >= 1.0) {
					value -= 2.0;
				}
			}
			sample = new FloatSample(f);
			sample.setFrameRate(44100f);
		}

		playSample(sample);
		System.out.println(sample.getFrameRate());
	}

	private void playSample(FloatSample sample) {

		synth = JSyn.createSynthesizer();
		synth.start();

		try {
			// Add an output mixer.
			synth.add(lineOut = new LineOut());

			// Load the sample and display its properties.
			// SampleLoader.setJavaSoundPreferred(false);

			System.out.println("Sample has: channels  = "
					+ sample.getChannelsPerFrame());
			System.out.println("            frames    = " + sample.getNumFrames());
			System.out.println("            rate      = " + sample.getFrameRate());
			System.out.println("            loopStart = " + sample.getSustainBegin());
			System.out.println("            loopEnd   = " + sample.getSustainEnd());

			if (sample.getChannelsPerFrame() == 1) {
				synth.add(samplePlayer = new VariableRateMonoReader());
				samplePlayer.output.connect(0, lineOut.input, 0);
			} else if (sample.getChannelsPerFrame() == 2) {
				synth.add(samplePlayer = new VariableRateStereoReader());
				samplePlayer.output.connect(0, lineOut.input, 0);
				samplePlayer.output.connect(1, lineOut.input, 1);
			} else {
				throw new RuntimeException("Can only play mono or stereo samples.");
			}

			samplePlayer.rate.set(sample.getFrameRate());

			// We only need to start the LineOut. It will pull data from the
			// sample player.
			lineOut.start();

			// We can simply queue the entire file.
			// Or if it has a loop we can play the loop for a while.
			if (sample.getSustainBegin() < 0) {
				System.out.println("queue the sample");
				samplePlayer.dataQueue.queue(sample);
			} else {
				System.out.println("queueOn the sample");
				samplePlayer.dataQueue.queueOn(sample);
				synth.sleepFor(8.0);
				System.out.println("queueOff the sample");
				samplePlayer.dataQueue.queueOff(sample);
			}

			// Wait until the sample has finished playing.
			do {
				synth.sleepFor(1.0);
			} while (samplePlayer.dataQueue.hasMore());

			synth.sleepFor(0.5);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Stop everything.
		synth.stop();
	}

	public static void main(String[] args) {
		new JSynPlaySample().test();
	}
}
