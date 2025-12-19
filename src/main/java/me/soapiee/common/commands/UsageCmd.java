package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UsageCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;
    private final CmdCooldownManager cooldownManager;
    private final HashMap<Integer, Biome> enabledBiomes;

    public UsageCmd(BiomeMastery main) {
        this.main = main;
        DataManager dataManager = main.getDataManager();
        playerDataManager = dataManager.getPlayerDataManager();
        configManager = dataManager.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();
        customLogger = main.getCustomLogger();
        cooldownManager = dataManager.getCooldownManager();
        enabledBiomes = createEnabledBiomes();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!checkPermission(sender, "biomemastery.player.command")) return true;

        // /bm - Opens the GUI
        if (args.length == 0) {
//            TODO:
//             openGUI((Player) sender);
            sendHelpMessage(sender, label);
            return true;
        }

        // /bm help
        if (args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender, label);
            return true;
        }

        // /bm reward <biome> <level>
        if (args[0].equalsIgnoreCase("reward")) {
            if (!rewardBiome(sender, args)) sendHelpMessage(sender, label);
            return true;
        }

        // /bm info
        // /bm info <page>
        // /bm info [player]
        // /bm info [player] <page>
        // /bm info [biome]
        // /bm info [biome] <player>
        if (args[0].equalsIgnoreCase("info")) {
            // /bm info
            if (args.length == 1) {
                if (isConsole(sender)) return true;

                displayInfo(sender, (Player) sender, 1);
                return true;
            }

            if (args.length == 2 || args.length == 3) {
                // /bm info <page>
                int page = validatePage(args[1], sender);

                if (page >= 0) {
                    if (page == 0) return true;
                    displayInfo(sender, (Player) sender, page);
                    return true;
                }

                // /bm info <biome>
                // /bm info <biome> <player>
                Biome biome = validateBiome(args[1]);
                if (biome != null) {
                    infoBiome(sender, biome, args);
                    return true;
                }

                // /bm info <player>
                // /bm info <player> <page>
                OfflinePlayer target = getTarget(sender, args[1]);
                if (target == null) return true;
                if (!checkPermission(sender, "biomemastery.player.others")) return true;

                if (infoPlayer(sender, target, args)) return true;
            }
        }

        sendHelpMessage(sender, label);
        return true;
    }

    private OfflinePlayer getTarget(CommandSender sender, String playerName) {
        OfflinePlayer target = playerCache.getOfflinePlayer(playerName);
        if (target == null) {
            sendMessage(sender, messageManager.get(Message.PLAYERNOTFOUND));
            return null;
        }

        return ((!hasPlayerData(sender, target)) ? null : target);
    }

    private boolean hasPlayerData(CommandSender sender, OfflinePlayer target) {
        if (target == null) return false;

        if (!playerDataManager.has(target.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, target);
                playerDataManager.add(playerData);
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                return false;
            }
        }

        return true;
    }

    private boolean isConsole(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, messageManager.get(Message.CONSOLEUSAGEERROR));
            return true;
        }

        return false;
    }

    private int validatePage(String value, CommandSender sender) {
        int page;
        try {
            page = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }

        int enabledBiomesCount = enabledBiomes.size();
        int maxBiomes = configManager.getBiomesPerPage();
        int totalPages = ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);

        if (page < 1 || page > totalPages) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDPAGE, page, totalPages));
            return 0;
        }

        return page;
    }

    private Biome validateBiome(String value) {
        Biome biome;
        try {
            biome = Biome.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }

        return biome;
    }

    private void updateProgress(OfflinePlayer target) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();
        if (!configManager.isEnabledBiome(locBiome)) return;

        BiomeLevel biomeLevel = playerDataManager.getPlayerData(onlinePlayer.getUniqueId()).getBiomeLevel(locBiome);
        biomeLevel.updateProgress(locBiome);
    }

    private void toggleReward(Player player, Biome biome, String value) {
        int levelToClaim;
        try {
            levelToClaim = Integer.parseInt(value);
        } catch (NumberFormatException error) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, value));
            return;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        int currentLevel = biomeLevel.getLevel();
        int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

        if (levelToClaim > maxLevel || levelToClaim < 1) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.LEVELOUTOFBOUNDARY, maxLevel, value));
            return;
        }

        if (currentLevel < levelToClaim) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDNOTACHIEVED, currentLevel));
            return;
        }

        Reward reward = biomeDataManager.getBiomeData(biome).getReward(levelToClaim);

        if (reward == null) {
            sendHelpMessage(player, "biome");
            return;
        }

        if (!reward.isSingular()) {
            if (hasThisActiveReward(player, reward)) {
                deactivateReward(player, reward);
                return;
            }

            if (player.getLocation().getBlock().getBiome() == biome) {
                reward.give(player);
                return;
            }

            sendMessage(player, messageManager.getWithPlaceholder(Message.NOTINBIOME, biome.name(), reward.toString()));

        } else {
            sendMessage(player, messageManager.get(Message.REWARDALREADYCLAIMED));
        }
    }

    private boolean hasThisActiveReward(Player player, Reward reward) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData.hasActiveRewards()) {
            if (reward instanceof PotionReward) {
                PotionEffectType potion = ((PotionReward) reward).getPotion();
                return (player.hasPotionEffect(potion));
            }

            if (reward instanceof EffectReward) {
                Effect effect = ((EffectReward) reward).getEffect();
                return (effect.isActive(player));
            }
        }

        return false;
    }

    private void deactivateReward(Player player, Reward reward) {
        if (reward instanceof PotionReward) {
            ((PotionReward) reward).remove(player);
        }
        if (reward instanceof EffectReward) {
            ((EffectReward) reward).remove(player);
        }

        sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDDEACTIVATED, reward.toString()));
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        String message = messageManager.getWithPlaceholder(Message.PLAYERHELP, label);
        if (message == null) return;

        sendMessage(sender, message);
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!player.hasPermission(permission))
            sendMessage(player, messageManager.get(Message.NOPERMISSION));

        return player.hasPermission(permission);
    }

    private void infoBiome(CommandSender sender, Biome biome, String[] args) {
        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, args[1]));
            return;
        }

        OfflinePlayer target;
        if (args.length == 2) {
            target = (Player) sender;
        } else {
            if (!checkPermission(sender, "biomemastery.player.others")) return;
            target = getTarget(sender, args[2]);

            if (target == null) return;
        }

        displayBiomeInfo(sender, target, biome);
    }

    private boolean infoPlayer(CommandSender sender, OfflinePlayer target, String[] args) {
//      /bm info <player> <page>
        if (args.length == 3) {
            int page = validatePage(args[2], sender);
            if (page == 0) return true;
            if (page < 0) {
                sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, args[2]));
                return true;
            }

            displayInfo(sender, target, page);
            return true;
        }

//      /bm info <player>
        if (args.length == 2) {
            displayInfo(sender, target, 1);
            return true;
        }

        return false;
    }

    private boolean rewardBiome(CommandSender sender, String[] args) {
        if (args.length != 3) return false;

        Biome biome = validateBiome(args[1]);

        if (biome == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[1]));
            return true;
        }

        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, args[1]));
            return true;
        }

        toggleReward((Player) sender, biome, args[2]);
        return true;
    }

    private void displayInfo(CommandSender sender, OfflinePlayer target, int page) {
        int cooldown = (int) cooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return;
        }

        updateProgress(target);
        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
        StringBuilder builder = new StringBuilder();

        builder.append(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, target.getName())).append("\n");

        int maxBiomes = configManager.getBiomesPerPage();
        int startPoint = (page * maxBiomes) - (maxBiomes - 1);
        int endPoint = startPoint + maxBiomes;
        int enabledBiomesCount = enabledBiomes.size();
        int totalPages = ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);

        for (int i = startPoint; i < endPoint; i++) {
            if (i > enabledBiomes.size()) break;

            Biome biome = enabledBiomes.get(i);
            BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
            BiomeData biomeData = biomeDataManager.getBiomeData(biome);

            Message message = Message.BIOMEBASICINFOFORMAT;
            if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEBASICINFOMAX;

            builder.append(messageManager.getWithPlaceholder(message, target.getName(), biomeData, biomeLevel));

            builder.append("\n");
        }

        cooldownManager.addCooldown(sender);
        sendMessage(sender, builder.toString());

        sendDynamicPageFooter(sender, page, totalPages, target);
    }

    private void sendDynamicPageFooter(CommandSender sender, int page, int totalPages, OfflinePlayer target) {
        String message = Utils.addColour(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOFOOTER, page, totalPages));

        if (!(sender instanceof Player)) {
            sendMessage(sender, message);
            return;
        }

        ComponentBuilder builder = new ComponentBuilder();

        //Previous button
        if (page - 1 > 0) {
            TextComponent prevButton = createTextComponent(
                    messageManager.get(Message.BIOMEBASICINFOPREVBUTTON),
                    "/biome info " + target.getName() + " " + (page - 1),
                    messageManager.get(Message.PREVBUTTONHOVER)
            );

            if (prevButton != null) builder.append(prevButton);
        }

        //Footer
        builder.append(" ", ComponentBuilder.FormatRetention.NONE);
        String translatedColours = Utils.addColour(message);
        builder.append(TextComponent.fromLegacyText(translatedColours));
        builder.append(" ");

        //Next button
        if (page + 1 <= totalPages) {
            TextComponent nextButton = createTextComponent(
                    messageManager.get(Message.BIOMEBASICINFONEXTBUTTON),
                    "/biome info " + target.getName() + " " + (page + 1),
                    messageManager.get(Message.NEXTBUTTONHOVER)
            );

            if (nextButton != null) builder.append(nextButton);
        }

        sender.spigot().sendMessage(builder.create());
    }

    private TextComponent createTextComponent(String message, String cmd, String hoverText) {
        if (message == null || message.isEmpty()) return null;

        TextComponent component = new TextComponent("");
        String translatedMessage = Utils.addColour(message);
        for (BaseComponent child : TextComponent.fromLegacyText(translatedMessage)) {
            component.addExtra(child);
        }

        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));

        if (hoverText != null && !hoverText.isEmpty()) {
            String translatedHover = Utils.addColour(hoverText);

            BaseComponent[] hoverComponents = TextComponent.fromLegacyText(translatedHover);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverComponents)));
        }

        return component;
    }

    private void displayBiomeInfo(CommandSender sender, OfflinePlayer target, Biome biome) {
        int cooldown = (int) cooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return;
        }

        updateProgress(target);
        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        BiomeData biomeData = biomeDataManager.getBiomeData(biome);
        StringBuilder builder = new StringBuilder();

        Message message = Message.BIOMEDETAILEDFORMAT;
        if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEDETAILEDMAX;

        builder.append(messageManager.getWithPlaceholder(message, target.getName(), biomeData, biomeLevel));

        for (int i = 1; i <= biomeData.getMaxLevel(); i++) {
            Reward reward = biomeData.getReward(i);

            builder.append("\n")
                    .append(messageManager.getWithPlaceholder(Message.BIOMEREWARDFORMAT,
                            i,
                            reward,
                            getRewardStatus(target, biomeData, i, biomeLevel.getLevel())));
        }

        cooldownManager.addCooldown(sender);
        sendMessage(sender, builder.toString());
    }

    private String getRewardStatus(OfflinePlayer player, BiomeData biomeData, int rewardLevel, int currentLevel) {
        if (currentLevel < rewardLevel) {
            String message = messageManager.get(Message.REWARDUNCLAIMED);
            return message == null ? "" : message;
        }

        Reward reward = biomeData.getReward(rewardLevel);
        if (reward.isSingular()) {
            String message = messageManager.get(Message.REWARDCLAIMED);
            return message == null ? "" : message;
        }

        if (!player.isOnline()) {
            String message = messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiomeName());
            return message == null ? "" : message;
        }

        Player onlinePlayer = player.getPlayer();
        if (hasThisActiveReward(onlinePlayer, reward)) {
            String message = messageManager.get(Message.REWARDDEACTIVATE);
            return message == null ? "" : message;
        }

        Biome targetLocation = onlinePlayer.getLocation().getBlock().getBiome();
        String message;
        if (targetLocation.name().equalsIgnoreCase(biomeData.getBiomeName()))
            message = messageManager.get(Message.REWARDACTIVATE);
        else
            message = messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiomeName());

        return message == null ? "" : message;
    }

    private void openGUI(Player player) {
        //TODO: Open gui for player
        // if (!hasPlayerData(player)) return;
        // updateProgress((Player) sender);
        // sendMessage(player, messageManager.get(Message.GUIOPENED));
    }

    private HashMap<Integer, Biome> createEnabledBiomes(){
        HashMap<Integer, Biome> map = new HashMap<>();

        int i = 1;
        for (Biome biome : configManager.getEnabledBiomes()) {
            map.put(i, biome);
            i++;
        }

        return map;
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
