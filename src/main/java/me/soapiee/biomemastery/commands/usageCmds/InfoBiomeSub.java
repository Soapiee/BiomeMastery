package me.soapiee.biomemastery.commands.usageCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.effects.EffectInterface;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.logic.rewards.types.PotionReward;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class InfoBiomeSub extends AbstractUsageSub {

    @Getter private final String IDENTIFIER = "infobiome";
    private final String PERMISSION_OTHER = "biomemastery.player.others";

    public InfoBiomeSub(BiomeMastery main) {
        super(main, null, 2, 3);
    }

    // /bm info [biome]
    // /bm info [biome] <player>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == MIN_ARGS && sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.CONSOLEUSAGEERROR)));
            return;
        }

        if (!checkRequirements(sender, args, label)) return;

        Biome biome = getBiome(sender, args[1]);
        if (biome == null) return;

        OfflinePlayer target = getTarget(sender, args);
        if (target == null) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(target)
                .thenAcceptAsync(data -> displayInfo(sender, target, data, biome), BukkitExecutor.sync(main))
                .exceptionally(error -> {
//                    logger.logToPlayer(target, null, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, target.getName())));
                    return null;
                });
    }

    private Biome getBiome(CommandSender sender, String value) {
        Biome biome;
        try {
            biome = Biome.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException error) {
            biome = null;
        }

        if (biome == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, value));
            return null;
        }

        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, value));
            return null;
        }

        return biome;
    }

    private OfflinePlayer getTarget(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length == 2) return (Player) sender;

        else {
            if (!checkPermission(sender, PERMISSION_OTHER)) {
                sendMessage(sender, messageManager.get(Message.NOPERMISSION));
                return null;
            }
            target = main.getPlayerCache().getOfflinePlayer(args[2]);

        }

        if (target == null) sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERNOTFOUND, args[2]));

        return target;
    }

    public void displayInfo(CommandSender sender, OfflinePlayer target, PlayerData playerData, Biome biome) {
        updateProgress(target, playerData);
        cmdCooldownManager.addCooldown(sender);
        sendMessage(sender, createInfoString(target, playerData, biome));
    }

    private String createInfoString(OfflinePlayer target, PlayerData playerData, Biome biome){
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
                            getRewardStatus(target, playerData, biomeData, i, biomeLevel.getLevel())));
        }

        return builder.toString();
    }

    private String getRewardStatus(OfflinePlayer player, PlayerData playerData, BiomeData biomeData, int rewardLevel, int currentLevel) {
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
        if (hasThisActiveReward(onlinePlayer, playerData, reward)) {
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

    private boolean hasThisActiveReward(Player player, PlayerData playerData, Reward reward) {
        if (playerData.hasActiveRewards()) {
            if (reward instanceof PotionReward) {
                PotionEffectType potion = ((PotionReward) reward).getPotion();
                return (player.hasPotionEffect(potion));
            }

            if (reward instanceof EffectReward) {
                EffectInterface effect = ((EffectReward) reward).getEffect();
                return (effect.isActive(player));
            }
        }

        return false;
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
