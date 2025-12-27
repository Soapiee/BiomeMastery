package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.data.BukkitExecutor;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.PendingReward;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.CustomLogger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResetSub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PendingRewardsManager pendingRewardsManager;

    @Getter private final String IDENTIFIER = "reset";
    @Getter private final String PERMISSION = "biomemastery.reset";
    @Getter private final int MIN_ARGS = 2;
    @Getter private final int MAX_ARGS = 3;

    public ResetSub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
        DataManager dataManager = main.getDataManager();
        configManager = dataManager.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        pendingRewardsManager = dataManager.getPendingRewardsManager();
    }

    // /abm reset <player>
    // /abm reset <player> <biome>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, main, args, label)) return;

        OfflinePlayer target = getTarget(sender, args);
        if (target == null) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(target)
                .thenAcceptAsync(data -> reset(sender, data, target, args), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private OfflinePlayer getTarget(CommandSender sender, String[] args) {
        OfflinePlayer target = main.getPlayerCache().getOfflinePlayer(args[1]);

        if (target == null) sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERNOTFOUND, args[1]));

        return target;
    }

    private void reset(CommandSender sender, PlayerData playerData, OfflinePlayer target, String[] args){
        if (args.length == MIN_ARGS){
            resetPlayer(sender, playerData, target);
            return;
        }

        Biome biome = getBiome(sender, args[2]);
        if (biome == null) return;

        resetBiome(sender, playerData, target, biome);
    }

    private void resetPlayer(CommandSender sender, PlayerData playerData, OfflinePlayer target){
        for (BiomeLevel biomeLevel : playerData.getBiomeLevels()) {
            biomeLevel.reset();
        }

        sendMessage(sender, messageManager.getWithPlaceholder(Message.RESETPLAYER, target.getName()));
        if (target.isOnline()) {
            playerData.clearActiveRewards();

            if (sender instanceof ConsoleCommandSender || sender instanceof Player && sender != target.getPlayer())
                sendMessage(target.getPlayer(), messageManager.get(Message.ADMINRESETALL));
        }

        pendingRewardsManager.removeAll(target.getUniqueId());
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

    private void resetBiome(CommandSender sender, PlayerData playerData, OfflinePlayer target, Biome inputBiome) {
        String biomeName = inputBiome.name();
        BiomeLevel biomeLevel = playerData.getBiomeLevel(inputBiome);

        if (target.isOnline()) {
            removeActiveRewards(target, playerData, inputBiome, biomeLevel.getLevel());

            if (sender instanceof ConsoleCommandSender || (sender instanceof Player && sender != target.getPlayer()))
                sendMessage(target.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINRESETBIOME, biomeName));
        }

        biomeLevel.reset();
        sendMessage(sender, messageManager.getWithPlaceholder(Message.RESETPLAYERBIOME, biomeName, target.getName()));

        UUID uuid = target.getUniqueId();
        if (pendingRewardsManager.has(uuid))
            removeBiomePendingRewards(uuid, pendingRewardsManager.get(uuid), biomeLevel.getBiome());
    }

    private void removeActiveRewards(OfflinePlayer player, PlayerData playerData, Biome biome, int oldLevel) {
        BiomeData biomeData = biomeDataManager.getBiomeData(biome);

        for (int i = oldLevel; i > 0; i--) {
            Reward reward = biomeData.getReward(i);
            if (player.isOnline()) checkRewardType(player.getPlayer(), playerData, reward);
        }
    }

    private void checkRewardType(Player player, PlayerData playerData, Reward reward) {
        if (reward instanceof PotionReward) {
            ((PotionReward) reward).remove(player);
            playerData.clearActiveReward(reward);
        }

        if (reward instanceof EffectReward) {
            ((EffectReward) reward).remove(player);
            playerData.clearActiveReward(reward);
        }
    }

    private void removeBiomePendingRewards(UUID uuid, ArrayList<PendingReward> rewards, Biome biome) {
        ArrayList<PendingReward> list = new ArrayList<>();

        for (PendingReward reward : rewards) {
            if (reward.getBiome().equalsIgnoreCase(biome.name())) continue;
            list.add(reward);
        }

        pendingRewardsManager.addAll(uuid, list);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
