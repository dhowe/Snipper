package pdelay;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;

public class RecBuffer {
	
	// Add octave-up/down (with pitch)
	// Add reverse               
	// Add tremolo ?

	static int SAMPLE_RATE = 44100;

	static Synthesizer synth;

	VariableRateDataReader samplePlayer;
	FixedRateMonoWriter writer;
	FloatSample sample;
	ChannelIn channelIn;
	LineOut lineOut;

	int numFrames;

	public RecBuffer(Synthesizer synth, float lengthSec) {

		synth.add(channelIn = new ChannelIn());
		synth.add(writer = new FixedRateMonoWriter());
		
		synth.start(SAMPLE_RATE, AudioDeviceManager.USE_DEFAULT_DEVICE, 2,
				AudioDeviceManager.USE_DEFAULT_DEVICE, 2);

		this.setLength(lengthSec);
	}

	public RecBuffer setLength(float lengthSec) {

		this.numFrames = (int) (SAMPLE_RATE * lengthSec);
		return this;
	}

	public RecBuffer start() {

		sample = new FloatSample(numFrames, 1);

		channelIn.output.connect(writer.input);

		writer.start(); // b/c writer is not pulled by anything
		lineOut.start();

		writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
		
		return this;
	}

	public float[] getCurrentFrames() {
		
		//System.out.println("NumFrames: "+numFrames);
		int writeCursor = (int) (writer.dataQueue.getFrameCount() % numFrames);
		float[] frames = new float[numFrames];
		sample.read(frames); // fill frames array

		float[] result = new float[numFrames]; // reorder
		for (int i = 0; i < numFrames; i++) {
			result[i] = frames[(writeCursor + i) % numFrames];
		}

		return result;
	}

	static FloatSample createSample(float[] frames) {
		
		FloatSample sample = new FloatSample(frames);
		sample.setFrameRate(SAMPLE_RATE);
		return sample;
	}

	void playFrames(FloatSample sample) {

		if (lineOut == null) {
			synth.add(lineOut = new LineOut());
			lineOut.start();
		}
		if (samplePlayer == null) {
			synth.add(samplePlayer = new VariableRateMonoReader());
			samplePlayer.output.connect(0, lineOut.input, 0);
		}
		
		samplePlayer.dataQueue.clear();
		samplePlayer.dataQueue.queue(sample);
		System.out.println("PLAY TIL END");
		
		try {
			int i = 0;
			do {
				if ((++i % 80) == 79)
					System.out.println();
				System.out.print(".");
				synth.sleepFor(.1);
			} while (samplePlayer.dataQueue.hasMore());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}

	public static void main(String[] args) {

		Synthesizer synth = JSyn.createSynthesizer();
		RecBuffer recBuffer = new RecBuffer(synth, 1.5f);
		recBuffer.start();

		System.out.println("RECORDING");

		try {
			double time = synth.getCurrentTime();
			synth.sleepUntil(time + 5.0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("DONE");
		
		FloatSample fs = createSample(recBuffer.getCurrentFrames());
		Player.playSample(synth, recBuffer.lineOut, fs, true);

		synth.stop();
	}

	public boolean enabled() {
		// ...
		return false;
	}
	
	public RecBuffer enabled(boolean val) {
		// ...
		return this;
	}
	
	public boolean toggleEnabled() {
		// ...
		return enabled();
	}

}
