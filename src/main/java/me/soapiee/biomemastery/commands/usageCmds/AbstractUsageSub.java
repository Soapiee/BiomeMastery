package me.soapiee.biomemastery.commands.usageCmds;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.commands.SubCmd;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.*;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class AbstractUsageSub implements SubCmd {

    protected final BiomeMastery main;
    protected final MessageManager messageManager;
    protected final CustomLogger customLogger;
    protected final ConfigManager configManager;
    protected final BiomeDataManager biomeDataManager;
    protected final CmdCooldownManager cmdCooldownManager;
    protected final PlayerDataManager playerDataManager;
    protected final Map<Integer, BiomeData> orderedBiomeData;

    protected final String PERMISSION;
    protected final int MIN_ARGS;
    protected final int MAX_ARGS;

    public AbstractUsageSub(BiomeMastery main, String PERMISSION, int MIN_ARGS, int MAX_ARGS) {
        this.main = main;
        messageManager = main.getMessageManager();
        customLogger = main.getCustomLogger();
        DataManager dataManager = main.getDataManager();
        configManager = main.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        cmdCooldownManager = dataManager.getCooldownManager();
        playerDataManager = dataManager.getPlayerDataManager();
        orderedBiomeData = biomeDataManager.getBiomeDataOrdered();

        this.PERMISSION = PERMISSION;
        this.MIN_ARGS = MIN_ARGS;
        this.MAX_ARGS = MAX_ARGS;
    }

    public boolean checkRequirements(CommandSender sender, String[] args, String label) {
        if (!checkPermission(sender, PERMISSION)) {
            sendMessage(sender, messageManager.get(Message.NOPERMISSION));
            return false;
        }

        if (!checkArgs(args)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
            return false;
        }

        return !hasCooldown(sender);
    }

    protected boolean checkPermission(CommandSender sender, String permission){
        if (permission == null) return true;
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        return player.hasPermission(permission);
    }

    private boolean checkArgs(String[] args){
        if (MIN_ARGS == -1 && MAX_ARGS == -1) return true;

        if (args.length < MIN_ARGS) return false;
        return !(args.length > MAX_ARGS);
    }

    private boolean hasCooldown(CommandSender sender) {
        if (!(sender instanceof Player)) return false;

        int cooldown = (int) cmdCooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return true;
        }

        return false;
    }

    protected void updateProgress(OfflinePlayer target, PlayerData data) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();

        if (!configManager.isEnabledBiome(locBiome)) return;

        BiomeLevel biomeLevel = data.getBiomeLevel(locBiome);
        biomeLevel.updateProgress(locBiome);
    }

    protected void sendMessage(CommandSender sender, String message){
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

}
