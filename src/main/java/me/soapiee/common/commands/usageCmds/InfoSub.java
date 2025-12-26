package me.soapiee.common.commands.usageCmds;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public abstract class InfoSub {

    protected final BiomeMastery main;
    protected final ConfigManager configManager;
    protected final BiomeDataManager biomeDataManager;
    protected final MessageManager messageManager;
    protected final Map<Integer, Biome> enabledBiomes;

    public InfoSub(BiomeMastery main) {
        this.main = main;
        this.configManager = main.getDataManager().getConfigManager();
        this.biomeDataManager = main.getDataManager().getBiomeDataManager();
        this.messageManager = main.getMessageManager();
        this.enabledBiomes = createEnabledBiomes();
    }

    private HashMap<Integer, Biome> createEnabledBiomes() {
        HashMap<Integer, Biome> map = new HashMap<>();

        int i = 1;
        for (Biome biome : configManager.getEnabledBiomes()) {
            map.put(i, biome);
            i++;
        }

        return map;
    }
}
