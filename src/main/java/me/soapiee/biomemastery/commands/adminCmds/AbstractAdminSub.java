package me.soapiee.biomemastery.commands.adminCmds;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.commands.SubCmd;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.*;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractAdminSub implements SubCmd {

    protected final BiomeMastery main;
    protected final MessageManager messageManager;
    protected final ConfigManager configManager;
    protected final BiomeDataManager biomeDataManager;
    protected final PendingRewardsManager pendingRewardsManager;

    protected final String PERMISSION;
    protected final int MIN_ARGS;
    protected final int MAX_ARGS;

    public AbstractAdminSub(BiomeMastery main, String PERMISSION, int MIN_ARGS, int MAX_ARGS) {
        this.main = main;
        this.messageManager = main.getMessageManager();
        DataManager dataManager = main.getDataManager();
        this.configManager = dataManager.getConfigManager();
        this.biomeDataManager = dataManager.getBiomeDataManager();
        this.pendingRewardsManager = main.getDataManager().getPendingRewardsManager();

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

        return true;
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

    protected void updateProgress(OfflinePlayer target, BiomeLevel biomeLevel) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();

        if (!configManager.isEnabledBiome(locBiome)) return;

        biomeLevel.updateProgress(locBiome);
    }

    protected void sendMessage(CommandSender sender, String message){
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    protected void sendAdminUpdateMsg(CommandSender sender, OfflinePlayer target, String message){
        if (target.isOnline())
            if (sender instanceof ConsoleCommandSender || sender instanceof Player && sender != target.getPlayer())
                sendMessage(target.getPlayer(), message);
    }

    protected OfflinePlayer getTarget(CommandSender sender, String input) {
        OfflinePlayer target = main.getPlayerCache().getOfflinePlayer(input);

        if (target == null) sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERNOTFOUND, input));

        return target;
    }

    protected Biome getBiome(CommandSender sender, String input) {
        Biome biome;
        try {
            biome = Biome.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException error) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, input));
            return null;
        }

        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.DISABLEDBIOME, input));
            return null;
        }

        return biome;
    }

    protected int getValue(CommandSender sender, String input){
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException error) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, input));
            return -1;
        }

        if (value < 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNEGNUMBER, input));
            return -1;
        }

        return value;
    }
}
