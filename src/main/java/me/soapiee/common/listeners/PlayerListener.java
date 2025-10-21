package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final MessageManager messageManager;
    private final Logger logger;

    private final Map<UUID, Biome> playerBiomeMap;

    public PlayerListener(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
        messageManager = main.getMessageManager();
        logger = main.getCustomLogger();
        playerBiomeMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Biome playerBiome = player.getLocation().getBlock().getBiome();

        if (!dataManager.has(player.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, player);
                dataManager.add(playerData);
            } catch (IOException | SQLException error) {
                logger.logToPlayer(player, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
            }
        }

        playerBiomeMap.put(uuid, playerBiome);

        World playerWorld = player.getWorld();
        if (!dataManager.playerInEnabledWorld(playerWorld)) return;
        if (!dataManager.playerInEnabledBiome(playerBiome)) return;

        setBiomeStart(dataManager.getPlayerData(uuid), playerBiome);
    }

    @EventHandler
    public void onBiomeChange(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World previousWorld = event.getFrom().getWorld();
        Biome previousBiome = event.getFrom().getBlock().getBiome();
        World newWorld = event.getTo().getWorld();
        Biome newBiome = event.getTo().getBlock().getBiome();
        PlayerData playerData = dataManager.getPlayerData(event.getPlayer().getUniqueId());

        if (!playerBiomeMap.containsKey(uuid)) {
//            Utils.consoleMsg(ChatColor.YELLOW + "Player wasnt in playerBiome map, added them");
            playerBiomeMap.put(uuid, newBiome);
        }

        if (previousWorld == newWorld)
            if (previousBiome == newBiome) return;
            else if (previousBiome.name().equalsIgnoreCase(newBiome.name())) return;

        if (!dataManager.playerInEnabledWorld(previousWorld)) return;
        setBiomeProgress(playerData, previousBiome);

        if (!dataManager.playerInEnabledWorld(newWorld)) return;
        setBiomeStart(playerData, newBiome);
    }

    private void setBiomeProgress(PlayerData playerData, Biome previousBiome) {
        if (!dataManager.playerInEnabledBiome(previousBiome)) return;

        BiomeLevel previousBiomeData = playerData.getBiomeData(previousBiome);

//        int progress = (int) ChronoUnit.SECONDS.between(previousBiomeData.getEntryTime(), LocalDateTime.now());

//           Utils.consoleMsg(ChatColor.YELLOW + "Progress: " + progress);
        //Progress is not added if they are in the biome for less than 5 seconds
//        if (progress <= 5) return;

        previousBiomeData.addProgress();
        previousBiomeData.clearEntryTime();
    }

    private void setBiomeStart(PlayerData playerData, Biome newBiome) {
        if (!dataManager.playerInEnabledBiome(newBiome)) return;

//            Utils.consoleMsg(ChatColor.YELLOW + "new Biome: " + (newBiome != null ? newBiome.name() : "null"));
        if (newBiome == null) return;

        BiomeLevel playerLevel = playerData.getBiomeData(newBiome);
        playerLevel.setEntryTime(LocalDateTime.now());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World currentWorld = player.getWorld();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (dataManager.playerInEnabledWorld(currentWorld)) {
            setBiomeProgress(playerData, currentBiome);
        }

        playerData.saveData(true);

        dataManager.remove(uuid);
        playerBiomeMap.remove(uuid);
//        Utils.consoleMsg(ChatColor.RED + "Removed player from playerBiome map");
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        OfflinePlayer player = event.getOfflinePlayer();
        if (!player.isOnline()) return;

        ((Player) player).sendMessage(Utils.colour(ChatColor.GREEN + "You leveled up to level " + event.getNewLevel()));
    }

}
