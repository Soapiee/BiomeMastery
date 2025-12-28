package me.soapiee.biomemastery.logic.rewards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.soapiee.biomemastery.manager.MessageManager;

@AllArgsConstructor
public abstract class Reward implements RewardInterface {

    @Getter protected final RewardType type;
    @Getter private final boolean isSingular;
    @Getter protected final MessageManager messageManager;
}
