package me.soapiee.biomemastery.commands.adminCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AddProgressSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "addprogress";

    public AddProgressSub(BiomeMastery main) {
        super(main, null, 4, 4);
    }

    // /abm addProgress <player> <biome> <value>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

        OfflinePlayer target = getTarget(sender, args[1]);
        if (target == null) return;

        Biome biome = getBiome(sender, args[2]);
        if (biome == null) return;

        int value = getValue(sender, args[3]);
        if (value == -1) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(target)
                .thenAcceptAsync(data -> addProgress(sender, data, target, biome, value), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private void addProgress(CommandSender sender, PlayerData playerData, OfflinePlayer target, Biome biome, int value){
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        updateProgress(target, biomeLevel);

        if (biomeLevel.isMaxLevel()) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.ADDERROR, target.getName()));
            return;
        }

        Message message = Message.PROGRESSADDED;

        if (value > 0){
            long newProgress = biomeLevel.getProgress() + value;
            long outcome = biomeLevel.setProgress(newProgress);

            if (outcome == -1) message = Message.PROGRESSADDERROR;
            else if (outcome == -2) message = Message.PROGRESSADDEDMAX;

            if (outcome >= 0) sendAdminUpdateMsg(sender, target, messageManager.getWithPlaceholder(Message.ADMINADDEDPROGRESS, value, biomeLevel.getBiomeName()));
        } else message = Message.PROGRESSADDERROR;

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, target.getName(), value, biomeLevel.getBiomeName()));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
