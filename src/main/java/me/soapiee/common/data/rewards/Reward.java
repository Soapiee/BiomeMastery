package me.soapiee.common.data.rewards;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

public class Reward {
    private final BiomeMastery main;
    private final MessageManager messageManager;
    private final VaultHook vaultHook;
    private final Logger logger;
    private final RewardType type;
    private final String path;
    private final CommandSender sender;

    private ArrayList<ItemStack> itemList;
    private ArrayList<String> permissionList;
    private ArrayList<String> commandsList;
    private int xpAmount;
    private double money;
    private PotionType potion;
    private EffectType effect;

    public Reward(BiomeMastery main, CommandSender sender, String path) {
        this.main = main;
        this.messageManager = main.getMessageManager();
        this.vaultHook = main.getVaultHook();
        this.path = path;

        FileConfiguration config = main.getConfig();

        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path));
        } catch (IllegalArgumentException ex) {
            rewardType = RewardType.NONE;
        }

        this.type = rewardType;

        switch (type) {
            case PERMISSION:
                cmdPermType();
                return;
            case ITEM:
                return;
            case NONE:
                return;
        }
    }

    //Potion Type
    public void potionType() {
        this.potion = potion;
    }

    //Effect Type
    public void effectType() {
        EffectType effectType;
        try {
            effectType = EffectType.valueOf(config.getString(path));
        } catch (IllegalArgumentException ex) {
            effectType = EffectType.NONE;
        }
        this.effect = effect;
    }

    //Currency Type
    public void currencyType() {
        this.money = main.getConfig().getDouble(path + "reward_item");
    }

    //XP Type
    public void xpType() {
        this.xpAmount = main.getConfig().getInt(path + "reward_item");
    }

    //Item Type
    public void itemType() {
        this.itemList = (ArrayList<ItemStack>) list;
    }

    //Command or Permission Type
    public void cmdPermType() {
        switch (type) {
            case PERMISSION:
                this.permissionList = list;
                break;
            case COMMAND:
                this.commandsList = list;
                break;
        }
    }

    public void give(Player player) {
        switch (type) {
            case PERMISSION:
                for (String permission : getPermissionList()) {
                    vaultHook.setPermission(player, permission);
                }
                break;
            case CURRENCY:
                vaultHook.deposit(player, getMoneyAmount());
                break;
            case EXPERIENCE:
                player.giveExp(getxpAmount());
                break;
            case ITEM:
                for (ItemStack item : getItemList()) {
                    if (Utils.hasFreeSpace(item.getType(), item.getAmount(), player)) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getLocation().getWorld().dropItem(player.getLocation(), item);
//                        player.sendMessage(Utils.colour(messageManager.get(Message.GAMEITEMWINERROR)));
                        return;
                    }
                }
                break;
            case COMMAND:
                for (String command : getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
                break;
            case NONE:
                break;
        }
//        if (getMessage() != null) player.sendMessage(Utils.colour(getMessage()));
    }

    public RewardType getType() {
        return this.type;
    }

//    public String getMessage() {
//        return this.message;
//    }

    public ArrayList<ItemStack> getItemList() {
        return this.itemList;
    }

    public ArrayList<String> getCommands() {
        return this.commandsList;
    }

    public int getxpAmount() {
        return this.xpAmount;
    }

    public ArrayList<String> getPermissionList() {
        return this.permissionList;
    }

    public double getMoneyAmount() {
        return this.money;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        switch (type) {
            case COMMAND:
                builder.append(type.toString().toLowerCase()).append("s: ");
                for (String permission : commandsList) {
                    builder.append(permission);
                    if (commandsList.size() > i) builder.append(", ");
                    i++;
                }
                break;
            case PERMISSION:
                builder.append(type.toString().toLowerCase()).append("s: ");
                for (String permission : permissionList) {
                    builder.append(permission);
                    if (permissionList.size() > i) builder.append(", ");
                    i++;
                }
                break;
            case ITEM:
                for (ItemStack item : itemList) {
                    builder.append(item.getAmount()).append(" ").append(item.getType().toString().toLowerCase().replace("_", " "));
                    if (itemList.size() > i) builder.append(", ");
                    i++;
                }
                break;
            case CURRENCY:
                builder.append(money).append(vaultHook.getCurrencyName());
                break;
            case EXPERIENCE:
                builder.append(xpAmount).append(" exp");
                break;
        }

        return builder.toString();
    }
}
