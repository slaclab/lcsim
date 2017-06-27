/*
 * LayerCylinder_Test.java
 *
 * Created on July 24, 2007, 8:35 PM
 *
 * $Id: LayerCylinder_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Miss;
import org.lcsim.recon.tracking.trfbase.MissTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trflayer.ClusterFindAll;
import org.lcsim.recon.tracking.trflayer.ClusterFindManager;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class LayerCylinder_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of LayerCylinder_Test */
    public void testLayerCylinder()
    {
        String component = "LayerCylinder";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        Surface srf1 = new SurfCylinder(20.0);
        ClusterFindManager   find1 = new ClusterFindAll( srf1)  ;
        double par1 = 123.;
        double like1 = 0.246;
        Miss miss1 = new MissTest(par1,like1);
        
        Assert.assertTrue( miss1.likelihood() == like1 );
        Surface   srf2 = new BSurfCylinder(40.0,-50.,50.)  ;
        ClusterFindManager  find2 = new ClusterFindAll( srf2)  ;
        LayerCylinder lcy1 = new LayerCylinder( 20.0, -50.0, 50.0, find1, miss1 );
        LayerCylinder lcy2= new LayerCylinder( 40.0, -70.0, 70.0, find2 );
        if(debug) System.out.println( lcy1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test type." );
        if(debug) System.out.println( lcy1.type() );
        if(debug) System.out.println( lcy2.type() );
        Assert.assertTrue( lcy1.type() != null);
        Assert.assertTrue( lcy1.type().equals(LayerCylinder.staticType()) );
        Assert.assertTrue( lcy1.type().equals(lcy2.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Define a surface and track." );
        Surface srf0 = new SurfCylinder(20.0) ;
        Surface srf4 = new SurfCylinder(5.0) ;
        TrackVector vec = new TrackVector();
        vec.set(0, 1.0);    // phi
        vec.set(1, 5.0);    // z
        vec.set(2, 0.1);    // alpha
        vec.set(3, -0.2);   // lambda
        vec.set(4, 0.002);  // q/p
        TrackError err = new TrackError();
        err.set(0,0, 0.01);
        err.set(1,1, 0.02);
        err.set(2,2, 0.03);
        err.set(3,3, 0.04);
        err.set(4,4, 0.05);
        ETrack tre0 = new ETrack(srf0,vec,err);
        ETrack tre = new ETrack(srf4,vec,err);
        if(debug) System.out.println( tre );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test surface." );
        Surface srf = (Surface)lcy1.clusterSurfaces().get(0);
        {
            Surface srf3 = lcy1.surface();
            if(debug) System.out.println( srf3 );
            Assert.assertTrue( lcy1.clusterSurfaces().size() == 1 );
            Assert.assertTrue( srf3.equals(srf) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check cluster access" );
        Assert.assertTrue( lcy1.hasClusters() );
        lcy1.addCluster(new ClusCylPhi(20,1.0,0.1) );
        lcy1.addCluster(new ClusCylPhi(20,2.0,0.1) );
        lcy1.addCluster(new ClusCylPhi(20,3.0,0.1) );
        LayerCylinder lcy1c = new LayerCylinder(lcy1);
        if(debug) System.out.println( lcy1c.clusters().size() );
        Assert.assertTrue( lcy1c.clusters().size() == 3 );
        Assert.assertTrue( lcy1c.clusters(srf).size() == 3 );
        
        //********************************************************************
/*
  if(debug) System.out.println( ok_prefix + "Propagate from inside." );
  PropCyl prop = new PropCyl(2.0);
  List ltracks = lcy1.propagate(tre,prop);
  Assert.assertTrue( ltracks.size() == 1 );
  // save a copy of the propagated track
    LTrack trl1 = (LTrack) ltracks.get(0);
    ETrack tre1 = trl1.get_track();
    LayerStat lstat1 = trl1.get_status();
  if(debug) System.out.println( trl1 );
  Assert.assertTrue( lstat1.at_exit() );
  Assert.assertTrue( lstat1.get_state() == 1 );
  ltracks = lcy1.propagate(trl1,prop);
  Assert.assertTrue( ltracks.size() == 0 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Check returned miss." );
  Assert.assertTrue( lstat1.get_miss() != 0 );
  miss.update(tre1);
  Assert.assertTrue( lstat1.get_miss().get_likelihood() ==
          miss1.get_likelihood() );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Check returned clusters." );
  Assert.assertTrue( lstat1.has_clusters() );
  Assert.assertTrue( lstat1.get_clusters().size() == 3 );
  Assert.assertTrue( lstat1.get_clusters(tre).size() == 3 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Propagate from outside." );
  SurfacePtr psrf2( new SurfCylinder(50.0) );
  tre.set_surface(psrf2);
  if(debug) System.out.println( tre );
  ltracks = lcy1.propagate(tre,prop);
  Assert.assertTrue( ltracks.size() == 1 );
  // save a copy of the propagated track
  const LTrack trl2 = ltracks.front();
  const ETrack& tre2 = trl2.get_track();
  const LayerStat& lstat2 = trl2.get_status();
  if(debug) System.out.println( trl2 );
  Assert.assertTrue( lstat2.at_exit() );
  Assert.assertTrue( lstat2.get_state() == 1 );
  ltracks = lcy1.propagate(trl2,prop);
  Assert.assertTrue( ltracks.size() == 0 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Propagate backward from outside." );
  TrackVector vec2 = vec;
  vec2(2, -3.1;
  ETrack tre30(psrf2,vec2,err);
  if(debug) System.out.println( tre30 );
  ltracks = lcy1.propagate(tre30,prop);
  Assert.assertTrue( ltracks.size() == 1 );
  // save a copy of the propagated track
  const LTrack trl3 = ltracks.front();
  const ETrack& tre3 = trl3.get_track();
  const LayerStat& lstat3 = trl3.get_status();
  if(debug) System.out.println( trl3 );
  Assert.assertTrue( lstat3.at_exit() );
  Assert.assertTrue( lstat3.get_state() == 1 );
  ltracks = lcy1.propagate(trl3,prop);
  Assert.assertTrue( ltracks.size() == 0 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Propagate from layer surface." );
  SurfacePtr psrf3( new SurfCylinder(20.0) );
  tre.set_surface(psrf3);
  if(debug) System.out.println( tre );
  ltracks = lcy1.propagate(tre,prop);
  Assert.assertTrue( ltracks.size() == 1 );
  // save a copy of the propagated track
  const LTrack trl4 = ltracks.front();
  const ETrack& tre4 = trl4.get_track();
  const LayerStat& lstat4 = trl4.get_status();
  if(debug) System.out.println( trl4 );
  Assert.assertTrue( lstat4.at_exit() );
  Assert.assertTrue( lstat4.get_state() == 1 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Propagate out of bounds." );
  tre.set_surface(psrf1);
  vec.set(1, 300.0;
  tre.set_vector(vec);
  if(debug) System.out.println( tre );
  ltracks = lcy1.propagate(tre,prop);
  Assert.assertTrue( ltracks.size() == 1 );
  // save a copy of the propagated track
  const LTrack trl5 = ltracks.front();
  const ETrack& tre5 = trl5.get_track();
  const LayerStat& lstat5 = trl5.get_status();
  if(debug) System.out.println( trl5 );
  Assert.assertTrue( lstat5.at_exit() );
  Assert.assertTrue( lstat5.get_state() == 1 );
 
  //********************************************************************
 
  if(debug) System.out.println( ok_prefix + "Drop clusters." );
  if(debug) System.out.println( "Before drop: " + lcy1.get_clusters().size() );
  Assert.assert ( lcy1.get_clusters().size() == 3 );
  lcy1.drop_clusters();
  if(debug) System.out.println( "After drop: " + lcy1.get_clusters().size() );
  Assert.assert ( lcy1.get_clusters().size() == 0 );
 
  //********************************************************************
 */
        //********************************************************************
/*
  if(debug) System.out.println( ok_prefix
       + "------------- All tests passed. -------------" );
 */
        //********************************************************************
               
    }
    
}
