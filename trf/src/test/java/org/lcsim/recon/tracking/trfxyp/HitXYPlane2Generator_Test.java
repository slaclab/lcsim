/*
 * HitXYPlane2Generator_Test.java
 *
 * Created on July 24, 2007, 10:30 PM
 *
 * $Id: HitXYPlane2Generator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class HitXYPlane2Generator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitXYPlane2Generator_Test */
    public void testHitXYPlane2Generator()
    {
        String component = "HitXYPlane2Generator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double dist = 25.0;
        double phi = 1.;
        HitError dhm = new HitError(2);
        dhm.set(ClusXYPlane2.IV,ClusXYPlane2.IV, 0.02);
        dhm.set(ClusXYPlane2.IV,ClusXYPlane2.IZ, -0.01);
        dhm.set(ClusXYPlane2.IZ,ClusXYPlane2.IZ, 0.03);
        HitXYPlane2Generator gen = new HitXYPlane2Generator(dist,phi,dhm);
        if(debug) System.out.println( gen );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a list of hits." );
        double v_track = 1.2345;
        double z_track = 6.789;
        SurfXYPlane sxyp = new SurfXYPlane(dist,phi);
        TrackVector vec = new TrackVector();
        vec.set(SurfXYPlane.IV, v_track);
        vec.set(SurfXYPlane.IZ, z_track);
        TrackError err =  new TrackError();
        VTrack trv = new VTrack(sxyp.newPureSurface(),vec);
        ETrack tre = new ETrack(trv,err);
        List clusters = new ArrayList();
        int nclus = 100;
        for ( int i=0; i<nclus; ++i )
            clusters.add( gen.newCluster(trv) );
        // Verify and display the list.
        List hits = new ArrayList();
        for ( Iterator iclu=clusters.iterator(); iclu.hasNext(); )
        {
            Cluster clu = (Cluster) iclu.next();
            List newhits = clu.predict(tre,clu);
            Assert.assertTrue( newhits.size() == 1 );
            hits.add( newhits.get(0) );
        }
        Assert.assertTrue( hits.size() == nclus );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate list of measured values." );
        double avg1 = 0.0;
        double avg2 = 0.0;
        double sdev11 = 0.0;
        double sdev12 = 0.0;
        double sdev22 = 0.0;
        for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
        {
            Hit hit = (Hit) ihit.next();
            double mval1 = hit.measuredVector().get(0);
            double mval2 = hit.measuredVector().get(1);
            double merr11 = hit.measuredError().get(0,0);
            double merr12 = hit.measuredError().get(0,1);
            double merr22 = hit.measuredError().get(1,1);
            double pval1 = hit.predictedVector().get(0);
            double pval2 = hit.predictedVector().get(1);
            double perr11 = hit.predictedError().get(0,0);
            double perr12 = hit.predictedError().get(0,1);
            double perr22 = hit.predictedError().get(1,1);
            Assert.assertTrue( pval1 == v_track);
            Assert.assertTrue( pval2 == z_track);
            Assert.assertTrue( merr11 ==  dhm.get(ClusXYPlane2.IV,ClusXYPlane2.IV));
            Assert.assertTrue( merr12 ==  dhm.get(ClusXYPlane2.IV,ClusXYPlane2.IZ));
            Assert.assertTrue( merr22 ==  dhm.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ));
            if(debug) System.out.println( mval1 + " "+ mval2);
            avg1 += mval1;
            avg2 += mval2;
            sdev11 += (mval1-pval1)*(mval1-pval1);
            sdev12 += (mval1-pval1)*(mval2-pval2);
            sdev22 += (mval2-pval2)*(mval2-pval2);
        }
        avg1 /= nclus;
        avg2 /= nclus;
        sdev11 /= nclus;
        sdev12 /= nclus;
        sdev22 /= nclus;
        if(debug) System.out.println( " Avg1: " + avg1+ " Avg2: " + avg2 );
        if(debug) System.out.println( "Sdev:[ " + sdev11+" | "+ sdev12+" | "+ sdev22+" ]" );
        double dvv = Math.sqrt(dhm.get(ClusXYPlane2.IV,ClusXYPlane2.IV));
        double dzz = Math.sqrt(dhm.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ));
        Assert.assertTrue( Math.abs( avg1 - v_track ) < dvv);
        Assert.assertTrue( Math.abs( avg2 - z_track ) < dzz );
        Assert.assertTrue( Math.abs( Math.sqrt(sdev11) - dvv) < dvv );
        Assert.assertTrue( Math.abs( Math.sqrt(sdev22) - dzz) < dzz );
        double dvz = dhm.get(ClusXYPlane2.IV,ClusXYPlane2.IZ);
        Assert.assertTrue( Math.abs( sdev12 - dvz) < Math.abs(dvz) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
