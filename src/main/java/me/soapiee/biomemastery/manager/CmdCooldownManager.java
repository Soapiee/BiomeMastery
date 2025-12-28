package me.soapiee.biomemastery.manager;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.util.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

public class CmdCooldownManager {

    private final CustomLogger customLogger;
    private final File file;
    private YamlConfiguration contents;

    private final HashMap<UUID, LocalDateTime> cooldowns;
    private int threshold;

    public CmdCooldownManager(BiomeMastery main, int threshold) {
        customLogger = main.getCustomLogger();
        file = new File(main.getDataFolder() + File.separator + "Data", "cooldowns.yml");
        contents = new YamlConfiguration();
        cooldowns = new HashMap<>();
        this.threshold = Math.max(threshold, 1);

        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                contents.save(file);
            } catch (Exception error) {
                customLogger.logToFile(error, ChatColor.RED + "Could not create the cooldowns file");
            }
            return;
        }

        try {
            contents.load(file);

            for (String key : contents.getKeys(true)) {
                UUID uuid = UUID.fromString(key);
                if (!Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) continue;

                String startTime = contents.getString(key);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.parse(startTime, dtf);
                addCooldown(uuid, time);
            }

        } catch (Exception error) {
            customLogger.logToFile(error, ChatColor.RED + "Could not load the cooldowns file");
        }
    }

    public void save() {
        contents = new YamlConfiguration();
        try {
            for (HashMap.Entry<UUID, LocalDateTime> entry : cooldowns.entrySet()) {
                UUID uuid = entry.getKey();
                if (getCooldownLong(uuid, false) <= 0) continue;
                contents.set(uuid.toString(), getStartTime(entry.getValue()));
            }

            contents.save(file);
        } catch (Exception error) {
            customLogger.logToFile(error, ChatColor.RED + "Could not save the cooldowns file");
        }
    }

    public void updateThreshold(int threshold) {
        this.threshold = Math.max(threshold, 1);
    }

    public void addCooldown(CommandSender sender) {
        UUID uuid = getUUID(sender);
        cooldowns.put(uuid, LocalDateTime.now());
    }

    public void addCooldown(UUID uuid, LocalDateTime time) {
        cooldowns.put(uuid, time);
    }

    private UUID getUUID(CommandSender sender) {
        UUID uuid;
        if (sender instanceof Player) uuid = ((Player) sender).getUniqueId();
        else uuid = UUID.fromString("2fcaf22d-9f2d-41f3-bb31-ff220e85c685");

        return uuid;
    }

    public long getCooldown(CommandSender sender) {
        UUID uuid = getUUID(sender);
        return getCooldownLong(uuid, true);
    }

    private long getCooldownLong(UUID uuid, boolean clean) {
        if (!cooldowns.containsKey(uuid)) return 0;

        long difference = ChronoUnit.SECONDS.between(cooldowns.get(uuid), LocalDateTime.now());
        long timeRemaining = threshold - difference;

        if (clean && timeRemaining <= 0) cooldowns.remove(uuid);

        return (timeRemaining < 0 ? 0 : timeRemaining);
    }

    private String getStartTime(LocalDateTime time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
        return time.format(dtf);
    }
}
