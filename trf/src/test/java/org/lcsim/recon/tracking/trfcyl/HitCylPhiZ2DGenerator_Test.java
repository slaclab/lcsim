/*
 * HitCylPhiZ2DGenerator_Test.java
 *
 * Created on July 24, 2007, 8:43 PM
 *
 * $Id: HitCylPhiZ2DGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class HitCylPhiZ2DGenerator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitCylPhiZ2DGenerator_Test */
    public void testHitCylPhiZ2DGenerator()
    {
        String component = "HitCylPhiZ2DGenerator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        int IPHI = SurfCylinder.IPHI;
        int IZ = SurfCylinder.IZ;
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double radius = 25.0;
        double dphi = 0.01;
        double dz   = 0.1;
        double dphidz = -0.0005;
        SurfCylinder scyl = new SurfCylinder(radius);
        HitCylPhiZ2DGenerator gen = new HitCylPhiZ2DGenerator(scyl, dphi, dz, dphidz);
        if(debug) System.out.println( gen );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a list of hits." );
        double phi0 = 1.2345;
        double z0 = 6.7;
        Surface srf =  new SurfCylinder(radius);
        TrackVector vec = new TrackVector();
        vec.set(SurfCylinder.IPHI, phi0);
        vec.set(SurfCylinder.IZ, z0 );
        TrackError err = new TrackError();
        VTrack trv = new VTrack(srf,vec);
        ETrack tre = new ETrack(trv,err);
        List clusters = new ArrayList();
        int nclus = 20;
        for ( int i=0; i<nclus; ++i )
            clusters.add( gen.newCluster(trv) );
        // Verify and display the list.
        List hits = new ArrayList();
        for ( Iterator iclu=clusters.iterator(); iclu.hasNext();   )
        {
            Cluster clu = (Cluster) iclu.next();
            Assert.assertTrue( clu != null );
            List newhits = clu.predict(tre,clu);
            Assert.assertTrue( newhits.size() == 1 );
            hits.add( newhits.get(0) );
        }
        Assert.assertTrue( hits.size() == nclus );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate list of measured values." );
        double avgphi = 0.0;
        double sdevphi = 0.0;
        double avgz = 0.0;
        double sdevz = 0.0;
        double sdevphiz = 0.0;
        for ( Iterator ihit=hits.iterator(); ihit.hasNext();   )
        {
            Hit hit = (Hit) ihit.next();
            Assert.assertTrue( hit  != null );
            // phi
            double mvalphi = hit.measuredVector().get(IPHI);
            double merrphi = hit.measuredError().get(IPHI,IPHI);
            double pvalphi = hit.predictedVector().get(IPHI);
            double perrphi = hit.predictedError().get(IPHI,IPHI);
            Assert.assertTrue( TRFMath.isEqual(pvalphi,phi0) );
            Assert.assertTrue( TRFMath.isEqual(merrphi,dphi*dphi) );
            if(debug) System.out.println( mvalphi );
            avgphi += mvalphi;
            sdevphi += (mvalphi-pvalphi)*(mvalphi-pvalphi);
            // z
            double mvalz = hit.measuredVector().get(IZ);
            double merrz = hit.measuredError().get(IZ,IZ);
            double pvalz = hit.predictedVector().get(IZ);
            double perrz = hit.predictedError().get(IZ,IZ);
            Assert.assertTrue( TRFMath.isEqual(pvalz,z0) );
            Assert.assertTrue( TRFMath.isEqual(merrz,dz*dz) );
            if(debug) System.out.println( mvalz +"\n");
            avgz += mvalz;
            sdevz += (mvalz-pvalz)*(mvalz-pvalz);
            // phiz
            sdevphiz += (mvalphi-pvalphi)*(mvalz-pvalz);
        }
        //phi
        avgphi /= nclus;
        sdevphi = Math.sqrt(sdevphi/nclus);
        if(debug) System.out.println( " Phi Avg: " + avgphi );
        if(debug) System.out.println( "Phi Sdev: " + sdevphi );
        Assert.assertTrue( Math.abs( avgphi - phi0 ) < dphi );
        Assert.assertTrue( Math.abs( sdevphi - dphi ) < dphi );
        
        // z
        avgz /= nclus;
        sdevz = Math.sqrt(sdevz/nclus);
        if(debug) System.out.println( "   Z Avg: " + avgz );
        if(debug) System.out.println( "  Z Sdev: " + sdevz );
        Assert.assertTrue( Math.abs( avgz - z0 ) < dz );
        Assert.assertTrue( Math.abs( sdevz - dz ) < dz );
        
        // phiz
        sdevphiz = sdevphiz/nclus;
        if(debug) System.out.println( "PhiZ Sdev: " + sdevphiz );
        // Need to check correlation term...
        if (dphidz == 0.0 )
        {
            Assert.assertTrue( Math.abs( sdevphiz - dphidz ) < 1E-4 );
        }
        else
        {
            // check sign of correlation term
            Assert.assertTrue( sdevphiz*dphidz >= 0. );
            // check magnitude
            Assert.assertTrue( Math.abs( sdevphiz - dphidz ) < Math.abs(dphidz) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        //********************************************************************
        
    }
    
}
