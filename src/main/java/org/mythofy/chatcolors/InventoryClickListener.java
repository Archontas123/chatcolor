// InventoryClickListener.java
package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    private final ChatColorsPlugin plugin;
    private final ChatColorManager colorManager;
    private final ChatColorGUI gui;

    public InventoryClickListener(ChatColorsPlugin plugin, ChatColorManager colorManager, ChatColorGUI gui) {
        this.plugin = plugin;
        this.colorManager = colorManager;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(gui.getGuiTitle()))) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Component displayName = clickedItem.getItemMeta().displayName();

        if (displayName == null) return;

        if (displayName.equals(TextFormatter.parseColorCodes("&aPrevious Page"))) {
            int currentPage = gui.getCurrentPage(player);
            if (currentPage > 1) {
                gui.openGUI(player, currentPage - 1);
            }
        } else if (displayName.equals(TextFormatter.parseColorCodes("&aNext Page"))) {
            int currentPage = gui.getCurrentPage(player);
            if (currentPage < gui.getTotalPages()) {
                gui.openGUI(player, currentPage + 1);
            }
        } else {
            for (String colorKey : colorManager.getColorOptions().keySet()) {
                ChatColorOption option = colorManager.getColorOptions().get(colorKey);
                if (displayName.equals(TextFormatter.parseColorCodes(option.getGuiDisplayName()))) {
                    if (colorManager.hasColorUnlocked(player, colorKey)) {
                        colorManager.setPlayerColor(player.getUniqueId(), colorKey);
                        player.sendMessage(TextFormatter.parseColorCodes("&aYour chat color has been set to " + option.getDisplay() + "&a."));
                    } else {
                        player.sendMessage(TextFormatter.parseColorCodes("&cYou haven't unlocked this color yet!"));
                    }
                    player.closeInventory();
                    break;
                }
            }
        }
    }
}
