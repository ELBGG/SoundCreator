package me.elb.soundCreator.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static final String PREFIX =
            ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SoundCreator" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    private MessageUtil() {}

    public static void info(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.WHITE + message);
    }

    public static void success(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + message);
    }

    public static void error(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void usage(CommandSender sender, String usage) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Uso: " + ChatColor.WHITE + usage);
    }
}
