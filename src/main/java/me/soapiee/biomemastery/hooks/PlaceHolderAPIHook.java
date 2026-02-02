package me.soapiee.biomemastery.hooks;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.manager.DataManager;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class PlaceHolderAPIHook extends PlaceholderExpansion {

    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;

    public PlaceHolderAPIHook(BiomeMastery main) {
        messageManager = main.getMessageManager();
        DataManager dataManager = main.getDataManager();
        configManager = dataManager.getConfigManager();
        playerDataManager = dataManager.getPlayerDataManager();
    }


    @Override
    public @NotNull String getIdentifier() {
        return "biomemastery";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Soapiee";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) return null;

        Player player = offlinePlayer.getPlayer();
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return "no data";

        // plains_biome_level || forest_biome_level
        if (identifier.contains("_level")) return getBiomeLevel(identifier, playerData);

        // plains_biome_progress || forest_biome_progress
        if (identifier.contains("_progress")) return getBiomeProgress(identifier, playerData);

        return null;
    }

    private String getBiomeLevel(String identifier, PlayerData playerData) {
        Biome biome = validateBiome(identifier.replace("_level", ""));
        if (biome == null) return null;

        if (!configManager.isEnabledBiome(biome)) return null;

        return String.valueOf(playerData.getBiomeLevel(biome).getLevel());
    }

    private String getBiomeProgress(String identifier, PlayerData playerData) {
        Biome biome = validateBiome(identifier.replace("_progress", ""));
        if (biome == null) return null;

        if (!configManager.isEnabledBiome(biome)) return null;

        return Utils.formatTargetDuration(playerData.getBiomeLevel(biome).getProgress());
    }

    private Biome validateBiome(String stringBiome){
        Biome biome;
        try {
            biome = Biome.valueOf(stringBiome);
        } catch (IllegalArgumentException error){
            biome = null;
        }

        return biome;
    }

    @Override
    public boolean persist() {
        return true;
    }

}
