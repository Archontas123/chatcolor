// ChatColorGUI.java
package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ChatColorGUI {
    private final ChatColorsPlugin plugin;
    private final ChatColorManager colorManager;
    private final int guiSize;
    private final Material backgroundMaterial;
    private final String guiTitle;
    private final Material nextPageMaterial;
    private final Map<UUID, Integer> playerPages;
    private final Material prevPageMaterial;

    public ChatColorGUI(ChatColorsPlugin plugin, ChatColorManager colorManager) {
        this.plugin = plugin;
        this.colorManager = colorManager;
        this.guiSize = plugin.getConfig().getInt("gui-settings.size", 54);
        this.backgroundMaterial = Material.valueOf(plugin.getConfig().getString("gui-settings.background-material", "GRAY_STAINED_GLASS_PANE"));
        this.guiTitle = plugin.getConfig().getString("gui-settings.title", "Chat Colors");
        this.nextPageMaterial = Material.valueOf(plugin.getConfig().getString("gui-settings.next-page-material", "ARROW"));
        this.prevPageMaterial = Material.valueOf(plugin.getConfig().getString("gui-settings.prev-page-material", "ARROW"));
        this.playerPages = new HashMap<>();
    }

    public void openGUI(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, guiSize, Component.text(guiTitle));

        setBackground(inventory);
        setPlayerHead(player, inventory);
        setColorOptions(player, inventory, page);
        setNavigationButtons(inventory, page);

        player.openInventory(inventory);
        playerPages.put(player.getUniqueId(), page);
    }

    private void setBackground(Inventory inventory) {
        ItemStack backgroundItem = new ItemStack(backgroundMaterial);
        ItemMeta backgroundMeta = backgroundItem.getItemMeta();
        backgroundMeta.displayName(Component.text(" "));
        backgroundItem.setItemMeta(backgroundMeta);

        for (int i = 0; i < guiSize; i++) {
            inventory.setItem(i, backgroundItem);
        }
    }

    private void setPlayerHead(Player player, Inventory inventory) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.displayName(Component.text(player.getName()).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        String currentColor = colorManager.getPlayerColor(player.getUniqueId());
        if (currentColor != null) {
            ChatColorOption currentOption = colorManager.getColorOptions().get(currentColor);
            lore.add(TextFormatter.parseColorCodes("&7Current Color: " + currentOption.getDisplay()).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Current Color: Default").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        skullMeta.lore(lore);
        playerHead.setItemMeta(skullMeta);
        inventory.setItem(4, playerHead);
    }

    private void setColorOptions(Player player, Inventory inventory, int page) {
        List<Map.Entry<String, ChatColorOption>> colorOptions = new ArrayList<>(colorManager.getColorOptions().entrySet());
        int totalPages = (int) Math.ceil(colorOptions.size() / 28.0);
        int startIndex = (page - 1) * 28;
        int endIndex = Math.min(startIndex + 28, colorOptions.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, ChatColorOption> entry = colorOptions.get(i);
            String colorKey = entry.getKey();
            ChatColorOption option = entry.getValue();

            ItemStack colorItem = new ItemStack(Material.valueOf(option.getGuiMaterial()));
            ItemMeta colorMeta = colorItem.getItemMeta();
            colorMeta.displayName(TextFormatter.parseColorCodes(option.getGuiDisplayName()));

            List<Component> lore = new ArrayList<>();
            for (String loreLine : option.getGuiLore()) {
                lore.add(TextFormatter.parseColorCodes(loreLine).decoration(TextDecoration.ITALIC, false));
            }
            if (colorManager.hasColorUnlocked(player, colorKey)) {
                lore.add(TextFormatter.parseColorCodes("&aUnlocked").decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(TextFormatter.parseColorCodes("&cLocked").decoration(TextDecoration.ITALIC, false));
                lore.add(TextFormatter.parseColorCodes(colorManager.getUnlockRequirementText(option)).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            colorMeta.lore(lore);

            colorItem.setItemMeta(colorMeta);
            int slot = ((i - startIndex) / 7) * 9 + ((i - startIndex) % 7) + 10;
            inventory.setItem(slot, colorItem);
        }
    }

    private void setNavigationButtons(Inventory inventory, int page) {
        int totalPages = (int) Math.ceil(colorManager.getColorOptions().size() / 28.0);

        if (page > 1) {
            ItemStack prevPage = new ItemStack(prevPageMaterial);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.displayName(TextFormatter.parseColorCodes("&aPrevious Page"));
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(48, prevPage);
        }

        if (page < totalPages) {
            ItemStack nextPage = new ItemStack(nextPageMaterial);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.displayName(TextFormatter.parseColorCodes("&aNext Page"));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(50, nextPage);
        }
    }

    public String getGuiTitle() {
        return this.guiTitle;
    }

    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }

    public int getTotalPages() {
        return (int) Math.ceil(colorManager.getColorOptions().size() / 28.0);
    }
}
