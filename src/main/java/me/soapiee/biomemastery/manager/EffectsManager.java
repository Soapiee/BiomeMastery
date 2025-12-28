package me.soapiee.biomemastery.manager;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class EffectsManager {

    private final BiomeMastery main;
    private final CustomLogger customLogger;
    private final MessageManager messageManager;

    private final File file;
    @Getter private final YamlConfiguration config;

    public EffectsManager(BiomeMastery main) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        file = new File(main.getDataFolder(), "effects.yml");
        config = new YamlConfiguration();

        load(null);
    }

    public boolean load(CommandSender sender) {
        if (!file.exists()) {
            main.saveResource("effects.yml", false);
        }

        try {
            config.load(file);
        } catch (Exception ex) {
            if (sender != null) {
                customLogger.logToPlayer(sender, ex, messageManager.get(Message.RELOADERROR));
            }
            return false;
        }
        return true;
    }
}
