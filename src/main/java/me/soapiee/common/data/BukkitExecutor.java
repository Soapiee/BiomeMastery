package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import org.bukkit.Bukkit;

import java.util.concurrent.Executor;

public final class BukkitExecutor {

    private BukkitExecutor() {}

    public static Executor sync(BiomeMastery main) {
        return command -> Bukkit.getScheduler().runTask(main, command);
    }
}
