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
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfoPlayerSub extends InfoSub implements SubCmd {

    @Getter private final String IDENTIFIER = "infoplayer";
    @Getter private final String PERMISSION = "biomemastery.player.others";
    @Getter private final int MIN_ARGS = 2;
    @Getter private final int MAX_ARGS = 3;

    public InfoPlayerSub(BiomeMastery main) {
        super(main);
    }

    // /bm info [player]
    // /bm info [player] [page]
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, main, args, label)) return;

        OfflinePlayer target = getTarget(sender, args);
        if (target == null) return;

        int pageNumber = getPageNumber(sender, args);
        if (pageNumber == -1) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad(target)
                .thenAcceptAsync(data -> displayInfo(sender, target, data, pageNumber), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.onlyLogToPlayer(sender, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private OfflinePlayer getTarget(CommandSender sender, String[] args) {
        OfflinePlayer target = main.getPlayerCache().getOfflinePlayer(args[1]);

        if (target == null) sendMessage(sender, messageManager.getWithPlaceholder(Message.PLAYERNOTFOUND, args[2]));

        return target;
    }

    private int getPageNumber(CommandSender sender, String[] args) {
        if (args.length == 2) return 1;

        int page;
        try {
            page = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, args[2]));
            return -1;
        }

        int enabledBiomesCount = enabledBiomes.size();
        int maxBiomes = configManager.getBiomesPerPage();
        int totalPages = ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);

        if (page < 1 || page > totalPages) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDPAGE, page, totalPages));
            return -1;
        }

        return page;
    }

    public void displayInfo(CommandSender sender, OfflinePlayer target, PlayerData playerData, int pageNumber) {
        CmdCooldownManager cmdCooldownManager = main.getDataManager().getCooldownManager();
        if (hasCooldown(sender, cmdCooldownManager, messageManager)) return;

        updateProgress(target, playerData, configManager);
        cmdCooldownManager.addCooldown(sender);

        sendMessage(sender, createInfoString(target, playerData, pageNumber));
        sendDynamicPageFooter(sender, pageNumber, calcTotalPages(), target);
    }

    private int calcTotalPages(){
        int maxBiomes = configManager.getBiomesPerPage();
        int enabledBiomesCount = enabledBiomes.size();

        return ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);
    }

    private String createInfoString(OfflinePlayer target, PlayerData playerData, int pageNumber){
        StringBuilder builder = new StringBuilder();

        builder.append(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, target.getName())).append("\n");

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

            builder.append(messageManager.getWithPlaceholder(message, target.getName(), biomeData, biomeLevel));
            builder.append("\n");
        }

        return builder.toString();
    }

    private void sendDynamicPageFooter(CommandSender sender, int page, int totalPages, OfflinePlayer target) {
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
                    "/biome info " + target.getName() + " " + (page - 1),
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
                    "/biome info " + target.getName() + " " + (page + 1),
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
