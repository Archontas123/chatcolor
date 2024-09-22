// ChatColorsPlugin.java
package org.mythofy.chatcolors;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ChatColorsPlugin extends JavaPlugin {
    private ChatColorManager colorManager;
    private ChatColorGUI gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.colorManager = new ChatColorManager(this);
        this.gui = new ChatColorGUI(this, this.colorManager);

        ChatColorCommand chatColorCommand = new ChatColorCommand(this.colorManager, this.gui);
        PluginCommand command = getCommand("chatcolor");
        if (command != null) {
            command.setExecutor(chatColorCommand);
            command.setTabCompleter(chatColorCommand);
        }
        getServer().getPluginManager().registerEvents(new ChatListener(this.colorManager, this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, this.colorManager, this.gui), this);

        loadColorOptions();
        loadMessages();

        getLogger().info("ChatColors plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.colorManager != null) {
            this.colorManager.closeDatabase();
        }
        getLogger().info("ChatColors plugin has been disabled!");
    }

    private void loadColorOptions() {
        ConfigurationSection colorSection = getConfig().getConfigurationSection("chat-colors");
        if (colorSection == null) {
            getLogger().warning("No chat-colors section found in config.yml!");
            return;
        }

        for (String key : colorSection.getKeys(false)) {
            ConfigurationSection colorConfig = colorSection.getConfigurationSection(key);
            if (colorConfig == null) continue;

            String display = colorConfig.getString("display");
            String hexCode = colorConfig.getString("hex-code");
            List<String> gradient = colorConfig.getStringList("gradient");
            String mode = colorConfig.getString("mode", "normal");
            String unlockMethod = colorConfig.getString("unlock-method");
            String unlockRequirement = colorConfig.getString("unlock-requirement");

            ConfigurationSection guiSection = colorConfig.getConfigurationSection("gui.icon");
            String guiMaterial = guiSection != null ? guiSection.getString("material") : "WHITE_WOOL";
            String guiDisplayName = guiSection != null ? guiSection.getString("display-name", display) : display;
            List<String> guiLore = guiSection != null ? guiSection.getStringList("lore") : null;

            ChatColorOption option = new ChatColorOption(display, hexCode, gradient, mode, unlockMethod,
                    unlockRequirement, guiMaterial, guiDisplayName, guiLore);
            colorManager.addColorOption(key, option);
        }
    }

    private void loadMessages() {
        ConfigurationSection messagesSection = getConfig().getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                String message = messagesSection.getString(key);
                getConfig().set("messages." + key, message);
            }
        }
    }

    public ChatColorManager getColorManager() {
        return this.colorManager;
    }

    public ChatColorGUI getGui() {
        return this.gui;
    }
}