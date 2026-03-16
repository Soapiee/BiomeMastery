package me.soapiee.biomemastery.internals;

import me.soapiee.biomemastery.BiomeMastery;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public interface TexturesProvider {

    default void initialise(BiomeMastery main) {}

    ItemStack getTexturedSkull(ItemStack itemStack, String input, CommandSender sender);
}
