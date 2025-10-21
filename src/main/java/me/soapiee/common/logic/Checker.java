package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Checker extends BukkitRunnable {

    private final DataManager dataManager;
    private final List<World> enabledWorlds;

    public Checker(BiomeMastery main, long delay) {
        dataManager = main.getDataManager();
        enabledWorlds = dataManager.getEnabledWorlds();
        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            World playerWorld = player.getWorld();
            if (!dataManager.playerInEnabledWorld(playerWorld)) return;

            Biome playerBiome = player.getLocation().getBlock().getBiome();
            if (!dataManager.playerInEnabledBiome(playerBiome)) return;

            BiomeLevel playerLevel = dataManager.getPlayerData(player.getUniqueId()).getBiomeData(playerBiome);
            int currentProgress = playerLevel.getProgress();
            //int totalProgress = difference between: playerLevel.getEntryTime() + LocalDateTime.now();
//            if (//totalProgress is more than biome targetTime){
//            playerLevel.addProgress(totalProgress)){};


        }
        // Get a list of all players
        // Loop the list and check their current progress value
        // If progress is above threshold, level them up
    }
}
