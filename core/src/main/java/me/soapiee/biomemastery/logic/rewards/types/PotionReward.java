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

public class PotionReward extends Reward {

    private final PotionEffectType potionEffectType;
    private final PotionEffect potion;
    private final PlayerDataManager playerDataManager;

    public PotionReward(BiomeMastery main, PlayerDataManager playerDataManager, PotionEffectType potionEffectType, int amplifier, boolean isSingular) {
        super(RewardType.POTION, isSingular, main.getMessageManager());

        this.potionEffectType = potionEffectType;
        potion = potionEffectType.createEffect(Integer.MAX_VALUE, amplifier);
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

    public void remove(Player player) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;

        playerData.clearActiveReward(this);
        player.removePotionEffect(potionEffectType);
    }

    public PotionEffectType getPotion() {
        return potionEffectType;
    }

    @Override
    public String toString() {
//        return Utils.capitalise(potionEffectType.getTranslationKey()) + " " + potion.getAmplifier();
        return Utils.capitalise(potionEffectType.getName()) + " " + potion.getAmplifier();
    }
}
