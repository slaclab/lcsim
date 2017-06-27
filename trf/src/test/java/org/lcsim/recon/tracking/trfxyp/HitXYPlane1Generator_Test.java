/*
 * HitXYPlane1Generator_Test.java
 *
 * Created on July 24, 2007, 10:33 PM
 *
 * $Id: HitXYPlane1Generator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class HitXYPlane1Generator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitXYPlane1Generator_Test */
    public void testHitXYPlane1Generator()
    {
        String component = "HitXYPlane1Generator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double dist = 25.0;
        double phi = 1.;
        double wv = 0.1;
        double wz = 0.5;
        double davz = 0.01;
        HitXYPlane1Generator gen = new HitXYPlane1Generator(dist,phi,wv,wz,davz);
        if(debug) System.out.println( gen );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a list of hits." );
        double v_track = 1.2345;
        double z_track = 6.789;
        double     avz = wv*v_track+wz*z_track;
        SurfXYPlane sxyp = new SurfXYPlane(dist,phi);
        TrackVector vec = new TrackVector();
        vec.set(SurfXYPlane.IV, v_track);
        vec.set(SurfXYPlane.IZ, z_track);
        TrackError err = new TrackError();
        VTrack trv = new VTrack(sxyp.newPureSurface(),vec);
        ETrack tre = new ETrack(trv,err);
        List clusters = new ArrayList();
        int nclus = 20;
        for ( int i=0; i<nclus; ++i )
            clusters.add( gen.newCluster(trv) );
        // Verify and display the list.
        List hits = new ArrayList();
        for ( Iterator iclu=clusters.iterator(); iclu.hasNext(); )
        {
            Cluster clu = (Cluster) iclu.next();
            Assert.assertTrue( clu != null);
            List newhits = clu.predict(tre,clu);
            Assert.assertTrue( newhits.size() == 1 );
            hits.add( newhits.get(0) );
        }
        Assert.assertTrue( hits.size() == nclus );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate list of measured values." );
        double avg = 0.0;
        double sdev = 0.0;
        for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
        {
            Hit hit = (Hit) ihit.next();
            double mval = hit.measuredVector().get(0);
            double merr = hit.measuredError().get(0,0);
            double pval = hit.predictedVector().get(0);
            double perr = hit.predictedError().get(0,0);
            Assert.assertTrue( TRFMath.isEqual(pval,avz) );
            Assert.assertTrue( TRFMath.isEqual(merr,davz*davz) );
            if(debug) System.out.println( mval );
            avg += mval;
            sdev += (mval-pval)*(mval-pval);
        }
        avg /= nclus;
        sdev = Math.sqrt(sdev/nclus);
        if(debug) System.out.println( " Avg: " + avg );
        if(debug) System.out.println( "Sdev: " + sdev );
        Assert.assertTrue( Math.abs( avg - avz ) < davz );
        Assert.assertTrue( Math.abs( sdev - davz ) < davz );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************
    }
    
}
