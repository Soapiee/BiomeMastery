package me.soapiee.biomemastery.manager;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.rewards.RewardFactory;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.block.Biome;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeDataManager {

    private final BiomeMastery main;
    private final ConfigManager configManager;
    private final RewardFactory rewardFactory;
    private final boolean isDebugMode;

    @Getter private final Map<Biome, BiomeData> biomeDataMap = new ConcurrentHashMap<>();
    @Getter private final Map<Integer, BiomeData> biomeDataOrdered = new ConcurrentHashMap<>();

    public BiomeDataManager(BiomeMastery main, RewardFactory rewardFactory) {
        this.main = main;
        configManager = main.getConfigManager();
        this.rewardFactory = rewardFactory;
        isDebugMode = configManager.isDebugMode();
        createAllBiomeData();
    }

    private void createAllBiomeData() {
        int i = 1;
        for (Biome enabledBiome : configManager.getEnabledBiomes()) {
            createBiomeData(enabledBiome, i);
            i++;
        }
    }

    private void createBiomeData(Biome biome, int index) {
        if (isDebugMode) Utils.debugMsg("", "&eEnabled biome: " + biome.name());

        BiomeData biomeData = new BiomeData(main, rewardFactory, biome);
        biomeDataMap.put(biome, biomeData);
        biomeDataOrdered.put(index, biomeData);
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.getOrDefault(biome, null);
    }

    public BiomeData getBiomeData(String biome) throws IllegalArgumentException {
        return biomeDataMap.getOrDefault(Biome.valueOf(biome), null);
    }
}
