package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import org.bukkit.scheduler.BukkitRunnable;

public class Checker extends BukkitRunnable {

    public Checker(BiomeMastery main, long delay) {
        runTaskTimer(main, 0, delay * 60);
    }

    @Override
    public void run() {
        // Get a list of all players
        // Loop the list and check their current progress value
        // If progress is above threshold, level them up
    }
}
