package me.soapiee.biomemastery.manager;

import me.soapiee.biomemastery.gui.core.InventoryHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GUIManager {

    private final Map<Inventory, InventoryHandler> activeInventories = new HashMap<>();
    private final HashSet<Player> viewers = new HashSet<>();

    public GUIManager() {
    }

    public void registerHandledInventory(Inventory inv, InventoryHandler handler, Player viewer) {
        activeInventories.put(inv, handler);
        viewers.add(viewer);
    }

    public void unregisterHandledInventory(Inventory inv, Player viewer) {
        activeInventories.remove(inv);
        viewers.remove(viewer);
    }

    public void closeAll() {
        if (viewers.isEmpty()) return;
        for (Player viewer : viewers) {
            viewer.closeInventory();
        }
    }

    public void openGUI(InventoryHandler inv, Player player) {
        registerHandledInventory(inv.getInventory(), inv, player);
        player.openInventory(inv.getInventory());
    }

    public void handleClick(InventoryClickEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());

        if (handler != null) handler.onClick(event, this);
    }

    public void handleOpen(InventoryOpenEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());

        if (handler != null) handler.onOpen(event);
    }

    public void handleClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        InventoryHandler handler = activeInventories.get(inv);

        if (handler != null) {
            handler.onClose(event);
            unregisterHandledInventory(inv, (Player) event.getPlayer());
        }
    }
}
