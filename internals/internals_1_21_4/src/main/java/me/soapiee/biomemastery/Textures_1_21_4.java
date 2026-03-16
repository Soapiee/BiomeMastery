package me.soapiee.biomemastery;

import me.soapiee.biomemastery.internals.TexturesProvider;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class Textures_1_21_4 implements TexturesProvider {

    private CustomLogger customLogger;
    private MessageManager messageManager;

    @Override
    public void initialise(BiomeMastery main) {
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
    }

    @Override public ItemStack getTexturedSkull(ItemStack itemStack, String textureInput, CommandSender sender) {
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(extractTextureUrl(textureInput)));
            profile.setTextures(textures);

            meta.setOwnerProfile(profile);
        } catch (Exception ex) {
            customLogger.logToPlayer(sender, null, messageManager.get(Message.TEXTUREFAILED));
            itemStack.setType(Material.GRASS_BLOCK);

            return itemStack;
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private String extractTextureUrl(String base64) {
        String decoded = new String(Base64.getDecoder().decode(base64));
        return decoded.split("\"url\":\"")[1].split("\"")[0];
    }
}
