// ChatColorCommand.java
package org.mythofy.chatcolors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChatColorCommand implements CommandExecutor, TabCompleter {
    private final ChatColorManager colorManager;
    private final ChatColorGUI gui;

    public ChatColorCommand(ChatColorManager manager, ChatColorGUI gui) {
        this.colorManager = manager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormatter.parseColorCodes("&cOnly players can use this command."));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("gui"))) {
            gui.openGUI(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("gui") && args.length == 2) {
            try {
                int page = Integer.parseInt(args[1]);
                if (page > 0 && page <= gui.getTotalPages()) {
                    gui.openGUI(player, page);
                } else {
                    player.sendMessage(Component.text("Invalid page number.").color(NamedTextColor.RED));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid page number.").color(NamedTextColor.RED));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("gradient")) {
            if (args.length < 3) {
                player.sendMessage(TextFormatter.parseColorCodes("&cUsage: /chatcolor gradient <start_color> <end_color>"));
                return true;
            }
            String startColor = args[1];
            String endColor = args[2];

            // Validate hex codes
            if (!isValidHex(startColor) || !isValidHex(endColor)) {
                player.sendMessage(TextFormatter.parseColorCodes("&cPlease provide valid hex codes (e.g., #FF0000)."));
                return true;
            }

            // Create and set gradient
            List<String> gradientColors = Arrays.asList(startColor, endColor);
            colorManager.setPlayerGradient(player.getUniqueId(), gradientColors);
            player.sendMessage(TextFormatter.parseColorCodes("&aYour chat color has been set to a gradient from &b" + startColor + " &ato &b" + endColor + "&a."));
            return true;
        }

        String selectedColor = args[0].toLowerCase();

        if (selectedColor.equals("gray") || selectedColor.equals("default")) {
            this.colorManager.setPlayerColor(player.getUniqueId(), null);
            player.sendMessage(TextFormatter.parseColorCodes("&aYour chat color has been reset to default."));
            return true;
        }

        ChatColorOption option = this.colorManager.getColorOptions().get(selectedColor);

        if (option == null) {
            player.sendMessage(TextFormatter.parseColorCodes("&cUnknown color. Use /chatcolor to see available options."));
            return true;
        }

        if (!this.colorManager.hasColorUnlocked(player, selectedColor)) {
            player.sendMessage(Component.text("You haven't unlocked this color yet. ").color(NamedTextColor.RED)
                    .append(Component.text("Requirement: " + colorManager.getUnlockRequirementText(option)).color(NamedTextColor.YELLOW)));
            return true;
        }

        this.colorManager.setPlayerColor(player.getUniqueId(), selectedColor);

        return true;
    }

    private boolean isValidHex(String color) {
        return color.matches("^#([A-Fa-f0-9]{6})$");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(colorManager.getColorOptions().keySet());
            completions.add("gui");
            completions.add("default");
            if (colorManager.getColorOptions().containsKey("gradient")) {
                completions.add("gradient");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("gui")) {
            return IntStream.rangeClosed(1, gui.getTotalPages())
                    .mapToObj(String::valueOf)
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("gradient")) {
            // Suggest available colors for start_color
            return colorManager.getColorOptions().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("gradient")) {
            // Suggest available colors for end_color
            return colorManager.getColorOptions().keySet().stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
