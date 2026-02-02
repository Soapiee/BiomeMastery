package me.soapiee.biomemastery.gui.pages;

import lombok.Getter;
import me.soapiee.biomemastery.Main;
import me.soapiee.biomemastery.utils.Utils;
import me.soapiee.biomemastery.handlers.RelationalButtonHandler;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.handlers.SinglePageHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SingleExamplePage extends SinglePageHandler {

    private final Main main;

    @Getter private final Inventory inventory;
    private final String title = "#1baecf&lExample Single GUI ";
    private final int size = 9 * 3;
    private final Player player;

    public SingleExamplePage(Main main, Player player) {
        super();
        this.main = main;
        this.player = player;
        inventory = createInventory();
    }

    private Inventory createInventory() {
        return Bukkit.createInventory(null, size, Utils.addColour(title));
    }

    public void setButtons() {
        for (int i = 9; i < 18; i++) {
            this.addButton(createButton(i));
        }
    }

    private RelationalButtonHandler createButton(int slot){
        return new RelationalButtonHandler(player, slot) {
            @Override
            public ItemStack setIcon() {
                ItemStack stack = new ItemStack(Material.DIAMOND, (getSlot()-8));
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(Utils.addColour("&bI am a Diamond"));
                stack.setItemMeta(meta);

                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                {
                    if (getIcon().getItemMeta().getDisplayName().contains("CLICK TO CLAIM")) {
                        Player player = (Player) event.getWhoClicked();
                        player.sendMessage("You clicked " + event.getRawSlot());
                    }
                }
            }
        };
    }
}
