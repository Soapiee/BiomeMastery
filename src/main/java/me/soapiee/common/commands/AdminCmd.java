package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger logger;

    public AdminCmd(BiomeMastery main) {
        this.main = main;
        this.playerCache = main.getPlayerCache();
        this.messageManager = main.getMessageManager();
        this.logger = main.getCustomLogger();
    }

    private void sendLoadDataError(CommandSender sender, String playerName, Exception e) {
        logger.logToPlayer(sender, e, messageManager.getWithPlaceholder(Message.DATAERROR, playerName));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!sender.hasPermission("admin.command")) {
            sender.sendMessage(Utils.colour(this.messageManager.get(Message.NOPERMISSION)));
            return true;
        }

//            OfflinePlayer target = this.playerCache.getOfflinePlayer(args[1]);

        if (args.length == 1) {

        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("money");
                results.add("multi");
                results.add("help");
                if (sender instanceof Player && sender.hasPermission("eveconomy.admin")) {
                    results.add("reload");
                }
                break;
            case 2:
                for (final OfflinePlayer player : this.playerCache.getList()) {
                    results.add(player.getName().toLowerCase());
                }
                break;
            case 3:
                results.add("set");
                results.add("add");
                results.add("take");
                results.add("subtract");
                results.add("reset");
        }
        return results.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
