package me.soapiee.biomemastery.commands.adminCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.rewards.PendingReward;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.logic.rewards.types.PotionReward;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoveLevelSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "removelevel";

    public RemoveLevelSub(BiomeMastery main) {
        super(main, null, 4, 4);
    }

    // /abm removeLevel <player> <biome> <value>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

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
