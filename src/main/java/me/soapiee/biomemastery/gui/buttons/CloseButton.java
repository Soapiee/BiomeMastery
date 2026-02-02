package me.soapiee.biomemastery.gui.buttons;

import me.soapiee.biomemastery.handlers.StandardButtonHandler;
import me.soapiee.biomemastery.utils.Utils;
import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CloseButton extends StandardButtonHandler {

    public CloseButton(int slot) {
        super(slot);
    }

    public ItemStack setIcon() {
        ItemStack stack = new ItemStack(Material.BARRIER);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Utils.addColour("&cClose Menu"));

        List<String> loreList = new ArrayList<>();
        loreList.add(Utils.addColour(""));
        loreList.add(Utils.addColour("&7Click me to close the menu"));
        meta.setLore(loreList);

        stack.setItemMeta(meta);
        return stack;
    }

    public void onClick(InventoryClickEvent event, GUIManager guiManager) {
        event.getWhoClicked().closeInventory();
    }
}
