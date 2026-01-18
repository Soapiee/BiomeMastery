package me.soapiee.biomemastery.commands.adminCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReloadSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "reload";

    public ReloadSub(BiomeMastery main) {
        super(main, "biomemastery.reload", 1, 1);
    }

    // /abm reload
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

        reload(sender);
    }

    private void reload(CommandSender sender){
        sendMessage(sender, messageManager.get(Message.RELOADINPROGRESS));
        String reloadOutcome = messageManager.get(Message.RELOADSUCCESS);

        boolean errors = false;
        main.getDataManager().reloadData(main);
        if (!messageManager.load(sender)) errors = true;

        if (errors) reloadOutcome = messageManager.get(Message.RELOADERROR);

        if (sender instanceof Player) {
            Utils.consoleMsg(messageManager.getPrefix(Message.RELOADSUCCESS) + ChatColor.GOLD + sender.getName() + reloadOutcome.replace("[BM]", ""));
        }

        sendMessage(sender, reloadOutcome);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
