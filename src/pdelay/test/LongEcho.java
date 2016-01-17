package pdelay.test;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.*;
import com.jsyn.util.WaveFileWriter;

/**
 * Echo the input using a circular buffer in a sample.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class LongEcho {
	
    final static float DELAY_SECONDS = .5f;
    
    Synthesizer synth;
    ChannelIn channelIn;
    ChannelOut channelOut;
    FloatSample sample;
    FixedRateMonoReader reader;
    FixedRateMonoWriter writer;
    Minimum minner;
    Maximum maxxer;

    private void test() {
    	
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(channelIn = new ChannelIn());
        // Add an output mixer.
        synth.add(channelOut = new ChannelOut());

        synth.add(minner = new Minimum());
        synth.add(maxxer = new Maximum());
        synth.add(reader = new FixedRateMonoReader());
        synth.add(writer = new FixedRateMonoWriter());

        sample = new FloatSample((int) (44100 * DELAY_SECONDS), 1);

        maxxer.inputB.set(-0.98); // clip
        minner.inputB.set(0.98);

        // Connect the input to the output.
        channelIn.output.connect(minner.inputA);
        minner.output.connect(maxxer.inputA);
        maxxer.output.connect(writer.input);

        reader.output.connect(channelOut.input);

        // Both stereo.
        synth.start(44100, AudioDeviceManager.USE_DEFAULT_DEVICE, 2,
                AudioDeviceManager.USE_DEFAULT_DEVICE, 2);

        writer.start();
        channelOut.start();

        // For a long echo, read cursor should be just in front of the write cursor.
        reader.dataQueue.queue(sample, 1000, sample.getNumFrames() - 1000);
        
        // Loop both forever.
        reader.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
        writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
        
        System.out.println("\nStart talking, echo is " + DELAY_SECONDS + " seconds...");
        
        // Sleep a while.
        try {
            double time = synth.getCurrentTime();
            synth.sleepUntil(time + 10.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synth.stop();
    }

    public static void main(String[] args) {
    	new LongEcho().test();
    }
}
