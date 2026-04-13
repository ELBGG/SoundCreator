package me.elb.soundCreator.voice;

import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;

@Addon(
        id = "pv-addon-soundcreator",
        name = "Sound Creator",
        version = "Mambo",
        authors = {"ELB_GG"}
)
public class SoundCreatorAddon implements AddonInitializer {

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    private ServerSourceLine sourceLine;

    @Override
    public void onAddonInitialize() {
        sourceLine = voiceServer.getSourceLineManager()
                .createBuilder(
                        this,
                        "soundcreator",
                        "SoundTrack",
                        "plasmovoice:textures/icons/speaker_disc.png",
                        10
                )
                .build();

        System.out.println("[SoundCreator] PlasmoVoice addon inicializado.");
    }

    public PlasmoVoiceServer getVoiceServer() {
        return voiceServer;
    }

    public ServerSourceLine getSourceLine() {
        return sourceLine;
    }

    @Override
    public void onAddonShutdown() {
        System.out.println("[SoundCreator] PlasmoVoice addon apagado.");
    }

}
