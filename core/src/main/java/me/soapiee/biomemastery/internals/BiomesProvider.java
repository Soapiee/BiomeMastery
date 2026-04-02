package me.soapiee.biomemastery.internals;

import me.soapiee.biomemastery.BiomeMastery;
import org.bukkit.block.Biome;

import java.util.List;

public interface BiomesProvider {

    default void initialise(BiomeMastery main) {}

    List<Biome> getAllMCBiomes();
    String biomeToString(Biome biome);
    Biome validateBiome(String string);

}
