/*
 * DeDxBethe_Test.java
 *
 * Created on July 24, 2007, 10:10 PM
 *
 * $Id: DeDxBethe_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfeloss;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class DeDxBethe_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of DeDxBethe_Test */
    public void testDeDxBethe()
    {
        String component = "DeDxBethe";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------");
        
        //********************************************************************
        
        double pionMass = 0.13957;
        double density = 3.0;
        DeDx dedx = new DeDxBethe(density);
        double thickness = 10.0;
        double energy = 10.0;
        double testenergy = energy;
        // not much of a test here...
        double de_dx = dedx.dEdX(energy);
        testenergy -= de_dx*thickness;
        double finalenergy = dedx.loseEnergy(energy, thickness);
        if(debug) System.out.println("energy= "+energy+", testenergy= "+testenergy+", finalenergy= "+finalenergy);
        
        // Check that we gain energy if we go backwards
        Assert.assertTrue(Math.abs(testenergy-finalenergy)<0.0001);
        
        double gainenergy = dedx.loseEnergy(energy, -thickness);
        Assert.assertTrue( gainenergy > energy);
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------");
        
        
    }
    
}
