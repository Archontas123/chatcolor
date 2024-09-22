// ChatColorManager.java
package org.mythofy.chatcolors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChatColorManager {
    private final JavaPlugin plugin;
    private final Map<String, ChatColorOption> colorOptions = new HashMap<>();
    private final DatabaseManager dbManager;
    private boolean allowCustomColors;
    private String customColorUnlockMethod;
    private String customColorUnlockRequirement;

    public ChatColorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbManager = new DatabaseManager(plugin);
        this.dbManager.setup();
        loadColors();
        loadCustomColorSettings();
    }

    private void loadColors() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("chat-colors");
        if (section == null) {
            plugin.getLogger().severe("No chat-colors section found in config.yml!");
            return;
        }
        for (String key : section.getKeys(false)) {
            String display = section.getString(key + ".display");
            String hexCode = section.getString(key + ".hex-code");
            List<String> gradient = section.getStringList(key + ".gradient");
            String mode = section.getString(key + ".mode");
            String unlockMethod = section.getString(key + ".unlock-method");
            String unlockRequirement = section.getString(key + ".unlock-requirement");

            ConfigurationSection guiSection = section.getConfigurationSection(key + ".gui.icon");
            String guiMaterial = guiSection != null ? guiSection.getString("material", "WHITE_WOOL") : "WHITE_WOOL";
            String guiDisplayName = guiSection != null ? guiSection.getString("display-name", display) : display;
            List<String> guiLore = guiSection != null ? guiSection.getStringList("lore") : new ArrayList<>();

            plugin.getLogger().info("Loading color " + key + " with material: " + guiMaterial);

            ChatColorOption option = new ChatColorOption(display, hexCode, gradient, mode, unlockMethod,
                    unlockRequirement, guiMaterial, guiDisplayName, guiLore);
            this.colorOptions.put(key.toLowerCase(), option);
        }
    }

    public void setPlayerGradient(UUID playerUUID, List<String> colors) {
        ChatColorOption gradientOption = new ChatColorOption("Custom Gradient", null, colors, "gradient", "default", "", "GRAY_WOOL", "&bCustom Gradient", Collections.emptyList());
        String key = "custom_gradient_" + playerUUID.toString();
        this.colorOptions.put(key, gradientOption);
        setPlayerColor(playerUUID, key);
    }

    private void loadCustomColorSettings() {
        ConfigurationSection customSection = plugin.getConfig().getConfigurationSection("custom-colors");
        if (customSection != null) {
            this.allowCustomColors = customSection.getBoolean("allow", false);
            this.customColorUnlockMethod = customSection.getString("unlock-method", "permission");
            this.customColorUnlockRequirement = customSection.getString("unlock-requirement", "chatcolor.custom");
        }
    }

    public void addColorOption(String key, ChatColorOption option) {
        colorOptions.put(key.toLowerCase(), option);
    }

    public Map<String, ChatColorOption> getColorOptions() {
        return Collections.unmodifiableMap(colorOptions);
    }

    public String getCustomColorUnlockRequirement() {
        return customColorUnlockRequirement;
    }

    public boolean hasHexColorUnlocked(Player player, String hexCode) {
        if (!plugin.getConfig().getBoolean("custom-colors.allow", true)) {
            return false;
        }
        String unlockMethod = plugin.getConfig().getString("custom-colors.unlock-method", "permission");
        String unlockRequirement = plugin.getConfig().getString("custom-colors.unlock-requirement", "chatcolor.custom");

        if (unlockMethod.equals("permission")) {
            return player.hasPermission(unlockRequirement);
        }
        // Add other unlock methods if needed
        return false;
    }

    public boolean hasCustomColorUnlocked(Player player) {
        switch (customColorUnlockMethod) {
            case "default":
                return true;
            case "playtime":
                return player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) >= Integer.parseInt(customColorUnlockRequirement) * 20;
            case "achievement":
                NamespacedKey key = NamespacedKey.minecraft(customColorUnlockRequirement);
                Advancement advancement = Bukkit.getAdvancement(key);
                if (advancement == null) {
                    plugin.getLogger().warning("Advancement " + customColorUnlockRequirement + " not found for custom colors! Treating as unlocked.");
                    return true;
                }
                return player.getAdvancementProgress(advancement).isDone();
            case "level":
                return player.getLevel() >= Integer.parseInt(customColorUnlockRequirement);
            case "permission":
                return player.hasPermission(customColorUnlockRequirement);
            default:
                return false;
        }
    }

    public boolean hasColorUnlocked(Player player, String colorKey) {
        ChatColorOption option = this.colorOptions.get(colorKey.toLowerCase());
        if (option == null) return false;

        switch (option.getUnlockMethod()) {
            case "default":
                return true;
            case "playtime":
                return player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) >= Integer.parseInt(option.getUnlockRequirement()) * 20;
            case "achievement":
                NamespacedKey key = NamespacedKey.minecraft(option.getUnlockRequirement());
                Advancement advancement = Bukkit.getAdvancement(key);
                if (advancement == null) {
                    plugin.getLogger().warning("Advancement " + option.getUnlockRequirement() + " not found! Treating as unlocked.");
                    return true;
                }
                return player.getAdvancementProgress(advancement).isDone();
            case "level":
                return player.getLevel() >= Integer.parseInt(option.getUnlockRequirement());
            case "permission":
                return player.hasPermission(option.getUnlockRequirement());
            default:
                return false;
        }
    }

    public String getUnlockRequirementText(ChatColorOption option) {
        switch (option.getUnlockMethod()) {
            case "default":
                return "Available by default";
            case "playtime":
                int hours = Integer.parseInt(option.getUnlockRequirement()) / 3600;
                return hours + " hour" + (hours > 1 ? "s" : "") + " of playtime";
            case "achievement":
                return "Complete achievement: " + option.getUnlockRequirement();
            case "level":
                return "Reach level " + option.getUnlockRequirement();
            case "permission":
                return "Special permission required: " + option.getUnlockRequirement();
            default:
                return "Unknown requirement";
        }
    }

    public void setPlayerColor(UUID playerUUID, String colorKey) {
        // If colorKey is null or "gray", we're resetting to default
        if (colorKey == null || colorKey.equalsIgnoreCase("gray")) {
            this.dbManager.setPlayerColor(playerUUID, null);
        } else {
            this.dbManager.setPlayerColor(playerUUID, colorKey);
        }

        // Send message to player
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            String message;
            if (colorKey == null || colorKey.equalsIgnoreCase("gray")) {
                message = plugin.getConfig().getString("messages.color-reset", "&aYour chat color has been reset to default.");
            } else {
                ChatColorOption option = getColorOptions().get(colorKey);
                message = plugin.getConfig().getString("messages.color-changed", "&aYour chat color has been changed to {color}.")
                        .replace("{color}", option.getDisplay());
            }
            player.sendMessage(TextFormatter.parseColorCodes(message));
        }
    }

    public String getPlayerColor(UUID playerUUID) {
        return this.dbManager.getPlayerColor(playerUUID);
    }

    public void closeDatabase() {
        this.dbManager.close();
    }

    public boolean hasUnlockedAnyColor(Player player) {
        for (String colorKey : colorOptions.keySet()) {
            if (hasColorUnlocked(player, colorKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean canUseColor(Player player, String colorKey) {
        return hasColorUnlocked(player, colorKey);
    }
}
