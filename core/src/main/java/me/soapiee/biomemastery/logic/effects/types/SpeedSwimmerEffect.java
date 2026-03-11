package me.soapiee.biomemastery.logic.effects.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.effects.Effect;
import me.soapiee.biomemastery.logic.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpeedSwimmerEffect extends Effect {

    private static final EffectType TYPE = EffectType.SPEEDSWIMMER;
//    @Getter private final HashSet<EffectType> conflicts = new HashSet<>();

    private static final double SPEED_MIN = 0.1;
    private static final double SPEED_MAX = 1;
    private static final double SPEED_DEFAULT = 0.4;

    public SpeedSwimmerEffect(BiomeMastery main, FileConfiguration config) {
        super(main, config, TYPE);

        String key = TYPE.name();
        double speed = loadSpeed(config, key);
        listener.setWaterSwimmingSpeed(speed);
    }

    private double loadSpeed(FileConfiguration config, String path) {
        double speed = config.getDouble(path + ".speed", SPEED_DEFAULT);
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

    public void deActivate(Player player) {
        UUID uuid = player.getUniqueId();
        if (!listener.hasActiveEffect(TYPE, uuid)) return;

//        player.setWalkSpeed(DEFAULT_WALK_SPEED);
        listener.removeActiveEffect(TYPE, uuid);
    }

    public boolean isActive(Player player) {
        return listener.hasActiveEffect(TYPE, player.getUniqueId());
    }
}
