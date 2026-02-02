package me.soapiee.biomemastery.handlers;

import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class SinglePageHandler implements InventoryHandler {

    private final Map<Integer, ButtonsHandler> buttonMap = new HashMap<>();

    public SinglePageHandler() {
    }

    public abstract void setButtons();

    public void addButton(ButtonsHandler button) {
        buttonMap.put(button.getSlot(), button);
    }

    public void displayButtons() {
        setButtons();
        buttonMap.forEach((slot, button) -> getInventory().setItem(slot, button.getIcon()));
    }

    @Override
    public void onClick(InventoryClickEvent event, GUIManager guiManager) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ButtonsHandler button = buttonMap.get(slot);

        if (button != null) button.onClick(event,guiManager);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        displayButtons();

//        String name = event.getPlayer().getName();
//        String message = ChatColor.GREEN + "Opened inventory " + getInventory().toString() + " for player " + name;
//        Utils.debugMsg(name, message);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
//        String name = event.getPlayer().getName();
//        String message = ChatColor.GREEN + "Closed inventory " + getInventory().toString() + " for player " + name;
//        Utils.debugMsg(name, message);
    }
}
