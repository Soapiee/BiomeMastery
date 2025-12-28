package me.soapiee.biomemastery.logic.rewards.types;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.RewardType;
import me.soapiee.biomemastery.hooks.VaultHook;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PermissionReward extends Reward {

    private final VaultHook vaultHook;
    private final ArrayList<String> permissions;

    public PermissionReward(BiomeMastery main, ArrayList<String> permission) {
        super(RewardType.PERMISSION, true, main.getMessageManager());
        this.vaultHook = main.getVaultHook();
        this.permissions = permission;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.addColour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));

        for (String permission : permissions) {
            vaultHook.setPermission(player, permission);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        builder.append(type.toString().toLowerCase()).append("s: ");
        for (String permission : permissions) {
            builder.append(permission);
            if (permissions.size() > i) builder.append(", ");
            i++;
        }

        return builder.toString();
    }
}
