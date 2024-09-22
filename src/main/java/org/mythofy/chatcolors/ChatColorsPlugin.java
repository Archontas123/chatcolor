package org.mythofy.chatcolors;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatColorsPlugin extends JavaPlugin {
    private ChatColorManager colorManager;
    private ChatColorGUI gui;
    private BukkitAudiences adventure;

    public void onEnable() {
        saveDefaultConfig();
        this.adventure = BukkitAudiences.create((Plugin)this);
        this.colorManager = new ChatColorManager(this);
        this.gui = new ChatColorGUI(this, this.colorManager);
        String guiTitle = getConfig().getString("gui-settings.title", "Chat Colors");
        getServer().getPluginManager().registerEvents(new InventoryClickListener(guiTitle), this);
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this.colorManager, this.gui));
        getServer().getPluginManager().registerEvents(new ChatListener(this.colorManager), (Plugin)this);
    }

    public void onDisable() {
        if (this.adventure != null)
            this.adventure.close();
        this.colorManager.closeDatabase();
    }

    public ChatColorManager getColorManager() {
        return this.colorManager;
    }

    public BukkitAudiences getAdventure() {
        return this.adventure;
    }
}
