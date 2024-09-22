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
    private boolean allowColorCodes;
    private boolean allowColorCodesIfUnlocked;

    public ChatColorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbManager = new DatabaseManager(plugin);
        this.dbManager.setup();
        loadColors();
        loadCustomColorSettings();
        loadSettings();
    }

    private void loadSettings() {
        this.allowColorCodes = plugin.getConfig().getBoolean("settings.allow-color-codes", false);
        this.allowColorCodesIfUnlocked = plugin.getConfig().getBoolean("settings.allow-color-codes-ifunlocked", false);
    }

    public boolean canUseColorCode(Player player, String colorCode) {
        if (!plugin.getConfig().getBoolean("settings.allow-color-codes", false)) {
            return false;
        }
        return player.hasPermission("chatcolor.use.colorcodes") ||
                player.hasPermission("chatcolor." + getColorNameFromCode(colorCode));
    }


    private void loadColors() {
        ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("chat-colors");
        if (section == null) {
            this.plugin.getLogger().severe("No chat-colors section found in config.yml!");
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
            String guiMaterial = guiSection.getString("material", "WHITE_WOOL");
            String guiDisplayName = guiSection.getString("display-name", display);
            List<String> guiLore = guiSection.getStringList("lore");

            ChatColorOption option = new ChatColorOption(display, hexCode, gradient, mode, unlockMethod,
                    unlockRequirement, guiMaterial, guiDisplayName, guiLore);
            this.colorOptions.put(key.toLowerCase(), option);
        }
    }

    private void loadCustomColorSettings() {
        ConfigurationSection customSection = plugin.getConfig().getConfigurationSection("custom-colors");
        if (customSection != null) {
            this.allowCustomColors = customSection.getBoolean("allow", false);
            this.customColorUnlockMethod = customSection.getString("unlock-method", "permission");
            this.customColorUnlockRequirement = customSection.getString("unlock-requirement", "chatcolor.custom");
        }
    }

    private String getColorNameFromCode(String code) {
        switch (code.toLowerCase()) {
            case "a": return "green";
            case "b": return "aqua";
            case "c": return "red";
            case "d": return "light_purple";
            case "e": return "yellow";
            case "f": return "white";
            case "0": return "black";
            case "1": return "dark_blue";
            case "2": return "dark_green";
            case "3": return "dark_aqua";
            case "4": return "dark_red";
            case "5": return "dark_purple";
            case "6": return "gold";
            case "7": return "gray";
            case "8": return "dark_gray";
            case "9": return "blue";
            default: return "";
        }
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

    public void setPlayerColor(UUID playerUUID, String colorKey) {
        // If colorKey is null or "gray", we're resetting to default
        if (colorKey == null || colorKey.equalsIgnoreCase("gray")) {
            this.dbManager.setPlayerColor(playerUUID, null);
        } else {
            this.dbManager.setPlayerColor(playerUUID, colorKey);
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

    public boolean allowColorCodes() {
        return this.plugin.getConfig().getBoolean("settings.allow-color-codes", false);
    }

    // This method replaces the old hasColorPermission method
    public boolean canUseColor(Player player, String colorKey) {
        return hasColorUnlocked(player, colorKey);
    }




}