package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListSub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    @Getter private final String IDENTIFIER = "list";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 1;
    @Getter private final int MAX_ARGS = 2;

    public ListSub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
        configManager = main.getDataManager().getConfigManager();
    }

    // /abm list worlds
    // /abm list biomes
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, main, args, label)) return;

        String listType = args[1];
        switch (listType){
            case "worlds":
                listWorlds(sender);
                break;
            case "biomes":
                listBiomes(sender);
                break;
            default:
                sendMessage(sender, messageManager.getWithPlaceholder(Message.ADMINHELP, label));
        }
    }

    private void listWorlds(CommandSender sender) {
        StringBuilder enabledWorlds = new StringBuilder();
        enabledWorlds.append(messageManager.get(Message.WORLDLISTHEADER));

        for (World world : configManager.getEnabledWorlds()) {
            enabledWorlds.append("\n").append(Utils.capitalise(world.getName())).append(", ");
        }

        try {
            enabledWorlds.deleteCharAt(enabledWorlds.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sendMessage(sender, enabledWorlds.toString());
    }

    private void listBiomes(CommandSender sender) {
        StringBuilder enabledBiomes = new StringBuilder();
        enabledBiomes.append(messageManager.get(Message.BIOMELISTHEADER));

        for (Biome biome : configManager.getEnabledBiomes()) {
            enabledBiomes.append("\n").append(Utils.capitalise(biome.name())).append(", ");
        }

        try {
            enabledBiomes.deleteCharAt(enabledBiomes.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sendMessage(sender, enabledBiomes.toString());
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
