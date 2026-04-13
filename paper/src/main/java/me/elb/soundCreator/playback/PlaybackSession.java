package me.elb.soundCreator.playback;

import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.ServerBroadcastSource;

import java.util.UUID;

public class PlaybackSession {

    private final UUID id;
    private final String soundName;
    private final ServerBroadcastSource source;
    private final AudioSender sender;

    public PlaybackSession(UUID id, String soundName, ServerBroadcastSource source, AudioSender sender) {
        this.id = id;
        this.soundName = soundName;
        this.source = source;
        this.sender = sender;
    }

    public void stop() {
        sender.stop();
    }

    public UUID getId() {
        return id;
    }

    public String getSoundName() {
        return soundName;
    }
}
