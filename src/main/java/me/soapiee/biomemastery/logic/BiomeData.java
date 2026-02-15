package me.soapiee.biomemastery.logic;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.core.Icon;
import me.soapiee.biomemastery.gui.core.Path;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardFactory;
import me.soapiee.biomemastery.manager.ConfigGUIManager;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class BiomeData {

    private final BiomeMastery main;
    private final ConfigGUIManager configGUIManager;

    @Getter private final Biome biome;
    @Getter private final String biomeName;
    private final Map<Integer, Integer> levels = new HashMap<>();
    private final Map<Integer, Reward> rewards = new HashMap<>();

    private final String path;
    @Getter private Icon icon;

    public BiomeData(BiomeMastery main, RewardFactory rewardFactory, Biome biome) {
        this.main = main;
        this.biome = biome;
        biomeName = biome.name();
        path = Path.SPECIFIC_BIOME_DATA.getPath().replace("{biome}", biomeName);
        configGUIManager = main.getConfigGUIManager();
        icon = configGUIManager.getIconFactory().createIcon(path, Bukkit.getConsoleSender(), configGUIManager.getBiomePageSettings());

        setRewards(rewardFactory);
    }

    private void setRewards(RewardFactory rewardFactory) {
        ConfigManager configManager = main.getConfigManager();
        FileConfiguration config = main.getConfig();
        boolean isDefault = config.getConfigurationSection(path) == null;
        if (configManager.isDebugMode()) Utils.debugMsg("", biomeName + "&e is default: " + isDefault);

        if (isDefault) {
            levels.putAll(configManager.getDefaultLevelsThresholds());
            rewards.putAll(configManager.getDefaultRewards());

        } else {
            for (String key : config.getConfigurationSection(path + ".levels").getKeys(false)) {
                int level = Integer.parseInt(key);
                String extendedPath = path + ".levels." + level + ".";
                levels.put(level, config.getInt(extendedPath + "target_duration"));
                rewards.put(level, rewardFactory.create(extendedPath));
            }
        }
    }

    public int getTargetDuration(int level) {
        return levels.getOrDefault(level + 1, 0);
    }

    public Reward getReward(int level) {
        return rewards.get(level);
    }

    public int getMaxLevel() {
        int max = 0;

        for (Integer level : levels.keySet()) {
            if (level >= max) max = level;
        }

        return max;
    }

    public void setIcon(CommandSender sender) {
        icon = configGUIManager.getIconFactory().createIcon(path, sender, configGUIManager.getBiomePageSettings());
    }

}
