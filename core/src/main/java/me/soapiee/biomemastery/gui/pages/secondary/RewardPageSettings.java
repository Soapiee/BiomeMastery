package me.soapiee.biomemastery.gui.pages.secondary;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.core.Icon;
import me.soapiee.biomemastery.gui.core.IconFactory;
import me.soapiee.biomemastery.gui.core.Path;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RewardPageSettings {

    protected final BiomeMastery main;
    protected final CustomLogger customLogger;
    protected final MessageManager messageManager;
    protected FileConfiguration config;
    protected final IconFactory iconFactory;

    @Getter protected String title;
    @Getter protected int size;

    @Getter private Icon rewardIcon;
    @Getter private Set<Integer> rewardSlots = new HashSet<>();
    @Getter private Icon infoIcon;
    @Getter private Icon fillerIcon;
    @Getter private Set<Integer> fillerSlots = new HashSet<>();

    public RewardPageSettings(BiomeMastery main, IconFactory iconFactory) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        config = main.getConfig();
        this.iconFactory = iconFactory;
        load(Bukkit.getConsoleSender());
    }

    public void load(CommandSender sender) {
        title = readTitle(Path.REWARD_TITLE);
        size = readSize(Path.REWARD_SIZE);
        rewardIcon = iconFactory.createIcon(Path.REWARD_ICON, sender);
        rewardSlots = new HashSet<>();
        fillAllowedSlots(Path.REWARD_ICON, rewardSlots, sender);
        infoIcon = iconFactory.createIcon(Path.REWARD_INFO, sender);
        fillerIcon = iconFactory.createIcon(Path.REWARD_FILLER, sender);
        fillerSlots = new HashSet<>();
        fillAllowedSlots(Path.REWARD_FILLER, fillerSlots, sender);
    }

    public void reload(CommandSender sender) {
        config = main.getConfig();
        load(sender);
    }

    private String readTitle(Path rawPath) {
        String path = rawPath.getPath();
        return config.getString(path, " ");
    }

    private int readSize(Path rawPath) {
        String path = rawPath.getPath();
        int configSize = config.getInt(path, 54);
        return configSize % 9 == 0 ? configSize : 54;
    }

    private void fillAllowedSlots(Path rawPath, Set<Integer> set, CommandSender sender) {
        String path = rawPath.getPath() + ".slot";
        List<Integer> list = config.getIntegerList(path);

        if (!config.isSet(path) || list.isEmpty()) {
            customLogger.logToPlayer(sender, null, messageManager.getWithPlaceholder(Message.INVALIDGUISLOT, path.split("\\.")[2]));
            set.add(-1);
            return;
        }

        set.addAll(list);
    }
}
