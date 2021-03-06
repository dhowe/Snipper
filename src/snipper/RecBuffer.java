package snipper;

import util.AudioUtils;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;
import com.jsyn.unitgen.PeakFollower;

public class RecBuffer {

	static Synthesizer synth;

	FixedRateMonoWriter writer;
	PeakFollower follower;
	FloatSample sample;
	ChannelIn channelIn;

	int numFrames;

	public RecBuffer(Synthesizer s, float lengthSec) {

		synth = s;
		initSynth();
		this.length(lengthSec);
	}

	private void initSynth() {
		
		if (channelIn != null)
			synth.remove(channelIn);
		
		if (writer != null)
			synth.remove(writer);
		
		synth.add(follower = new PeakFollower());
		synth.add(channelIn = new ChannelIn());
		synth.add(writer = new FixedRateMonoWriter());
		
		channelIn.output.connect(follower.input);

		if (!synth.isRunning()) {
			synth.start(AudioUtils.SAMPLE_RATE, AudioDeviceManager.USE_DEFAULT_DEVICE, 1,
					AudioDeviceManager.USE_DEFAULT_DEVICE, 2);
		}
	}
	
	public float inputLevel() {
		
		return (float) follower.output.get();
	}

	public RecBuffer length(float lengthSec) {

		this.numFrames = (int) (AudioUtils.SAMPLE_RATE * lengthSec);
		return this;
	}
	
	public float length() {

		return this.numFrames/AudioUtils.SAMPLE_RATE;
	}
	
	public int size() {

		return this.numFrames;
	}

	public RecBuffer stop() {
		
		channelIn.flattenOutputs();
		channelIn.output.disconnectAll();
		
		follower.stop();

		writer.input.disconnectAll();
		writer.flattenOutputs();
		writer.dataQueue.endFrame();
		writer.dataQueue.clear();
		writer.stop();

		return this;
	}
	
	public RecBuffer start() {

		initSynth();
		
		sample = new FloatSample(numFrames, 1);
		channelIn.output.connect(writer.input);
		
		follower.start();

		writer.start(); // b/c writer is not pulled by anything
		writer.dataQueue.clear();
		writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
		
		return this;
	}
	
	public boolean enabled() {

		return channelIn.output.isConnected();
	}
	
	public boolean toggleEnabled() {
		if (enabled()) {
			stop();
			return false;
		}
		start();
		return true;
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
		sample.setFrameRate(AudioUtils.SAMPLE_RATE);
		return sample;
	}

	public static void main(String[] args) {

		Synthesizer synth = JSyn.createSynthesizer();
		RecBuffer recBuffer = new RecBuffer(synth, 10.5f);
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
		LineOut lineOut = new LineOut();
		synth.add(lineOut);
		lineOut.start();
		Player.playSample(synth, lineOut, fs, true);

		synth.stop();
	}
}
