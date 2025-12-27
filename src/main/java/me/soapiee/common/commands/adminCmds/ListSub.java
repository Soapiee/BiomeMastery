package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ListSub extends AbstractAdminSub {

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
        switch (listType) {
            case "worlds":
                if (sender instanceof Player) listWorldsPlayer((Player) sender);
                else listWorldsConsole(sender);
                break;
            case "biomes":
                if (sender instanceof Player) listBiomesPlayer((Player) sender);
                else listBiomesConsole(sender);
                break;
            default:
                sendMessage(sender, messageManager.getWithPlaceholder(Message.ADMINHELP, label));
        }
    }

    private void listWorldsConsole(CommandSender sender) {
        StringBuilder enabledWorlds = new StringBuilder();
        enabledWorlds.append(messageManager.get(Message.WORLDLISTHEADER));

        for (World world : configManager.getEnabledWorlds()) {
            enabledWorlds.append("\n").append(Utils.capitalise(world.getName()));
        }

        sendMessage(sender, enabledWorlds.toString());
    }

    private void listWorldsPlayer(Player player) {
        ComponentBuilder builder = new ComponentBuilder();
        String header = Utils.addColour(messageManager.get(Message.WORLDLISTHEADER));
        builder.append(TextComponent.fromLegacyText(header));

        String colour = messageManager.get(Message.WORLDTEXTCOLOR);

        for (World world : configManager.getEnabledWorlds()) {
            builder.append("", ComponentBuilder.FormatRetention.NONE)
                    .append("\n")
                    .append(TextComponent.fromLegacyText(Utils.addColour(colour)))
                    .append(getComponentMsg(world.getName()));
        }

        player.spigot().sendMessage(builder.create());
    }

    private void listBiomesConsole(CommandSender sender) {
        StringBuilder enabledBiomes = new StringBuilder();
        enabledBiomes.append(messageManager.get(Message.BIOMELISTHEADER));

        for (Biome biome : configManager.getEnabledBiomes()) {
            enabledBiomes.append("\n").append(Utils.capitalise(biome.name()));
        }

        sendMessage(sender, enabledBiomes.toString());
    }

    private void listBiomesPlayer(Player player) {
        ComponentBuilder builder = new ComponentBuilder();
        String header = Utils.addColour(messageManager.get(Message.WORLDLISTHEADER));
        builder.append(TextComponent.fromLegacyText(header));
        String colour = messageManager.get(Message.BIOMETEXTCOLOR);

        for (Biome biome : configManager.getEnabledBiomes()) {
            builder.append("", ComponentBuilder.FormatRetention.NONE)
                    .append("\n")
                    .append(TextComponent.fromLegacyText(Utils.addColour(colour)))
                    .append(getComponentMsg(Utils.capitalise(biome.name())));
        }

        player.spigot().sendMessage(builder.create());
    }

    private TextComponent getComponentMsg(String string) {
        return createTextComponent(string,
                "/abm disable " + string,
                messageManager.get(Message.DISABLEHOVER));
    }

    private TextComponent createTextComponent(String message, String cmd, String hoverText) {
        if (message == null || message.isEmpty()) return null;

        TextComponent component = new TextComponent("");
        String translatedMessage = Utils.addColour(message);
        for (BaseComponent child : TextComponent.fromLegacyText(translatedMessage)) {
            component.addExtra(child);
        }

        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));

        if (hoverText != null && !hoverText.isEmpty()) {
            String translatedHover = Utils.addColour(hoverText);

            BaseComponent[] hoverComponents = TextComponent.fromLegacyText(translatedHover);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverComponents)));
        }

        return component;
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
