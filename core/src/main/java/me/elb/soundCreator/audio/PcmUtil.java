package me.elb.soundCreator.audio;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class PcmUtil {

    static final float TARGET_SAMPLE_RATE = 48000f;

    private PcmUtil() {}

    public static byte[] readAllBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }

    public static short[] bytesToShorts(byte[] bytes, boolean bigEndian) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < shorts.length; i++) {
            int b0 = bytes[i * 2] & 0xFF;
            int b1 = bytes[i * 2 + 1] & 0xFF;
            shorts[i] = bigEndian
                    ? (short) ((b0 << 8) | b1)
                    : (short) (b0 | (b1 << 8));
        }
        return shorts;
    }

    public static short[] toMono(short[] pcm, int channels) {
        if (channels == 1) return pcm;
        short[] mono = new short[pcm.length / channels];
        for (int i = 0; i < mono.length; i++) {
            int sum = 0;
            for (int c = 0; c < channels; c++) {
                sum += pcm[i * channels + c];
            }
            mono[i] = (short) (sum / channels);
        }
        return mono;
    }

    public static short[] resample(short[] pcm, float sourceRate, float targetRate) {
        if (sourceRate == targetRate) return pcm;
        int targetLength = (int) ((double) pcm.length * targetRate / sourceRate);
        short[] out = new short[targetLength];
        for (int i = 0; i < targetLength; i++) {
            double srcIdx = (double) i * sourceRate / targetRate;
            int lo = Math.min((int) srcIdx, pcm.length - 1);
            int hi = Math.min(lo + 1, pcm.length - 1);
            double frac = srcIdx - lo;
            out[i] = (short) (pcm[lo] * (1.0 - frac) + pcm[hi] * frac);
        }
        return out;
    }

    public static short[] applyVolume(short[] pcm, float volume) {
        if (volume == 1.0f) return pcm;
        short[] out = new short[pcm.length];
        for (int i = 0; i < pcm.length; i++) {
            out[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int) (pcm[i] * volume)));
        }
        return out;
    }
}
