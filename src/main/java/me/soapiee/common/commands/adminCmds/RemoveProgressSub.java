package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BukkitExecutor;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.CustomLogger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RemoveProgressSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "removeprogress";

    public RemoveProgressSub(BiomeMastery main) {
        super(main, null, 4, 4);
    }

    // /abm removeProgress <player> <biome> <value>
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
                .thenAcceptAsync(data -> removeProgress(sender, data, target, biome, value), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private void removeProgress(CommandSender sender, PlayerData playerData, OfflinePlayer target, Biome biome, int value){
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        updateProgress(target, biomeLevel);

        long newProgress = biomeLevel.getProgress() - value;
        long outcome = biomeLevel.setProgress(newProgress);

        Message message = Message.PROGRESSREMOVEERROR;
        if (outcome != -1) message = Message.PROGRESSREMOVED;

        if (outcome >= 0)
            sendAdminUpdateMsg(sender, target, messageManager.getWithPlaceholder(Message.ADMINSETPROGRESS, value, biomeLevel.getBiomeName()));

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, target.getName(), value, biomeLevel.getBiomeName()));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
