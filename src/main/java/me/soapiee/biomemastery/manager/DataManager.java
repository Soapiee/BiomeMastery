package me.soapiee.biomemastery.manager;

import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.HikariCPConnection;
import me.soapiee.biomemastery.logic.ProgressChecker;
import me.soapiee.biomemastery.logic.rewards.RewardFactory;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class DataManager {

    @Getter private final PlayerDataManager playerDataManager;
    @Getter private final PendingRewardsManager pendingRewardsManager;
    @Getter private final CmdCooldownManager cooldownManager;
    @Getter private final ConfigManager configManager;
    @Getter private BiomeDataManager biomeDataManager;
    @Getter private EffectsManager effectsManager;
    @Getter private RewardFactory rewardFactory;
    @Getter private HikariCPConnection database;
    private final CustomLogger customLogger;
    private final MessageManager messageManager;

    private ProgressChecker progressChecker;

    public DataManager(BiomeMastery main) {
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();

        playerDataManager = new PlayerDataManager(main);
        configManager = new ConfigManager(main);
        checkDirectory(main);
        cooldownManager = new CmdCooldownManager(main, configManager);
        pendingRewardsManager = new PendingRewardsManager(main, biomeDataManager);
    }

    private void checkDirectory(BiomeMastery main) {
        if (Files.isDirectory(Paths.get(main.getDataFolder() + File.separator + "Data"))) return;

        try {
            Files.createDirectories(Paths.get(main.getDataFolder() + File.separator + "Data"));
        } catch (IOException e) {
            customLogger.logToFile(e, messageManager.get(Message.FILEFOLDERERROR));
        }
    }

    public void initialise(BiomeMastery main) throws IOException {
        FileConfiguration mainConfig = main.getConfig();

        if (configManager.isDatabaseEnabled()) {
            try {
                initialiseDatabase(mainConfig);
                Utils.consoleMsg(messageManager.get(Message.DATABASECONNECTED));
            } catch (SQLException | HikariPool.PoolInitializationException e) {
                customLogger.logToFile(e, messageManager.get(Message.DATABASEFAILED));
                initialiseFiles(main);
            }

        } else initialiseFiles(main);
    }

    private void initialiseDatabase(FileConfiguration config) throws SQLException, IOException {
        database = new HikariCPConnection(config);
        database.connect(configManager.getEnabledBiomes());
    }

    public void initialiseFiles(BiomeMastery main) throws IOException {
        configManager.setDatabaseEnabled(false);
        database = null;

        Files.createDirectories(Paths.get(main.getDataFolder() + File.separator + "Data" + File.separator + "BiomeLevels"));
        Utils.consoleMsg(messageManager.get(Message.FILESYSTEMACTIVATED));
    }

    public void initialiseRewards(BiomeMastery main) {
        effectsManager = new EffectsManager(main);
        rewardFactory = new RewardFactory(main, playerDataManager, effectsManager);
        configManager.setUpDefaultRewards(rewardFactory);
    }

    public void initialiseBiomeData(FileConfiguration mainConfig) {
        biomeDataManager = new BiomeDataManager(configManager, rewardFactory, mainConfig);
    }

    public void reloadData(BiomeMastery main) {
        configManager.reload(main, this);
        startChecker(main);
    }

    public void startChecker(BiomeMastery main) {
        if (progressChecker != null)
            try {
                progressChecker.cancel();
            } catch (IllegalStateException ignored) {
            }

        progressChecker = new ProgressChecker(main, this);
    }

    public void saveAll() {
        playerDataManager.saveAll(false);
        cooldownManager.save();
        pendingRewardsManager.save();
    }

}
