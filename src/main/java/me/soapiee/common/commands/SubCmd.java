package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.CmdCooldownManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface SubCmd {
    
    String getIDENTIFIER();
    String getPERMISSION();
    int getMIN_ARGS();
    int getMAX_ARGS();
    void execute(CommandSender sender, String label, String[] args);
    List<String> getTabCompletions(String[] args);

    default void sendMessage(CommandSender sender, String message){
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    default boolean checkRequirements(CommandSender sender, BiomeMastery main, String[] args, String label) {
        MessageManager messageManager = main.getMessageManager();
        if (!checkPermission(sender, getPERMISSION())) {
            sendMessage(sender, messageManager.get(Message.NOPERMISSION));
            return false;
        }

        if (!checkArgs(args)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
            return false;
        }

        CmdCooldownManager cmdCooldownManager = main.getDataManager().getCooldownManager();
        return !hasCooldown(sender, cmdCooldownManager, messageManager);
    }

    default boolean checkPermission(CommandSender sender, String permission){
        if (permission == null) return true;
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        return player.hasPermission(permission);
    }

    default boolean checkArgs(String[] args){
        if (getMIN_ARGS() == -1 && getMAX_ARGS() == -1) return true;

        if (args.length < getMIN_ARGS()) return false;
        return !(args.length > getMAX_ARGS());
    }

    default void updateProgress(OfflinePlayer target, PlayerData data, ConfigManager configManager) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();

        if (!configManager.isEnabledBiome(locBiome)) return;

        BiomeLevel biomeLevel = data.getBiomeLevel(locBiome);
        biomeLevel.updateProgress(locBiome);
    }

    default boolean hasCooldown(CommandSender sender, CmdCooldownManager cmdCooldownManager, MessageManager messageManager) {
        if (!(sender instanceof Player)) return false;

        int cooldown = (int) cmdCooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return true;
        }

        return false;
    }
}
