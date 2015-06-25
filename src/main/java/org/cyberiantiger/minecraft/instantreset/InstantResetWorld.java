/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import java.io.File;
import java.io.IOException;
import org.bukkit.Difficulty;
import org.bukkit.World;

/**
 *
 * @author antony
 */
public class InstantResetWorld {
    private final InstantReset plugin;
    private final String name;
    private final World.Environment env;
    private final Difficulty difficulty;
    private final String template;
    private String worldSave;

    public InstantResetWorld(InstantReset plugin, String name, World.Environment env, Difficulty difficulty, String template, String worldSave) {
        this.plugin = plugin;
        this.name = name;
        this.env = env;
        this.difficulty = difficulty;
        this.template = template;
        this.worldSave = worldSave;
    }

    public String getName() {
        return name;
    }

    public World.Environment getEnvironment() {
        return env;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getTemplate() {
        return template;
    }

    public File getTemplateDir() {
        return new File(plugin.getTemplateDir(), template);
    }

    void setWorldSave(String worldSave) {
        this.worldSave = worldSave;
    }

    public String getWorldSave() {
        return worldSave;
    }

    public File getWorldSaveDir() {
        if (getWorldSave() == null)
            return null;
        else
            return new File(plugin.getWorldDir(), getWorldSave());
    }

    void createWorldSave() {
        try {
            File tmpFile = File.createTempFile(getName() + ".", ".world",plugin.getWorldDir());
            if (!tmpFile.delete()) throw new IOException("Could not delete file: " + tmpFile);
            if (!tmpFile.mkdir()) throw new IOException("Could not make temporary directory: " + tmpFile);
            setWorldSave(tmpFile.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Could not create world folder");
        }
    }

    public void checkValid() throws IllegalStateException {
        if (getTemplate() == null) {
            throw new IllegalStateException("Template dir not specified for worlld: " + getName());
        }
        File templateDir = getTemplateDir();
        if (!templateDir.exists()) {
            throw new IllegalStateException("Template dir: " + templateDir + " for world: " + getName() + " does not exist");
        }
        if (!templateDir.isDirectory()) {
            throw new IllegalStateException("Template dir: " + templateDir + " for world: " + getName() + " is not a directory");
        }
        if (!templateDir.canRead()) {
            throw new IllegalStateException("Template dir: " + templateDir + " for world: " + getName() + " cannot be read");
        }
        if (getWorldSave() == null) {
            createWorldSave();
        }
        File worldSaveDir = getWorldSaveDir();
        if (!worldSaveDir.exists()) {
            throw new IllegalStateException("World save dir: " + worldSaveDir + " for world: " + getName() + " does not exist");
        }
        if (!worldSaveDir.isDirectory()) {
            throw new IllegalStateException("World save dir: " + worldSaveDir + " for world: " + getName() + " is not a directory");
        }
        if (!worldSaveDir.canRead()) {
            throw new IllegalStateException("World save dir: " + worldSaveDir + " for world: " + getName() + " cannot be read");
        }
        if (!worldSaveDir.canWrite()) {
            throw new IllegalStateException("World save dir: " + worldSaveDir + " for world: " + getName() + " cannot be written to");
        }
    }
}
