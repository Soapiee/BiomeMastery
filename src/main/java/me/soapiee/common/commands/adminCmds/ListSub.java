package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListSub extends AbstractAdminSub{

    @Getter private final String IDENTIFIER = "list";

    public ListSub(BiomeMastery main) {
        super(main, null, 1, 2);
    }

    // /abm list worlds
    // /abm list biomes
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

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
