package me.soapiee.biomemastery.internals;

import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.List;

public class Biomes_1_16 implements BiomesProvider {

    public List<Biome> getAllMCBiomes(){
        return Arrays.asList(Biome.values());
    }

    public String biomeToString(Biome biome){
        return biome.name();
    }

    public Biome validateBiome(String string){
        Biome biome;

        try {
            biome = Biome.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return biome;
    }
}
