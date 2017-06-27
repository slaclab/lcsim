/*
 * DistSurface_Test.java
 *
 * Created on July 24, 2007, 9:59 PM
 *
 * $Id: DistSurface_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfdca;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.StatusDouble;

/**
 *
 * @author Norman Graf
 */
public class DistSurface_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of DistSurface_Test */
    public void testDistSurface()
    {
        String component = "DistSurface";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component+ " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "--------------- testing component" + component
                + ". -------------");
        //*******************************************
        
        // Test data.
        
        double[] x     = {  0.,  0.,  2.,   1.,   2.,   3.  };
        double[] y     = {  0.,  0.,  3.,   2.,   2.,   0.  };
        double[] z     = {  0., 10., 10.,   0.,   0.,   0.  };
        double[] phid  = {  0.,  0.,  0.,   0.,   0.,   0.  };
        double[] tlam  = {  0.,  0.,  0., 0.75, 0.75, 0.75  };
        double[] wc    = {  0.,  0.,  0.,   0.,   0.,   0.  };
        double[] xbeam = {  0.,  1.,  1.,  -1.,  -1.,   0.  };
        double[] ybeam = {  0.,  2.,  2.,  -2.,  -1.,   0.  };
        double[] bx    = {  0.,  0.,  0.,   0.,   1.,   0.  };
        double[] by    = {  0.,  0.,  0.,   0.,   1.,   0.  };
        double[] s     = {  0.,  1., -1., -2.5, -15., 8.75  };
        double[] r     = {  0.,  0.,  0.,   0.,   0.,  10.  };
        
        int n = x.length;
        
        
        if(debug) System.out.println( ok_prefix+" Test constructor");
        for(int i=0; i<n; ++i)
        {
            Surface srf;
            if(r[i] == 0.)
            {
                srf = new SurfDCA(xbeam[i], ybeam[i], bx[i], by[i]);
            }
            else
            {
                srf = new SurfCylinder(r[i]);
            }
            DistSurface dist = new DistSurface(x[i], y[i], z[i], phid[i], tlam[i], wc[i], srf);
            if(debug) System.out.println(dist);
            
            StatusDouble s1 = dist.distance();
            if(debug) System.out.println( i + "\t" + s[i] + "\t" + s1.value() );
            Assert.assertTrue(s1.status() == 0);
            Assert.assertTrue(Math.abs(s[i] - s1.value()) < 1.e-6);
            
        }
        
        //*******************************************
        if(debug) System.out.println( ok_prefix
                + "-------------------- All tests passed. -------------------");
        //*******************************************
    }
    
}
