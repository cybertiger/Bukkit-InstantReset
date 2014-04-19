/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.World;

/**
 *
 * @author antony
 */
public class MultiverseCoreHooks implements Hooks {
    private InstantReset plugin;

    private MultiverseCore getMultiverseCore() {
        return (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
    }

    public MultiverseCoreHooks(InstantReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public void preUnload(InstantResetWorld world) {
    }

    @Override
    public void postUnload(InstantResetWorld world) {
    }

    @Override
    public void preLoad(InstantResetWorld world) {
    }

    @Override
    public void postLoad(InstantResetWorld world) {
        World bukkitWorld = plugin.getServer().getWorld(world.getName());
        if (getMultiverseCore().getMVWorldManager().getMVWorld(world.getName()) == null) {
            getMultiverseCore().getMVWorldManager().addWorld(world.getName(), world.getEnvironment(), String.valueOf(bukkitWorld.getSeed()), bukkitWorld.getWorldType(), Boolean.TRUE, null);
        }
    }
}
