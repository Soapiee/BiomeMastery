package me.soapiee.biomemastery.logic.effects.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.effects.Effect;
import me.soapiee.biomemastery.logic.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LavaSwimmerEffect extends Effect {

    private static final EffectType TYPE = EffectType.LAVASWIMMER;

    private static final int SPEED_MIN = 10;
    private static final int SPEED_MAX = 50;
    private static final int SPEED_DEFAULT = 10;

    public LavaSwimmerEffect(BiomeMastery main, FileConfiguration config) {
        super(main, config, TYPE);

        String key = TYPE.name();
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
        if (listener.hasActiveEffect(TYPE, uuid)) return;

        playerSound(player);
        listener.addActiveEffect(TYPE, uuid);
    }

    @Override
    public void deActivate(Player player) {
        UUID uuid = player.getUniqueId();
        if (!listener.hasActiveEffect(TYPE, uuid)) return;

        listener.removeActiveEffect(TYPE, uuid);
    }

    @Override
    public boolean isActive(Player player) {
        return listener.hasActiveEffect(TYPE, player.getUniqueId());
    }

}
