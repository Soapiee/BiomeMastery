package me.soapiee.biomemastery.logic.rewards.types;

import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;

public class NullReward extends Reward {

    public NullReward() {
        super(RewardType.NONE, true, null);
    }

    @Override
    public String toString() {
        return "No reward";
    }
}
