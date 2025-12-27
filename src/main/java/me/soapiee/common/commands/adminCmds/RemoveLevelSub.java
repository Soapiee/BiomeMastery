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
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.CustomLogger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoveLevelSub extends ModifySub implements SubCmd {

    @Getter private final String IDENTIFIER = "removelevel";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 4;
    @Getter private final int MAX_ARGS = 4;

    public RemoveLevelSub(BiomeMastery main) {
        super(main);
    }

    // /abm removeLevel <player> <biome> <value>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, main, args, label)) return;

        OfflinePlayer target = getTarget(sender, args[1]);
        if (target == null) return;

        Biome biome = getBiome(sender, args[2]);
        if (biome == null) return;

        int value = getValue(sender, args[3]);
        if (value == -1) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(target)
                .thenAcceptAsync(data -> removeLevel(sender, data, target, biome, value), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private void removeLevel(CommandSender sender, PlayerData playerData, OfflinePlayer target, Biome biome, int value) {
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        updateProgress(target, biomeLevel);

        boolean wasMaxLevel = biomeLevel.isMaxLevel();
        int oldLevel = biomeLevel.getLevel();
        int newLevel = biomeLevel.getLevel() - value;

        if (biomeLevel.setLevel(newLevel) == -1) {
            sendMessage(sender, messageManager.getWithPlaceholder(
                    Message.LEVELREMOVEERROR, target.getName(), value, biomeLevel.getBiomeName()));
            return;
        }

        sendMessage(sender, messageManager.getWithPlaceholder(
                Message.LEVELREMOVED, target.getName(), value, biomeLevel.getBiomeName()));

        sendAdminUpdateMsg(sender, target, messageManager.getWithPlaceholder(Message.ADMINREMOVEDLEVEL, value, biomeLevel.getBiomeName()));

        removeActiveRewards(target, playerData, biome, oldLevel, newLevel);

        UUID uuid = target.getUniqueId();
        if (pendingRewardsManager.has(uuid))
            removePendingRewards(uuid, pendingRewardsManager.get(uuid), biomeLevel.getBiome(), newLevel);
        if (wasMaxLevel) biomeLevel.setEntryTime(LocalDateTime.now());
    }

    private void removeActiveRewards(OfflinePlayer player, PlayerData playerData, Biome biome, int oldLevel, int newLevel) {
        BiomeData biomeData = biomeDataManager.getBiomeData(biome);

        for (int i = oldLevel; i > newLevel; i--) {
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

    private void removePendingRewards(UUID uuid, ArrayList<PendingReward> rewards, Biome biome, int newLevel) {
        ArrayList<PendingReward> list = new ArrayList<>();

        for (PendingReward reward : rewards) {
            if (reward.getBiome().equalsIgnoreCase(biome.name()) && reward.getLevel() > newLevel) continue;
            list.add(reward);
        }

        pendingRewardsManager.addAll(uuid, list);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
