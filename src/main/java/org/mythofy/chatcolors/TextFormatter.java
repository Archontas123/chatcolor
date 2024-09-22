package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&[0-9a-fk-or])|(#[A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("gradient\\{([^,}]+),([^}]+)}(.+?)(?=gradient\\{|$)");


    public static Component parseColorCodes(String text) {
        TextComponent.Builder builder = Component.text();
        Matcher matcher = COLOR_PATTERN.matcher(text);
        int lastEnd = 0;
        TextColor currentColor = null;
        boolean bold = false, italic = false, underlined = false, strikethrough = false;

        while (matcher.find()) {
            String beforeColor = text.substring(lastEnd, matcher.start());
            if (!beforeColor.isEmpty()) {
                builder.append(Component.text(beforeColor)
                        .color(currentColor)
                        .decoration(TextDecoration.BOLD, bold)
                        .decoration(TextDecoration.ITALIC, italic)
                        .decoration(TextDecoration.UNDERLINED, underlined)
                        .decoration(TextDecoration.STRIKETHROUGH, strikethrough));
            }

            String colorCode = matcher.group();
            if (colorCode.startsWith("#")) {
                currentColor = TextColor.fromHexString(colorCode);
            } else {
                switch (colorCode.toLowerCase()) {
                    case "&0": currentColor = NamedTextColor.BLACK; break;
                    case "&1": currentColor = NamedTextColor.DARK_BLUE; break;
                    case "&2": currentColor = NamedTextColor.DARK_GREEN; break;
                    case "&3": currentColor = NamedTextColor.DARK_AQUA; break;
                    case "&4": currentColor = NamedTextColor.DARK_RED; break;
                    case "&5": currentColor = NamedTextColor.DARK_PURPLE; break;
                    case "&6": currentColor = NamedTextColor.GOLD; break;
                    case "&7": currentColor = NamedTextColor.GRAY; break;
                    case "&8": currentColor = NamedTextColor.DARK_GRAY; break;
                    case "&9": currentColor = NamedTextColor.BLUE; break;
                    case "&a": currentColor = NamedTextColor.GREEN; break;
                    case "&b": currentColor = NamedTextColor.AQUA; break;
                    case "&c": currentColor = NamedTextColor.RED; break;
                    case "&d": currentColor = NamedTextColor.LIGHT_PURPLE; break;
                    case "&e": currentColor = NamedTextColor.YELLOW; break;
                    case "&f": currentColor = NamedTextColor.WHITE; break;
                    case "&l": bold = true; break;
                    case "&m": strikethrough = true; break;
                    case "&n": underlined = true; break;
                    case "&o": italic = true; break;
                    case "&r":
                        currentColor = null;
                        bold = italic = underlined = strikethrough = false;
                        break;
                }
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            builder.append(Component.text(text.substring(lastEnd))
                    .color(currentColor)
                    .decoration(TextDecoration.BOLD, bold)
                    .decoration(TextDecoration.ITALIC, italic)
                    .decoration(TextDecoration.UNDERLINED, underlined)
                    .decoration(TextDecoration.STRIKETHROUGH, strikethrough));
        }

        return builder.build();
    }



    public static Component applyRainbow(String text) {
        TextComponent.Builder builder = Component.text();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            float hue = (float) i / length;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            builder.append(Component.text(String.valueOf(c)).color(TextColor.color(rgb)));
        }

        return builder.build();
    }

    public static Component applyGradient(String text, String[] colors) {
        if (colors == null || colors.length < 2) {
            return Component.text(text);
        }

        TextColor startColor = TextColor.fromHexString(colors[0]);
        TextColor endColor = TextColor.fromHexString(colors[1]);

        TextComponent.Builder builder = Component.text();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            double position = (double) i / (length - 1);
            TextColor color = interpolateColor(startColor, endColor, position);
            builder.append(Component.text(String.valueOf(c)).color(color));
        }

        return builder.build();
    }



    private static Component applyGradientColors(String text, String[] colors) {
        if (colors == null || colors.length < 2) {
            Bukkit.getLogger().warning("Invalid gradient colors provided");
            return Component.text(text);
        }

        TextColor startColor = TextColor.fromHexString(colors[0]);
        TextColor endColor = TextColor.fromHexString(colors[1]);

        if (startColor == null || endColor == null) {
            Bukkit.getLogger().warning("Invalid hex colors: " + colors[0] + ", " + colors[1]);
            return Component.text(text);
        }

        TextComponent.Builder builder = Component.text();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            double position = (double) i / (length - 1);
            TextColor color = interpolateColor(startColor, endColor, position);
            builder.append(Component.text(String.valueOf(c)).color(color));
        }

        return builder.build();
    }

    public static Component applyGradient(String text, List<String> colors) {
        return applyGradient(text, colors.toArray(new String[0]));
    }

    public static Component applyRainbow(Component component) {
        return applyRainbowRecursive(component, 0, getTotalTextLength(component));
    }

    private static Component applyRainbowRecursive(Component component, int startIndex, int totalLength) {
        if (component instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) component;
            String content = textComponent.content();
            TextComponent.Builder builder = Component.text().style(textComponent.style());

            for (int i = 0; i < content.length(); i++) {
                float hue = (float) (startIndex + i) / totalLength;
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                builder.append(Component.text(String.valueOf(content.charAt(i))).color(TextColor.color(rgb)));
            }

            return builder.build();
        } else if (component.children().isEmpty()) {
            return component;
        } else {
            TextComponent.Builder builder = Component.text().style(component.style());
            int currentIndex = startIndex;
            for (Component child : component.children()) {
                builder.append(applyRainbowRecursive(child, currentIndex, totalLength));
                currentIndex += getTotalTextLength(child);
            }
            return builder.build();
        }
    }

    private static int getTotalTextLength(Component component) {
        if (component instanceof TextComponent) {
            return ((TextComponent) component).content().length();
        } else {
            return component.children().stream()
                    .mapToInt(TextFormatter::getTotalTextLength)
                    .sum();
        }
    }


    public static TextColor getTextColor(ChatColorOption option) {
        if (option.getHexCode() != null && !option.getHexCode().isEmpty()) {
            return TextColor.fromHexString(option.getHexCode());
        } else if (option.isGradient()) {
            List<String> gradient = option.getGradient();
            if (gradient != null && !gradient.isEmpty()) {
                return TextColor.fromHexString(gradient.get(0));
            }
        }
        return NamedTextColor.WHITE;
    }

    private static TextColor interpolateColor(TextColor start, TextColor end, double position) {
        int r = interpolateColorComponent(start.red(), end.red(), position);
        int g = interpolateColorComponent(start.green(), end.green(), position);
        int b = interpolateColorComponent(start.blue(), end.blue(), position);
        return TextColor.color(r, g, b);
    }


    private static int interpolateColorComponent(int start, int end, double position) {
        return (int) Math.round(start + (end - start) * position);
    }

    public static String stripColorCodes(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }

    public static String componentToString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}