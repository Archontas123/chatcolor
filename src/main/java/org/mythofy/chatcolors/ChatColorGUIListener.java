package org.mythofy.chatcolors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorGUIListener implements Listener {
    private final ChatColorManager colorManager;
    private final ChatColorGUI gui;
    private static final Pattern PAGE_PATTERN = Pattern.compile("Chat Colors \\(Page (\\d+)/(\\d+)\\)");

    public ChatColorGUIListener(ChatColorManager colorManager, ChatColorGUI gui) {
        this.colorManager = colorManager;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        Matcher matcher = PAGE_PATTERN.matcher(title);
        if (!matcher.matches()) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        String displayName = meta.getDisplayName();

        if (clickedItem.getType().name().endsWith("_WOOL") || clickedItem.getType().name().endsWith("_DYE")) {
            handleColorSelection(player, displayName);
        } else if (displayName.equals(TextFormatter.parseColorCodes("&aNext Page"))) {
            handlePageNavigation(player, matcher, true);
        } else if (displayName.equals(TextFormatter.parseColorCodes("&aPrevious Page"))) {
            handlePageNavigation(player, matcher, false);
        }
    }

    private void handleColorSelection(Player player, String displayName) {
        for (String colorKey : colorManager.getColorOptions().keySet()) {
            ChatColorOption option = colorManager.getColorOptions().get(colorKey);
            if (TextFormatter.parseColorCodes(option.getDisplay()).equals(displayName)) {
                if (colorManager.hasColorUnlocked(player, colorKey)) {
                    colorManager.setPlayerColor(player.getUniqueId(), colorKey);
                    player.sendMessage(Component.text("Your chat color has been set to ")
                            .color(NamedTextColor.GREEN)
                            .append(Component.text(option.getDisplay())
                                    .color(TextFormatter.getTextColor(option))));
                } else {
                    player.sendMessage(Component.text("You haven't unlocked this color yet.")
                            .color(NamedTextColor.RED));
                }
                player.closeInventory();
                return;
            }
        }
        player.sendMessage(Component.text("Error: Color not found.").color(NamedTextColor.RED));
    }

    private void handlePageNavigation(Player player, Matcher matcher, boolean isNext) {
        int currentPage = Integer.parseInt(matcher.group(1));
        int totalPages = Integer.parseInt(matcher.group(2));

        int newPage = isNext ? currentPage + 1 : currentPage - 1;

        if (newPage >= 1 && newPage <= totalPages) {
            gui.openGUI(player, newPage);
        } else {
            player.sendMessage(Component.text("Error: Invalid page number.").color(NamedTextColor.RED));
        }
    }
}