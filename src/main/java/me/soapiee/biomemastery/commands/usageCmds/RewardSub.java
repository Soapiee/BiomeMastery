package me.soapiee.biomemastery.commands.usageCmds;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.data.BukkitExecutor;
import me.soapiee.biomemastery.data.PlayerData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.effects.EffectInterface;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.logic.rewards.types.EffectReward;
import me.soapiee.biomemastery.logic.rewards.types.PotionReward;
import me.soapiee.biomemastery.manager.BiomeDataManager;
import me.soapiee.biomemastery.manager.ConfigManager;
import me.soapiee.biomemastery.manager.PlayerDataManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class RewardSub extends AbstractUsageSub {

    @Getter private final String IDENTIFIER = "reward";

    public RewardSub(BiomeMastery main) {
        super(main, null, 3, 3);
    }

    // /bm reward <biome> <level>
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, Utils.addColour(messageManager.get(Message.MUSTBEPLAYERERROR)));
            return;
        }

        if (!checkRequirements(sender, args, label)) return;

        Biome biome = validateBiome(sender, args, label);
        if (biome == null) return;

        PlayerDataManager playerDataManager = main.getDataManager().getPlayerDataManager();
        CustomLogger logger = main.getCustomLogger();
        playerDataManager.getOrLoad((Player) sender)
                .thenAcceptAsync(data -> toggleReward(data, biome, args[2], label), BukkitExecutor.sync(main))
                .exceptionally(error -> {
                    logger.logToPlayer(sender, error, Utils.addColour(messageManager.getWithPlaceholder(Message.DATAERROR, sender.getName())));
                    return null;
                });
    }

    private Biome validateBiome(CommandSender sender, String[] args, String label) {
        if (args.length != 3) {
            sendMessage(sender,  messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
            return null;
        }

        Biome biome = getBiome(args[1]);

        if (biome == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[1]));
            return null;
        }

        ConfigManager configManager = main.getDataManager().getConfigManager();
        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, args[1]));
            return null;
        }

        return biome;
    }

    private Biome getBiome(String value) {
        Biome biome;
        try {
            biome = Biome.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }

        return biome;
    }

    private void toggleReward(PlayerData playerData, Biome biome, String levelString, String label) {
        Player player = playerData.getPlayer().getPlayer();

        int levelToClaim;
        try {
            levelToClaim = Integer.parseInt(levelString);
        } catch (NumberFormatException error) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, levelString));
            return;
        }

        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        BiomeDataManager biomeDataManager = main.getDataManager().getBiomeDataManager();
        int currentLevel = biomeLevel.getLevel();
        int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

        if (levelToClaim > maxLevel || levelToClaim < 1) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.LEVELOUTOFBOUNDARY, maxLevel, levelString));
            return;
        }

        if (currentLevel < levelToClaim) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDNOTACHIEVED, currentLevel));
            return;
        }

        Reward reward = biomeDataManager.getBiomeData(biome).getReward(levelToClaim);

        if (reward == null) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.PLAYERHELP, label));
            return;
        }

        if (!reward.isSingular()) {
            if (hasThisActiveReward(player, playerData, reward)) {
                deactivateReward(player, reward);
                return;
            }

            if (player.getLocation().getBlock().getBiome() == biome) {
                reward.give(player);
                return;
            }

            sendMessage(player, messageManager.getWithPlaceholder(Message.NOTINBIOME, biome.name(), reward.toString()));

        } else {
            sendMessage(player, messageManager.get(Message.REWARDALREADYCLAIMED));
        }
    }

    private boolean hasThisActiveReward(Player player, PlayerData playerData, Reward reward) {
        if (playerData.hasActiveRewards()) {
            if (reward instanceof PotionReward) {
                PotionEffectType potion = ((PotionReward) reward).getPotion();
                return (player.hasPotionEffect(potion));
            }

            if (reward instanceof EffectReward) {
                EffectInterface effect = ((EffectReward) reward).getEffect();
                return (effect.isActive(player));
            }
        }

        return false;
    }

    private void deactivateReward(Player player, Reward reward) {
        if (reward instanceof PotionReward) ((PotionReward) reward).remove(player);
        if (reward instanceof EffectReward) ((EffectReward) reward).remove(player);

        sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDDEACTIVATED, reward.toString()));
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
