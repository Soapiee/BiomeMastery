package me.soapiee.biomemastery;

import lombok.Getter;
import me.soapiee.biomemastery.commands.AdminCmd;
import me.soapiee.biomemastery.commands.UsageCmd;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.hooks.PlaceHolderAPIHook;
import me.soapiee.biomemastery.hooks.VaultHook;
import me.soapiee.biomemastery.listeners.*;
import me.soapiee.biomemastery.manager.*;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.PlayerCache;
import me.soapiee.biomemastery.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;

public class BiomeMastery extends JavaPlugin {

    @Getter private PlayerCache playerCache;
    @Getter private MessageManager messageManager;
    @Getter private CustomLogger customLogger;
    @Getter private ConfigManager configManager;
    @Getter private GUIManager guiManager;
    @Getter private ConfigGUIManager configGUIManager;
    @Getter private DataManager dataManager;
    private VaultHook vaultHook;
    @Getter private EffectsListener effectsListener;
    @Getter private UpdateManager updateChecker;

    public BiomeMastery() {
        super();
    }

    //MockedBukkit
    public BiomeMastery(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerCache = new PlayerCache(Bukkit.getServer().getOfflinePlayers());
        messageManager = new MessageManager(this);
        customLogger = new CustomLogger(this);
        configGUIManager = new ConfigGUIManager(this);
        configManager = new ConfigManager(this);
        guiManager = new GUIManager();

        dataManager = new DataManager(this);

        try {
            dataManager.initialise(this);
        } catch (IOException e) {
            customLogger.logToFile(e, messageManager.get(Message.MAJORDATAERROR));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        registerHooks();
        new Metrics(this, 28832);

        effectsListener = new EffectsListener(this);
        getServer().getPluginManager().registerEvents(effectsListener, this);

        dataManager.initialiseRewards(this);
        dataManager.initialiseBiomeData(this);
        dataManager.startChecker(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new PotionRemovalListener(dataManager.getPlayerDataManager()), this);
        getServer().getPluginManager().registerEvents(new LevelUpListener(configManager, messageManager, customLogger, dataManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(guiManager), this);

        getCommand("abiomemastery").setExecutor(new AdminCmd(this));
        getCommand("biomemastery").setExecutor(new UsageCmd(this));

        updateChecker = new UpdateManager(this, 130906);
        updateChecker.updateAlert(Bukkit.getConsoleSender());
    }

    @Override
    public void onDisable() {
        if (dataManager == null) return;

        PlayerDataManager playerDataManager = dataManager.getPlayerDataManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

            if (playerData == null) continue;
            if (playerData.hasActiveRewards()) playerData.clearActiveRewards();
        }

        dataManager.saveAll();
        if (dataManager.getDatabase() != null) dataManager.getDatabase().disconnect();
    }

    public VaultHook getVaultHook() {
        return (getServer().getPluginManager().getPlugin("Vault") == null) ? null : vaultHook;
    }

    private void registerHooks() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceHolderAPIHook(this).register();
            Utils.consoleMsg(messageManager.get(Message.HOOKEDPLACEHOLDERAPI));
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook();
            Utils.consoleMsg(messageManager.get(Message.HOOKEDVAULT));
        } else {
            vaultHook = null;
            Utils.consoleMsg(messageManager.get(Message.HOOKEDVAULTERROR));
        }
    }
}
