package me.soapiee.biomemastery.internals;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class TexturesUnsupported implements TexturesProvider {

    private CustomLogger customLogger;
    private MessageManager messageManager;

    @Override
    public void initialise(BiomeMastery main) {
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
    }

    public ItemStack getTexturedSkull(ItemStack itemStack, String input, CommandSender sender){
        customLogger.logToPlayer(sender, null, messageManager.get(Message.TEXTURESUNSUPPORTED));
        itemStack.setType(Material.GRASS_BLOCK);
        return itemStack;
    }

}
