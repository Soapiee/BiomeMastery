package me.soapiee.biomemastery.logic.effects;

import org.bukkit.entity.Player;

import java.util.HashSet;

public interface EffectInterface {

    EffectType getType();

    HashSet<EffectType> getConflicts();

    void activate(Player player);

    void deActivate(Player player);

    boolean isActive(Player player);
}
