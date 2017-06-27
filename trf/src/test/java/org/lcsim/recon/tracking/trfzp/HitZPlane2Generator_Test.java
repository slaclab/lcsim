/*
 * HitZPlane2Generator_Test.java
 *
 * Created on July 24, 2007, 11:05 PM
 *
 * $Id: HitZPlane2Generator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

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
public class HitZPlane2Generator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitZPlane2Generator_Test */
    public void testHitZPlane2Generator()
    {
        String component = "HitZPlane2Generator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double zpos = 25.0;
        HitError dhm = new HitError(2);
        dhm.set(ClusZPlane2.IX,ClusZPlane2.IX, 0.01);
        dhm.set(ClusZPlane2.IX,ClusZPlane2.IY, 0.001);
        dhm.set(ClusZPlane2.IY,ClusZPlane2.IY, 0.02);
        HitZPlane2Generator gen = new HitZPlane2Generator(zpos,dhm);
        if(debug) System.out.println( gen );
        Assert.assertTrue( new HitZPlane2Generator(zpos,dhm) != null);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a list of hits." );
        double x_track = 1.2345;
        double y_track = 6.789;
        SurfZPlane szp = new SurfZPlane(zpos);
        TrackVector vec = new TrackVector();
        vec.set(SurfZPlane.IX, x_track);
        vec.set(SurfZPlane.IY, y_track);
        TrackError err = new TrackError();
        VTrack trv = new VTrack(szp.newPureSurface(),vec);
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
            Assert.assertTrue( pval1 == x_track);
            Assert.assertTrue( pval2 == y_track);
            Assert.assertTrue( merr11 ==  dhm.get(ClusZPlane2.IX,ClusZPlane2.IX));
            Assert.assertTrue( merr12 ==  dhm.get(ClusZPlane2.IX,ClusZPlane2.IY));
            Assert.assertTrue( merr22 ==  dhm.get(ClusZPlane2.IY,ClusZPlane2.IY));
            if(debug) System.out.println( mval1 + " "+ mval2 );
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
        double dxx = Math.sqrt(dhm.get(ClusZPlane2.IX,ClusZPlane2.IX));
        double dyy = Math.sqrt(dhm.get(ClusZPlane2.IY,ClusZPlane2.IY));
        Assert.assertTrue( Math.abs( avg1 - x_track ) < dxx);
        Assert.assertTrue( Math.abs( avg2 - y_track ) < dyy );
        Assert.assertTrue( Math.abs( Math.sqrt(sdev11) - dxx) < dxx );
        Assert.assertTrue( Math.abs( Math.sqrt(sdev22) - dyy) < dyy );
        double dxy = dhm.get(ClusZPlane2.IX,ClusZPlane2.IY);
        Assert.assertTrue( Math.abs( sdev12 - dxy) < Math.abs(dxy) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        
        //********************************************************************
        
    }
    
}
