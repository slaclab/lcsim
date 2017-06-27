package org.lcsim.recon.tracking.gtrbase;


import java.util.Set;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
/**
 * This class describes a global track consisting of a list of states
 * which specify the fit and hit or miss at different points along
 * the track trajectory.  See GTrackState for a more complete
 * description of the states.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class GTrack
{
    
    // static attributes
    
    // Minimum and maximum values for s.
    // All states should have values in the range SMIN <= s <= SMAX.
    static final double SMIN = -1.e30;;
    static final double SMAX =  1.e30;;
    
    static final GTrackState BAD_STATE = new GTrackState();
    // propagator
    private static Propagator _prop;
    
    // attributes
    
    // Flag indicating the track is valid.
    // This is set false for the default constructor.
    private boolean _valid;
    
    // List of states.
    private TreeSet _states;
    
    // methods
    
    
    /**
     * Construct a default instance.
     * Leaves object in an invalid state.
     *
     */
    public GTrack()
    {
        _valid = false;
    }
    
    /**
     *Construct an instance from a list of states.
     * Track is set valid if all states meet the following:
     * 1. have SMIN <= s <= SMAX
     * 2. are valid
     * 3. have valid fits (not INVALID)
     *
     * @param   states The list of track states.
     */
    public GTrack( TreeSet states)
    {
        _valid = true;
        _states = new TreeSet();
        _states.addAll(states);
        
        
        // Check below assumes list has at least one entry.
        Assert.assertTrue( states().size()>0 );
        if ( states().size()==0 ) _valid = false;
        
        // Check that the s-values are in range.
        Assert.assertTrue( ((GTrackState) states().first()).s() >= SMIN );
        Assert.assertTrue( ((GTrackState) states().last()).s() <= SMAX );
        
        // Check that all states are valid and have valid fits.
        for ( Iterator ista=states().iterator(); ista.hasNext(); )
        {
            FitStatus fstat = ((GTrackState) ista.next()).fitStatus();
            Assert.assertTrue( fstat != FitStatus.BADSTATE );
            if ( fstat == FitStatus.BADSTATE ) _valid = false;
        }
    }
    
    /**
     * Construct a replica of the GTrack (copy constructor).
     *
     * @param gtr The GTrack to replicate.
     */
    public GTrack( GTrack gtr)
    {
        _valid = gtr._valid;
        _states = new TreeSet();
        if( _valid) _states.addAll(gtr._states);
    }
    
    /**
     *Rebuild the existing track from new set of GTrackStates (needed for Java's inability
     * to modify local handles)
     *
     * @param   states The list of GTrackStates.
     */
    public void update( TreeSet states )
    {
        _valid = true;
        _states = new TreeSet();
        _states.addAll(states);
        
        
        // Check below assumes list has at least one entry.
        Assert.assertTrue( states().size()>0 );
        if ( states().size()==0 ) _valid = false;
        
        // Check that the s-values are in range.
        Assert.assertTrue( ((GTrackState) states().first()).s() >= SMIN );
        Assert.assertTrue( ((GTrackState) states().last()).s() <= SMAX );
        
        // Check that all states are valid and have valid fits.
        for ( Iterator ista=states().iterator(); ista.hasNext(); )
        {
            FitStatus fstat = ((GTrackState) ista.next()).fitStatus();
            Assert.assertTrue( fstat != FitStatus.BADSTATE );
            if ( fstat == FitStatus.BADSTATE ) _valid = false;
        }
    }
    
    /**
     * Drop the fit for the state at the specified path distance s.
     *
     * @param s The path distance at which to drop a fit.
     * @return 0 for success.
     */
    public int dropFit(double s)
    {
        GTrackState gts = new GTrackState(s);
        if( _states.contains(gts) )
        {
            ((GTrackState) _states.tailSet(gts).iterator().next()).dropFit();
            return 0;
        }
        else
        {
            return 1;
        }
    }
    
    /**
     * Check the tracks validity.
     *
     * @return true if the GTrack is valid.
     */
    public boolean isValid()
    {
        return _valid;
    }
    
    /**
     *Return the list of states.
     *
     * @return The list of states.
     */
    public TreeSet states()
    {
        return _states;
    }
    
    /**
     *Return a state at a path distance s.
     *
     * @param   s The path distance for which to return a state.
     * @return an invalid state if there is no state at that s.
     */
    public GTrackState state(double s)
    {
        GTrackState gts = new GTrackState(s);
        if( _states.contains(gts) )
        {
            return new GTrackState( (GTrackState) _states.tailSet(gts).iterator().next() );
        }
        else
        {
            return BAD_STATE;
        }
    }
    
    /**
     *Return the existing state at a particular surface.
     * The first match  with SMIN <= s < SMAX is returned.
     * A reference to an invalid state is returned if the surface
     * cannot be matched.
     * Surface bounds are not required to match.
     *
     * @param   srf The surface for which to return a state.
     * @return an invalid state if there is no state at that s.
     */
    public  GTrackState state( Surface srf)
    {
        return state(srf, SMIN, SMAX);
    }
    
    /**
     *Return the existing state at a particular surface.
     * The first match  with s1 <= s < s2 is returned.
     * A reference to an invalid state is returned if the surface
     * cannot be matched.
     * Surface bounds are not required to match.	 *
     * @param   srf The surface for which to return a state.
     * @param   s1  The lower bound on the path distance.
     * @param   s2  The upper bound on the path distance.
     * @return an invalid state if there is no state betwee s1 and s2.
     */
    public  GTrackState state( Surface srf,
            double s1, double s2)
    {
        // Fetch the states between s1 and s2
        SortedSet sset = _states.subSet(new GTrackState(s1), new GTrackState(s2) );
        // If no states in this interval return
        if( sset.size() == 0 ) return BAD_STATE;
        // Else loop over the states and check the surface
        for( Iterator it = sset.iterator(); it.hasNext(); )
        {
            GTrackState state = (GTrackState) it.next();
            if ( state.track().surface().pureEqual(srf) ) return state;
        }
        return BAD_STATE;
    }
    
    // Return the number of measurements in the fit for this track
    public int numberOfMeasurements()
    {
        // Need to fix this to reflect measurements, not states
        // Need Clusters in GTrackState
        return _states.size();
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer( "GTrack: \n" );
        for (Iterator it = _states.iterator(); it.hasNext(); )
        {
            sb.append( (GTrackState) it.next() );
        }
        return sb.toString();
    }
    
    /**
     *Test equality.
     *
     * @param   gtr The GTrack to test.
     * @return true if the tracks are equal.
     */
    public boolean equals( GTrack gtr)
    {
        if(_valid != gtr._valid) return false;
        if(_states.size() != gtr._states.size() ) return false;
        Iterator it0 = _states.iterator();
        for ( Iterator it1 = gtr._states.iterator(); it1.hasNext(); )
        {
            if ( !( (GTrackState) it1.next()).equals( (GTrackState) it0.next() ) ) return false;
        }
        return true;
    }
    
    /**
     *Test equality.
     *
     * @param   gtr The GTrack to test.
     * @return true if the tracks are not equal.
     */
    public boolean notEquals( GTrack gtr)
    {
        return !equals(gtr);
    }
    
}


