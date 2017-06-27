package org.lcsim.recon.tracking.gtrbase;

// Create a Gtrack for testing.
import java.util.*;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;
import org.lcsim.recon.tracking.trfutil.Assert;

public class GTrackTest
{
    
    public int nstate;
    public List surfs = new ArrayList();
    public List etracks = new ArrayList();
    public List statii = new ArrayList();
    public TreeSet stateset = new TreeSet();
    public List states = new ArrayList();
    public GTrack gtr = new GTrack();
    public double[] ss = { 10.0, 20.0, 30.0, 40.0, 50.0 };
    
    
    public
            GTrackTest( String ok_prefix, double vec0 )
    {
        //******************************************************************
        
        //******************************************************************
        System.out.println( ok_prefix + "Build tracks." );
        surfs.add( new SurfCylinder(10.0) );
        surfs.add( new SurfCylinder(20.0) );
        surfs.add( new SurfCylinder(30.0) );
        surfs.add( new SurfCylinder(40.0) );
        surfs.add( new SurfCylinder(50.0) );
        nstate = surfs.size();
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        for ( int i=0; i<5; ++i )
        {
            vec.set(i, 0.011*i);
            err.set(i,i, .001*i);
        }
        
        for (Iterator it = surfs.iterator(); it.hasNext(); )
        {
            vec.set(0,vec0);
            etracks.add( new ETrack((Surface) it.next(),vec,err) );
            vec0 += 1.1;
            
        }
        
        //******************************************************************
        System.out.println( ok_prefix + "Build chi-squares." );
        double[] chsqs = { 12.3, 23.4, 34.5, 45.6, 56.7 };
        
        //******************************************************************
        System.out.println( ok_prefix + "Build ss" );
        //	double[] ss = { 10.0, 20.0, 30.0, 40.0, 50.0 };
        
        //******************************************************************
        System.out.println( ok_prefix + "Build fit statii" );
        statii.add(FitStatus.OPTIMAL);
        statii.add(FitStatus.COMPLETE);
        statii.add(FitStatus.COMPLETE);
        statii.add(FitStatus.COMPLETE);
        statii.add(FitStatus.OPTIMAL);
        
        //******************************************************************
        System.out.println( ok_prefix + "Build global track states." );
        Assert.assertTrue( nstate == etracks.size() );
        Assert.assertTrue( nstate == chsqs.length );
        Assert.assertTrue( nstate == statii.size() );
        
        Iterator trkit = etracks.iterator();
        Iterator statit = statii.iterator();
        
        for ( int i=0; i<nstate; ++i )
        {
            stateset.add( new GTrackState( ss[i], (ETrack) trkit.next(), (FitStatus) statit.next(),
                    chsqs[i] ) );
        }
        
        
        for ( Iterator ista=stateset.iterator(); ista.hasNext(); )
        {
            states.add( (GTrackState) ista.next());
        }
        
        //******************************************************************
        System.out.println( ok_prefix + "Build global track." );
        gtr = new GTrack(stateset);
        System.out.println( gtr );
        Assert.assertTrue( gtr.isValid() );
        Assert.assertTrue( gtr.states().size() == 5 );
        //******************************************************************
        
    }
}
