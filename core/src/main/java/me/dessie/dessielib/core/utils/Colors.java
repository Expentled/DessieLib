package me.dessie.dessielib.core.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for adding color to Minecraft messages.
 */
public class Colors {
    private static final Pattern colorPattern = Pattern.compile("#[A-Fa-f0-9]{6}");

    /**
     * Creates a colored message that supports both ampersand format and HEX format. (#ffffff)
     * @param message The message to colorize.
     * @return The colored message.
     */
    public static String color(String message) {
        Matcher matcher = colorPattern.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = colorPattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
