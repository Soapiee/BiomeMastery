package me.soapiee.biomemastery.handlers;

import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.utils.Utils;
import me.soapiee.biomemastery.gui.buttons.CloseButton;
import me.soapiee.biomemastery.gui.buttons.NextButton;
import me.soapiee.biomemastery.gui.buttons.PrevButton;
import me.soapiee.biomemastery.gui.pages.MultiExamplePage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public abstract class MultiPageHandler implements InventoryHandler {

    private final Map<Integer, ButtonsHandler> buttonMap = new HashMap<>();

    private final int PREV_BUTTON_SLOT = 0;
    private final int NEXT_BUTTON_SLOT = 8;

    private final int currentPage;
    private final int totalPages;

    public MultiPageHandler(int currentPage, int totalPages) {
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public abstract void setButtons();

    protected void addButton(ButtonsHandler button) {
        buttonMap.put(button.getSlot(), button);
    }

    protected void displayButtons(){
        setButtons();
        addPageButtons();
        buttonMap.forEach((slot, button) -> getInventory().setItem(slot, button.getIcon()));
    }

    private void addPageButtons() {
        if (currentPage > 1) addButton(createPreviousButton());
        if (currentPage < totalPages) addButton(createNextButton());

        addButton(new CloseButton((6 * 9) - 1));
    }

    private PrevButton createPreviousButton(){
        return new PrevButton(PREV_BUTTON_SLOT) {

            @Override
            public ItemStack setIcon() {
                ItemStack stack = super.setIcon();
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(Utils.addColour("&aPrevious Page (" + (currentPage - 1) + ")"));
                stack.setItemMeta(meta);
                return stack;
            }

            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                Player player = (Player) event.getWhoClicked();
                guiManager.openGUI(new MultiExamplePage(player, currentPage - 1, totalPages), player);
            }
        };
    }

    private NextButton createNextButton(){
        return new NextButton(NEXT_BUTTON_SLOT) {

            @Override
            public ItemStack setIcon() {
                ItemStack stack = super.setIcon();
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(Utils.addColour("&aNext Page (" + (currentPage + 1) + ")"));
                stack.setItemMeta(meta);
                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                Player player = (Player) event.getWhoClicked();
                guiManager.openGUI(new MultiExamplePage(player, currentPage + 1, totalPages), player);
            }
        };
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
