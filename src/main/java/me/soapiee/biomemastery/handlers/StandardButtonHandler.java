package me.soapiee.biomemastery.handlers;

import lombok.Getter;
import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class StandardButtonHandler implements ButtonsHandler {

    @Getter private final ItemStack icon;
    @Getter private final int slot;

    public StandardButtonHandler(int slot) {
        icon = setIcon();
        this.slot = slot;
    }

    public abstract ItemStack setIcon();

    public abstract void onClick(InventoryClickEvent event, GUIManager guiManager);
}
