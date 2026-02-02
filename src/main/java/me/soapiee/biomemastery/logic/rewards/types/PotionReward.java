package me.soapiee.biomemastery.logic.rewards.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionReward extends Reward {

    private final PotionEffect potion;
    private final PlayerDataManager playerDataManager;

    public PotionReward(BiomeMastery main, PlayerDataManager playerDataManager, PotionType potionType, int amplifier, boolean isSingular) {
        super(RewardType.POTION, isSingular, main.getMessageManager());
        potion = new PotionEffect(potionType.getEffectType(), Integer.MAX_VALUE, amplifier);
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void give(Player player) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;

        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, toString())));
        playerData.addActiveReward(this);
        player.addPotionEffect(potion);
    }

    public void remove(Player player){
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;

        playerData.clearActiveReward(this);
        player.removePotionEffect(potion.getType());
    }

    public PotionEffectType getPotion() {
        return potion.getType();
    }

    @Override
    public String toString() {
        return Utils.capitalise(potion.getType().getName()) + " " + potion.getAmplifier();
    }
}
