package me.soapiee.biomemastery.gui.pages;

import lombok.Getter;
import me.soapiee.biomemastery.utils.Utils;
import me.soapiee.biomemastery.handlers.StandardButtonHandler;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.handlers.MultiPageHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MultiExamplePage extends MultiPageHandler {

    @Getter private final Inventory inventory;
    private final String title = "&#1baecf&lExample Multi Page ";
    private final int size = 9 * 6;
    private final Player player;

    public MultiExamplePage(Player player, int currentPage, int totalPages) {
        super(currentPage, totalPages);
        this.player = player;
        inventory = createInventory();
    }

    private Inventory createInventory() {
        return Bukkit.createInventory(null, size, Utils.addColour(title));
    }

    public void setButtons() {
        for (int i = 18; i <= 35; i++) addButton(createPlaceholderButton(i));
    }

    private StandardButtonHandler createPlaceholderButton(int slot){
        return new StandardButtonHandler(slot) {
            @Override public ItemStack setIcon() {
                return new ItemStack(Material.DARK_OAK_BUTTON);
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                if (getIcon().getItemMeta().getDisplayName().contains("CLICK TO CLAIM")) {
                    Player player = (Player) event.getWhoClicked();
                    player.sendMessage("You clicked " + event.getRawSlot());
                }
            }
        };
    }

}
