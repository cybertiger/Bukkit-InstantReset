/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

import org.bukkit.command.CommandSender;

/**
 *
 * @author antony
 */
class ListSubCommand implements SubCommand {
    private final InstantReset plugin;

    public ListSubCommand(InstantReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws CommandException {
        if (args.length != 0) {
            throw new CommandUsageException();
        }

        StringBuilder response = new StringBuilder("==== Instant Reset Worlds ====\n");

        for (String world : plugin.getInstantResetWorldNames()) {
            response.append(world).append("\n");
        }

        response.append("===========================");

        sender.sendMessage(response.toString());
    }
    
}
