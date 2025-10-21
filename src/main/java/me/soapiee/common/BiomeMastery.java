package me.soapiee.common;

import me.soapiee.common.commands.AdminCmd;
import me.soapiee.common.commands.UsageCmd;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.hooks.PlaceHolderAPIHook;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.listeners.PlayerListener;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.sql.SQLException;

public final class BiomeMastery extends JavaPlugin {

    private DataManager dataManager;
    private MessageManager messageManager;
    private PlayerCache playerCache;
    private VaultHook vaultHook;
    private Logger logger;
    private PlayerListener playerListener;
    private boolean debugMode;


    // TODO:
    // Every X time,
    //    Player is checked if they're within a valid biome.
    //    If true: players progress is checked. Progress = threshold - (seconds between now + entry time).
    //    If progress is <=0 then they level up and a new entry time is set.
    //
    //
    // A list of enabled biomes is created on server load
    //    (if blacklist, all biomes are added, then the blacklisted ones are removed)
    //    (if whitelist is used, only those in the whitelist are added)
    //
    //
    // On player join,
    //     Biome is established. World + Biome are checked against enabled worlds + biomes list.
    //     If its a valid biome then entry time is recorded.
    //
    // During the biome change event,
    //    Player is checked if they're within a valid biome.
    //    If true: seconds between now + entry time are added to the players progress
    //    players progress is checked
    //    If progress is <=0 then they level up in the old biome.
    //    A new entry time is set for the new biome and the entry time for the old biome is cleared
    //
    // On player quit,
    //    Player is checked if they're within a valid biome.
    //    If true: seconds between now + entry time are added to the players progress
    //    players progress is checked
    //    If progress is <=0 then they level up in the old biome.
    //    A new entry time is set for the new biome and the entry time for the old biome is cleared


    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerCache = new PlayerCache(Bukkit.getServer().getOfflinePlayers());
        messageManager = new MessageManager(this);
        logger = new Logger(this);
        debugMode = getConfig().getBoolean("debug_mode", false);
        dataManager = new DataManager(getConfig(), messageManager, vaultHook, logger, debugMode);

        try {
            dataManager.initialise(this);
        } catch (SQLException | CommunicationException e) {
            logger.logToFile(e, ChatColor.RED + "Database could not connect. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (IOException e) {
            logger.logToFile(e, ChatColor.RED + "There was an error creating the player data folder. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        dataManager.loadData(this, Bukkit.getConsoleSender());

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) new PlaceHolderAPIHook(this).register();
        if (getServer().getPluginManager().getPlugin("Vault") != null) vaultHook = new VaultHook();
        else vaultHook = null;

        getCommand("admin").setExecutor(new AdminCmd(this));
        getCommand("placeholder").setExecutor(new UsageCmd(this));

        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        // TODO: Enable later
//        UpdateChecker updateChecker = new UpdateChecker(this, 125077);
//        updateChecker.updateAlert(this);
    }

    @Override
    public void onDisable() {
        if (dataManager == null) return;

        dataManager.saveAll(false);

        if (dataManager.getDatabase() != null) {
            dataManager.getDatabase().disconnect();
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public VaultHook getVaultHook() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return null;
        else return vaultHook;
    }

    public Logger getCustomLogger() {
        return logger;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

}
