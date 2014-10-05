/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyberiantiger.minecraft.unsafe.CBShim;
import org.cyberiantiger.minecraft.unsafe.InstanceTools;

/**
 *
 * @author antony
 */
public class InstantReset extends JavaPlugin {
    private static final String COMMAND_NAME = "ir";

    private Map<String, SubCommand> subcommands = new HashMap<String, SubCommand>();

    private File templateDir;
    private File worldDir;
    private boolean resetOnRestart;
    private final Map<String, InstantResetWorld> worlds = new HashMap<String, InstantResetWorld>();
    private final List<Hooks> hooks = new ArrayList<Hooks>();
    private final FilePurgeTask filePurger = new FilePurgeTask(this);

    private InstanceTools tools = null;

    @Override
    public void onEnable() {
        try { 
            tools = CBShim.createShim(InstanceTools.class, this);
        } catch (UnsupportedOperationException e) {
            getLogger().log(Level.SEVERE, "Unsupported server version: {0}", getServer().getVersion());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error creating NMS hooks", e);
        }
        if (tools == null) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        subcommands.put("list", new ListSubCommand(this));
        subcommands.put("reset", new ResetSubCommand(this));

        if (getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
            hooks.add(new MultiverseCoreHooks(this));
        }

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        templateDir = new File(getDataFolder(), config.getString("templatedir"));
        worldDir = new File(getDataFolder(), config.getString("worlddir"));
        resetOnRestart = config.getBoolean("resetOnRestart", false);

        templateDir.mkdirs();
        worldDir.mkdirs();

        if (config.isConfigurationSection("worlds")) {
            ConfigurationSection worlds = config.getConfigurationSection("worlds");
            for (String key : worlds.getKeys(false)) {
                ConfigurationSection world = worlds.getConfigurationSection(key);
                if (!world.isString("template")) {
                    getLogger().log(Level.WARNING, "Ignoring world: {0} template not specified.", key);
                    continue;
                }
                Difficulty difficulty = Difficulty.valueOf(world.getString("difficulty", "NORMAL").toUpperCase());
                if (difficulty == null) {
                    getLogger().log(Level.WARNING, "World: {0} has invalid difficulty: {1} using NORMAL", new Object[]{key, difficulty});
                    difficulty = Difficulty.NORMAL;
                }
                World.Environment env = World.Environment.valueOf(world.getString("environment", "NORMAL").toUpperCase());
                if (env == null) {
                    getLogger().log(Level.WARNING, "World: {0} has invalid environment: {1} using NORMAL", new Object[]{key, env});
                    env = World.Environment.NORMAL;
                }
                InstantResetWorld theWorld =
                        new InstantResetWorld(this, key, env, difficulty, world.getString("template"), resetOnRestart ? null : world.getString("worldsave"));
                this.worlds.put(key, theWorld);
                try {
                    theWorld.checkValid();
                    reloadWorld(theWorld);
                } catch (IllegalStateException e) {
                    getLogger().warning(e.getMessage());
                }
            }
        }
        filePurger.start();
    }

    @Override
    public void onDisable() {
        tools = null;
        FileConfiguration config = getConfig();
        ConfigurationSection worldsSection = config.createSection("worlds");
        for (InstantResetWorld world : this.worlds.values()) {
            ConfigurationSection worldSection = worldsSection.createSection(world.getName());
            worldSection.set("environment", world.getEnvironment().name());
            worldSection.set("difficulty", world.getDifficulty().name());
            worldSection.set("template", world.getTemplate());
            worldSection.set("worldsave", world.getWorldSave());
        }
        saveConfig();
        this.worlds.clear();
        this.subcommands.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            for (Map.Entry<String, SubCommand> e : subcommands.entrySet()) {
                if (label.endsWith(e.getKey())) {
                    e.getValue().onCommand(sender, args);
                    return true;
                }
                if (args.length > 0) {
                    if (e.getKey().equals(args[0])) {
                        String[] tmp = new String[args.length - 1];
                        System.arraycopy(args, 1, tmp, 0, tmp.length);
                        e.getValue().onCommand(sender, tmp);
                        return true;
                    }
                }
            }
            return false;
        } catch (CommandUsageException e) {
            return false;
        } catch (CommandException e) {
            sender.sendMessage(e.getMessage());
            return true;
        }
    }

    private World loadWorld(InstantResetWorld world) {
        for (Hooks hook : hooks) {
            try {
                hook.preLoad(world);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error calling hook", e);
            }
        }
        World result = getTools().createInstance(this, world.getName(), world.getEnvironment(), world.getDifficulty(), world.getTemplateDir(), world.getWorldSaveDir());
        
        for (Hooks hook : hooks) {
            try {
                hook.postLoad(world);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error calling hook", e);
            }
        }
        return result;
    }
    
    private Map<Player, Location> unloadWorld(InstantResetWorld world, boolean save) {
        Map<Player, Location> players = new HashMap<Player, Location>();
        World bukkitWorld = getServer().getWorld(world.getName());
        if (bukkitWorld != null) {
            for (Player player : bukkitWorld.getPlayers()) {
                players.put(player, player.getLocation());
                player.teleport(getServer().getWorlds().get(0).getSpawnLocation());
            }
            for (Hooks hook : hooks) {
                try {
                    hook.preUnload(world);
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Error calling hook", e);
                }
            }
            if (!getServer().unloadWorld(bukkitWorld, save)) {
                for (Map.Entry<Player, Location> e : players.entrySet()) {
                    e.getKey().teleport(e.getValue());
                }
                throw new IllegalStateException("Bukkit cowardly refused to unload the world: " + world.getName());
            }
            for (Hooks hook : hooks) {
                try {
                    hook.postUnload(world);
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Error calling hook", e);
                }
            }
        }
        return players;
    }

    public void reloadWorld(InstantResetWorld world) {
        world.checkValid();
        Map<Player, Location> players = unloadWorld(world, true);
        loadWorld(world);
        for (Map.Entry<Player, Location> e : players.entrySet()) {
            e.getKey().teleport(e.getValue());
        }
    }

    public void resetWorld(InstantResetWorld world) {
        world.checkValid();
        Map<Player, Location> players = unloadWorld(world, false);
        world.createWorldSave();
        World bukkitWorld = loadWorld(world);
        for (Player player : players.keySet()) {
            player.teleport(bukkitWorld.getSpawnLocation());
        }
        filePurger.cancel();
        filePurger.runTaskLaterAsynchronously(this, FilePurgeTask.PURGE_INTERVAL);
    }

    public InstantResetWorld getInstantResetWorld(String name) {
        return worlds.get(name);
    }

    public Set<String> getInstantResetWorldNames() {
        return Collections.unmodifiableSet(worlds.keySet());
    }

    public File getTemplateDir() {
        return templateDir;
    }

    public File getWorldDir() {
        return worldDir;
    }

    public InstanceTools getTools() {
        return tools;
    }
} 