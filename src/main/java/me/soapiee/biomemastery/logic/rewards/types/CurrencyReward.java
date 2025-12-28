package me.soapiee.biomemastery.logic.rewards.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.hooks.VaultHook;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.entity.Player;

public class CurrencyReward extends Reward {

    private final VaultHook vaultHook;
    private final double amount;

    public CurrencyReward(BiomeMastery main, double amount) {
        super(RewardType.CURRENCY, true, main.getMessageManager());
        this.vaultHook = main.getVaultHook();
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));
        vaultHook.deposit(player, amount);
    }

    @Override
    public String toString() {
        return amount + vaultHook.getCurrencyName();
    }
}
