package me.soapiee.biomemastery.gui.core;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class Icon {

    @Getter private final ItemStack itemStack;
    @Getter private final List<String> lore;
    @Getter private final int slot;

    public Icon(ItemStack itemStack, List<String> lore, int slot) {
        this.itemStack = itemStack;
        this.lore = lore;
        this.slot = slot;
    }
}
