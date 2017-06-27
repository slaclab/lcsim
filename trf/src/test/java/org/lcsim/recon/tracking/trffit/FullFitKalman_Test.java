/*
 * FullFitKalman_Test.java
 *
 * Created on July 24, 2007, 5:00 PM
 *
 * $Id: FullFitKalman_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trflayer.PropTest;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.WAvg;

/**
 *
 * @author Norman Graf
 */
public class FullFitKalman_Test extends TestCase
{
    private boolean debug = false;
    /** Creates a new instance of FullFitKalman_Test */
    public void testFullFitKalman()
    {
        String component = "FullFitKalman";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix + "-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        // Create starting surface and track.
        
        SurfTest stest = new SurfTest(1);
        double tvec = 2.1;
        double terr = 0.014;
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        vec.set(0, tvec);
        err.set(0,0, terr);
        err.set(1,1, 0.000001);
        err.set(2,2, 0.000001);
        err.set(3,3, 0.000001);
        err.set(4,4, 0.000001);
        ETrack tre = new ETrack( stest.newPureSurface(), vec, err );
        WAvg avg = new WAvg();
        avg.addPair(tvec,terr);
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Create clusters and generate prediction."
                );
        List clusters = new ArrayList();
        int nmsmt = 5;
        double msmt[] = { 2.00, 2.33, 1.94, 2.22, 1.87 };
        double emsmt[] = { 0.01, 0.015, 0.016, 0.023, 0.012 };
        int i;
        for ( i=0; i<nmsmt; ++i )
        {
            clusters.add( new ClusTestFit1(stest,msmt[i],emsmt[i]) );
            avg.addPair(msmt[i],emsmt[i]);
        };
        if(debug) System.out.println("avg = "+avg );
        
        //********************************************************************
        
        // Create hits.
        List hits = new ArrayList();
        
        for( Iterator iclu=clusters.iterator(); iclu.hasNext();  )
        {
            Cluster clu = (Cluster)iclu.next();
            List newhits = clu.predict(tre, clu);
            hits.add( newhits.get(newhits.size()-1) );
        }
        // Check the list of Hits
        if(debug)
        {
            for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
            {
                System.out.println("Hit= "+ihit.next());
            }
        }
        
        //********************************************************************
        
        // Put hit on an HTrack.
        HTrack trh = new HTrack(tre);
        for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
        {
            trh.addHit((Hit)ihit.next());
        }
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Construct fitter." );
        Propagator pprop = new PropTest();
        FullFitKalman fitk = new FullFitKalman(pprop);
        if(debug) System.out.println(fitk );
        
        //********************************************************************
        if(debug) System.out.println("tre= \n"+tre);
        if(debug) System.out.println("trh= \n"+trh);
        if(debug) System.out.println(ok_prefix + "Fit with all hits." );
        Assert.assertTrue( ! trh.isFit() );
        fitk.fit(trh);
        if(debug) System.out.println( "trh= "+trh );
        double small = 1.e-10;
        Assert.assertTrue( trh.isFit() );
        if(debug) System.out.println(Math.abs( trh.newTrack().vector().get(0) - avg.average()));
        Assert.assertTrue( Math.abs( trh.newTrack().vector().get(0) - avg.average() )
        < small );
        Assert.assertTrue( Math.abs( trh.newTrack().error().get(0,0) - avg.error() )
        < small );
        if(debug) System.out.println("dchisq="+Math.abs( trh.chisquared() - avg.chiSquared() ) );
        Assert.assertTrue( Math.abs( trh.chisquared() - avg.chiSquared() )
        < small );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
