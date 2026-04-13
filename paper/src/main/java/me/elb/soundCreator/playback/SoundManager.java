package me.elb.soundCreator.playback;

import me.elb.soundCreator.SoundCreator;
import me.elb.soundCreator.audio.PcmUtil;
import me.elb.soundCreator.sound.SoundCache;
import me.elb.soundCreator.voice.SoundCreatorAddon;
import org.bukkit.entity.Player;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.provider.ArrayAudioFrameProvider;
import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.ServerBroadcastSource;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SoundManager {

    private static final int OPUS_FRAME_SIZE = 960;

    private final SoundCreator plugin;
    private final SoundCreatorAddon addon;
    private final SoundCache cache = new SoundCache();
    private final ConcurrentHashMap<UUID, PlaybackSession> sessions = new ConcurrentHashMap<>();

    public SoundManager(SoundCreator plugin, SoundCreatorAddon addon) {
        this.plugin = plugin;
        this.addon = addon;
    }

    public UUID play(File file, String soundName, float volume, boolean loop, Player targetPlayer) throws Exception {
        PlasmoVoiceServer voiceServer = addon.getVoiceServer();
        if (voiceServer == null) {
            throw new IllegalStateException("PlasmoVoice no esta inicializado todavia.");
        }

        ServerSourceLine sourceLine = addon.getSourceLine();
        if (sourceLine == null) {
            throw new IllegalStateException("Source line de SoundCreator no esta registrada todavia.");
        }

        int maxSessions = plugin.getPluginConfig().getMaxConcurrentSounds();
        if (maxSessions > 0 && sessions.size() >= maxSessions) {
            throw new IllegalStateException("Limite de reproducciones simultaneas alcanzado (" + maxSessions + ").");
        }

        short[] pcm = cache.getOrLoad(file);
        short[] volumePcm = (volume != 1.0f) ? PcmUtil.applyVolume(pcm, volume) : pcm;

        ServerBroadcastSource source = sourceLine.createBroadcastSource(false);

        if (targetPlayer != null) {
            VoicePlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(targetPlayer);
            if (voicePlayer == null) {
                source.remove();
                throw new IllegalStateException(targetPlayer.getName() + " no tiene PlasmoVoice instalado.");
            }
            source.setPlayers(List.of(voicePlayer));
        }

        String opusKey = file.getAbsolutePath() + "|v=" + volume;
        ArrayAudioFrameProvider provider = new ArrayAudioFrameProvider(voiceServer, false);
        encodeIntoProvider(voiceServer, volumePcm, opusKey, provider);
        provider.setLoop(loop);

        AudioSender sender = source.createAudioSender(provider);
        UUID sessionId = UUID.randomUUID();
        sender.onStop(() -> {
            provider.close();
            sessions.remove(sessionId);
            source.remove();
        });

        sender.start();

        sessions.put(sessionId, new PlaybackSession(sessionId, soundName, source, sender));
        return sessionId;
    }

    public void preloadAll(Collection<File> files) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlasmoVoiceServer voiceServer = addon.getVoiceServer();
            if (voiceServer == null) return;

            int count = 0;
            for (File file : files) {
                try {
                    short[] pcm = cache.getOrLoad(file);
                    String opusKey = file.getAbsolutePath() + "|v=1.0";
                    if (cache.getOpusFrames(opusKey) == null) {
                        buildOpusCache(voiceServer, pcm, opusKey);
                        count++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("[SoundCreator] Error precargando " + file.getName() + ": " + e.getMessage());
                }
            }
            if (count > 0) {
                plugin.getLogger().info("[SoundCreator] " + count + " sonido(s) precodificado(s) en Opus.");
            }
        });
    }

    private void buildOpusCache(PlasmoVoiceServer voiceServer, short[] pcm, String opusKey) throws Exception {
        AudioEncoder encoder = voiceServer.createOpusEncoder(false);
        List<byte[]> frames = new ArrayList<>();
        try {
            for (int off = 0; off + OPUS_FRAME_SIZE <= pcm.length; off += OPUS_FRAME_SIZE) {
                frames.add(encoder.encode(Arrays.copyOfRange(pcm, off, off + OPUS_FRAME_SIZE)));
            }
        } finally {
            encoder.close();
        }
        cache.putOpusFrames(opusKey, frames.toArray(new byte[0][]));
    }

    private void encodeIntoProvider(PlasmoVoiceServer voiceServer, short[] pcm,
                                    String opusKey, ArrayAudioFrameProvider provider) throws Exception {
        if (cache.getOpusFrames(opusKey) == null) {
            buildOpusCache(voiceServer, pcm, opusKey);
        }
        for (byte[] frame : cache.getOpusFrames(opusKey)) {
            provider.addEncodedFrame(frame);
        }
    }

    public void stopAll() {
        sessions.values().forEach(PlaybackSession::stop);
    }

    public int stopByName(String soundName) {
        int count = 0;
        for (PlaybackSession session : sessions.values()) {
            if (session.getSoundName().equalsIgnoreCase(soundName)) {
                session.stop();
                count++;
            }
        }
        return count;
    }

    public Collection<PlaybackSession> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    public SoundCache getCache() {
        return cache;
    }
}
