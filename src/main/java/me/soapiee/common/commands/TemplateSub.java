package me.soapiee.common.commands;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BukkitExecutor;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.CustomLogger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TemplateSub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;

    @Getter private final String IDENTIFIER = "TEMPLATE";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 1;
    @Getter private final int MAX_ARGS = 2;

    public TemplateSub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
    }

    // /bm
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.MUSTBEPLAYERERROR)));
            return;
        }

        if (!checkRequirements(sender, main, args, label)) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad((Player) sender)
                .thenAcceptAsync(data -> METHOD(data), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.onlyLogToPlayer(sender, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private void METHOD(PlayerData playerData){

    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
