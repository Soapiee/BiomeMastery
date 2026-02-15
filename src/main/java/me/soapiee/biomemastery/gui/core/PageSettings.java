package me.soapiee.biomemastery.gui.core;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PageSettings {

    @Getter private final IconFactory iconFactory;

    @Getter private Icon nextPageIcon;
    @Getter private Icon prevPageIcon;
    @Getter private Icon closeIcon;

    public PageSettings(IconFactory iconFactory) {
        this.iconFactory = iconFactory;
        load(Bukkit.getConsoleSender());
    }

    public void reload(CommandSender sender) {
        load(sender);
    }

    private void load(CommandSender sender){
        nextPageIcon = iconFactory.createIcon(Path.NEXT_PAGE, sender);
        prevPageIcon = iconFactory.createIcon(Path.PREV_PAGE, sender);
        closeIcon = iconFactory.createIcon(Path.CLOSE, sender);
    }
}
