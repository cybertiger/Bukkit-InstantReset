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
public interface SubCommand {

    public void onCommand(CommandSender sender, String... args) throws CommandException;
    
}
