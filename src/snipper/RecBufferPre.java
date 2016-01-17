package snipper;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;
import com.softsynth.jsyn.Synth;
import com.softsynth.jsyn.SynthContext;

public class RecBufferPre {

	static int SAMPLE_RATE = 44100;

	static Synthesizer synth;

	FixedRateMonoWriter writer;
	FloatSample sample;
	ChannelIn channelIn;
	LineOut lineOut;

	int numFrames;

	public RecBufferPre(Synthesizer synth, float lengthSec) {

		SynthContext context = Synth.getSharedContext();
		channelIn = new ChannelIn();
		lineOut = new LineOut();
		synth.add(writer = new FixedRateMonoWriter());
		synth.start(SAMPLE_RATE, AudioDeviceManager.USE_DEFAULT_DEVICE, 2,
				AudioDeviceManager.USE_DEFAULT_DEVICE, 2);
		
		this.setLength(lengthSec);
	}

	public RecBufferPre setLength(float lengthSec) {

		this.numFrames = (int) (SAMPLE_RATE * lengthSec);
		return this;
	}

	public RecBufferPre start() {

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

	static void playFrames1(float[] frames) {

		synth = JSyn.createSynthesizer();

		FloatSample sample = new FloatSample(frames);
		sample.setFrameRate(SAMPLE_RATE);
		VariableRateDataReader samplePlayer = new VariableRateMonoReader();
		LineOut lineOut = new LineOut();
		synth.add(lineOut);
		synth.add(samplePlayer);
		samplePlayer.output.connect(0, lineOut.input, 0);
		samplePlayer.rate.set(sample.getFrameRate());
		synth.start();
		lineOut.start();
		samplePlayer.dataQueue.queue(sample);
		System.out.println("GOT SAMPLE, PLAY TIL END");
		try {
			int i = 80;
			do {
				if ((++i % 20) == 19)
					System.out.println();
				System.out.print(".");
				synth.sleepFor(.1);
			} while (samplePlayer.dataQueue.hasMore());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}

	void playFrames(FloatSample sample) {

		VariableRateDataReader samplePlayer = new VariableRateMonoReader();
		synth.add(samplePlayer);

		samplePlayer.output.connect(0, lineOut.input, 0);
		samplePlayer.dataQueue.queue(sample);

		lineOut.start();
		samplePlayer.dataQueue.queue(sample);
		System.out.println("GOT SAMPLE, PLAY TIL END");
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
		RecBufferPre recBuffer = new RecBufferPre(synth, 1.5f);
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

}
