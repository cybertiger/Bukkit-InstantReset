/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.instantreset;

/**
 *
 * @author antony
 */
interface Hooks {

    void preUnload(InstantResetWorld world);

    void postUnload(InstantResetWorld world);

    void preLoad(InstantResetWorld world);

    void postLoad(InstantResetWorld world);
    
}
