package me.soapiee.biomemastery.data;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.logic.rewards.types.PotionReward;
import me.soapiee.biomemastery.manager.ConfigManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlayerData {

    @Getter private final OfflinePlayer player;
    private final Map<Biome, BiomeLevel> biomesMap = new HashMap<>();
    @Getter private final ArrayList<Reward> activeRewards = new ArrayList<>();
    private final PlayerStorageHandler storageHandler;

    public PlayerData(BiomeMastery main, @NotNull OfflinePlayer player) {
        this.player = player;

        ConfigManager configManager = main.getConfigManager();
        storageHandler = (configManager.isDatabaseEnabled() ? new PlayerDatabaseStorage(main, this) : new PlayerFileStorage(main, this));
    }

    public CompletableFuture<PlayerData> load() {
        return storageHandler.readData();
    }

    public void saveData(boolean async) {
        storageHandler.saveData(async);
    }

    public BiomeLevel getBiomeLevel(Biome biome) {
        return biomesMap.get(biome);
    }

    public ArrayList<BiomeLevel> getBiomeLevels() {
        return new ArrayList<>(biomesMap.values());
    }

    public boolean hasActiveRewards() {
        return !activeRewards.isEmpty();
    }

    public void addActiveReward(Reward reward) {
        activeRewards.add(reward);
    }

    public void clearActiveRewards() {
        if (!player.isOnline()) return;

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return;

        ArrayList<Reward> copyActiveRewards = new ArrayList<>(getActiveRewards());

        for (Reward reward : copyActiveRewards) {
            if (reward instanceof PotionReward) {
                ((PotionReward) reward).remove(onlinePlayer);
            }
            if (reward instanceof EffectReward) {
                ((EffectReward) reward).remove(onlinePlayer);
            }
        }
    }

    public void clearActiveReward(Reward reward) {
        activeRewards.remove(reward);
    }

    public void addBiomeLevel(Biome key, BiomeLevel biomeLevel){
        biomesMap.put(key, biomeLevel);
    }
}
