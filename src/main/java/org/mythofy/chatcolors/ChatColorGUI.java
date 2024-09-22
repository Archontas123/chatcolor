package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatColorGUI {
    private final ChatColorsPlugin plugin;
    private final ChatColorManager colorManager;
    private final int guiSize;
    private final Material backgroundMaterial;
    private final String guiTitle;
    private final Material nextPageMaterial;
    private final Material prevPageMaterial;

    public ChatColorGUI(ChatColorsPlugin plugin, ChatColorManager colorManager) {
        this.plugin = plugin;
        this.colorManager = colorManager;
        ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("gui-settings");
        this.guiSize = guiConfig.getInt("size", 54);
        this.backgroundMaterial = Material.valueOf(guiConfig.getString("background-material", "GRAY_STAINED_GLASS_PANE"));
        this.guiTitle = guiConfig.getString("title", "Chat Colors");
        this.nextPageMaterial = Material.valueOf(guiConfig.getString("next-page-material", "ARROW"));
        this.prevPageMaterial = Material.valueOf(guiConfig.getString("prev-page-material", "ARROW"));
    }

    public void openGUI(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, guiSize, guiTitle);

        setBackground(inventory);
        setPlayerHead(player, inventory);
        setColorOptions(player, inventory, page);
        setNavigationButtons(inventory, page);

        player.openInventory(inventory);
    }

    private void setBackground(Inventory inventory) {
        ItemStack backgroundItem = new ItemStack(backgroundMaterial);
        ItemMeta backgroundMeta = backgroundItem.getItemMeta();
        backgroundMeta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
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

        List<Component> headLore = new ArrayList<>();
        String currentColor = colorManager.getPlayerColor(player.getUniqueId());
        if (currentColor != null) {
            ChatColorOption currentOption = colorManager.getColorOptions().get(currentColor);
            if (currentOption != null) {
                TextColor color = getTextColor(currentOption.getHexCode());
                headLore.add(Component.text("Current Color: ").color(NamedTextColor.GRAY)
                        .append(Component.text(currentOption.getDisplay()).color(color))
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                headLore.add(Component.text("Current Color: Unknown").color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            headLore.add(Component.text("Current Color: Default").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        skullMeta.lore(headLore);
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
            TextColor textColor = getTextColor(option.getHexCode());
            colorMeta.displayName(Component.text(option.getDisplay()).color(textColor).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            for (String loreLine : option.getGuiLore()) {
                lore.add(Component.text(loreLine).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            if (colorManager.hasColorUnlocked(player, colorKey)) {
                lore.add(Component.text("Unlocked").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Locked").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text(option.getUnlockRequirement()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            colorMeta.lore(lore);

            colorItem.setItemMeta(colorMeta);
            inventory.addItem(colorItem);
        }
    }

    private void setNavigationButtons(Inventory inventory, int page) {
        int totalPages = (int) Math.ceil(colorManager.getColorOptions().size() / 28.0);

        if (page > 1) {
            ItemStack prevPage = new ItemStack(prevPageMaterial);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(guiSize - 9, prevPage);
        }

        if (page < totalPages) {
            ItemStack nextPage = new ItemStack(nextPageMaterial);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.displayName(Component.text("Next Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(guiSize - 1, nextPage);
        }
    }

    private TextColor getTextColor(String hexCode) {
        if (hexCode != null && hexCode.startsWith("#")) {
            try {
                return TextColor.fromHexString(hexCode);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid hex color code: " + hexCode);
            }
        }
        return NamedTextColor.WHITE;
    }
}