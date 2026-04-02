package me.soapiee.biomemastery.manager;

import lombok.Getter;
import lombok.Setter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.internals.BiomesProvider;
import me.soapiee.biomemastery.logic.effects.EffectInterface;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardFactory;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private FileConfiguration config;
    private final CustomLogger customLogger;
    private final MessageManager messageManager;
    private final BiomesProvider biomesProvider;

    @Getter @Setter private boolean databaseEnabled;
    @Getter private boolean debugMode;
    @Getter private boolean updateNotif;
    @Getter private int biomesPerPage;
    @Getter private int cmdCooldown;
    @Getter private Sound lvlUpSound;
    @Getter private final HashSet<World> enabledWorlds = new HashSet<>();
    @Getter private final HashSet<Biome> enabledBiomes = new HashSet<>();
    @Getter private final HashMap<Integer, Integer> defaultLevelsThresholds = new HashMap<>();
    @Getter private final HashMap<Integer, Reward> defaultRewards = new HashMap<>();
    @Getter private final HashMap<String, EffectInterface> effects = new HashMap<>();
    @Getter private int updateInterval;

    public ConfigManager(BiomeMastery main) {
        config = main.getConfig();
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        biomesProvider = main.getInternalsManager().getBiomesProvider();

        databaseEnabled = config.getBoolean("database.enabled", false);
        debugMode = config.getBoolean("debug_mode", false);
        updateNotif = config.getBoolean("settings.plugin_update_notification", true);
        updateInterval = Math.max(config.getInt("settings.update_interval", 60), 1);
        biomesPerPage = Math.max(config.getInt("settings.biomes_per_page", 5), 1);
        cmdCooldown = Math.max(config.getInt("settings.command_cooldown", 3), 1);
        lvlUpSound = validateSound(config.getString("settings.levelup_sound", null));

        enabledWorlds.addAll(setUpEnabledWords());
        enabledBiomes.addAll(setUpEnabledBiomes());
    }

    private Sound validateSound(String string) {
        if (string == null || string.equalsIgnoreCase("null")) return null;

        Sound sound;
        try {
            sound = Sound.valueOf(string);
        } catch (IllegalArgumentException error) {
            sound = null;
            customLogger.logToFile(error, messageManager.getWithPlaceholder(Message.INVALIDSOUND, string));
        }

        return sound;
    }

    public ArrayList<World> setUpEnabledWords() {
        ArrayList<World> worldList = new ArrayList<>();
        boolean worldsListExists = config.isSet("default_biome_settings.enabled_worlds");
        if (worldsListExists) {
            for (String worldString : config.getStringList("default_biome_settings.enabled_worlds")) {
                World world = Bukkit.getWorld(worldString);
                if (world != null) worldList.add(world);
            }
        }
        return worldList;
    }

    public void setUpDefaultRewards(RewardFactory rewardFactory) {
        ConfigurationSection levelsSection = config.getConfigurationSection("default_biome_settings.levels");
        if (levelsSection != null) {
            for (String key : config.getConfigurationSection("default_biome_settings.levels").getKeys(false)) {
                defaultLevelsThresholds.put(Integer.parseInt(key), config.getInt("default_biome_settings.levels." + key + ".target_duration"));
                defaultRewards.put(Integer.parseInt(key), rewardFactory.create("default_biome_settings.levels." + key + "."));
            }
        }
    }

    public List<Biome> setUpEnabledBiomes() {
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist", true);
        if (!config.isSet("default_biome_settings.biomes_blacklist")) {
            config.set("default_biome_settings.biomes_blacklist", new ArrayList<>());
        }
        List<String> listedBiomes = config.getStringList("default_biome_settings.biomes_blacklist");

        if (whiteList) return createBiomeWhitelist(listedBiomes);
        else return createBiomeBlacklist(listedBiomes);
    }

    public void reload(BiomeMastery main, DataManager dataManager) {
        config = main.getConfig();
        debugMode = config.getBoolean("debug_mode", false);
        updateNotif = config.getBoolean("settings.plugin_update_notification", true);
        updateInterval = Math.max(config.getInt("settings.update_interval", 60), 1);
        biomesPerPage = Math.max(config.getInt("settings.biomes_per_page", 5), 1);
        cmdCooldown = Math.max(config.getInt("settings.command_cooldown", 3), 1);
        dataManager.getCooldownManager().setThreshold(cmdCooldown);
        lvlUpSound = validateSound(config.getString("settings.levelup_sound", null));
    }

    public List<Biome> getAllMCBiomes() {
        return biomesProvider.getAllMCBiomes();
    }

    private String biomeToString(Biome biome) {
        return biomesProvider.biomeToString(biome);
    }


    public List<Biome> createBiomeBlacklist(List<String> listedBiomes) {
        List<Biome> blacklist = new ArrayList<>();

        for (Biome biome : getAllMCBiomes()) {
            String biomeString = biomeToString(biome);
            if (biomeString == null) continue;

            if (listedBiomes.contains(biomeString.toUpperCase())) continue;
            if (listedBiomes.contains(biomeString.toLowerCase())) continue;
            blacklist.add(biome);
        }

        return blacklist;
    }

    public List<Biome> createBiomeWhitelist(List<String> listedBiomes) {
        List<Biome> whitelist = new ArrayList<>();

        for (String rawBiome : listedBiomes) {
            Biome biome = validateBiome(rawBiome);
            if (biome != null) whitelist.add(biome);
        }

        return whitelist;
    }

    private Biome validateBiome(String string) {
        return biomesProvider.validateBiome(string);
    }

    public boolean isEnabledWorld(World world) {
        return enabledWorlds.contains(world);
    }

    public boolean isEnabledBiome(Biome biome) {
        return enabledBiomes.contains(biome);
    }

    public List<Biome> generateEnabledBiomesList() {
        return setUpEnabledBiomes();
    }

    public List<World> generateEnabledWorldsList() {
        return setUpEnabledWords();
    }
}
