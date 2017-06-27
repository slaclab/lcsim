/*
 * Hit_Test.java
 *
 * Created on July 24, 2007, 12:17 PM
 *
 * $Id: Hit_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Hit_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Hit_Test */
    public void testHit()
    {
        String component = "Hit";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Test constructors." );
        
        SurfTest stest =  new SurfTest(1);
        Cluster ct1 = new ClusterTest(stest,3);
        
        if(debug) System.out.println( ct1 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test surface." );
        if(debug) System.out.println( stest );
        Surface srfchk = ct1.surface();
        if(debug) System.out.println( srfchk );
        Assert.assertTrue( stest.pureEqual(srfchk) );
        
        //********************************************************************
        
        if(debug) System.out.println("Fetch hits." );
        ETrack tre = new ETrack( stest.newPureSurface() );
        //List hits = new ArrayList();
        List hits = ct1.predict(tre,ct1);
        Iterator it = hits.iterator();
        if(debug)
        {
            while(it.hasNext()) System.out.println( it.next() );
        }
        Assert.assertTrue( hits.size() == 3 );
        
        //********************************************************************
        
        if(debug) System.out.println("Check hit size." );
        Hit hit0 = (Hit)hits.get(0);
        if(debug) System.out.println( "size = " + hit0.size() );
        Assert.assertTrue( hit0.size() == 2 );
        
        //********************************************************************
        
        if(debug) System.out.println("Check hit." );
        Cluster pclus = hit0.cluster();
        if(debug) System.out.println( ct1 );
        if(debug) System.out.println( pclus );
        Assert.assertTrue( ct1==pclus );
        
        //********************************************************************
        
        if(debug) System.out.println("Check hit surface." );
        if(debug) System.out.println( ct1.surface() );
        if(debug) System.out.println( hit0.surface() );
        Assert.assertTrue( hit0.surface().equals(ct1.surface()) );
        
        //********************************************************************
        
        if(debug) System.out.println("Check hit data." );
        if(debug) System.out.println( hit0.measuredVector() );
        Assert.assertTrue( hit0.measuredVector().get(1) == 2.0 );
        if(debug) System.out.println( hit0.measuredError() );
        Assert.assertTrue( hit0.measuredError().get(1,1) == 4.0 );
        if(debug) System.out.println( hit0.predictedVector() );
        Assert.assertTrue( hit0.predictedVector().get(1) == 4.0 );
        if(debug) System.out.println( hit0.predictedError() );
        Assert.assertTrue( hit0.predictedError().get(1,1) == 6.0 );
        if(debug) System.out.println( hit0.dHitdTrack() );
        Assert.assertTrue( hit0.dHitdTrack().get(1,4) == 14.0 );
        if(debug) System.out.println( hit0.differenceVector() );
        Assert.assertTrue( hit0.differenceVector().amax() == 2.0 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test types." );
        if(debug) System.out.println( ct1.type() );
        Assert.assertTrue( ct1.type() != null );
        Cluster ct2 = new ClusterTest(stest,2);
        if(debug) System.out.println( ct2.type() );
        Assert.assertTrue( ct1.type().equals(ct2.type()) );
        if(debug) System.out.println( hit0.type() );
        Assert.assertTrue( hit0.type() != null );
        Assert.assertTrue( !hit0.type().equals(ct1.type()) );
/*  ihit = hits.begin();
   Hit* phit = (*++ihit).pointer();
  if(debug) System.out.println( phit->type() );
  Assert.assertTrue( hit0.get_type() == phit->type() );
 */
        //********************************************************************
        
        if(debug) System.out.println("Test equalities." );
        // cluster
        Assert.assertTrue( ct1.equals(ct1) );
        Assert.assertTrue( ! ( ct1.notEquals(ct1) ) );
        Assert.assertTrue( ! ( ct1.equals(ct2) ) );
        Assert.assertTrue( ct1.notEquals(ct2) );
        // hit
        List hits2 = ct2.predict(tre,ct2);
        Assert.assertTrue( hits2.size() == 2 );
        Hit hit2 = (Hit) hits2.get(0);
        Assert.assertTrue( hit0.equals(hit0) );
        Assert.assertTrue( ! ( hit0.notEquals(hit0) ) );
        Assert.assertTrue( hit0.notEquals(hit2) );
        Assert.assertTrue( ! ( hit0.equals(hit2) ) );
        
        //********************************************************************
        
        {
            List oids = ct1.mcIds();
            Assert.assertTrue( oids.size() == 0 );
        }
        {
            List oids = hit0.mcIds();
            Assert.assertTrue( oids.size() == 0 );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
