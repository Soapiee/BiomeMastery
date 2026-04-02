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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResetSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "reset";
    @Getter private final String PERMISSION = "biomemastery.reset";

    public ResetSub(BiomeMastery main) {
        super(main, "biomemastery.reset", 2, 3);
    }

    // /abm reset <player>
    // /abm reset <player> <biome>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

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
