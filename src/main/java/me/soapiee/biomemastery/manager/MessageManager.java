package me.soapiee.biomemastery.manager;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.core.Path;
import me.soapiee.biomemastery.logic.BiomeData;
import me.soapiee.biomemastery.logic.BiomeLevel;
import me.soapiee.biomemastery.logic.rewards.Reward;
import me.soapiee.biomemastery.utils.Languages;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private final BiomeMastery main;

    private final File file;
    private final YamlConfiguration contents;
    private final String language;

    public MessageManager(BiomeMastery main) {
        this.main = main;

        //TODO: Revert code back in future updates
//        file = new File(main.getDataFolder() + File.separator + "language", language + ".yml");
//        contents = new YamlConfiguration();

//        load(null);

        String languageString = validateLanguage();
        if (languageString == null) language = Languages.LANG_EN.toString().toLowerCase();
        else language = languageString;

        file = getFile();
        contents = new YamlConfiguration();

        load(null);

        if (languageString == null) Utils.consoleMsg(get(Message.INVALIDLANGUAGE));
    }

    private File getFile() {
        File newLangFile = new File(main.getDataFolder() + File.separator + "language", language + ".yml");
        if (!newLangFile.exists()) main.saveResource("language" + File.separator + language + ".yml", false);

        File legacyFile = new File(main.getDataFolder() + File.separator + "messages.yml");
        if (legacyFile.exists()) {
            Utils.consoleMsg(ChatColor.RED.toString() + ChatColor.BOLD + "[IMPORTANT] " + ChatColor.RESET
                    + ChatColor.RED + "Please transfer the contents of your messages.yml file to the new language file. Located in the \"language\" folder. Then delete the messages.yml file");
            return legacyFile;
        }

        return newLangFile;
    }

    private String validateLanguage() {
        String configString = main.getConfig().getString("settings.language", "lang_en");

        Languages lang;
        try {
            lang = Languages.valueOf(configString.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }

        return lang.toString().toLowerCase();
    }

    public boolean load(CommandSender sender) {
        //TODO: Revert code back in future updates
//        if (!file.exists()) main.saveResource("language" + File.separator + language + ".yml", false);

        try {
            contents.load(file);
        } catch (Exception ex) {
            if (sender != null) {
                main.getCustomLogger().logToPlayer(sender, ex, get(Message.LANGUAGEFILEERROR));
            } else ex.printStackTrace();
            return false;
        }
        return true;
    }

    private void save() {
        try {
            contents.save(file);
        } catch (Exception ex) {
            main.getCustomLogger().logToFile(ex, get(Message.LANGUAGEFIELDERROR));
        }
    }

    public String getPrefix(Message messageEnum) {
        if (messageEnum == Message.UPDATEAVAILABLE
                || messageEnum == Message.HOOKEDPLACEHOLDERAPI || messageEnum == Message.HOOKEDVAULT
                || messageEnum == Message.HOOKEDVAULTERROR || messageEnum == Message.MAJORDATAERROR
                || messageEnum == Message.INVALIDLANGUAGE
                || messageEnum == Message.LANGUAGEFIELDERROR || messageEnum == Message.LANGUAGEFILEERROR
                || messageEnum == Message.DATASAVEERROR || messageEnum == Message.COOLDOWNFILECREATE
                || messageEnum == Message.COOLDOWNFILELOAD || messageEnum == Message.COOLDOWNFILESAVE
                || messageEnum == Message.PENDINGFILECREATE || messageEnum == Message.PENDINGFILELOAD
                || messageEnum == Message.PENDINGFILESAVE || messageEnum == Message.MISSINGREWARD
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
                || messageEnum == Message.INVALIDGUIMATERIAL || messageEnum == Message.INVALIDGUISLOT
                || messageEnum == Message.INVALIDCONFLICTTYPE || messageEnum == Message.REWARDGUIACTIVATE
                || messageEnum == Message.REWARDGUIDEACTIVATE
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

            save();
            return getPrefix(messageEnum) + defaultText;
        }
    }

    public String getWithPlaceholder(Message messageEnum, Path path) {
        String message = get(messageEnum);
        if (message == null) return null;

        return message.replace("%invalid_field%", path.getPath());
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, BiomeData biomeData, BiomeLevel biomeLevel) {
        String message = get(messageEnum);
        if (message == null) return null;

        int currentLevel = biomeLevel.getLevel();

        if (message.contains("%biome%"))
            message = message.replace("%biome%", Utils.capitalise(biomeData.getBiomeName()));
        if (message.contains("%player_name%")) message = message.replace("%player_name%", playerName);
        if (message.contains("%player_level%"))
            message = message.replace("%player_level%", String.valueOf(currentLevel));
        if (message.contains("%biome_max_level%"))
            message = message.replace("%biome_max_level%", String.valueOf(biomeData.getMaxLevel()));
        if (message.contains("%player_progress%"))
            message = message.replace("%player_progress%", Utils.formatTargetDuration(biomeLevel.getProgress()));
        if (message.contains("%target_duration_formatted%"))
            message = message.replace("%target_duration_formatted%", Utils.formatTargetDuration(biomeData.getTargetDuration(currentLevel)));

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, String string) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%player_name%")) message = message.replace("%player_name%", string);
        if (message.contains("%invalid_field%")) message = message.replace("%invalid_field%", string);
        if (message.contains("%cmd_label%")) message = message.replace("%cmd_label%", string);
        if (message.contains("%world%")) message = message.replace("%world%", string);
        if (message.contains("%reward%")) message = message.replace("%reward%", string);
        if (message.contains("%input%")) message = message.replace("%input%", string);
        if (message.contains("%biome%")) message = message.replace("%biome%", Utils.capitalise(string));

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, String string1, String string2) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%player_name%")) message = message.replace("%player_name%", string2);
        if (message.contains("%reward%")) message = message.replace("%reward%", string2);
        if (message.contains("%biome%"))
            message = message.replace("%biome%", (string1.equals("levels") ? "default" : string1));
        if (message.contains("%config_level%")) message = message.replace("%config_level%", string2);
        if (message.contains("%conflicting_effect%")) message = message.replace("%conflicting_effect%", string1);
        if (message.contains("%effect%")) message = message.replace("%effect%", string2);

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, int value, String input) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%level%")) message = message.replace("%level%", String.valueOf(value));
        if (message.contains("%level_formatted%"))
            message = message.replace("%level_formatted%", value + (value > 1 ? " levels" : " level"));
        if (message.contains("%progress%")) message = message.replace("%progress%", Utils.formatTargetDuration(value));
        if (message.contains("%max_level%")) message = message.replace("%max_level%", String.valueOf(value));
        if (message.contains("%input%")) message = message.replace("%input%", input);
        if (message.contains("%total_pages%")) message = message.replace("%total_pages%", String.valueOf(value));
        if (message.contains("%biome%")) message = message.replace("%biome%", input);

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, int currentPage, int totalPages) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%current_page%"))
            message = message.replace("%current_page%", String.valueOf(currentPage));
        if (message.contains("%total_pages%")) message = message.replace("%total_pages%", String.valueOf(totalPages));
        if (message.contains("%input%")) message = message.replace("%input%", String.valueOf(currentPage));

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, int level, Reward reward, String string) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%level%")) message = message.replace("%level%", String.valueOf(level));
        if (message.contains("%reward%")) message = message.replace("%reward%", reward.toString());
        if (message.contains("%biome%")) message = message.replace("%biome%", Utils.capitalise(string));
        if (message.contains("%reward_status%")) message = message.replace("%reward_status%", string);

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, int integer) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%level%")) message = message.replace("%level%", String.valueOf(integer));
        if (message.contains("%cooldown%"))
            message = message.replace("%cooldown%", integer + (integer > 1 ? " seconds" : " second"));
        if (message.contains("%current_level%")) message = message.replace("%current_level%", String.valueOf(integer));
        if (message.contains("%input%")) message = message.replace("%input%", String.valueOf(integer));

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, int integer, String biomeName) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%level%"))
            message = message.replace("%level%", integer + (integer > 1 ? " levels" : " level"));
        if (message.contains("%value%")) message = message.replace("%value%", String.valueOf(integer));
        if (message.contains("%progress%"))
            message = message.replace("%progress%", Utils.formatTargetDuration(integer));
        if (message.contains("%biome%")) message = message.replace("%biome%", biomeName);
        if (message.contains("%player_name%")) message = message.replace("%player_name%", playerName);

        return message;
    }

    public String getWithPlaceholder(Message messageEnum, String string1, String string2, Message invalidObject) {
        String message = get(messageEnum);
        if (message == null) return null;

        if (message.contains("%biome%"))
            message = message.replace("%biome%", (string1.equals("levels") ? "DEFAULT" : string1));
        if (message.contains("%config_level%")) message = message.replace("%config_level%", string2);
        if (message.contains("%invalid_field%")) message = message.replace("%invalid_field%", get(invalidObject));

        return message;
    }
}
