package me.elb.soundCreator.command;

import me.elb.soundCreator.SoundCreator;
import me.elb.soundCreator.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SoundCreatorCommand implements CommandExecutor, TabCompleter {

    private final SoundCreator plugin;

    public SoundCreatorCommand(SoundCreator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] rawArgs) {
        String[] args = parseQuotedArgs(rawArgs);

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "play"   -> handlePlay(sender, args);
            case "stop"   -> handleStop(sender, args);
            case "list"   -> handleList(sender);
            case "reload" -> handleReload(sender);
            default       -> sendHelp(sender, label);
        }
        return true;
    }

    private void handlePlay(CommandSender sender, String[] args) {
        if (!sender.hasPermission("soundcreator.play")) {
            MessageUtil.error(sender, "No tienes permiso para reproducir sonidos.");
            return;
        }
        if (args.length < 2) {
            MessageUtil.usage(sender, "/sc play <sonido> [jugador|all]");
            return;
        }

        String soundName = args[1].toLowerCase();
        Optional<File> soundFile = plugin.getSoundLibrary().getSound(soundName);
        if (soundFile.isEmpty()) {
            MessageUtil.error(sender, "Sonido '" + soundName + "' no encontrado. Usa /sc list para ver los disponibles.");
            return;
        }

        Player targetPlayer = null;
        if (args.length >= 3 && !args[2].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("soundcreator.play.others")) {
                MessageUtil.error(sender, "No tienes permiso para reproducir a otros jugadores.");
                return;
            }
            targetPlayer = Bukkit.getPlayerExact(args[2]);
            if (targetPlayer == null) {
                MessageUtil.error(sender, "Jugador '" + args[2] + "' no encontrado o no esta en linea.");
                return;
            }
        } else if (args.length >= 3 && args[2].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("soundcreator.play.all")) {
                MessageUtil.error(sender, "No tienes permiso para reproducir a todos.");
                return;
            }
        }

        final File file = soundFile.get();
        final float volume = plugin.getPluginConfig().getDefaultVolume();
        final Player finalTarget = targetPlayer;
        final String targetDesc = (finalTarget != null) ? finalTarget.getName() : "todos";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getSoundManager().play(file, soundName, volume, false, finalTarget);
                MessageUtil.success(sender, "Reproduciendo '" + soundName + "' → " + targetDesc + ".");
            } catch (Exception e) {
                MessageUtil.error(sender, "Error: " + e.getMessage());
                plugin.getLogger().warning("Error al reproducir " + soundName + ": " + e.getMessage());
            }
        });
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("soundcreator.stop")) {
            MessageUtil.error(sender, "No tienes permiso para detener sonidos.");
            return;
        }

        if (args.length >= 2 && !args[1].equalsIgnoreCase("all")) {
            String soundName = args[1].toLowerCase();
            int stopped = plugin.getSoundManager().stopByName(soundName);
            MessageUtil.success(sender, "Detenidas " + stopped + " reproduccion(es) de '" + soundName + "'.");
        } else {
            plugin.getSoundManager().stopAll();
            MessageUtil.success(sender, "Todas las reproducciones detenidas.");
        }
    }

    private void handleList(CommandSender sender) {
        Collection<String> sounds = plugin.getSoundLibrary().listSounds();
        if (sounds.isEmpty()) {
            MessageUtil.info(sender, "No hay sonidos cargados. Coloca archivos .mp3, .wav u .ogg en plugins/SoundCreator/sounds/");
            return;
        }
        MessageUtil.info(sender, "Sonidos disponibles (" + sounds.size() + "):");
        for (String name : sounds) {
            sender.sendMessage("  §7- §e" + name);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("soundcreator.reload")) {
            MessageUtil.error(sender, "No tienes permiso para recargar.");
            return;
        }
        plugin.getPluginConfig().reload();
        plugin.getSoundLibrary().reload();
        plugin.getSoundManager().getCache().invalidateAll();
        plugin.getSoundManager().preloadAll(plugin.getSoundLibrary().getAllFiles());
        int count = plugin.getSoundLibrary().listSounds().size();
        MessageUtil.success(sender, "Recargado. " + count + " sonido(s) disponible(s). Precargando cache...");
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§8[§bSoundCreator§8] §7Comandos disponibles:");
        sender.sendMessage("  §e/" + label + " play §7<sonido> [jugador|all]");
        sender.sendMessage("  §e/" + label + " stop §7[sonido|all]");
        sender.sendMessage("  §e/" + label + " list");
        sender.sendMessage("  §e/" + label + " reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] rawArgs) {
        String[] args = parseQuotedArgs(rawArgs);

        if (args.length == 1) {
            return filter(List.of("play", "stop", "list", "reload"), args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("play")) {
                if (rawArgs.length > args.length) return List.of();
                return filterSounds(args[1]);
            }
            if (args[0].equalsIgnoreCase("stop")) {
                if (rawArgs.length > args.length) return List.of();
                List<String> opts = new ArrayList<>();
                opts.add("all");
                opts.addAll(filterSounds(args[1]));
                return opts.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("play")) {
            List<String> targets = new ArrayList<>();
            targets.add("all");
            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(targets::add);
            return filter(targets, args[2]);
        }

        return List.of();
    }

    private List<String> filterSounds(String partial) {
        return plugin.getSoundLibrary().listSounds().stream()
                .filter(s -> s.toLowerCase().startsWith(partial.toLowerCase()))
                .map(s -> s.contains(" ") ? "\"" + s + "\"" : s)
                .collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }

    private static String[] parseQuotedArgs(String[] raw) {
        String joined = String.join(" ", raw);
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inQuote = false;
        for (char c : joined.toCharArray()) {
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ' ' && !inQuote) {
                if (token.length() > 0) {
                    result.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(c);
            }
        }
        if (inQuote || token.length() > 0) result.add(token.toString());
        return result.toArray(new String[0]);
    }
}
