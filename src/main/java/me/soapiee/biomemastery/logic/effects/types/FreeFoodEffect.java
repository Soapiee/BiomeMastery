package me.soapiee.biomemastery.logic.effects.types;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.effects.Effect;
import me.soapiee.biomemastery.logic.effects.EffectType;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreeFoodEffect extends Effect {

    private static final EffectType TYPE = EffectType.FREEFOOD;
    @Getter private final Map<UUID, BukkitTask> activePlayers = new ConcurrentHashMap<>();

    private final long cooldown;
    private static final long COOLDOWN_MIN = 1;
    private static final long COOLDOWN_DEFAULT = 10;
    private final int hungerToIncrease;
    private static final int HUNGER_MIN = 1;
    private static final int HUNGER_MAX = 20;
    private static final int HUNGER_DEFAULT = 1;

    public FreeFoodEffect(BiomeMastery main, FileConfiguration config) {
        super(main, config, TYPE);

        String key = TYPE.name();
        cooldown = loadCooldown(config, key);
        hungerToIncrease = loadHungerIncrease(config, key);
    }

    private long loadCooldown(FileConfiguration config, String path) {
        double cd = config.getDouble(path + ".cooldown", COOLDOWN_DEFAULT);
        return (long) Math.max(cd, COOLDOWN_MIN);
    }

    private int loadHungerIncrease(FileConfiguration config, String path) {
        int hunger = config.getInt(path + ".hunger_to_restore", HUNGER_DEFAULT);
        if (hunger < HUNGER_MIN || hunger > HUNGER_MAX) return HUNGER_DEFAULT;

        return hunger;
    }

    @Override
    public void activate(Player player) {
        UUID uuid = player.getUniqueId();

        playerSound(player);
        activePlayers.computeIfAbsent(uuid, id -> new BukkitRunnable() {
            @Override
            public void run() {
                int oldHunger = player.getFoodLevel();
                if (oldHunger >= HUNGER_MAX) return;

                player.setFoodLevel(Math.min(HUNGER_MAX, oldHunger + hungerToIncrease));

                if (main.getDataManager().getConfigManager().isDebugMode())
                    Utils.debugMsg(player.getName(), ChatColor.BLUE.toString() + hungerToIncrease + " hunger added");

            }
        }.runTaskTimer(main, cooldown * 20L, cooldown * 20L));
    }

    @Override
    public void deActivate(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activePlayers.containsKey(uuid)) return;

        BukkitTask task = activePlayers.remove(uuid);
        if (task != null) task.cancel();
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.containsKey(player.getUniqueId());
    }
}
