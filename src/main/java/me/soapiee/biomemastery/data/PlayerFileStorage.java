package me.soapiee.biomemastery.data;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.BiomeDataManager;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PlayerFileStorage implements PlayerStorageHandler {

    private final BiomeMastery main;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PlayerData playerData;
    private final CustomLogger customLogger;
    private final Object fileLock = new Object();
    private final Set<Biome> enabledBiomes;

    private final File file;
    private final OfflinePlayer player;

    public PlayerFileStorage(BiomeMastery main, PlayerData playerData) {
        this.main = main;
        configManager = main.getDataManager().getConfigManager();
        biomeDataManager = main.getDataManager().getBiomeDataManager();
        this.playerData = playerData;
        customLogger = main.getCustomLogger();
        player = playerData.getPlayer();
        enabledBiomes = Collections.unmodifiableSet(new HashSet<>(configManager.getEnabledBiomes()));

        UUID uuid = playerData.getPlayer().getUniqueId();
        file = new File(main.getDataFolder() + File.separator + "Data" + File.separator + "BiomeLevels", uuid + ".yml");
    }

    @Override
    public CompletableFuture<PlayerData> readData() {
        CompletableFuture<Map<Biome, AsyncData>> completableFuture =
                (file.exists() ? CompletableFuture.supplyAsync(this::getAsync) : CompletableFuture.supplyAsync(this::createFile));

        return completableFuture
                .thenAcceptAsync(this::setBiomeLevelData, BukkitExecutor.sync(main))
                .thenApplyAsync(ignored -> playerData);
    }

    private Map<Biome, AsyncData> getAsync() {
        Map<Biome, AsyncData> loadedData = new HashMap<>();

        try {
            YamlConfiguration contents = YamlConfiguration.loadConfiguration(file);
            boolean updated = false;

            synchronized (fileLock) {
                for (Biome biome : enabledBiomes) {
                    String levelPath = biome.name() + ".Level";
                    String progressPath = biome.name() + ".Progress";

                    if (!contents.isSet(levelPath) || !contents.isSet(progressPath)) {
                        contents.set(levelPath, 0);
                        contents.set(progressPath, 0);
                        updated = true;
                        loadedData.put(biome, new AsyncData(0, 0));
                    } else {
                        int level = contents.getInt(levelPath);
                        int progress = contents.getInt(progressPath);
                        loadedData.put(biome, new AsyncData(level, progress));
                    }
                }

                if (updated) contents.save(file);
            }
        } catch (IOException error) {
            throw new CompletionException(error);
        }

        return loadedData;
    }

    private Map<Biome, AsyncData> createFile() {
        Map<Biome, AsyncData> loadedData = new HashMap<>();

        synchronized (fileLock) {
            try {
                YamlConfiguration localCopy = new YamlConfiguration();

                for (Biome biome : enabledBiomes) {
                    String biomeName = biome.name();
                    localCopy.set(biomeName + ".Level", 0);
                    localCopy.set(biomeName + ".Progress", 0);
                    loadedData.put(biome, new AsyncData(0, 0));
                }

                localCopy.save(file);
            } catch (IOException error) {
                throw new CompletionException(error);
            }
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

    private void saveSync() {
        YamlConfiguration localCopy = YamlConfiguration.loadConfiguration(file);

        synchronized (fileLock) {
            for (Biome biomeKey : enabledBiomes) {
                String biome = biomeKey.name();
                BiomeLevel level = playerData.getBiomeLevel(biomeKey);
                localCopy.set(biome + ".Level", level.getLevel());
                localCopy.set(biome + ".Progress", level.getProgress());
            }
        }

        try {
            localCopy.save(file);
        } catch (IOException e) {
            customLogger.logToFile(e, main.getMessageManager().getWithPlaceholder(Message.DATAERROR, player.getName()));
        }
    }

    private void saveAsync(final String playerName) {
        Map<Biome, AsyncData> loadedData = new HashMap<>();
        for (Biome biome : enabledBiomes) {
            BiomeLevel level = playerData.getBiomeLevel(biome);
            loadedData.put(biome, new AsyncData(level.getLevel(), (int) level.getProgress()));
        }

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            YamlConfiguration contents = YamlConfiguration.loadConfiguration(file);

            synchronized (fileLock) {
                for (Map.Entry<Biome, AsyncData> entry : loadedData.entrySet()) {
                    AsyncData data = entry.getValue();
                    String biome = entry.getKey().name();

                    contents.set(biome + ".Level", data.getLevel());
                    contents.set(biome + ".Progress", data.getProgress());
                }
            }

            try {
                contents.save(file);
            } catch (IOException e) {
                customLogger.logToFile(e, main.getMessageManager().getWithPlaceholder(Message.DATAERROR, playerName));
            }
        });
    }
}
