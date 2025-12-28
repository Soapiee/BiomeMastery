package me.soapiee.biomemastery.commands.adminCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DisableSub extends AbstractAdminSub {

    @Getter private final String IDENTIFIER = "disable";

    public DisableSub(BiomeMastery main) {
        super(main, null, 2, 2);
    }

    // /abm disable <world>
    // /abm disable <biome>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!checkRequirements(sender, args, label)) return;

        World inputWorld = validateWorld(args[1]);
        Biome inputBiome = validateBiome(args[1]);

        if (inputWorld == null && inputBiome == null) {
            sendMessage(sender, messageManager.get(Message.INVALIDWORLDBIOME));
            return;
        }

        if (inputWorld != null) disableWorld(sender, inputWorld);
        if (inputBiome != null) disableBiome(sender, inputBiome);
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

    private void disableWorld(CommandSender sender, World inputWorld) {
        Message message;

        List<World> configEnabledWorlds = configManager.generateEnabledWorldsList();

        if (!configEnabledWorlds.contains(inputWorld)) message = Message.WORLDALREADYDISABLED;
        else {
            saveWorldList(inputWorld.getName());
            message = Message.WORLDDISABLED;
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputWorld.getName()));
    }

    private void saveWorldList(String worldString) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> worldList = new ArrayList<>();
        String worldListPath = "default_biome_settings.enabled_worlds";

        if (config.isSet(worldListPath)) worldList.addAll(config.getStringList(worldListPath));
        worldList.remove(worldString);

        config.set("default_biome_settings.enabled_worlds", worldList);
        main.saveConfig();
    }

    private void disableBiome(CommandSender sender, Biome inputBiome) {
        Message message;
        List<Biome> configEnabledBiomes = configManager.generateEnabledBiomesList();

        if (!configEnabledBiomes.contains(inputBiome)) message = Message.BIOMEALREADYDISABLED;
        else {
            saveBiomeList(inputBiome.name());
            message = Message.BIOMEDISABLED;
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputBiome.name()));
    }

    private void saveBiomeList(String biomeString) {
        FileConfiguration config = main.getConfig();
        String blackListPath = "default_biome_settings.biomes_blacklist";
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist");

        List<String> biomeList = (config.isSet(blackListPath) ? config.getStringList(blackListPath) : new ArrayList<>());

        if (whiteList) biomeList.remove(biomeString);
        else biomeList.add(biomeString);

        config.set(blackListPath, biomeList);
        main.saveConfig();
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
