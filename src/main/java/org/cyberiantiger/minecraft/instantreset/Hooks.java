/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

/**
 *
 * @author antony
 */
public interface Hooks {

    public void preUnload(InstantResetWorld world);

    public void postUnload(InstantResetWorld world);

    public void preLoad(InstantResetWorld world);

    public void postLoad(InstantResetWorld world);
    
}
