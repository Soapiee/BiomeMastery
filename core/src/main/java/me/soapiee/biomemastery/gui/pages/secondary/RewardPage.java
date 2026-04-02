package me.soapiee.biomemastery.gui.pages.secondary;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.gui.core.Icon;
import me.soapiee.biomemastery.gui.buttons.StandardButton;
import me.soapiee.biomemastery.gui.core.MultiPageHandler;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.effects.EffectInterface;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.logic.rewards.types.PotionReward;
import me.soapiee.biomemastery.manager.GUIManager;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.Message;
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
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RewardPage extends MultiPageHandler {

    protected final MessageManager messageManager;
    protected final RewardPageSettings rewardPageSettings;
    @Getter private final Inventory inventory;
    private final Player player;
    private final PlayerData playerData;
    private final BiomeData biomeData;
    private final BiomeLevel biomeLevel;

    private final String title;
    @Getter public final int size;
    private final int max_icons;

    public RewardPage(BiomeMastery main, Player player, BiomeData biomeData, int currentPage, int totalPages) {
        super(main, currentPage, totalPages);
        messageManager = main.getMessageManager();
        this.player = player;
        playerData = main.getDataManager().getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) throw new NullPointerException();
        this.biomeData = biomeData;
        biomeLevel = playerData.getBiomeLevel(biomeData.getBiome());
        rewardPageSettings = main.getConfigGUIManager().getRewardPageSettings();

        title = rewardPageSettings.getTitle();
        size = rewardPageSettings.getSize();
        max_icons = rewardPageSettings.getRewardSlots().size();
        inventory = createInventory();
    }

    protected Inventory createInventory() {
        return Bukkit.createInventory(null, size, Utils.addColour(title));
    }

    protected void setButtons() {
        addButton(createInfoIcon());

        for (int slot : rewardPageSettings.getFillerSlots()) {
            addButton(createFillerIcon(slot));
        }

        addRewardInfoButtons();
    }

    private void addRewardInfoButtons() {
        int level = ((currentPage - 1) * max_icons) + 1;
        int endLevel = (currentPage) * max_icons;
        int slot = 10;
        int lastValidSlot = size - 1;

        for (int i = slot; i <= lastValidSlot; i++) {
            if (!rewardPageSettings.getRewardSlots().contains(i)) continue;
            if (level > biomeData.getMaxLevel() || level > endLevel) break;

            addButton(createRewardIcon(i, level));
            level++;
        }
    }

    private StandardButton createRewardIcon(int slot, int level) {
        return new StandardButton(slot) {
            @Override public ItemStack setIcon() {
                return formatIcon(rewardPageSettings.getRewardIcon(), level, false);
            }

            @Override public void onClick(InventoryClickEvent event, GUIManager guiManager) {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                Bukkit.dispatchCommand(player, "biome reward " + biomeData.getBiomeName() + " " + level);
            }
        };
    }

    private StandardButton createInfoIcon() {
        return new StandardButton(rewardPageSettings.getInfoIcon().getSlot()) {
            @Override public ItemStack setIcon() {
                return formatIcon(rewardPageSettings.getInfoIcon(), 1, false);
            }
        };
    }

    private StandardButton createFillerIcon(int slot) {
        return new StandardButton(slot) {
            @Override public ItemStack setIcon() {
                return formatIcon(rewardPageSettings.getFillerIcon(), 1, true);
            }
        };
    }

    private ItemStack formatIcon(Icon icon, int level, boolean hideAttributes) {
        ItemStack itemStack = fillPlaceholders(icon.getItemStack().clone(), level);

        int validLevel = level <= 0 ? 1 : level;
        itemStack.setAmount(validLevel);

        ItemMeta meta = itemStack.getItemMeta();
        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(fillPlaceholders(icon.getLore(), level));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack fillPlaceholders(ItemStack itemStack, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        String newName = meta.getDisplayName();

        if (newName.contains("%player_name%")) newName = newName.replace("%player_name%", player.getName());
        if (newName.contains("%biome%")) newName = newName.replace("%biome%", biomeLevel.getBiomeName());
        if (newName.contains("%level%")) newName = newName.replace("%level%", String.valueOf(level));
        if (newName.contains("%progress%")) newName = newName.replace("%progress%", Utils.formatTargetDuration(biomeLevel.getProgress()));

        meta.setDisplayName(Utils.addColour(newName));

        if (itemStack.getType() == Material.PLAYER_HEAD) ((SkullMeta) meta).setOwningPlayer(player);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private List<String> fillPlaceholders(List<String> list, int level) {
        ArrayList<String> updatedLore = new ArrayList<>();
        Reward reward = biomeData.getReward(level);

        for (String line : list) {
            String editedLine = line;
            if (editedLine.contains("%player_name%")) editedLine = editedLine.replace("%player_name%", player.getName());
            if (editedLine.contains("%reward%")) editedLine = editedLine.replace("%reward%", reward.toString());
            if (editedLine.contains("%reward_status%")) editedLine = editedLine.replace("%reward_status%", getRewardStatus(level, reward));

            updatedLore.add(Utils.addColour(editedLine));
        }

        return updatedLore;
    }

    private String getRewardStatus(int rewardLevel, Reward reward) {
        int currentLevel = biomeLevel.getLevel();

        if (currentLevel < rewardLevel) return messageManager.get(Message.REWARDUNCLAIMED);

        if (reward.isSingular()) return messageManager.get(Message.REWARDCLAIMED);

        if (!player.isOnline())
            return messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiomeName());

        if (hasThisActiveReward(reward)) return messageManager.get(Message.REWARDDEACTIVATE);

        Biome targetLocation = player.getLocation().getBlock().getBiome();
        if (targetLocation.name().equalsIgnoreCase(biomeData.getBiomeName()))
            return messageManager.get(Message.REWARDGUIACTIVATE);
        else
            return messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiomeName());
    }

    private boolean hasThisActiveReward(Reward reward) {
        if (playerData.hasActiveRewards()) {
            if (reward instanceof PotionReward) {
                PotionEffectType potion = ((PotionReward) reward).getPotion();
                return (player.hasPotionEffect(potion));
            }

            if (reward instanceof EffectReward) {
                EffectInterface effect = ((EffectReward) reward).getEffect();
                return (effect.isActive(player));
            }
        }

        return false;
    }

    protected void nextPage(Player player, @NotNull GUIManager guiManager) {
        guiManager.openGUI(new RewardPage(main, player, biomeData, currentPage + 1, totalPages), player);
    }

    protected void prevPage(Player player, GUIManager guiManager) {
        guiManager.openGUI(new RewardPage(main, player, biomeData, currentPage - 1, totalPages), player);
    }
}
