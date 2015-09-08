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
class FilePurgeTask implements Runnable {
    // Maybe not hardcode this.
    public static final long PURGE_INTERVAL = 20 * 60; // 60 seconds.

    private final InstantReset plugin;
    private int taskId = -1;

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
            InstantResetWorld instantResetWorld = plugin.getInstantResetWorld(world);
            File worldSaveDir = instantResetWorld.getWorldSaveDir();
            if (worldSaveDir != null) {
                try {
                    worldSaves.add(worldSaveDir.getCanonicalFile());
                } catch (IOException ex) {
                    worldSaves.add(worldSaveDir.getAbsoluteFile());
                }
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
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : parentDir.listFiles()) {
                if (worldSaves.contains(file)) {
                    plugin.getLogger().log(Level.INFO, "Skipping active world save dir: {0}", file);
                } else if (file.isDirectory()) {
                    if (!FileUtils.deleteRecursively(file)) {
                        plugin.getLogger().log(Level.WARNING, "Failed to fully delete unused world save: {0}", file);
                        success = false;
                    } else {
                        plugin.getLogger().log(Level.INFO, "Deleted unused world save directory: {0}", file);
                    }
                }
            }
        }
        if (success) {
            stop();
        }
    }

    private void cancel() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public synchronized void start() {
        cancel();
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, PURGE_INTERVAL, PURGE_INTERVAL);
    }

    public synchronized void stop() {
        cancel();
    }
}
