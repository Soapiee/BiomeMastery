package me.soapiee.biomemastery.gui.core;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.buttons.ButtonsHandler;
import me.soapiee.biomemastery.gui.buttons.StandardButton;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MultiPageHandler implements InventoryHandler {

    protected final BiomeMastery main;
    private final PageSettings pageSettings;
    private final Map<Integer, ButtonsHandler> buttonMap = new HashMap<>();

    protected final int currentPage;
    protected final int totalPages;

    public MultiPageHandler(BiomeMastery main, int currentPage, int totalPages) {
        this.main = main;
        pageSettings = main.getConfigGUIManager().getPageSettings();
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    protected abstract Inventory createInventory();

    protected abstract void setButtons();

    protected abstract int getSize();

    protected void addButton(ButtonsHandler button) {
        int slot = button.getSlot();
        if (slot == -1 || slot > getSize()) return;
        buttonMap.put(slot, button);
    }

    protected void displayButtons() {
        setButtons();
        addPageButtons();

        buttonMap.forEach((slot, button) -> getInventory().setItem(slot, button.getIcon()));
    }

    private void addPageButtons() {
        if (currentPage > 1) addButton(createPreviousButton());
        if (currentPage < totalPages) addButton(createNextButton());
        addButton(createCloseButton());
    }

    private StandardButton createCloseButton() {
        return new StandardButton(pageSettings.getCloseIcon().getSlot()) {

            @Override
            public ItemStack setIcon() {
                return formatIcon(pageSettings.getCloseIcon());
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                event.getWhoClicked().closeInventory();
            }
        };
    }

    private StandardButton createPreviousButton() {
        return new StandardButton(pageSettings.getPrevPageIcon().getSlot()) {

            @Override
            public ItemStack setIcon() {
                return formatIcon(pageSettings.getPrevPageIcon());
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                Player player = (Player) event.getWhoClicked();
                prevPage(player, guiManager);
            }
        };
    }

    private StandardButton createNextButton() {
        return new StandardButton(pageSettings.getNextPageIcon().getSlot()) {

            @Override
            public ItemStack setIcon() {
                return formatIcon(pageSettings.getNextPageIcon());
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                Player player = (Player) event.getWhoClicked();
                nextPage(player, guiManager);
            }
        };
    }

    protected abstract void nextPage(Player player, GUIManager guiManager);

    protected abstract void prevPage(Player player, GUIManager guiManager);

    private ItemStack formatIcon(Icon icon) {
        ItemStack itemStack = fillPlaceholders(icon.getItemStack().clone());
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(fillPlaceholders(icon.getLore()));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack fillPlaceholders(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        String newName = meta.getDisplayName()
                .replace("%next_page_number%", String.valueOf(currentPage + 1))
                .replace("%prev_page_number%", String.valueOf(currentPage - 1));
        meta.setDisplayName(Utils.addColour(newName));

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private List<String> fillPlaceholders(List<String> list) {
        ArrayList<String> updatedLore = new ArrayList<>();

        for (String line : list) {
            if (line.contains("%")) {
                updatedLore.add(Utils.addColour(line.replace("%next_page_number%", String.valueOf(currentPage + 1))
                        .replace("%prev_page_number%", String.valueOf(currentPage - 1))));
            } else updatedLore.add(Utils.addColour(line));
        }

        return updatedLore;
    }

    @Override
    public void onClick(InventoryClickEvent event, GUIManager guiManager) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ButtonsHandler button = buttonMap.get(slot);

        if (button != null) button.onClick(event, guiManager);
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
