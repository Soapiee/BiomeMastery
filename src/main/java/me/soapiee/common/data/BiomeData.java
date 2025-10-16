package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.Reward;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class BiomeData {

    private final Biome biome;
    private final HashMap<Integer, Integer> levels;
    private final HashMap<Integer, Reward> rewards;

    public BiomeData(BiomeMastery main, Biome biome, boolean isDefault) {
        this.biome = biome;
        levels = new HashMap<>();
        rewards = new HashMap<>();

        DataManager dataManager = main.getDataManager();
        FileConfiguration config = main.getConfig();

        if (isDefault) {
            levels.putAll(dataManager.getDefaultLevels());
            rewards.putAll(dataManager.getDefaultRewards());

        } else {
            String biomeName = biome.name();
            for (String key : config.getConfigurationSection("Biomes." + biomeName).getKeys(false)) {
                int level = Integer.parseInt(key);
                levels.put(level, config.getInt("Biomes." + biomeName + "." + level + ".target_duration"));
                rewards.put(level, new Reward(main, "Biomes." + biomeName + "." + level + "."));
            }
        }

    }

    public int getTargetDuration(int level) {
        return levels.get(level);
    }

    public Reward getReward(int level) {
        return rewards.get(level);
    }

    public Biome getBiome() {
        return biome;
    }
}
