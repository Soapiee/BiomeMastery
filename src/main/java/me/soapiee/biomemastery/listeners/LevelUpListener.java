package me.soapiee.biomemastery.listeners;

import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.events.LevelUpEvent;
import me.soapiee.biomemastery.logic.rewards.PendingReward;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.manager.DataManager;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.manager.PendingRewardsManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LevelUpListener implements Listener {

    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PendingRewardsManager pendingRewardsManager;
    private final CustomLogger logger;

    public LevelUpListener(MessageManager messageManager, CustomLogger logger, DataManager dataManager) {
        this.messageManager = messageManager;
        this.logger = logger;
        configManager = dataManager.getConfigManager();
        pendingRewardsManager = dataManager.getPendingRewardsManager();
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        BiomeLevel biomeLevel = event.getBiomeLevel();
        Reward reward = biomeLevel.getReward(event.getNewLevel());
        String biomeName = biomeLevel.getBiomeName();

        if (!rewardIsValid(reward, biomeName)) return;

        OfflinePlayer offlinePlayer = event.getOfflinePlayer();
        if (!offlinePlayer.isOnline()) {
            addPendingReward(reward, offlinePlayer, event.getNewLevel(), biomeLevel.getBiome());
            return;
        }

        Player player = offlinePlayer.getPlayer();
        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.LEVELLEDUP, event.getNewLevel(), biomeName)));
        playLevelUpSound(player);

        if (!reward.isSingular())
            if (!playerInCorrectBiome(player, biomeLevel.getBiome(), reward)) return;

        reward.give(player);
    }

    private void addPendingReward(Reward reward, OfflinePlayer offlinePlayer, int newLevel, Biome biome) {
        if (!reward.isSingular()) return;

        if (configManager.isDebugMode()) Utils.debugMsg(offlinePlayer.getName(), "&eAdded Pending Reward");
        pendingRewardsManager.add(offlinePlayer.getUniqueId(), new PendingReward(newLevel, biome.name(), reward));
    }

    private boolean rewardIsValid(Reward reward, String biomeName) {
        if (reward == null) {
            logger.logToFile(new NullPointerException(), messageManager.getWithPlaceholder(Message.MISSINGREWARD, biomeName));
            return false;
        }

        return true;
    }

    private boolean playerInCorrectBiome(Player player, Biome correctBiome, Reward reward) {
        Biome playerBiome = player.getLocation().getBlock().getBiome();

        if (!playerBiome.name().equalsIgnoreCase(correctBiome.name())) {
            player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.NOTINBIOME, correctBiome.name(), reward.toString())));
            return false;
        }
        return true;
    }

    private void playLevelUpSound(Player player){
         if (configManager.getLvlUpSound() == null) return;

         player.playSound(player.getLocation(), configManager.getLvlUpSound(), 5, 1);
    }

}
