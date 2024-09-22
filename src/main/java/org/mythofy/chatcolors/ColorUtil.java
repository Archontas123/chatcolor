package org.mythofy.chatcolors;

import java.awt.Color;

public class ColorUtil {
    public static int hsvToRgb(float hue, float saturation, float value) {
        return Color.HSBtoRGB(hue / 360.0F, saturation, value) & 0xFFFFFF;
    }
}
