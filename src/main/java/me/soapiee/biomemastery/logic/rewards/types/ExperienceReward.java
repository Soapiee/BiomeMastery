package me.soapiee.biomemastery.logic.rewards.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.entity.Player;

public class ExperienceReward extends Reward {

    private final int amount;

    public ExperienceReward(BiomeMastery main, int amount) {
        super(RewardType.EXPERIENCE, true, main.getMessageManager());
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));
        player.giveExpLevels(amount);
    }

    @Override
    public String toString() {
        return amount + " exp level" + (amount != 1 ? "s" : "");
    }
}
