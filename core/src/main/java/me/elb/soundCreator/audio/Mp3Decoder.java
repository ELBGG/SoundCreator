package me.elb.soundCreator.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class Mp3Decoder implements AudioDecoder {

    @Override
    public short[] decode(File file) throws Exception {
        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            try (AudioInputStream compressed = AudioSystem.getAudioInputStream(file)) {
                AudioFormat compFmt = compressed.getFormat();

                int channels = compFmt.getChannels() == AudioSystem.NOT_SPECIFIED ? 2 : compFmt.getChannels();
                float sampleRate = compFmt.getSampleRate() == AudioSystem.NOT_SPECIFIED ? 44100f : compFmt.getSampleRate();
                AudioFormat pcmFmt = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sampleRate, 16, channels,
                        channels * 2, sampleRate, true
                );

                try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFmt, compressed)) {
                    byte[] bytes = PcmUtil.readAllBytes(pcmStream);
                    boolean bigEndian = pcmStream.getFormat().isBigEndian();
                    short[] shorts = PcmUtil.bytesToShorts(bytes, bigEndian);

                    if (channels > 1) shorts = PcmUtil.toMono(shorts, channels);
                    if (sampleRate != PcmUtil.TARGET_SAMPLE_RATE) shorts = PcmUtil.resample(shorts, sampleRate, PcmUtil.TARGET_SAMPLE_RATE);
                    return shorts;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(prev);
        }
    }
}
