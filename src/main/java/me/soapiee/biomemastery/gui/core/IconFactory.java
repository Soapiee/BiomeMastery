package me.soapiee.biomemastery.gui.core;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.pages.main.BiomePageSettings;
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
    private FileConfiguration config;

    public IconFactory(BiomeMastery main) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        config = main.getConfig();
    }

    public void reload(){
        config = main.getConfig();
    }

    public Icon createIcon(Path rawPath, CommandSender sender) {
        //TODO:
        // if (config.getString(path + ".icon.type").equalsIgnoreCase("texture")) return createTextureIcon();

        String path = rawPath.getPath();
        Material material = getMaterial(path + ".value", sender);

        return new Icon(createItemStack(material, path), createLore(path), getSlot(path));
    }

    public Icon createIcon(String path, CommandSender sender, BiomePageSettings biomePageSettings) {
        //TODO:
        // if (config.getString(path + ".icon.type").equalsIgnoreCase("texture")) return createTextureIcon();

        boolean isDefault = config.getConfigurationSection(path) == null || !config.isSet(path + ".gui");
        path = path + ".gui";

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

    private List<String> createLore(String path) {
        if (!config.isSet(path + ".lore")) return new ArrayList<>();

        return config.isList(path + ".lore") ? config.getStringList(path + ".lore") : new ArrayList<>();
    }

    private int getSlot(String path) {
        return config.getInt(path + ".slot", -1);
    }
}
