package me.soapiee.biomemastery.util;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletionException;


public class CustomLogger {

    private final MessageManager messageManager;
    private final BiomeMastery main;
    private final File logFile;

    public CustomLogger(BiomeMastery main) {
        this.main = main;
        messageManager = main.getMessageManager();

        logFile = new File(main.getDataFolder() + File.separator + "logger.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Utils.consoleMsg(messageManager.get(Message.LOGGERFILEERROR));
            }
        }
    }

    public void logToFile(Throwable error, String string) {
        LogType logType = (error == null) ? LogType.WARNING : LogType.SEVERE;
        if (!string.isEmpty()) Utils.consoleMsg(string);

        Throwable cause = (error instanceof CompletionException && error.getCause() != null) ? error.getCause() : error;

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true), true);
            Date dt = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(dt);
            writer.write("--------------------------------------------------------------------------------------------------");
            writer.write(System.lineSeparator());
            writer.write(time + " [" + logType.name() + "] " + string);
            writer.write(System.lineSeparator());
            writer.write(messageManager.get(Message.PLUGINVERSIONSTRING) + Bukkit.getPluginManager().getPlugin("BiomeMastery").getDescription().getVersion());
            writer.write(System.lineSeparator());
            writer.write(messageManager.get(Message.SERVERVERSIONSTRING) + Bukkit.getBukkitVersion());
            writer.write(System.lineSeparator());
            if (cause != null) {
                writer.write(System.lineSeparator());
                cause.printStackTrace(writer);
            }
            writer.write("--------------------------------------------------------------------------------------------------");
            writer.write(System.lineSeparator());
            writer.write(System.lineSeparator());
            writer.close();
            Utils.consoleMsg(messageManager.get(Message.LOGGERLOGSUCCESS));
        } catch (IOException e) {
            Utils.consoleMsg(messageManager.get(Message.LOGGERLOGERROR));
        }
    }

    public void logToPlayer(CommandSender sender, Throwable error, String string) {
        if (error != null){
            Throwable cause = (error instanceof CompletionException) ? error.getCause() : error;
            if (!string.contains(" successfully created")) logToFile(cause, string);
        }

        if (sender == null) return;
        if (string.isEmpty()) return;

        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (sender instanceof Player && ((Player) sender).isOnline()) {
                sender.sendMessage(Utils.addColour(string));
                return;
            }

            Utils.consoleMsg(string);
        }, 20L);
    }

    enum LogType {
        SEVERE(""),
        WARNING("");

        public final String colour;

        LogType(String colour) {
            this.colour = colour;
        }
    }

}
