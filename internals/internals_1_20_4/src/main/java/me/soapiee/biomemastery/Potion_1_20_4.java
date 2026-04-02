package me.soapiee.biomemastery;

import me.soapiee.biomemastery.internals.PotionsProvider;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class Potion_1_20_4 implements PotionsProvider {

    @Override public PotionEffectType getPotionEffectType(String potionString) {
        PotionType potionType = Registry.POTION.match(potionString);
        if (potionType == null) return null;

        List<PotionEffect> effects = potionType.getPotionEffects();

        PotionEffectType effectType = null;
        if (!effects.isEmpty()) {
            effectType = effects.get(0).getType();
        }

        return effectType;
    }

    @Override public String toString(PotionEffectType potionEffectType, int amplifier) {
        return Utils.capitalise(potionEffectType.getTranslationKey() + " " + (amplifier +1));
    }

}
