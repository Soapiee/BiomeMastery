package me.soapiee.common.commands.usageCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class GUISub extends AbstractUsageSub {

    @Getter private final String IDENTIFIER = "gui";

    public GUISub(BiomeMastery main) {
        super(main, null, 0, 0);
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
