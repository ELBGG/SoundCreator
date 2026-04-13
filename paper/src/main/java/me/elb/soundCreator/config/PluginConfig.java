package me.elb.soundCreator.config;

import me.elb.soundCreator.SoundCreator;

public class PluginConfig {

    private final SoundCreator plugin;

    public PluginConfig(SoundCreator plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getSoundsFolder() {
        return plugin.getConfig().getString("sounds-folder", "sounds");
    }

    public String getSourceLine() {
        return plugin.getConfig().getString("source-line", "proximity");
    }

    public float getDefaultVolume() {
        return (float) plugin.getConfig().getDouble("default-volume", 1.0);
    }

    public boolean isSkipNonVoicePlayers() {
        return plugin.getConfig().getBoolean("skip-non-voice-players", true);
    }

    public int getMaxConcurrentSounds() {
        return plugin.getConfig().getInt("max-concurrent-sounds", 10);
    }
}
