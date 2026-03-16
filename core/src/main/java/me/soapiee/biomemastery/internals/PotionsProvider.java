package me.soapiee.biomemastery.internals;

import org.bukkit.potion.PotionEffectType;

public interface PotionsProvider {

    PotionEffectType getPotionEffectType(String potionString);

    String toString(PotionEffectType potionEffectType, int amplifier);

}
