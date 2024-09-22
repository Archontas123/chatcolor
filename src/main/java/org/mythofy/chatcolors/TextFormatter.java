package org.mythofy.chatcolors;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


public class TextFormatter {
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&[0-9a-fk-or])|(#[A-Fa-f0-9]{6})");


    public static Component applyGradient(String text, List<String> colors) {
        if (colors == null || colors.size() < 2) {
            return Component.text(text);
        }

        TextColor startColor = TextColor.fromHexString(colors.get(0));
        TextColor endColor = TextColor.fromHexString(colors.get(1));

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

    public static TextColor getTextColor(ChatColorOption option) {
        if (option.getHexCode() != null && !option.getHexCode().isEmpty()) {
            return TextColor.fromHexString(option.getHexCode());
        } else if (option.isGradient()) {
            List<String> gradient = option.getGradient();
            if (gradient != null && !gradient.isEmpty()) {
                return TextColor.fromHexString(gradient.get(0)); // Use the first color of the gradient
            }
        } else if (option.isRainbow()) {
            return NamedTextColor.WHITE; // Default color for rainbow
        }
        return NamedTextColor.WHITE; // Default fallback color
    }

    private static int interpolateColorComponent(int start, int end, double position) {
        return (int) Math.round(start + (end - start) * position);
    }

    private static TextColor interpolateColor(TextColor start, TextColor end, double position) {
        int r = interpolateColorComponent(start.red(), end.red(), position);
        int g = interpolateColorComponent(start.green(), end.green(), position);
        int b = interpolateColorComponent(start.blue(), end.blue(), position);
        return TextColor.color(r, g, b);
    }



    private static Component gradientTwoColors(String text, TextColor start, TextColor end) {
        int length = text.length();
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            double ratio = i / (length - 1);
            int red = (int)(start.red() + (end.red() - start.red()) * ratio);
            int green = (int)(start.green() + (end.green() - start.green()) * ratio);
            int blue = (int)(start.blue() + (end.blue() - start.blue()) * ratio);
            TextColor color = TextColor.color(red, green, blue);
            builder.append(Component.text(String.valueOf(c)).color(color));
        }
        return (Component)builder.build();
    }

    public static Component applyRainbow(String text, String mode) {
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

    public static Component parseColorCodes(String text) {
        TextComponent.Builder builder = Component.text();
        Matcher matcher = COLOR_PATTERN.matcher(text);
        int lastEnd = 0;
        TextColor currentColor = null;
        boolean bold = false, italic = false, underlined = false, strikethrough = false;

        while (matcher.find()) {
            String beforeColor = text.substring(lastEnd, matcher.start());
            if (!beforeColor.isEmpty()) {
                builder.append(Component.text(beforeColor).color(currentColor)
                        .decoration(TextDecoration.BOLD, bold)
                        .decoration(TextDecoration.ITALIC, italic)
                        .decoration(TextDecoration.UNDERLINED, underlined)
                        .decoration(TextDecoration.STRIKETHROUGH, strikethrough));
            }

            String colorCode = matcher.group();
            if (colorCode.startsWith("#")) {
                currentColor = TextColor.fromHexString(colorCode);
            } else {
                char code = colorCode.charAt(1);
                switch (code) {
                    case '0': currentColor = TextColor.color(0, 0, 0); break;
                    case '1': currentColor = TextColor.color(0, 0, 170); break;
                    case '2': currentColor = TextColor.color(0, 170, 0); break;
                    case '3': currentColor = TextColor.color(0, 170, 170); break;
                    case '4': currentColor = TextColor.color(170, 0, 0); break;
                    case '5': currentColor = TextColor.color(170, 0, 170); break;
                    case '6': currentColor = TextColor.color(255, 170, 0); break;
                    case '7': currentColor = TextColor.color(170, 170, 170); break;
                    case '8': currentColor = TextColor.color(85, 85, 85); break;
                    case '9': currentColor = TextColor.color(85, 85, 255); break;
                    case 'a': currentColor = TextColor.color(85, 255, 85); break;
                    case 'b': currentColor = TextColor.color(85, 255, 255); break;
                    case 'c': currentColor = TextColor.color(255, 85, 85); break;
                    case 'd': currentColor = TextColor.color(255, 85, 255); break;
                    case 'e': currentColor = TextColor.color(255, 255, 85); break;
                    case 'f': currentColor = TextColor.color(255, 255, 255); break;
                    case 'k': /* Obfuscated - not supported */ break;
                    case 'l': bold = true; break;
                    case 'm': strikethrough = true; break;
                    case 'n': underlined = true; break;
                    case 'o': italic = true; break;
                    case 'r':
                        currentColor = null;
                        bold = italic = underlined = strikethrough = false;
                        break;
                }
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            builder.append(Component.text(text.substring(lastEnd)).color(currentColor)
                    .decoration(TextDecoration.BOLD, bold)
                    .decoration(TextDecoration.ITALIC, italic)
                    .decoration(TextDecoration.UNDERLINED, underlined)
                    .decoration(TextDecoration.STRIKETHROUGH, strikethrough));
        }

        return builder.build();
    }

    private static Component rainbowCycle(String text) {
        TextComponent.Builder builder = Component.text();
        double frequency = 0.3D;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            double hue = (System.currentTimeMillis() / 1000.0D * frequency + i * 0.05D) % 1.0D;
            TextColor color = TextColor.color(ColorUtil.hsvToRgb((float)(hue * 360.0D), 1.0F, 1.0F));
            builder.append(Component.text(String.valueOf(c)).color(color));
        }
        return (Component)builder.build();
    }

    private static Component rainbowPerChar(String text) {
        TextComponent.Builder builder = Component.text();
        float hueIncrement = 360.0F / text.length();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float hue = i * hueIncrement % 360.0F;
            TextColor color = TextColor.color(ColorUtil.hsvToRgb(hue, 1.0F, 1.0F));
            builder.append(Component.text(String.valueOf(c)).color(color));
        }
        return (Component)builder.build();
    }

    public static Component translateColorCodes(Component component) {
        if (component instanceof TextComponent) {
            String content = ((TextComponent) component).content();
            Component translatedComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(content);
            return translatedComponent.color(component.color());
        }
        return component;
    }

}
