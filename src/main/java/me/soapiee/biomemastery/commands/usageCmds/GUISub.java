package me.soapiee.biomemastery.commands.usageCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.gui.pages.main.BiomePage;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GUISub extends AbstractUsageSub {

    @Getter private final String IDENTIFIER = "gui";
    private final String PERMISSION = "biomemastery.player.gui";

    private final GUIManager guiManager;
    private final int totalPages;
    private final int maxPerPage;

    public GUISub(BiomeMastery main) {
        super(main, null, 0, 0);
        guiManager = main.getGuiManager();
        maxPerPage = main.getConfigGUIManager().getBiomePageSettings().getBiomeSlots().size();
        totalPages = calculateTotalPages();
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.MUSTBEPLAYERERROR)));
            return;
        }

        if (!checkPermission(sender, PERMISSION)) {
            sendMessage(sender, messageManager.get(Message.NOPERMISSION));
            return;
        }

        Player player = (Player) sender;

        playerDataManager.getOrLoad(player)
                .thenAcceptAsync(data -> {
                    updateProgress(player, data);
                    guiManager.openGUI(new BiomePage(main, player, 1, totalPages), player);
                }, BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    customLogger.logToPlayer(sender, error, Utils.addColour(messageManager.get(Message.DATAERRORPLAYER)));
                    return null;
                });
    }

    private int calculateTotalPages(){
        int totalPages = orderedBiomeData.size() / maxPerPage;
        return (orderedBiomeData.size() % maxPerPage == 0 ? totalPages : totalPages + 1);
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();

    }
}
