package me.soapiee.biomemastery.logic;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.manager.DataManager;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProgressChecker extends BukkitRunnable {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;

    public ProgressChecker(BiomeMastery main, DataManager dataManager) {
        playerDataManager = dataManager.getPlayerDataManager();
        configManager = main.getConfigManager();

        long delay = configManager.getUpdateInterval();
        if (configManager.isDebugMode())
            Utils.debugMsg("", ChatColor.YELLOW + "Progress Checker started with a " + delay + " second delay");

        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Biome playerBiome = player.getLocation().getBlock().getBiome();

            if (!configManager.isEnabledBiome(playerBiome)) continue;
            if (!configManager.isEnabledWorld(player.getWorld())) continue;

            PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
            if (playerData == null) return;

            BiomeLevel playerLevel = playerData.getBiomeLevel(playerBiome);
            if (playerLevel.isMaxLevel()) continue;

            playerLevel.updateProgress(playerBiome);
        }
    }
}
