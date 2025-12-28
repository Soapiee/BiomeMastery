package me.soapiee.biomemastery.logic.effects.types;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.listeners.EffectsListener;
import me.soapiee.biomemastery.logic.effects.Effect;
import me.soapiee.biomemastery.logic.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class LavaSwimmerEffect implements Effect {

    @Getter private final EffectType type = EffectType.LAVASWIMMER;
    private final String identifier;
    @Getter private final HashSet<EffectType> conflicts = new HashSet<>();
    private final EffectsListener listener;

    private static final int SPEED_MIN = 10;
    private static final int SPEED_MAX = 50;
    private static final int SPEED_DEFAULT = 10;

    public LavaSwimmerEffect(BiomeMastery main, FileConfiguration config) {
        listener = main.getEffectsListener();
        String key = type.name();
        identifier = config.getString(key + ".friendly_name", key);
        conflicts.addAll(loadConflicts(config));

        int speed = loadSpeed(config, key);
        listener.setLavaSwimmingSpeed(speed / 100.0);
    }

    private int loadSpeed(FileConfiguration config, String path) {
        int speed = config.getInt(path + ".speed", SPEED_DEFAULT);
        if (speed < SPEED_MIN || speed > SPEED_MAX) return SPEED_DEFAULT;

        return speed;
    }

    @Override
    public void activate(Player player) {
        UUID uuid = player.getUniqueId();
        if (listener.hasActiveEffect(type, uuid)) return;

        listener.addActiveEffect(type, uuid);
    }

    @Override
    public void deActivate(Player player) {
        UUID uuid = player.getUniqueId();
        if (!listener.hasActiveEffect(type, uuid)) return;

        listener.removeActiveEffect(type, uuid);
    }

    @Override
    public boolean isActive(Player player) {
        return listener.hasActiveEffect(type, player.getUniqueId());
    }

    @Override
    public String toString() {
        return identifier;
    }
}
