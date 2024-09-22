// ChatColorOption.java
package org.mythofy.chatcolors;

import java.util.List;

public class ChatColorOption {
    private final String display;
    private final String hexCode;
    private final List<String> gradient;
    private final String mode;
    private final String unlockMethod;
    private final String unlockRequirement;
    private final String guiMaterial;
    private final String guiDisplayName;
    private final List<String> guiLore;

    public ChatColorOption(String display, String hexCode, List<String> gradient, String mode,
                           String unlockMethod, String unlockRequirement, String guiMaterial,
                           String guiDisplayName, List<String> guiLore) {
        this.display = display;
        this.hexCode = hexCode;
        this.gradient = gradient;
        this.mode = mode;
        this.unlockMethod = unlockMethod;
        this.unlockRequirement = unlockRequirement;
        this.guiMaterial = guiMaterial;
        this.guiDisplayName = guiDisplayName;
        this.guiLore = guiLore;
    }

    public String getDisplay() {
        return display;
    }

    public String getHexCode() {
        return hexCode;
    }

    public List<String> getGradient() {
        return gradient;
    }

    public String getMode() {
        return mode;
    }

    public String getUnlockMethod() {
        return unlockMethod;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }

    public String getGuiMaterial() {
        return guiMaterial != null ? guiMaterial : "WHITE_WOOL"; // Fallback to WHITE_WOOL if null
    }

    public String getGuiDisplayName() {
        return guiDisplayName != null ? guiDisplayName : display; // Fallback to display if null
    }

    public List<String> getGuiLore() {
        return guiLore;
    }

    public boolean isGradient() {
        return (this.gradient != null && !this.gradient.isEmpty());
    }

    public boolean isRainbow() {
        return "rainbow".equalsIgnoreCase(this.mode);
    }
}
