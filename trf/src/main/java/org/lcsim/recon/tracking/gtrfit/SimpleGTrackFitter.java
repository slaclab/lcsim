package org.lcsim.recon.tracking.gtrfit;

import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trffit.AddFitter;
import org.lcsim.recon.tracking.trffit.AddFitKalman;
import org.lcsim.recon.tracking.gtrbase.GTrack;
import org.lcsim.recon.tracking.gtrbase.GTrackState;
import org.lcsim.recon.tracking.gtrbase.FitStatus;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.PropDir;

import org.lcsim.recon.tracking.trffit.HTrack;
/**
 * A class which fits a GTrack.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class SimpleGTrackFitter
{
    
    //**********************************************************************
    // Local definitions.
    //**********************************************************************
    
    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private static final double ERRFAC = 10.;
    
    
    // attributes
    
    // Propagator.
    private Propagator _prop;
    
    // Fit order.
    private int _order;
    
    // Add fitter.
    private AddFitter _fitter;
    
    // Maximum chi-square.
    private double _chsq_max;
    
    
    // methods
    
    /**
     *Construct an instance from a propagator, the order in which to fit, and a
     * chi-square value on which to cut.
     * order = fit order (1 for going out, -1 for going in)
     *
     * @param   prop   The Propagator to use in the fit.
     * @param   order  The order in wich to fit (1 for going out, -1 for going in).
     * @param   chsq_max The maximum value of chi-square to allow for a hit.
     */
    public  SimpleGTrackFitter( Propagator prop, int order,
            double chsq_max)
    {
        _prop = prop;
        _order = order;
        _fitter =  new AddFitKalman();
        _chsq_max = chsq_max;
        if ( _order!=FORWARD && _order!=BACKWARD )
        {
            throw new IllegalArgumentException("Fit order must be FORWARD or BACKWARD!");
        }
    }
    
    
    /**
     *Fit a track.
     *
     * @param   gtr The GTrack to fit.
     * @return 0 for successful fit.
     */
    public int fit(GTrack gtr)
    {
        
        // Extract list of states.
        TreeSet oldstates = gtr.states();
        
        // Require track to have at least one valid state.
        if ( oldstates.size() < 1 )
        {
            return 1;
        }
        
        // Extract starting track and error.
        GTrackState state0 = new GTrackState();
        if ( _order == FORWARD ) state0  = (GTrackState) oldstates.first();
        if ( _order == BACKWARD ) state0 = (GTrackState) oldstates.last();
        
        // The first state must be valid.
        Assert.assertTrue( state0.isValid() );
        if ( ! state0.isValid() )
        {
            return 2;
        }
        
        double s0 = state0.s();
        ETrack tre0 = state0.track();
        //Starting surface
        Surface srf0 = tre0.surface();
        // Increase error.
        {
            TrackError err = tre0.error();
            for ( int j=0; j<5; ++j )
            {
                for ( int i=0; i<=j; ++i )
                {
                    err.set(i,j, err.get(i,j)*ERRFAC );
                }
            }
            tre0.setError(err);
        }
        
        // Build list of clusters, preserving or reversing order.
        
        List clusters = new ArrayList();
        
        for ( Iterator istate=oldstates.iterator(); istate.hasNext(); )
        {
            clusters.add( ((GTrackState) istate.next() ).cluster() );
        }
        if ( _order == BACKWARD ) Collections.reverse(clusters);
        
        // Build starting HTrack.
        HTrack trh = new HTrack(tre0);
        
        // New GTrack state list.
        TreeSet newstates = new TreeSet();
        
        //
        // Loop over states and add each cluster in turn to the fit.
        // If cluster does not exist, interact track at the state surface
        //
        double spath = 0.0;
        boolean first = true;
        for ( Iterator clusit = clusters.iterator(); clusit.hasNext(); )
        {
            
            // Get the TRF++ cluster.
            
            Cluster clus = (Cluster) clusit.next();
            double ds1 = 0;
            double ds2 = 0;
            
            // Propagate the fitted track to the cluster surface.
            Surface srf = clus.surface().newSurface();
            Assert.assertTrue( srf != null );
            if( !first || !srf.pureEqual(srf0) )
            {
                PropStat pstat =
                        trh.propagate( _prop, srf, PropDir.NEAREST );
                if ( ! pstat.success())
                {
                    return 3;
                }
                ds1 = pstat.pathDistance();
            }
            
            // Extract the hit.
            List hits = clus.predict( trh.newTrack(), clus );
            Assert.assertTrue( hits.size() == 1 );
            Hit hit = (Hit) hits.get(0);
            
            // Propagate the fitted track to the hit surface.
            Surface srf2 = (hit.surface()).newSurface();
            if( !srf2.pureEqual(srf) )
            {
                Assert.assertTrue( srf2 != null );
                PropStat pstat = trh.propagate( _prop, srf2, PropDir.NEAREST );
                if ( ! pstat.success())
                {
                    return 4;
                }
                ds2 = pstat.pathDistance();
            }
            
            // Update the path distance.
            double ds = ds1 + ds2;
            boolean ds_ok = ( _order == FORWARD && ds >= 0.0) ||
                    ( _order == BACKWARD && ds <= 0.0);
            if ( ! ds_ok )
            {
                return 5;
            }
            spath += ds;
            
            // Fit with the new hit.
            int stat = _fitter.addHit(trh,hit);
            
            // bad fit
            if ( stat != 0 )
            {
                return 6;
            }
            // good fit, but bad chi-squared
            if ( trh.chisquared() > _chsq_max )
            {
                return 7;
            }
            
            // Add new GTrackState.
            
            FitStatus fstat = FitStatus.INVALID;
            // If there are no more clusters this fit is OPTIMAL at this state
            if ( ! clusit.hasNext() )
            {
                fstat = FitStatus.OPTIMAL;
            }
            else
            {
                if ( _order == 1 ) fstat = FitStatus.FORWARD;
                if ( _order == -1 )fstat = FitStatus.BACKWARD;
            }
            
            ETrack tre = trh.newTrack();
            double chsq = trh.chisquared();
            newstates.add( new GTrackState( spath, tre, fstat, chsq, clus ));
            
            //	}
            
            first = false;
            
        }  // end loop over clusters.
        
        // Construct new GTrack.
        gtr.update(newstates);
        
        return 0;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "SimpleGTrackFitter: " +
                "\nPropagator: " + _prop +
                "\nFitter: " + _fitter +
                "\n with chsq_max = " + _chsq_max;
    }
}