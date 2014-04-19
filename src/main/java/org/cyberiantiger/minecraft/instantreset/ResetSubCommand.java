/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author antony
 */
class ResetSubCommand implements SubCommand {
    private final InstantReset plugin;

    public ResetSubCommand(InstantReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws CommandException {
        String worldName;
        if (args.length == 0) {
            if (sender instanceof Player) {
                worldName = ((Player)sender).getWorld().getName();
            } else if (sender instanceof BlockCommandSender) {
                worldName = ((BlockCommandSender)sender).getBlock().getWorld().getName();
            } else {
                throw new CommandException("You must specify a world from console.");
            }
        } else if (args.length == 1)  {
            worldName = args[0];
        } else {
            throw new CommandUsageException();
        }
        InstantResetWorld instantResetWorld = plugin.getInstantResetWorld(args[0]);
        if (instantResetWorld == null) {
            throw new CommandException(worldName + " is not an instant reset world.");
        }
        try {
            plugin.resetWorld(instantResetWorld);
            sender.sendMessage(worldName + " reset");
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage());
        }
    }
}