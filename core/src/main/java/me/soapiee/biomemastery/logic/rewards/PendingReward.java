package me.soapiee.biomemastery.logic.rewards;

import lombok.Getter;

public class PendingReward {

    @Getter private final int level;
    @Getter private final String biome;
    @Getter private final Reward reward;

    public PendingReward(int level, String biome, Reward reward) {
        this.level = level;
        this.biome = biome;
        this.reward = reward;
    }
}
