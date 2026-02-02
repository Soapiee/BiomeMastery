package me.soapiee.biomemastery.handlers;

import lombok.Getter;
import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class RelationalButtonHandler implements ButtonsHandler {

    @Getter private final Player player;
//    private final DataManager data;
    @Getter private ItemStack icon;
    @Getter private final int slot;
//    private final int level;

    public RelationalButtonHandler(Player player, int slot) {
        this.slot = slot;
        this.player = player;
        this.icon = this.setIcon();
    }

    public abstract ItemStack setIcon();

    public abstract void onClick(InventoryClickEvent event, GUIManager guiManager);
}
