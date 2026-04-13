package me.elb.soundCreator;

import me.elb.soundCreator.command.SoundCreatorCommand;
import me.elb.soundCreator.config.PluginConfig;
import me.elb.soundCreator.playback.SoundManager;
import me.elb.soundCreator.sound.SoundLibrary;
import me.elb.soundCreator.voice.SoundCreatorAddon;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.voice.api.server.PlasmoVoiceServer;

import java.util.Objects;

public final class SoundCreator extends JavaPlugin {

    private final SoundCreatorAddon addon = new SoundCreatorAddon();
    private PluginConfig pluginConfig;
    private SoundLibrary soundLibrary;
    private SoundManager soundManager;

    @Override
    public void onEnable() {
        pluginConfig = new PluginConfig(this);
        soundLibrary = new SoundLibrary(this, pluginConfig);
        soundLibrary.reload();

        PlasmoVoiceServer.getAddonsLoader().load(addon);

        soundManager = new SoundManager(this, addon);
        soundManager.preloadAll(soundLibrary.getAllFiles());

        SoundCreatorCommand cmd = new SoundCreatorCommand(this);
        Objects.requireNonNull(getCommand("sc")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("sc")).setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
        if (soundManager != null) {
            soundManager.stopAll();
        }
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public SoundLibrary getSoundLibrary() {
        return soundLibrary;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public SoundCreatorAddon getAddon() {
        return addon;
    }
}
