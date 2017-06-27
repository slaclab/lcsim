/*
 * AddFitKalman_Test.java
 *
 * Created on July 24, 2007, 5:05 PM
 *
 * $Id: AddFitKalman_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.WAvg;

/**
 *
 * @author Norman Graf
 */
public class AddFitKalman_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of AddFitKalman_Test */
    public void testAddFitKalman()
    {
         String component = "AddFitKalman";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" + "\n");
        
        //********************************************************************
        if(debug) System.out.println( ok_prefix + "Test constructor." + "\n");
        AddFitKalman fitk = new AddFitKalman();
        if(debug) System.out.println( fitk + "\n");
        Assert.assertTrue( fitk.type() == AddFitKalman.staticType() );
        if(debug) System.out.println( fitk.typeName() + "\n");
        Assert.assertTrue( fitk.typeName() == "AddFitKalman" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test fitting 1 hit." + "\n");
        double small = 1.e-12;
        WAvg avg = new WAvg();
        SurfTest srf = new SurfTest(10);
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        vec.set(0,2.9);
        err.set(0,0,0.04);
        err.set(1,1,0.0001);
        err.set(2,2,0.0001);
        err.set(3,3,0.0001);
        err.set(4,4,0.0001);
        avg.addPair(vec.get(0),err.get(0,0));
        
        ETrack tre = new ETrack( srf.newPureSurface(), vec, err );
        HTrack trh = new HTrack(tre);
        Cluster clu1 =  new ClusTestFit1(srf,3.0,0.01) ;
        avg.addPair(3.0,0.01);
        
        List hits = clu1.predict(tre,clu1);
        if(debug) System.out.println( trh + "\n");
        if(debug) System.out.println( hits.get(0) + "\n");
        Assert.assertTrue( fitk.addHit(trh,(Hit)hits.get(0)) == 0 );
        if(debug) System.out.println( trh + "\n");
        if(debug) System.out.println( avg + "\n");
        if(debug) System.out.println("trk= "+trh.newTrack().vector().get(0)+" "+avg.average());
        Assert.assertTrue( Math.abs( trh.newTrack().vector().get(0) - avg.average() )
        < small );
        Assert.assertTrue( Math.abs( trh.newTrack().error().get(0,0) - avg.error() )
        < small );
        if(debug) System.out.println("trk chisq= "+trh.chisquared()+", avg chsq= "+	avg.chiSquared());
        Assert.assertTrue( Math.abs( trh.chisquared() - avg.chiSquared() )
        < small );
        Assert.assertTrue( trh.hits().size() == 1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test fitting 2 hits." + "\n");
        Cluster clu2 = new ClusTestFit1(srf,3.3,0.02);
        avg.addPair(3.3,0.02);
        hits = clu2.predict(trh.newTrack(),clu2);
        Assert.assertTrue( fitk.addHit( trh, (Hit)hits.get(0) ) == 0 );
        if(debug) System.out.println( trh + "\n");
        if(debug) System.out.println( avg + "\n");
        Assert.assertTrue( Math.abs( trh.newTrack().vector().get(0) - avg.average() )
        < small );
        Assert.assertTrue( Math.abs( trh.newTrack().error().get(0,0) - avg.error() )
        < small );
        Assert.assertTrue( Math.abs( trh.chisquared() - avg.chiSquared() )
        < small );
        Assert.assertTrue( trh.hits().size() == 2 );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" + "\n");
        
        
        //********************************************************************       
    }
    
}
