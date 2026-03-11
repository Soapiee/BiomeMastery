package me.soapiee.biomemastery.gui.buttons;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.GUIManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class StandardButton implements ButtonsHandler {

    @Getter private final ItemStack icon;
    @Getter private final int slot;

    public StandardButton(int slot) {
        icon = setIcon();
        this.slot = slot;
    }

    public abstract ItemStack setIcon();

    @Override public void onClick(InventoryClickEvent event, GUIManager guiManager) {
    }

    protected void updateProgress(BiomeMastery main, OfflinePlayer target, PlayerData data) {
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();

        if (!main.getConfigManager().isEnabledBiome(locBiome)) return;

        BiomeLevel biomeLevel = data.getBiomeLevel(locBiome);
        biomeLevel.updateProgress(locBiome);
    }

}
