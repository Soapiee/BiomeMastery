package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.usageCmds.*;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsageCmd implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;

    private final String PERMISSION = "biomemastery.player.command";
    private final Map<String, SubCmd> subCommands = new HashMap<>();

    public UsageCmd(BiomeMastery main) {
        DataManager dataManager = main.getDataManager();
        configManager = dataManager.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();

        register(new GUISub(main));
        register(new RewardSub(main));
        register(new InfoPageSub(main));
        register(new InfoBiomeSub(main));
        register(new InfoPlayerSub(main));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!hasPermission(sender)) return true;

        if (args.length == 0) {
            subCommands.get("gui").execute(sender, label, null);
            return true;
        }

        SubCmd cmd = subCommands.get(getSubCmd(args));
        if (cmd == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
            return true;
        }

        cmd.execute(sender, label, args);
        return true;
    }

    private void register(SubCmd cmd) {
        subCommands.put(cmd.getIDENTIFIER(), cmd);
    }

    private String getSubCmd(String[] args) {
        if (!args[0].equalsIgnoreCase("info")) return args[0].toLowerCase();

        if (args.length == 1) return "infopage";

        String input = args[1];
        if (validatePage(input)) return "infopage";
        if (validateBiome(input)) return "infobiome";
        if (validatePlayer(input)) return "infoplayer";

        return "null";
    }

    private boolean validatePage(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return false;
        }

        return true;
    }

    private boolean validateBiome(String value) {
        try {
            Biome.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        return true;
    }

    private boolean validatePlayer(String value) {
        OfflinePlayer player = playerCache.getOfflinePlayer(value);
        return player != null;
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    private boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!player.hasPermission(PERMISSION))
            sendMessage(player, messageManager.get(Message.NOPERMISSION));

        return player.hasPermission(PERMISSION);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("help");
                results.add("info");
                results.add("reward");
                break;

            case 2:
                if (args[0].equalsIgnoreCase("help")) break;
                configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));

                if (!args[0].equalsIgnoreCase("info")) break;
                playerCache.getOfflinePlayers().forEach(offlinePlayer -> results.add(offlinePlayer.getName()));
                break;

            case 3:
                if (args[0].equalsIgnoreCase("reward")) {
                    Biome biome;
                    try {
                        biome = Biome.valueOf(args[0]);
                    } catch (IllegalArgumentException ignored) {
                        break;
                    }
                    int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) results.add(String.valueOf(i));

                } else if (args[0].equalsIgnoreCase("info")) {
                    playerCache.getOfflinePlayers().forEach(offlinePlayer -> results.add(offlinePlayer.getName()));
                }

        }
        return results.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
