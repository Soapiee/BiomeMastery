package me.soapiee.biomemastery.data;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.BiomeDataManager;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.manager.DataManager;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PlayerDatabaseStorage implements PlayerStorageHandler {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final MessageManager messageManager;
    private final PlayerData playerData;
    private final CustomLogger logger;
    private final Set<Biome> enabledBiomes;

    private final UUID uuid;
    private final OfflinePlayer player;

    public PlayerDatabaseStorage(BiomeMastery main, PlayerData playerData) {
        this.main = main;
        dataManager = main.getDataManager();
        configManager = main.getDataManager().getConfigManager();
        biomeDataManager = main.getDataManager().getBiomeDataManager();
        messageManager = main.getMessageManager();
        this.playerData = playerData;
        logger = main.getCustomLogger();
        uuid = playerData.getPlayer().getUniqueId();
        player = playerData.getPlayer();
        enabledBiomes = Collections.unmodifiableSet(new HashSet<>(configManager.getEnabledBiomes()));
    }

    @Override
    public CompletableFuture<PlayerData> readData() {
        CompletableFuture<Map<Biome, AsyncData>> completableFuture = CompletableFuture.supplyAsync(this::getAsync);

        return completableFuture
                .thenAcceptAsync(this::setBiomeLevelData, BukkitExecutor.sync(main))
                .thenApplyAsync(ignored -> playerData);
    }

    private Map<Biome, AsyncData> getAsync() {
        Map<Biome, AsyncData> loadedData = new HashMap<>();

        try (Connection connection = dataManager.getDatabase().getDatabase().getConnection()) {
            for (Biome biome : enabledBiomes) {
                String table = biome.name();

                //Check it exists, and if not, create entry
                try (PreparedStatement existsStatement = connection.prepareStatement("SELECT LEVEL, PROGRESS FROM " + table + " WHERE UUID=?")) {
                    existsStatement.setString(1, uuid.toString());
                    ResultSet results = existsStatement.executeQuery();

                    if (!results.next()) {
                        // Create new
                        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table + " VALUES (?,?,?);")) {
                            insertStatement.setString(1, uuid.toString());
                            insertStatement.setInt(2, 0);
                            insertStatement.setInt(3, 0);
                            insertStatement.executeUpdate();
                        }
                        loadedData.put(biome, new AsyncData(0, 0));
                    } else {
                        int level = results.getInt("LEVEL");
                        int progress = results.getInt("PROGRESS");
                        loadedData.put(biome, new AsyncData(level,progress));
                    }
                }
            }
        } catch (SQLException e) {
            throw new CompletionException(e);
        }

        return loadedData;
    }

    private void setBiomeLevelData(Map<Biome, AsyncData> loadedData) {
        for (Map.Entry<Biome, AsyncData> entry : loadedData.entrySet()) {
            Biome biome = entry.getKey();
            AsyncData data = entry.getValue();

            BiomeLevel biomeLevel = new BiomeLevel(
                    player,
                    biomeDataManager.getBiomeData(biome),
                    data.getLevel(),
                    data.getProgress()
            );

            playerData.addBiomeLevel(biome, biomeLevel);
            if (configManager.isDebugMode()) Utils.debugMsg(player.getName(),
                    ChatColor.GREEN + biome.name() + " data set (" + data.getLevel() + ":" + data.getProgress() + ")");
        }
    }

    @Override
    public void saveData(boolean async) {
        if (async) saveAsync(player.getName());
        else saveSync();
    }

    private void saveSync(){
        try (Connection connection = dataManager.getDatabase().getDatabase().getConnection()) {
            for (Biome biome : configManager.getEnabledBiomes()) {
                String table = biome.name();

                try (PreparedStatement saveStatement = connection.prepareStatement("UPDATE " + table + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                    saveStatement.setInt(1, playerData.getBiomeLevel(biome).getLevel());
                    saveStatement.setLong(2, playerData.getBiomeLevel(biome).getProgress());
                    saveStatement.setString(3, uuid.toString());
                    saveStatement.executeUpdate();
                }
            }

        } catch (SQLException error) {
            logger.logToFile(error, messageManager.getWithPlaceholder(Message.DATASAVEERROR, player.getName()));
        }
    }

    private void saveAsync(final String playerName) {
        Map<Biome, AsyncData> loadedData = new HashMap<>();
        for (Biome biome : configManager.getEnabledBiomes()) {
            BiomeLevel level = playerData.getBiomeLevel(biome);
            loadedData.put(biome, new AsyncData(level.getLevel(), (int) level.getProgress()));
        }

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try (Connection connection = dataManager.getDatabase().getDatabase().getConnection()) {
                for (Map.Entry<Biome, AsyncData> entry : loadedData.entrySet()) {
                    String table = entry.getKey().name();
                    AsyncData data = entry.getValue();

                    try (PreparedStatement saveStatement = connection.prepareStatement("UPDATE " + table + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                        saveStatement.setInt(1, data.getLevel());
                        saveStatement.setLong(2, data.getProgress());
                        saveStatement.setString(3, uuid.toString());
                        saveStatement.executeUpdate();
                    }
                }

            } catch (SQLException error) {
                logger.logToFile(error, main.getMessageManager().getWithPlaceholder(Message.DATASAVEERROR, playerName));
            }
        });
    }
}
