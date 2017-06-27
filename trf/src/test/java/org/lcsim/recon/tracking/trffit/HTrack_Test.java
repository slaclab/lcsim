/*
 * HTrack_Test.java
 *
 * Created on July 24, 2007, 4:46 PM
 *
 * $Id: HTrack_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import java.util.List;
import java.util.ListIterator;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ClusterTest;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class HTrack_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HTrack_Test */
    public void testHTrack()
    {
        String component = "HTrack";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix + "-------- Testing component " + component
                + ". --------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructors" );
        ETrack tre = new ETrack( new SurfTest(5) );
        if(debug) System.out.println(tre);
        HTrack trh1 = new HTrack(tre);
        if(debug) System.out.println(trh1 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test status" );
        // Track with good fit and no hits is fit.
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.hits().size() == 0 );
        trh1.unsetFit();
        Assert.assertTrue( ! trh1.isFit() );
        Assert.assertTrue( ! trh1.isFit() );
        trh1.setFit(tre,0.0);
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.isFit() );
        trh1.unsetFit();
        Assert.assertTrue( ! trh1.isFit() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Add hits." );
        SurfTest srf1 = new SurfTest(10);
        Cluster pclu = new ClusterTest(srf1,10);
        ETrack tre1 = new ETrack( srf1.newPureSurface() );
        List hits = pclu.predict(tre1,pclu);
        Assert.assertTrue( hits.size() == 10 );
        
        
        // ihit iterates over input hits
        
        ListIterator ihit = hits.listIterator();
        trh1.setFit(tre,0.0);
        //0
        trh1.addHit( (Hit)ihit.next() );
        Assert.assertTrue( ! trh1.isFit() );
        //1
        trh1.addHit( (Hit)ihit.next()  );
        trh1.setFit(tre,0.0);
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.isFit() );
        trh1.unsetFit();
        Assert.assertTrue( ! trh1.isFit() );
        Assert.assertTrue( ! trh1.isFit() );
        // 2
        trh1.addHit( (Hit)ihit.next()  );
        if(debug) System.out.println(trh1 );
        // jhit iterates over output hits
        Assert.assertTrue( trh1.hits().size() == 3 );
        
        ihit = hits.listIterator(0);
        ListIterator jhit = trh1.hits().listIterator();
        Assert.assertTrue( ((Hit)ihit.next()).equals( (Hit)jhit.next()) );
        // 1
        Assert.assertTrue(((Hit)ihit.next()).equals( (Hit)jhit.next()));
        // 2
        Assert.assertTrue(((Hit)ihit.next()).equals( (Hit)jhit.next()));
        Assert.assertTrue( trh1.hits().size() == 3 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Drop a hit" );
        trh1.setFit(tre,0.0);
        ihit = hits.listIterator(0);
        trh1.dropHit();
        Assert.assertTrue( trh1.hits().size() == 2 );
        Assert.assertTrue( ! trh1.isFit() );
        jhit = trh1.hits().listIterator();
        Assert.assertTrue( ((Hit)ihit.next()).equals( (Hit)jhit.next()) );
        // 1
        Assert.assertTrue( ((Hit)ihit.next()).equals( (Hit)jhit.next()) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Set track and chi-square." );
        TrackVector vec = new TrackVector();
        int i,j;
        for ( i=0; i<5; ++i ) vec.set(i,1.1*i);
        TrackError err = new TrackError();
        for ( i=0; i<5; ++i )
        {
            for ( j=0; j<=i; ++j )
            {
                double fac;
                if ( i == j ) fac = 0.1;
                else fac = 0.01;
                err.set(i,j, ( i + 0.1*j ) * fac);
            }
        }
        
        ETrack tre2 = new ETrack( srf1.newPureSurface(), vec, err );
        double chisq = 12.345;
        trh1.setFit(tre2,chisq);
        if(debug) System.out.println(trh1 );
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.newTrack().equals(tre2) );
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.chisquared() == chisq );
        Assert.assertTrue( trh1.isFit() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test copy constructor." );
        HTrack trh2 = new HTrack(trh1);
        if(debug) System.out.println(trh2 );
        Assert.assertTrue( trh2.isFit() );
        Assert.assertTrue( trh2.newTrack().equals(trh1.newTrack()) );
        Assert.assertTrue( trh2.chisquared() == trh1.chisquared() );
        Assert.assertTrue( trh2.hits().equals(trh1.hits()) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Check equality." );
        Assert.assertTrue( trh1.equals(trh1) );
        Assert.assertTrue( ! (trh1.notEquals(trh1)) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Check Number of measurements." );
        if(debug) System.out.println(trh1.numberOfMeasurements() );
        Assert.assertTrue( trh1.numberOfMeasurements()==4 );
        
        //********************************************************************
        
        // HitTest does not set mcids currently.
        if(debug) System.out.println(ok_prefix + "Check TrackMcHitInfo" );
        if(debug) System.out.println(trh1.mcInfo().bestMcId() );
        Assert.assertTrue( trh1.mcInfo().bestMcId()==0 );
        
        //********************************************************************
        
        
        if(debug) System.out.println(ok_prefix + "Drop all hits." );
        Assert.assertTrue( trh1.isFit() );
        Assert.assertTrue( trh1.hits().size() == 2 );
        trh1.dropHits();
        Assert.assertTrue( trh1.hits().size() == 0 );
        Assert.assertTrue( ! trh1.isFit() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
    }
    
}
