package me.soapiee.common.logic.effects;

import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.logic.rewards.types.EffectReward;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public interface Effect {

    EffectType getType();

    HashSet<EffectType> getConflicts();

    default Effect hasConflict(PlayerData playerData) {
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

    default List<EffectReward> getActiveEffects(PlayerData playerData){
        return playerData.getActiveRewards().stream()
                .filter(r -> r.getType() == RewardType.EFFECT)
                .map(r -> (EffectReward) r)
                .collect(Collectors.toList());
    }

    default HashSet<EffectType> loadConflicts(FileConfiguration config) {
        HashSet<EffectType> result = new HashSet<>();

        String stringType = getType().name();
        if (config.isSet(stringType + ".effect_conflicts")){
            for (String conflict : config.getStringList(stringType + ".effect_conflicts")) {
                try {
                    result.add(EffectType.valueOf(conflict.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
//                main.getCustomLogger().warning("Invalid conflict type: " + conflict);
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

    void activate(Player player);

    void deActivate(Player player);

    boolean isActive(Player player);
}
