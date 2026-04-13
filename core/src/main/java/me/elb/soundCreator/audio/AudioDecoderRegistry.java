package me.elb.soundCreator.audio;

import java.io.File;
import java.util.Map;

public class AudioDecoderRegistry {

    private static final Map<String, AudioDecoder> DECODERS = Map.of(
            "wav", new WavDecoder(),
            "mp3", new Mp3Decoder(),
            "ogg", new OggDecoder()
    );

    public AudioDecoder forFile(File file) {
        String ext = getExtension(file.getName());
        AudioDecoder decoder = DECODERS.get(ext);
        if (decoder == null) {
            throw new IllegalArgumentException("Formato de audio no soportado: " + ext);
        }
        return decoder;
    }

    public short[] decode(File file) throws Exception {
        return forFile(file).decode(file);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }
}
