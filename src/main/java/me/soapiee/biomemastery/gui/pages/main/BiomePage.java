package me.soapiee.biomemastery.gui.pages.main;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.gui.core.Icon;
import me.soapiee.biomemastery.gui.buttons.StandardButton;
import me.soapiee.biomemastery.gui.core.MultiPageHandler;
import me.soapiee.biomemastery.gui.pages.secondary.RewardPage;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomePage extends MultiPageHandler {

    @Getter private final Inventory inventory;
    private final PlayerData playerData;
    private final Player player;
    protected final BiomePageSettings biomePageSettings;

    private final Map<Integer, BiomeData> orderedBiomeData;
    private final int totalBiomes;
    private final String title;
    @Getter public final int size;
    private final int max_icons;

    public BiomePage(BiomeMastery main, Player player, int currentPage, int totalPages) {
        super(main, currentPage, totalPages);
        this.player = player;
        playerData = main.getDataManager().getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) throw new NullPointerException();
        biomePageSettings = main.getConfigGUIManager().getBiomePageSettings();
        orderedBiomeData = main.getDataManager().getBiomeDataManager().getBiomeDataOrdered();
        totalBiomes = orderedBiomeData.size();

        title = biomePageSettings.getTitle();
        size = biomePageSettings.getSize();
        max_icons = biomePageSettings.getBiomeSlots().size();
        inventory = createInventory();
    }

    protected Inventory createInventory() {
        return Bukkit.createInventory(null, size, Utils.addColour(title));
    }

    protected void setButtons() {
        addButton(createInfoIcon());

        for (int value : biomePageSettings.getFillerSlots()) {
            addButton(createFillerIcon(value));
        }

        addBiomeButtons();
    }

    private void addBiomeButtons() {
        int biomeData = ((currentPage - 1) * max_icons) + 1;
        int endBiomeData = (currentPage) * max_icons;
        int slot = 10;
        int lastValidSlot = size - 1;

        for (int i = slot; i <= lastValidSlot; i++) {
            if (!biomePageSettings.getBiomeSlots().contains(i)) continue;
            if (biomeData > endBiomeData || biomeData > totalBiomes) break;

            addButton(createBiomeIcon(getBiomeData(biomeData), i));

            biomeData++;
        }
    }

    private StandardButton createBiomeIcon(BiomeData biomeData, int slot) {
        return new StandardButton(slot) {
            @Override public ItemStack setIcon() {
                return formatIcon(biomeData.getIcon(), biomeData);
            }

            @Override public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                updateProgress(main, player, playerData);
                guiManager.openGUI(new RewardPage(main, player, biomeData, 1, getRewardMaxPages(biomeData)), player);
            }
        };
    }

    private StandardButton createInfoIcon() {
        return new StandardButton(biomePageSettings.getInfoIcon().getSlot()) {
            @Override public ItemStack setIcon() {
                return formatIcon(biomePageSettings.getInfoIcon(), false);
            }
        };
    }

    private StandardButton createFillerIcon(int slot) {
        return new StandardButton(slot) {
            @Override public ItemStack setIcon() {
                return formatIcon(biomePageSettings.getFillerIcon(), true);
            }
        };
    }

    private ItemStack formatIcon(Icon icon, boolean hideAttributes) {
        ItemStack itemStack = fillPlaceholders(icon.getItemStack().clone());
        ItemMeta meta = itemStack.getItemMeta();
        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(fillPlaceholders(icon.getLore()));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack formatIcon(Icon icon, BiomeData biomeData) {
        Biome biome = biomeData.getBiome();
        ItemStack itemStack = fillPlaceholders(icon.getItemStack().clone(), biome);

        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        int level = biomeLevel.getLevel() <= 0 ? 1 : biomeLevel.getLevel();
        itemStack.setAmount(level);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(fillPlaceholders(icon.getLore(), biomeLevel, biomeData));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack fillPlaceholders(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        String newName = meta.getDisplayName();

        if (newName.contains("%player_name%")) newName = newName.replace("%player_name%", player.getName());
        if (newName.contains("%next_page_number%"))
            newName = newName.replace("%next_page_number%", String.valueOf(currentPage + 1));
        if (newName.contains("%prev_page_number%"))
            newName = newName.replace("%prev_page_number%", String.valueOf(currentPage - 1));

        meta.setDisplayName(Utils.addColour(newName));

        if (itemStack.getType() == Material.PLAYER_HEAD) ((SkullMeta) meta).setOwningPlayer(player);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack fillPlaceholders(ItemStack itemStack, Biome biome) {
        ItemMeta meta = itemStack.getItemMeta();
        String newName = meta.getDisplayName();

        if (newName.contains("%biome%")) newName = newName.replace("%biome%", Utils.capitalise(biome.name()));

        meta.setDisplayName(Utils.addColour(newName));

        if (itemStack.getType() == Material.PLAYER_HEAD) ((SkullMeta) meta).setOwningPlayer(player);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private List<String> fillPlaceholders(List<String> list) {
        ArrayList<String> updatedLore = new ArrayList<>();

        for (String line : list) {
            if (line.contains("%player_name%"))
                updatedLore.add(Utils.addColour(line.replace("%player_name%", player.getName())));
            else updatedLore.add(Utils.addColour(line));
        }

        return updatedLore;
    }

    private List<String> fillPlaceholders(List<String> list, BiomeLevel biomeLevel, BiomeData biomeData) {
        ArrayList<String> updatedLore = new ArrayList<>();

        for (String line : list) {
            String editedLine = line;
            if (editedLine.contains("%player_name%"))
                editedLine = editedLine.replace("%player_name%", player.getName());
            if (editedLine.contains("%level%"))
                editedLine = editedLine.replace("%level%", String.valueOf(biomeLevel.getLevel()));
            if (editedLine.contains("%progress%"))
                editedLine = editedLine.replace("%progress%", Utils.formatTargetDuration(biomeLevel.getProgress()));
            if (editedLine.contains("%progress_bar%"))
                editedLine = editedLine.replace("%progress_bar%", Utils.progressBar((int) biomeLevel.getProgress(), biomeData.getTargetDuration(biomeLevel.getLevel())));

            updatedLore.add(Utils.addColour(editedLine));
        }

        return updatedLore;
    }

    private BiomeData getBiomeData(int index) {
        return orderedBiomeData.get(index);
    }

    private int getRewardMaxPages(BiomeData biomeData) {
        int maxIcons = main.getConfigGUIManager().getRewardPageSettings().getRewardSlots().size();
        int maxLevel = biomeData.getMaxLevel();
        int total = maxLevel / maxIcons;

        return (maxLevel % maxIcons == 0 ? total : total + 1);
    }

    protected void nextPage(Player player, GUIManager guiManager) {
        guiManager.openGUI(new BiomePage(main, player, currentPage + 1, totalPages), player);
    }

    protected void prevPage(Player player, GUIManager guiManager) {
        guiManager.openGUI(new BiomePage(main, player, currentPage - 1, totalPages), player);
    }

}
