package me.soapiee.biomemastery.gui.core;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.pages.main.BiomePageSettings;
import me.soapiee.biomemastery.internals.TexturesProvider;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IconFactory {

    private final BiomeMastery main;
    private final CustomLogger customLogger;
    private final MessageManager messageManager;
    private final TexturesProvider texturesProvider;
    private FileConfiguration config;

    public IconFactory(BiomeMastery main) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        texturesProvider = main.getInternalsManager().getTexturesProvider();
        config = main.getConfig();
    }

    public void reload() {
        config = main.getConfig();
    }

    // Used to create generic/required icons
    public Icon createIcon(Path rawPath, CommandSender sender) {
        String path = rawPath.getPath();

        //TODO:
        if (config.getString(path + ".type", "").equalsIgnoreCase("texture")) {
            //TODO: Check version
            // if (version >= 1.21.4) return new Icon(createItemStack(path), createLore(path), getSlot(path));
            // else customLogger.logToPlayer(sender, null, "You must be on 1.21.4 or above to use textures");

            return new Icon(createItemStack(path, sender), createLore(path), getSlot(path));
        }

        Material material = getMaterial(path + ".value", sender);
        return new Icon(createItemStack(material, path), createLore(path), getSlot(path));
    }

    // Used to create specified/unique Biome icons
    public Icon createIcon(String path, CommandSender sender, BiomePageSettings biomePageSettings) {
        boolean isDefault = config.getConfigurationSection(path) == null || !config.isSet(path + ".gui");
        path = path + ".gui";

        //TODO:
        if (config.getString(path + ".type", "").equalsIgnoreCase("texture")) {
            //TODO: Check version
            // if (version >= 1.21.4) return new Icon(createItemStack(path), createLore(path), getSlot(path));
            // else customLogger.logToPlayer(sender, null, "You must be on 1.21.4 or above to use textures");

            return new Icon(createItemStack(path, sender), createLore(path), getSlot(path));
        }

        if (isDefault) {
            return biomePageSettings.getBiomeIcon();
        } else {
            Material material = getMaterial(path + ".value", sender);
            return new Icon(createItemStack(material, path), createLore(path), getSlot(path));
        }
    }

    private Material getMaterial(String path, CommandSender sender) {
        String input = config.isSet(path) ? config.getString(path) : "GRASS_BLOCK";
        Material material;

        try {
            material = Material.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            material = Material.GRASS_BLOCK;
            customLogger.logToPlayer(sender, null, messageManager.getWithPlaceholder(Message.INVALIDGUIMATERIAL, path));
        }

        return material;
    }

    private ItemStack createItemStack(Material material, String path) {
        int quantity = config.getInt(path + ".quantity", 1);
        String displayName = config.getString(path + ".display_name", "&7 ");

        ItemStack itemStack = new ItemStack(material, quantity);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack createItemStack(String path, CommandSender sender) {
        String textureInput = config.getString(path + ".value");
        ItemStack itemStack = createItemStack(Material.PLAYER_HEAD, path);

        return texturesProvider.getTexturedSkull(itemStack, textureInput, sender);
    }

    private List<String> createLore(String path) {
        if (!config.isSet(path + ".lore")) return new ArrayList<>();

        return config.isList(path + ".lore") ? config.getStringList(path + ".lore") : new ArrayList<>();
    }

    private int getSlot(String path) {
        return config.getInt(path + ".slot", -1);
    }
}
