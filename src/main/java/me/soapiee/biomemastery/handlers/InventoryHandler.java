package me.soapiee.biomemastery.handlers;

import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public interface InventoryHandler {

    Inventory getInventory();

    void onClick(InventoryClickEvent event, GUIManager guiManager);

    void onOpen(InventoryOpenEvent event);

    void onClose(InventoryCloseEvent event);
}
