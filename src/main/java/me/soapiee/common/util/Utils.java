package me.soapiee.common.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static void consoleMsg(String message) {
        String prefix = "[" + Bukkit.getServer().getPluginManager().getPlugin("BiomeMastery").getDescription().getPrefix() + "]";
        Bukkit.getConsoleSender().sendMessage(colour(prefix + " " + message));
    }

    public static String colour(String message) { // 1.8 and above
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean hasFreeSpace(Material type, int amount, Player player) {
        Inventory inv = player.getInventory();
        int items = 0;
        for (ItemStack item : inv.getStorageContents())
            try {
                if (item == null) {
                    items += type.getMaxStackSize();
                } else if (item.getType() == type) {
                    int stackAmount = item.getAmount();
                    items += type.getMaxStackSize() - stackAmount;
                }
            } catch (NullPointerException ignored) {
            }
        return items > amount;
    }

}
