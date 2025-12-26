package me.soapiee.common.commands.usageCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class GUISub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;

    @Getter private final String IDENTIFIER = "gui";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 0;
    @Getter private final int MAX_ARGS = 0;

    public GUISub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        //TODO: Open gui for player
//        if (!checkRequirements(args, sender, main)) return;
        // updateProgress((Player) sender);
        // sendMessage(player, messageManager.get(Message.GUIOPENED));

        sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();

    }
}
