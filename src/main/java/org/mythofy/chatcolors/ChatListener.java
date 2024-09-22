package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("gradient\\{([^,}]+),([^}]+)}(.+?)(?=gradient\\{|$)");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(&[0-9a-fk-or])|(#[A-Fa-f0-9]{6})");
    private final ChatColorManager colorManager;
    private final ChatColorsPlugin plugin;

    public ChatListener(ChatColorManager colorManager, ChatColorsPlugin plugin) {
        this.colorManager = colorManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String colorKey = colorManager.getPlayerColor(event.getPlayer().getUniqueId());

        Component formattedMessage = parseMessage(message, colorKey, event.getPlayer());

        event.setCancelled(true);
        Component finalMessage = Component.text()
                .append(Component.text("<" + event.getPlayer().getName() + "> ").color(NamedTextColor.GRAY))
                .append(formattedMessage)
                .build();
        event.getPlayer().getServer().sendMessage(finalMessage);
    }

    private Component parseMessage(String message, String colorKey, Player player) {
        TextComponent.Builder builder = Component.text();
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        int lastEnd = 0;

        while (gradientMatcher.find()) {
            // Add text before gradient
            if (gradientMatcher.start() > lastEnd) {
                builder.append(TextFormatter.parseColorCodes(message.substring(lastEnd, gradientMatcher.start())));
            }

            // Process gradient
            String[] colors = {gradientMatcher.group(1), gradientMatcher.group(2)};
            String gradientText = gradientMatcher.group(3);
            builder.append(TextFormatter.applyGradient(gradientText, colors));

            lastEnd = gradientMatcher.end();
        }

        // Add remaining text
        if (lastEnd < message.length()) {
            builder.append(TextFormatter.parseColorCodes(message.substring(lastEnd)));
        }

        Component result = builder.build();

        // Apply overall chat color if set
        ChatColorOption option = colorKey != null ? colorManager.getColorOptions().get(colorKey) : null;
        if (option != null) {
            if ("rainbow".equalsIgnoreCase(option.getMode())) {
                result = applyRainbowWithColorCodes(result);
            } else if (option.isGradient()) {
                result = applyGradientWithColorCodes(result, option.getGradient());
            } else {
                TextColor color = TextFormatter.getTextColor(option);
                if (color != null) {
                    result = applyColorWithColorCodes(result, color);
                }
            }
        }

        return result;
    }

    private Component applyRainbowWithColorCodes(Component component) {
        String text = TextFormatter.componentToString(component);
        TextComponent.Builder builder = Component.text();
        Matcher matcher = COLOR_CODE_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(TextFormatter.applyRainbow(text.substring(lastEnd, matcher.start())));
            }
            builder.append(TextFormatter.parseColorCodes(matcher.group()));
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            builder.append(TextFormatter.applyRainbow(text.substring(lastEnd)));
        }

        return builder.build();
    }

    private Component applyGradientWithColorCodes(Component component, java.util.List<String> gradientColors) {
        String text = TextFormatter.componentToString(component);
        TextComponent.Builder builder = Component.text();
        Matcher matcher = COLOR_CODE_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(TextFormatter.applyGradient(text.substring(lastEnd, matcher.start()), gradientColors));
            }
            builder.append(TextFormatter.parseColorCodes(matcher.group()));
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            builder.append(TextFormatter.applyGradient(text.substring(lastEnd), gradientColors));
        }

        return builder.build();
    }

    private Component applyColorWithColorCodes(Component component, TextColor color) {
        return component.color(color);
    }
}