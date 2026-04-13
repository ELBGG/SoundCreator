package me.elb.soundCreator.audio;

import java.io.File;

public interface AudioDecoder {

    /**
     * @param file audio file to decode
     * @return short[] PCM samples ready for PlasmoVoice
     * @throws Exception if the file cannot be read or decoded
     */
    short[] decode(File file) throws Exception;
}
