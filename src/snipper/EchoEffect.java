package snipper;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;

public class EchoEffect {

	FixedRateMonoReader reader;
	FixedRateMonoWriter writer;
	Synthesizer synth;
	FloatSample sample;
	ChannelIn channelIn;
	ChannelOut channelOut;

	public EchoEffect() {

		synth = JSyn.createSynthesizer();
		synth.add(channelIn = new ChannelIn());
		synth.add(channelOut = new ChannelOut());
		synth.add(reader = new FixedRateMonoReader());
		synth.add(writer = new FixedRateMonoWriter());
	}

	public void setDelay(float delaySec) {

		float delay = delaySec;
		sample = new FloatSample((int) (44100 * delay), 1);

		reader.output.connect(channelOut.input);
		channelIn.output.connect(writer.input);

		synth.start(44100, AudioDeviceManager.USE_DEFAULT_DEVICE, 2,
				AudioDeviceManager.USE_DEFAULT_DEVICE, 2);

		writer.start(); // important because writer is not pulled by anything
		channelOut.start();

		// For a long echo, read cursor should be just in front of the write cursor.
		int ahead = 10;
		reader.dataQueue.queue(sample, ahead, sample.getNumFrames() - ahead);

		// Loop both forever.
		reader.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
		writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
	}

	public static void main(String[] args) {

		new EchoEffect().setDelay(.2f);
	}

}
