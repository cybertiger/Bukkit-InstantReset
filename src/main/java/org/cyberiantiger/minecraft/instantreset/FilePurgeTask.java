/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;
import org.cyberiantiger.minecraft.util.FileUtils;

/**
 *
 * @author antony
 */
class FilePurgeTask extends BukkitRunnable {
    // Maybe not hardcode this.
    public static final long PURGE_INTERVAL = 20 * 60; // 60 seconds.

    private final InstantReset plugin;

    public FilePurgeTask(InstantReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // May need to worry about synchronized access later.
        // For now, this is never modified by anyone except at startup.
        Set<File> worldSaves = new HashSet<File>();
        for (String world : plugin.getInstantResetWorldNames()) {
            // Try to use a canonical file to prevent some idiot 
            // from deleting all their shit by accident.
            try {
                worldSaves.add(plugin.getInstantResetWorld(world).getWorldSaveDir().getCanonicalFile());
            } catch (IOException ex) {
                worldSaves.add(plugin.getInstantResetWorld(world).getWorldSaveDir().getAbsoluteFile());
            }
        }
        // Files under worldSave should be regular directories.
        // So we just need to make the parent canonical.
        File parentDir = plugin.getWorldDir();
        try {
            parentDir = parentDir.getCanonicalFile();
        } catch (IOException ioe) {
            parentDir = parentDir.getAbsoluteFile();
        }
        boolean success = true;
        for (File file : parentDir.listFiles()) {
            if (worldSaves.contains(file)) {
                plugin.getLogger().log(Level.INFO, "Skipping active world save dir: {0}", file);
            } else if (file.isDirectory()) {
                try {
                    if (!FileUtils.deleteRecursively(file)) {
                        plugin.getLogger().log(Level.WARNING, "Failed to fully delete unused world save: {0}", file);
                        success = false;
                    } else {
                        plugin.getLogger().log(Level.INFO, "Deleted unused world save directory: {0}", file);
                    }
                }  catch (IOException ioe) {
                    // Never thrown, will be removed from method declaration in next release.
                }
            }
        }
        if (success) {
            stop();
        }
    }

    public synchronized void start() {
        // You'd think there was a way to find out if we're actually currently scheduled.
        // but no, that shit's too complicated for us plugin devs.
        try {
            cancel();
        } catch (IllegalStateException e) {
            // srsly bukkit.
        }
        runTaskTimerAsynchronously(plugin, PURGE_INTERVAL, PURGE_INTERVAL);
    }

    public synchronized void stop() {
        try {
            cancel();
        } catch (IllegalStateException e) {
        }
    }
}
