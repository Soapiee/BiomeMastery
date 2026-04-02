package me.soapiee.biomemastery;

import me.soapiee.biomemastery.internals.BiomesProvider;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.List;
import java.util.stream.Collectors;

public class Biomes_1_21_1 implements BiomesProvider {

    @Override
    public List<Biome> getAllMCBiomes() {
        return Registry.BIOME.stream().collect(Collectors.toList());
    }

    @Override
    public String biomeToString(Biome biome) {
        return biome.getKey().toString().replace("minecraft:", "");
    }

    @Override
    public Biome validateBiome(String string) {
        return Registry.BIOME.match(string);
    }
}
