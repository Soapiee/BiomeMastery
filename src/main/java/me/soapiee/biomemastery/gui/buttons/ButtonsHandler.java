package me.soapiee.biomemastery.gui.buttons;

import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface ButtonsHandler {

    ItemStack getIcon();

    int getSlot();

    void onClick(InventoryClickEvent event, GUIManager guiManager);
}
