package me.soapiee.biomemastery.internals;

import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Potion_1_16 implements PotionsProvider {

    @Override public PotionEffectType getPotionEffectType(String potionString) {
        PotionEffectType potionEffectType;

        try {
            potionEffectType = PotionType.valueOf(potionString.toUpperCase()).getEffectType();
        } catch (IllegalArgumentException e) {
            potionEffectType = null;
        }

        return potionEffectType;
    }

    @Override public String toString(PotionEffectType potionEffectType, int amplifier) {
        return Utils.capitalise(potionEffectType.getName()) + " " + (amplifier +1);
    }
}
