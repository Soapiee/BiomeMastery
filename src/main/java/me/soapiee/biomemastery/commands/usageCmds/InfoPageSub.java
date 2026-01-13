package me.soapiee.biomemastery.commands.usageCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfoPageSub extends AbstractUsageSub {

    @Getter private final String IDENTIFIER = "infopage";

    public InfoPageSub(BiomeMastery main) {
        super(main, null, 1, 2);
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
        if (!checkRequirements(sender, args, label)) return;

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
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDPAGE, totalPages, args[1]));
            return -1;
        }

        return page;
    }

    public void displayInfo(Player sender, PlayerData playerData, int pageNumber) {
        updateProgress(sender, playerData);
        cmdCooldownManager.addCooldown(sender);

        senderDynamicInfo(sender, playerData, pageNumber);
        sendDynamicPageFooter(sender, pageNumber, calcTotalPages());
    }

    private int calcTotalPages(){
        int maxBiomes = configManager.getBiomesPerPage();
        int enabledBiomesCount = enabledBiomes.size();

        return ((enabledBiomesCount % maxBiomes) == 0 ? (enabledBiomesCount / maxBiomes) : (enabledBiomesCount / maxBiomes) + 1);
    }

    private void senderDynamicInfo(Player sender, PlayerData playerData, int pageNumber){
        sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, sender.getName()));

        int maxBiomes = configManager.getBiomesPerPage();
        int startPoint = (pageNumber * maxBiomes) - (maxBiomes - 1);
        int endPoint = startPoint + maxBiomes;

        ComponentBuilder builder = new ComponentBuilder();

        for (int i = startPoint; i < endPoint; i++) {
            if (i > enabledBiomes.size()) break;

            Biome biome = enabledBiomes.get(i);
            BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
            BiomeData biomeData = biomeDataManager.getBiomeData(biome);

            Message message = Message.BIOMEBASICINFOFORMAT;
            if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEBASICINFOMAX;

            builder.append(createBiomeInfo(messageManager.getWithPlaceholder(message, sender.getName(), biomeData, biomeLevel), biome));
            if (i != endPoint -1) builder.append("\n");
        }

        sender.spigot().sendMessage(builder.create());
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
            TextComponent prevButton = createPrevButton(page);
            if (prevButton != null) builder.append(prevButton);
        }

        //Footer
        builder.append(" ", ComponentBuilder.FormatRetention.NONE);
        String translatedColours = Utils.addColour(message);
        builder.append(TextComponent.fromLegacyText(translatedColours));
        builder.append(" ");

        //Next button
        if (page + 1 <= totalPages) {
            TextComponent nextButton = createNextButton(page);
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

    private TextComponent createPrevButton(int page){
        return createTextComponent(
                messageManager.get(Message.BIOMEBASICINFOPREVBUTTON),
                "/biome info " + (page - 1),
                messageManager.get(Message.PREVBUTTONHOVER)
        );
    }

    private TextComponent createNextButton(int page){
        return createTextComponent(
                messageManager.get(Message.BIOMEBASICINFONEXTBUTTON),
                "/biome info " + (page + 1),
                messageManager.get(Message.NEXTBUTTONHOVER)
        );
    }

    private TextComponent createBiomeInfo(String message, Biome biome){
        return createTextComponent(
                message,
                "/biome info " + biome.name().toLowerCase(),
                messageManager.get(Message.BIOMEBASICINFOHOVER)
        );
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
