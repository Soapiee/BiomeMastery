package me.soapiee.biomemastery.logic.rewards.types;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.effects.Effect;
import me.soapiee.biomemastery.logic.effects.EffectType;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.manager.EffectsManager;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.entity.Player;

public class EffectReward extends Reward {

    @Getter private final Effect effect;
    private final PlayerDataManager playerDataManager;

    public EffectReward(BiomeMastery main,
                        PlayerDataManager playerDataManager,
                        EffectsManager effectsManager,
                        EffectType effect,
                        boolean isSingular) {
        super(RewardType.EFFECT, isSingular, main.getMessageManager());
        this.effect = effect.getInstance(main, effectsManager.getConfig());
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void give(Player player) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;

        Effect conflict = effect.hasConflict(playerData);
        if (conflict != null) {
            player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDCONFLICT, toString(), conflict + " effect")));
            return;
        }

        effect.activate(player);
        playerData.addActiveReward(this);
        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, toString())));
    }

    public void remove(Player player) {
        effect.deActivate(player);

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData != null) playerData.clearActiveReward(this);
    }

    @Override
    public String toString() {
        return effect.toString() + " effect";
    }
}
