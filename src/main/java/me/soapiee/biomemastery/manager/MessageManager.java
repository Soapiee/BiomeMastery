package me.soapiee.biomemastery.manager;

import lombok.Setter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.util.CustomLogger;
import me.soapiee.biomemastery.util.Message;
import me.soapiee.biomemastery.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private final BiomeMastery main;
    @Setter private CustomLogger customLogger;

    private final File file;
    private final YamlConfiguration contents;

    public MessageManager(BiomeMastery main) {
        this.main = main;
        file = new File(main.getDataFolder(), "messages.yml");
        contents = new YamlConfiguration();

        load(null);
    }

    public boolean load(CommandSender sender) {
        if (!file.exists()) {
            main.saveResource("messages.yml", false);
        }

        try {
            contents.load(file);
        } catch (Exception ex) {
            if (sender != null) {
//                customLogger.logToPlayer(sender, ex, get(Message.MESSAGESFILEERROR));
                Utils.consoleMsg(get(Message.MESSAGESFILEERROR));
                Bukkit.getLogger().throwing("Message Manager", "load()", ex);
            }
            return false;
        }
        return true;
    }

    public void save(Message messageEnum) {
        try {
            contents.save(file);
            contents.load(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, get(Message.MESSAGESFIELDERROR));
        }
    }

    public String getPrefix(Message messageEnum) {
        if (messageEnum == Message.UPDATEAVAILABLE
                || messageEnum == Message.HOOKEDPLACEHOLDERAPI || messageEnum == Message.HOOKEDVAULT
                || messageEnum == Message.HOOKEDVAULTERROR || messageEnum == Message.MAJORDATAERROR
                || messageEnum == Message.MESSAGESFILEERROR || messageEnum == Message.MESSAGESFIELDERROR
                || messageEnum == Message.DATASAVEERROR || messageEnum == Message.COOLDOWNFILECREATE
                || messageEnum == Message.COOLDOWNFILELOAD || messageEnum == Message.COOLDOWNFILESAVE
                || messageEnum == Message.PENDINGFILECREATE || messageEnum == Message.PENDINGFILELOAD
                || messageEnum == Message.PENDINGFILESAVE || messageEnum == Message.MISSINGREWARD
                || messageEnum == Message.PLUGINVERSIONSTRING || messageEnum == Message.SERVERVERSIONSTRING
                || messageEnum == Message.DATABASECONNECTED || messageEnum == Message.DATABASEFAILED
                || messageEnum == Message.FILESYSTEMACTIVATED || messageEnum == Message.FILEFOLDERERROR
                || messageEnum == Message.LOGGERFILEERROR || messageEnum == Message.LOGGERLOGSUCCESS
                || messageEnum == Message.LOGGERLOGERROR || messageEnum == Message.PLAYERHELP
                || messageEnum == Message.INVALIDREWARD || messageEnum == Message.INVALIDREWARDTYPE
                || messageEnum == Message.INVALIDPOTIONAMP || messageEnum == Message.INVALIDPOTIONTYPE
                || messageEnum == Message.INVALIDEFFECTTYPE || messageEnum == Message.INVALIDVAULTHOOK
                || messageEnum == Message.INVALIDAMOUNT || messageEnum == Message.INVALIDQUANTITY
                || messageEnum == Message.INVALIDMATERIAL || messageEnum == Message.INVALIDPERMISSION
                || messageEnum == Message.INVALIDCOMMAND || messageEnum == Message.INVALIDSOUND
                || messageEnum == Message.INVALIDCONFLICTTYPE
                || messageEnum == Message.ADMINHELP || messageEnum == Message.BIOMEBASICINFOHEADER
                || messageEnum == Message.BIOMEBASICINFOFOOTER || messageEnum == Message.BIOMEBASICINFOPREVBUTTON
                || messageEnum == Message.BIOMEBASICINFONEXTBUTTON || messageEnum == Message.PREVBUTTONHOVER
                || messageEnum == Message.NEXTBUTTONHOVER || messageEnum == Message.BIOMEBASICINFOFORMAT
                || messageEnum == Message.BIOMEBASICINFOMAX || messageEnum == Message.BIOMEBASICINFOHOVER
                || messageEnum == Message.BIOMEDETAILEDFORMAT || messageEnum == Message.BIOMEDETAILEDMAX
                || messageEnum == Message.BIOMEREWARDFORMAT || messageEnum == Message.DISABLEHOVER
                || messageEnum == Message.WORLDTEXTCOLOR || messageEnum == Message.BIOMETEXTCOLOR
                || messageEnum == Message.REWARDUNCLAIMED || messageEnum == Message.REWARDCLAIMED
                || messageEnum == Message.REWARDCLAIMINBIOME || messageEnum == Message.REWARDACTIVATE
                || messageEnum == Message.REWARDDEACTIVATE || messageEnum == Message.WORLDLISTHEADER
                || messageEnum == Message.BIOMELISTHEADER) return "";

        String path = Message.PREFIX.getPath();
        if (contents.isSet(path)) {
            return contents.getString(path).isEmpty() ? "" : contents.getString(path) + " ";
        } else return "";
    }

    public String get(Message messageEnum) {
        String path = messageEnum.getPath();
        String defaultText = messageEnum.getDefaultText();

        if (contents.isSet(path)) {
            String text = ((contents.isList(path)) ? String.join("\n", contents.getStringList(path)) : contents.getString(path));

            return text.isEmpty() ? null : getPrefix(messageEnum) + text;
        } else {
            if (defaultText.contains("\n")) {
                String[] list;
                list = defaultText.split("\n");
                contents.set(path, list);
            } else {
                contents.set(path, defaultText);
            }
            save(messageEnum);
            return getPrefix(messageEnum) + defaultText;
        }
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, BiomeData biomeData, BiomeLevel biomeLevel) {
        String formattedBiomeName = Utils.capitalise(biomeData.getBiomeName());
        int currentLevel = biomeLevel.getLevel();
        String formattedTarget = Utils.formatTargetDuration(biomeData.getTargetDuration(currentLevel));
        String formattedProgress = Utils.formatTargetDuration(biomeLevel.getProgress());

        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%biome%", formattedBiomeName)
                .replace("%player_name%", playerName)
                .replace("%player_level%", String.valueOf(biomeLevel.getLevel()))
                .replace("%biome_max_level%", String.valueOf(biomeData.getMaxLevel()))
                .replace("%player_progress%", formattedProgress)
                .replace("%target_duration_formatted%", formattedTarget);
    }

    public String getWithPlaceholder(Message messageEnum, String string) {
        String message = get(messageEnum);
        if (message == null) return null;

        return get(messageEnum).replace("%player_name%", string)
                .replace("%cmd_label%", string)
                .replace("%world%", string)
                .replace("%reward%", string)
                .replace("%input%", string)
                .replace("%biome%", Utils.capitalise(string));
    }

    public String getWithPlaceholder(Message messageEnum, String string1, String string2) {
        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%player_name%", string2)
                .replace("%reward%", string2)
                .replace("%biome%", (string1.equals("levels") ? "default" : string1))
                .replace("%config_level%", string2)
                .replace("%conflicting_effect%", string1)
                .replace("%effect%", string2);
    }

    public String getWithPlaceholder(Message messageEnum, int value, String input) {
        String message = get(messageEnum);
        if (message == null) return null;

        String valueString = String.valueOf(value);
        return message.replace("%level%", valueString)
                .replace("%level_formatted%", valueString + (value > 1 ? " levels" : " level"))
                .replace("%progress%", Utils.formatTargetDuration(value))
                .replace("%max_level%", valueString)
                .replace("%input%", input)
                .replace("%total_pages%", String.valueOf(value))
                .replace("%biome%", input);
    }

    public String getWithPlaceholder(Message messageEnum, int currentPage, int totalPages) {
        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%current_page%", String.valueOf(currentPage))
                .replace("%total_pages%", String.valueOf(totalPages))
                .replace("%input%", String.valueOf(currentPage));
    }

    public String getWithPlaceholder(Message messageEnum, int level, Reward reward, String string) {
        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%level%", String.valueOf(level))
                .replace("%reward%", reward.toString())
                .replace("%biome%", Utils.capitalise(string))
                .replace("%reward_status%", string);
    }

    public String getWithPlaceholder(Message messageEnum, int integer) {
        String message = get(messageEnum);
        if (message == null) return null;

        String string = String.valueOf(integer);
        return message.replace("%level%", string)
                .replace("%cooldown%", string + (integer > 1 ? " seconds" : " second"))
                .replace("%current_level%", string)
                .replace("%input%", string);
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, int integer, String biomeName) {
        String message = get(messageEnum);
        if (message == null) return null;

        String string = String.valueOf(integer);
        return message.replace("%level%", string + (integer > 1 ? " levels" : " level"))
                .replace("%value%", string)
                .replace("%progress%", Utils.formatTargetDuration(integer))
                .replace("%biome%", biomeName)
                .replace("%player_name%", playerName);
    }

    public String getWithPlaceholder(Message messageEnum, String string1, String string2, Message invalidObject) {
        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%biome%", (string1.equals("levels") ? "DEFAULT" : string1))
                .replace("%config_level%", string2)
                .replace("%invalid_field%", get(invalidObject));
    }
}
