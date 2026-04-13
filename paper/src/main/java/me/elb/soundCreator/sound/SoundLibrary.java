package me.elb.soundCreator.sound;

import me.elb.soundCreator.SoundCreator;
import me.elb.soundCreator.config.PluginConfig;

import java.io.File;
import java.util.*;

public class SoundLibrary {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("mp3", "wav", "ogg");

    private final SoundCreator plugin;
    private final PluginConfig config;
    private final Map<String, File> sounds = new LinkedHashMap<>();

    public SoundLibrary(SoundCreator plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reload() {
        sounds.clear();
        File folder = resolveSoundsFolder();

        if (!folder.exists()) {
            folder.mkdirs();
            plugin.getLogger().info("Carpeta de sonidos creada: " + folder.getPath());
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isFile()) continue;
            String ext = getExtension(file.getName());
            if (!SUPPORTED_EXTENSIONS.contains(ext)) continue;
            String name = stripExtension(file.getName()).toLowerCase();
            sounds.put(name, file);
        }

        plugin.getLogger().info("SoundCreator: " + sounds.size() + " sonido(s) encontrado(s).");
    }

    public Optional<File> getSound(String name) {
        return Optional.ofNullable(sounds.get(name.toLowerCase()));
    }

    public Collection<String> listSounds() {
        return Collections.unmodifiableCollection(sounds.keySet());
    }

    public Collection<File> getAllFiles() {
        return Collections.unmodifiableCollection(sounds.values());
    }

    public boolean soundExists(String name) {
        return sounds.containsKey(name.toLowerCase());
    }

    private File resolveSoundsFolder() {
        String folderName = config.getSoundsFolder();
        File folder = new File(folderName);
        if (!folder.isAbsolute()) {
            folder = new File(plugin.getDataFolder(), folderName);
        }
        return folder;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? filename : filename.substring(0, dot);
    }
}
