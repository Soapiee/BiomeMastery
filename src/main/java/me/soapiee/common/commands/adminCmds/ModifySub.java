package me.soapiee.common.commands.adminCmds;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PendingRewardsManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class ModifySub {

    protected final BiomeMastery main;
    protected final ConfigManager configManager;
    protected final BiomeDataManager biomeDataManager;
    protected final PendingRewardsManager pendingRewardsManager;
    protected final MessageManager messageManager;

    public ModifySub(BiomeMastery main) {
        this.main = main;
        this.configManager = main.getDataManager().getConfigManager();
        this.biomeDataManager = main.getDataManager().getBiomeDataManager();
        this.pendingRewardsManager = main.getDataManager().getPendingRewardsManager();
        this.messageManager = main.getMessageManager();
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

    protected void updateProgress(OfflinePlayer target, BiomeLevel biomeLevel) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();
        if (!configManager.isEnabledBiome(locBiome)) return;

        biomeLevel.updateProgress(locBiome);
    }

    private void sendMessage(CommandSender sender, String message){
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.addColour(message));
        else Utils.consoleMsg(message);
    }

    protected void sendAdminUpdateMsg(CommandSender sender, OfflinePlayer target, String message){
        if (target.isOnline())
            if (sender instanceof ConsoleCommandSender || sender instanceof Player && sender != target.getPlayer())
                sendMessage(target.getPlayer(), message);
    }

}
