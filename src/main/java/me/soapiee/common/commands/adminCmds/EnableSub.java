package me.soapiee.common.commands.adminCmds;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.commands.SubCmd;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class EnableSub implements SubCmd {

    private final BiomeMastery main;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    @Getter private final String IDENTIFIER = "enable";
    @Getter private final String PERMISSION = null;
    @Getter private final int MIN_ARGS = 2;
    @Getter private final int MAX_ARGS = 2;

    public EnableSub(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();
        configManager = main.getDataManager().getConfigManager();
    }

    // /abm enable <world>
    // /abm enable <biome>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, main, args, label)) return;

        World inputWorld = validateWorld(args[1]);
        Biome inputBiome = validateBiome(args[1]);

        if (inputWorld == null && inputBiome == null) {
            sendMessage(sender, messageManager.get(Message.INVALIDWORLDBIOME));
            return;
        }

        if (inputWorld != null) enableWorld(sender, inputWorld);
        if (inputBiome != null) enableBiome(sender, inputBiome);
    }

    private Biome validateBiome(String input) {
        Biome biome;
        try {
            biome = Biome.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }

        return biome;
    }

    private World validateWorld(String input) {
        return Bukkit.getWorld(input);
    }

    private void enableWorld(CommandSender sender, World inputWorld) {
        Message message;

        if (configManager.isEnabledWorld(inputWorld)) {
            message = Message.WORLDALREADYENABLED;
        } else {
            // Run async
            saveWorldList(inputWorld.getName());
            message = Message.WORLDENABLED;
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputWorld.getName()));
    }

    private void saveWorldList(String worldString) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> worldList = new ArrayList<>();
        String worldListPath = "default_biome_settings.enabled_worlds";

        worldList.add(worldString);
        if (config.isSet(worldListPath)) worldList.addAll(config.getStringList(worldListPath));

        config.set(worldListPath, worldList);
        main.saveConfig();
    }

    private void enableBiome(CommandSender sender, Biome inputBiome) {
        Message message;

        if (configManager.isEnabledBiome(inputBiome)) message = Message.BIOMEALREADYENABLED;
        else {
            saveBiomeList(inputBiome.name());
            message = Message.BIOMEENABLED;
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputBiome.name()));
    }

    private void saveBiomeList(String biomeString) {
        FileConfiguration config = main.getConfig();
        String blackListPath = "default_biome_settings.biomes_blacklist";

        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist");
        List<String> biomeList = (config.isSet(blackListPath) ? config.getStringList(blackListPath) : new ArrayList<>());

        if (whiteList) biomeList.add(biomeString);
        else biomeList.remove(biomeString);

        config.set(blackListPath, biomeList);
        main.saveConfig();
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
