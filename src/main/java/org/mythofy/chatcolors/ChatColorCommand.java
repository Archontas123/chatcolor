package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ChatColorCommand implements CommandExecutor {
    private final ChatColorManager colorManager;
    private final ChatColorGUI gui;

    public ChatColorCommand(ChatColorManager manager, ChatColorGUI gui) {
        this.colorManager = manager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            // Open the GUI when no arguments are provided
            gui.openGUI(player, 1);
            return true;
        }

        String selectedColor = args[0].toLowerCase();

        if (selectedColor.equals("gray") || selectedColor.equals("default")) {
            this.colorManager.setPlayerColor(player.getUniqueId(), null);
            player.sendMessage(Component.text("Your chat color has been reset to default.").color(NamedTextColor.GRAY));
            return true;
        }

        ChatColorOption option = this.colorManager.getColorOptions().get(selectedColor);

        if (option == null) {
            player.sendMessage(Component.text("Unknown color. Use /chatcolor to see available options.").color(NamedTextColor.RED));
            return true;
        }

        if (!this.colorManager.hasColorUnlocked(player, selectedColor)) {
            player.sendMessage(Component.text("You haven't unlocked this color yet. ").color(NamedTextColor.RED)
                    .append(Component.text("Requirement: " + getUnlockRequirementText(option)).color(NamedTextColor.YELLOW)));
            return true;
        }

        UUID uuid = player.getUniqueId();
        this.colorManager.setPlayerColor(uuid, selectedColor);

        Component successMessage = Component.text("Your chat color has been set to ").color(NamedTextColor.GREEN)
                .append(Component.text(option.getDisplay()).color(getTextColor(option)));
        player.sendMessage(successMessage);

        return true;
    }

    private void showAvailableColors(Player player) {
        Component message = Component.text("Available Chat Colors:").color(NamedTextColor.GOLD);
        player.sendMessage(message);

        for (Map.Entry<String, ChatColorOption> entry : this.colorManager.getColorOptions().entrySet()) {
            String key = entry.getKey();
            ChatColorOption chatColorOption = entry.getValue();
            Component colorComponent;

            if (this.colorManager.hasColorUnlocked(player, key)) {
                colorComponent = Component.text("- " + chatColorOption.getDisplay() + " ").color(getTextColor(chatColorOption))
                        .append(Component.text("[Unlocked]").color(NamedTextColor.GREEN));
            } else {
                colorComponent = Component.text("- " + chatColorOption.getDisplay() + " ").color(NamedTextColor.GRAY)
                        .append(Component.text("[Locked] ").color(NamedTextColor.RED))
                        .append(Component.text("Requirement: " + getUnlockRequirementText(chatColorOption)).color(NamedTextColor.YELLOW));
            }

            player.sendMessage(colorComponent);
        }

        // Add information about custom colors
        if (this.colorManager.hasCustomColorUnlocked(player)) {
            player.sendMessage(Component.text("- Custom Hex Colors [Unlocked]").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("- Custom Hex Colors [Locked]").color(NamedTextColor.RED)
                    .append(Component.text(" Requirement: " + this.colorManager.getCustomColorUnlockRequirement()).color(NamedTextColor.YELLOW)));
        }

        player.sendMessage(Component.text("Use /chatcolor gray to reset your color to default.").color(NamedTextColor.AQUA));
    }

    private TextColor getTextColor(ChatColorOption option) {
        if (option.getHexCode() != null) {
            return TextColor.fromHexString(option.getHexCode());
        } else if (option.isGradient()) {
            return TextColor.fromHexString(option.getGradient().get(0)); // Use the first color of the gradient
        } else if (option.isRainbow()) {
            return NamedTextColor.WHITE; // Default color for rainbow
        } else {
            return NamedTextColor.WHITE; // Default fallback color
        }
    }

    private String getUnlockRequirementText(ChatColorOption option) {
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
                return "Special permission required";
            default:
                return "Unknown requirement";
        }
    }


}