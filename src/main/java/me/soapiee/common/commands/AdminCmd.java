package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.adminCmds.*;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {

    private final BiomeDataManager biomeDataManager;
    private final ConfigManager configManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;

    private final String PERMISSION = "biomemastery.admin";
    private final Map<String, SubCmd> subCommands = new HashMap<>();

    public AdminCmd(BiomeMastery main) {
        DataManager dataManager = main.getDataManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        configManager = dataManager.getConfigManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();

        register(new ReloadSub(main));
        register(new ListSub(main));
        register(new EnableSub(main));
        register(new DisableSub(main));
        register(new ResetSub(main));
        register(new SetLevelSub(main));
        register(new AddLevelSub(main));
        register(new RemoveLevelSub(main));
        register(new SetProgressSub(main));
        register(new AddProgressSub(main));
        register(new RemoveProgressSub(main));
    }

    private void register(SubCmd cmd) {
        subCommands.put(cmd.getIDENTIFIER(), cmd);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof CommandBlock) return true;
        if (!hasPermission(sender)) return true;

        SubCmd cmd = subCommands.get(args[0]);
        if (cmd == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.ADMINHELP, label));
            return true;
        }

        cmd.execute(sender, label, args);
        return true;
    }

    private Biome validateBiome(String input) {
        Biome biome;
        try {
            biome = Biome.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }

        return biome;
    }

    private boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!player.hasPermission(PERMISSION))
            sendMessage(player, messageManager.get(Message.NOPERMISSION));

        return player.hasPermission(PERMISSION);
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.addAll(Arrays.asList("list", "enable", "disable", "setlevel", "addlevel", "removelevel",
                        "setprogress", "addprogress", "removeprogress", "reset"));

//                results.add("effect");

                if (sender instanceof Player && sender.hasPermission("biomemastery.admin")) {
                    results.add("reload");
                }
                break;

            case 2:
                if (args[0].equalsIgnoreCase("reload")) break;

                if (args[0].equalsIgnoreCase("list")) {
                    results.add("worlds");
                    results.add("biomes");
                    break;
                }

                if (args[0].equalsIgnoreCase("enable")) {
                    Bukkit.getWorlds().forEach(world -> results.add(world.getName()));

                    Arrays.stream(Biome.values()).forEach(biome -> results.add(biome.name().toLowerCase()));
                    break;
                }

                if (args[0].equalsIgnoreCase("disable")) {
                    Bukkit.getWorlds().forEach(world -> results.add(world.getName()));

                    configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));
                    break;
                }

                playerCache.getOfflinePlayers().forEach(player -> results.add(player.getName()));
                break;

            case 3:
                configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));
                break;

            case 4:
                if (args[0].equalsIgnoreCase("setlevel")) {
                    Biome biome = validateBiome(args[2]);
                    if (biome == null) break;
                    if (!configManager.isEnabledBiome(biome)) break;

                    int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) results.add(String.valueOf(i));
                }

        }
        return results.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
