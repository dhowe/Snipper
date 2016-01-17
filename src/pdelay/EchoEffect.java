package pdelay;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;

public class EchoEffect {

	int SAMPLE_RATE = 44100;

	FixedRateMonoReader reader;
	FixedRateMonoWriter writer;
	Synthesizer synth;
	FloatSample sample;
	ChannelIn channelIn;
	ChannelOut channelOut;
	
	float delay;

	public EchoEffect() {

		synth = JSyn.createSynthesizer();
		synth.add(channelIn = new ChannelIn());
		synth.add(channelOut = new ChannelOut());
		synth.add(reader = new FixedRateMonoReader());
		synth.add(writer = new FixedRateMonoWriter());
	}

	public void setDelay(float delaySec) {

		this.delay = delaySec;
		sample = new FloatSample((int) (SAMPLE_RATE * delay), 1);

		reader.output.connect(channelOut.input);
		channelIn.output.connect(writer.input);

		synth.start(SAMPLE_RATE, AudioDeviceManager.USE_DEFAULT_DEVICE, 2,
				AudioDeviceManager.USE_DEFAULT_DEVICE, 2);

		writer.start(); // important because writer is not pulled by anything
		channelOut.start();

		// For a long echo, read cursor should be just in front of the write cursor.
		reader.dataQueue.queue(sample, 1000, sample.getNumFrames() - 1000);

		// Loop both forever.
		reader.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
		writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
	}

	public float[] getCurrentFrames() {

		float[] frames = new float[sample.getNumFrames()];
		sample.read(frames);
		return frames;
	}

	public static void main(String[] args) {

		new EchoEffect().setDelay(1.5f);
	}

}
