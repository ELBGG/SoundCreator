package me.elb.soundCreator.sound;

import me.elb.soundCreator.audio.AudioDecoderRegistry;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class SoundCache {

    private final ConcurrentHashMap<String, short[]> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[][]> opusCache = new ConcurrentHashMap<>();
    private final AudioDecoderRegistry registry = new AudioDecoderRegistry();

    public short[] getOrLoad(File file) throws Exception {
        String key = file.getAbsolutePath();
        short[] cached = cache.get(key);
        if (cached != null) return cached;

        short[] pcm = registry.decode(file);
        cache.putIfAbsent(key, pcm);
        return cache.get(key);
    }

    public byte[][] getOpusFrames(String key) {
        return opusCache.get(key);
    }

    public void putOpusFrames(String key, byte[][] frames) {
        opusCache.putIfAbsent(key, frames);
    }

    public void invalidateAll() {
        cache.clear();
        opusCache.clear();
    }
}
