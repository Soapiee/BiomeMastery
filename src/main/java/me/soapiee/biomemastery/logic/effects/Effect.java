package me.soapiee.biomemastery.logic.effects;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.listeners.EffectsListener;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Effect implements EffectInterface{

    protected final BiomeMastery main;
    protected final CustomLogger customLogger;
    protected final MessageManager messageManager;
    protected final EffectsListener listener;

    @Getter private final EffectType type;
    protected final String identifier;
    private final Sound sound;
    @Getter private final HashSet<EffectType> conflicts = new HashSet<>();

    public Effect(BiomeMastery main, FileConfiguration config, EffectType effectType) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        listener = main.getEffectsListener();
        type = effectType;

        String key = type.name();
        identifier = config.getString(key + ".friendly_name", key);
        sound = validateSound(config.getString(key + ".activation_sound", null));
        conflicts.addAll(loadConflicts(config));
    }

    protected Sound validateSound(String string){
        if (string == null || string.equalsIgnoreCase("null")) return null;

        Sound sound;
        try {
            sound = Sound.valueOf(string);
        } catch (IllegalArgumentException error){
            sound = null;
            customLogger.logToFile(error, messageManager.getWithPlaceholder(Message.INVALIDSOUND, string));
        }

        return sound;
    }

    protected void playerSound(Player player){
        if (sound == null) return;
        player.playSound(player.getLocation(), sound, 5, 1);
    }

    private HashSet<EffectType> loadConflicts(FileConfiguration config) {
        HashSet<EffectType> result = new HashSet<>();

        String stringType = getType().name();
        if (config.isSet(stringType + ".effect_conflicts")){
            for (String conflict : config.getStringList(stringType + ".effect_conflicts")) {
                try {
                    result.add(EffectType.valueOf(conflict.toUpperCase()));
                } catch (IllegalArgumentException error) {
                    customLogger.logToFile(error, messageManager.getWithPlaceholder(Message.INVALIDCONFLICTTYPE, stringType, conflict));
                }
            }
        }

        for (EffectType type : EffectType.values()){
            if (!config.isConfigurationSection(type.name())) continue;

            for (String effectSection : config.getConfigurationSection(type.name()).getKeys(false)){
                for (String conflict : config.getStringList(effectSection + ".effect_conflicts")) {
                    if (conflict.equalsIgnoreCase(getType().name())) result.add(type);
                }
            }
        }

        return result;
    }

    public Effect hasConflict(PlayerData playerData) {
        if (getConflicts().isEmpty()) return null;

        List<EffectReward> activeEffects = getActiveEffects(playerData);
        if (activeEffects.isEmpty()) return null;

        for (EffectType type : getConflicts()) {
            for (EffectReward reward : activeEffects) {
                Effect conflict = reward.getEffect();
                if (conflict.getType() == type) return conflict;
            }
        }

        return null;
    }

    private List<EffectReward> getActiveEffects(PlayerData playerData){
        return playerData.getActiveRewards().stream()
                .filter(r -> r.getType() == RewardType.EFFECT)
                .map(r -> (EffectReward) r)
                .collect(Collectors.toList());
    }

    @Override
    public abstract void activate(Player player);

    @Override
    public abstract void deActivate(Player player);

    @Override
    public abstract boolean isActive(Player player);

    @Override
    public String toString() {
        return identifier;
    }

}
