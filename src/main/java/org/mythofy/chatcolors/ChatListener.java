package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final ChatColorManager colorManager;

    public ChatListener(ChatColorManager colorManager) {
        this.colorManager = colorManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String colorKey = colorManager.getPlayerColor(event.getPlayer().getUniqueId());

        Component formattedMessage = TextFormatter.parseColorCodes(message);

        if (colorKey != null) {
            ChatColorOption option = colorManager.getColorOptions().get(colorKey);
            if (option != null) {
                TextColor defaultColor = TextColor.fromHexString(option.getHexCode());
                formattedMessage = formattedMessage.color(defaultColor);
            }
        }

        event.setCancelled(true);
        Component finalMessage = Component.text()
                .append(Component.text("<" + event.getPlayer().getName() + "> "))
                .append(formattedMessage)
                .build();
        event.getPlayer().getServer().sendMessage(finalMessage);
    }
}