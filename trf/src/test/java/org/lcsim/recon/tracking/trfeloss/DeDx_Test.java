/*
 * DeDx_Test.java
 *
 * Created on July 24, 2007, 10:07 PM
 *
 * $Id: DeDx_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfeloss;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class DeDx_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of DeDx_Test */
    public void testDeDx()
    {
        String component = "DeDx";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        DeDxTest test = new DeDxTest();
        if(debug) System.out.println(test);
        double energy = 1.;
        double x = 10.;
        Assert.assertTrue(test.dEdX(energy)==0.1*energy);
        Assert.assertTrue(test.sigmaEnergy(energy,x)==0.01*energy*x);
        Assert.assertTrue(test.loseEnergy(energy,x)==-energy*x);
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
    }
    
}
