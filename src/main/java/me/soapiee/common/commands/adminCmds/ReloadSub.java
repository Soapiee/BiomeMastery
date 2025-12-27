package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReloadSub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;
    private final DataManager dataManager;

    @Getter private final String IDENTIFIER = "reload";
    @Getter private final String PERMISSION = "biomemastery.reload";
    @Getter private final int MIN_ARGS = 1;
    @Getter private final int MAX_ARGS = 1;

    public ReloadSub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
        dataManager = main.getDataManager();
    }

    // /abm reload
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.MUSTBEPLAYERERROR)));
            return;
        }

        if (!checkRequirements(sender, main, args, label)) return;

        reload(sender);
    }

    private void reload(CommandSender sender){
        sendMessage(sender, messageManager.get(Message.RELOADINPROGRESS));
        String reloadOutcome = messageManager.get(Message.RELOADSUCCESS);

        boolean errors = false;
        dataManager.reloadData(main, dataManager);
        if (!messageManager.load(sender)) errors = true;

        if (errors) reloadOutcome = messageManager.get(Message.RELOADERROR);

        if (sender instanceof Player) {
            if (reloadOutcome != null)
                Utils.consoleMsg(ChatColor.GOLD + sender.getName() + " " + reloadOutcome.replace("[BM] ", ""));
        }

        sendMessage(sender, reloadOutcome);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
