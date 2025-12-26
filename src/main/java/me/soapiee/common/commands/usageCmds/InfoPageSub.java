package me.soapiee.common.commands.usageCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.data.BukkitExecutor;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.CmdCooldownManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.CustomLogger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfoPageSub extends InfoSub implements SubCmd {

    @Getter private final String IDENTIFIER = "infopage";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 1;
    @Getter private final int MAX_ARGS = 2;

    public InfoPageSub(BiomeMastery main) {
        super(main);
    }

    // /bm info
    // /bm info [page]
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.MUSTBEPLAYERERROR)));
            return;
        }

        Player player = (Player) sender;

        if (!checkRequirements(sender, main, args, label)) return;
        sendMessage(sender, "&aPassed requirements");

        int pageNumber = getPageNumber(sender, args);
        if (pageNumber == -1) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(player)
                .thenAcceptAsync(data -> displayInfo(player, data, pageNumber), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.onlyLogToPlayer(sender, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private int getPageNumber(CommandSender sender, String[] args) {
        if (args.length == 1) return 1;

        int page = Integer.parseInt(args[1]);
        int enabledBiomesCount = enabledBiomes.size();
        int maxBiomes = configManager.getBiomesPerPage();
        int totalPages = ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);

        if (page < 1 || page > totalPages) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDPAGE, page, totalPages));
            return -1;
        }

        return page;
    }

    public void displayInfo(Player sender, PlayerData playerData, int pageNumber) {
        CmdCooldownManager cmdCooldownManager = main.getDataManager().getCooldownManager();
        if (hasCooldown(sender, cmdCooldownManager, messageManager)) return;

        updateProgress(sender, playerData, configManager);
        cmdCooldownManager.addCooldown(sender);

        sendMessage(sender, createInfoString(sender, playerData, pageNumber));
        sendDynamicPageFooter(sender, pageNumber, calcTotalPages());
    }

    private int calcTotalPages(){
        int maxBiomes = configManager.getBiomesPerPage();
        int enabledBiomesCount = enabledBiomes.size();

        return ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);
    }

    private String createInfoString(Player sender, PlayerData playerData, int pageNumber){
        StringBuilder builder = new StringBuilder();

        builder.append(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, sender.getName())).append("\n");

        int maxBiomes = configManager.getBiomesPerPage();
        int startPoint = (pageNumber * maxBiomes) - (maxBiomes - 1);
        int endPoint = startPoint + maxBiomes;

        for (int i = startPoint; i < endPoint; i++) {
            if (i > enabledBiomes.size()) break;

            Biome biome = enabledBiomes.get(i);
            BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
            BiomeData biomeData = biomeDataManager.getBiomeData(biome);

            Message message = Message.BIOMEBASICINFOFORMAT;
            if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEBASICINFOMAX;

            builder.append(messageManager.getWithPlaceholder(message, sender.getName(), biomeData, biomeLevel));
            builder.append("\n");
        }

        return builder.toString();
    }

    private void sendDynamicPageFooter(CommandSender sender, int page, int totalPages) {
        String message = Utils.addColour(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOFOOTER, page, totalPages));

        if (!(sender instanceof Player)) {
            sendMessage(sender, message);
            return;
        }

        ComponentBuilder builder = new ComponentBuilder();

        //Previous button
        if (page - 1 > 0) {
            TextComponent prevButton = createTextComponent(
                    messageManager.get(Message.BIOMEBASICINFOPREVBUTTON),
                    "/biome info " + (page - 1),
                    messageManager.get(Message.PREVBUTTONHOVER)
            );

            if (prevButton != null) builder.append(prevButton);
        }

        //Footer
        builder.append(" ", ComponentBuilder.FormatRetention.NONE);
        String translatedColours = Utils.addColour(message);
        builder.append(TextComponent.fromLegacyText(translatedColours));
        builder.append(" ");

        //Next button
        if (page + 1 <= totalPages) {
            TextComponent nextButton = createTextComponent(
                    messageManager.get(Message.BIOMEBASICINFONEXTBUTTON),
                    "/biome info " + (page + 1),
                    messageManager.get(Message.NEXTBUTTONHOVER)
            );

            if (nextButton != null) builder.append(nextButton);
        }

        sender.spigot().sendMessage(builder.create());
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
