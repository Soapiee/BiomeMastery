package me.soapiee.biomemastery.logic.rewards;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.hooks.VaultHook;
import me.soapiee.biomemastery.logic.effects.EffectType;
import me.soapiee.biomemastery.logic.rewards.types.*;
import me.soapiee.biomemastery.manager.EffectsManager;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

public class RewardFactory {

    private final BiomeMastery main;
    private final FileConfiguration config;
    private final CustomLogger customLogger;
    private final VaultHook vaultHook;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;
    private final EffectsManager effectsManager;

    public RewardFactory(BiomeMastery main, PlayerDataManager playerDataManager, EffectsManager effectsManager) {
        this.main = main;
        this.config = main.getConfig();
        this.customLogger = main.getCustomLogger();
        this.vaultHook = main.getVaultHook();
        this.messageManager = main.getMessageManager();
        this.playerDataManager = playerDataManager;
        this.effectsManager = effectsManager;
    }

    public Reward create(String path) {
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path + "reward_type").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            rewardType = RewardType.NONE;
            createLog(path, e, Message.INVALIDREWARDTYPE);
        }

        switch (rewardType) {
            case POTION:
                return potionReward(path);

            case EFFECT:
                return effectReward(path);

            case CURRENCY:
                return currencyReward(path);

            case EXPERIENCE:
                return experienceReward(path);

            case ITEM:
                return itemReward(path);

            case PERMISSION:
                return permissionReward(path);

            case COMMAND:
                return commandReward(path);
        }

        return new NullReward();
    }

    private Reward potionReward(String path) {
        String[] potionParts = config.getString(path + "reward_item").split(":");
        PotionType potionType;
        int amplifier;

        try {
            amplifier = Integer.parseInt(potionParts[1]);
            potionType = PotionType.valueOf(potionParts[0].toUpperCase());
        } catch (IllegalArgumentException error) {
            if (error instanceof NumberFormatException)
                createLog(path, error, Message.INVALIDPOTIONAMP);
            else
                createLog(path, error, Message.INVALIDPOTIONTYPE);

            return new NullReward();
        }

        String singular = config.getString(path + "type", "singular");

        return new PotionReward(main, playerDataManager, potionType, amplifier, (singular.equalsIgnoreCase("singular")));
    }

    private Reward effectReward(String path) {
        EffectType effectType;

        try {
            String effectString = config.getString(path + "reward_item").toUpperCase();
            effectType = EffectType.valueOf(effectString.replace("_", ""));
        } catch (IllegalArgumentException error) {
            createLog(path, error, Message.INVALIDEFFECTTYPE);
            return new NullReward();
        }

        String singular = config.getString(path + "type", "singular");

        return new EffectReward(main, playerDataManager, effectsManager, effectType, (singular.equalsIgnoreCase("singular")));
    }

    private Reward currencyReward(String path) {
        if (vaultHook == null) {
            createLog(path, null, Message.INVALIDVAULTHOOK);
            return new NullReward();
        }

        String rawDouble = config.getString(path + "reward_item");
        double money;

        try {
            money = Double.parseDouble(rawDouble);
        } catch (IllegalArgumentException error) {
            createLog(path, error, Message.INVALIDAMOUNT);
            return new NullReward();
        }

        if (money <= 0) {
            createLog(path, null, Message.INVALIDAMOUNT);
            return new NullReward();
        }

        return new CurrencyReward(main, money);
    }

    private Reward experienceReward(String path) {
        String rawInt = config.getString(path + "reward_item");
        int experience;

        try {
            experience = Integer.parseInt(rawInt);
        } catch (IllegalArgumentException error) {
            createLog(path, error, Message.INVALIDAMOUNT);
            return new NullReward();
        }

        if (experience <= 0) {
            createLog(path, null, Message.INVALIDAMOUNT);
            return new NullReward();
        }

        return new ExperienceReward(main, experience);
    }

    private Reward itemReward(String path) {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        String[] itemParts;
        Material material;
        int quantity;

        if (config.isString(path + "reward_item")) {
            itemParts = config.getString(path + "reward_item").split(":");
            try {
                material = Material.valueOf(itemParts[0].toUpperCase());
                quantity = Integer.parseInt(itemParts[1].replace(":", ""));
                itemList.add(new ItemStack(material, quantity));
            } catch (IllegalArgumentException | NullPointerException error) {

                if (error instanceof NumberFormatException)
                    createLog(path, error, Message.INVALIDQUANTITY);
                else
                    createLog(path, error, Message.INVALIDMATERIAL);
            }
        }

        if (config.isList(path + "reward_item")) {
            for (String rawItemString : config.getStringList(path + "reward_item")) {
                itemParts = rawItemString.split(":");
                try {
                    material = Material.valueOf(itemParts[0].toUpperCase());
                    quantity = Integer.parseInt(itemParts[1].replace(":", ""));
                    itemList.add(new ItemStack(material, quantity));
                } catch (IllegalArgumentException | NullPointerException error) {

                    if (error instanceof NumberFormatException)
                        createLog(path, error, Message.INVALIDQUANTITY);
                    else
                        createLog(path, error, Message.INVALIDMATERIAL);
                }
            }
        }

        if (itemList.isEmpty()) return new NullReward();

        return new ItemReward(messageManager, itemList);
    }

    private Reward permissionReward(String path) {
        ArrayList<String> permissionList = new ArrayList<>();

        if (vaultHook == null) {
            createLog(path, null, Message.INVALIDVAULTHOOK);
            return new NullReward();
        }

        if (config.isString(path + "reward_item"))
            permissionList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            permissionList.addAll(config.getStringList(path + "reward_item"));

        if (permissionList.isEmpty()) {
            createLog(path, null, Message.INVALIDPERMISSION);
            return new NullReward();
        }

        return new PermissionReward(main, permissionList);
    }

    private Reward commandReward(String path) {
        ArrayList<String> commandList = new ArrayList<>();

        if (config.isString(path + "reward_item"))
            commandList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            commandList.addAll(config.getStringList(path + "reward_item"));

        if (commandList.isEmpty()) {
            createLog(path, null, Message.INVALIDCOMMAND);
            return new NullReward();
        }

        String description = config.getString(path + "reward_description", "undefined");

        return new CommandReward(main, commandList, description);
    }

    private void createLog(String path, Exception error, Message invalidObject) {
        String[] pathParts = path.split("\\.");

        customLogger.logToFile(error,
                messageManager.getWithPlaceholder(
                        Message.INVALIDREWARD,
                        pathParts[1],
                        pathParts[2],
                        invalidObject));
    }
}
