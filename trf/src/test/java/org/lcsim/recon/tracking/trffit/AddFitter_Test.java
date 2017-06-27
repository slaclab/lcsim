/*
 * AddFitter_Test.java
 *
 * Created on July 24, 2007, 5:04 PM
 *
 * $Id: AddFitter_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ClusterTest;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class AddFitter_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of AddFitter_Test */
    public void testAddFitter()
    {
           String component = "AddFitter";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Define tracks and hit." );
        SurfTest srf = new SurfTest(10);
        ETrack tre = new ETrack( srf.newPureSurface() );
        HTrack trh = new HTrack(tre);
        Cluster pclu =  new ClusterTest(srf,3);
        Hit hit = (Hit)pclu.predict(tre,pclu).get(0);
        if(debug) System.out.println(hit);
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        AddFitterTest fit = new AddFitterTest(12);
        if(debug) System.out.println(fit );
        Assert.assertTrue( fit.addHit(trh,hit)!=0 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************     
    }
    
}
