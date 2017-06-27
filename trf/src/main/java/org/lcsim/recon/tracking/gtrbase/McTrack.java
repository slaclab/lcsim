package org.lcsim.recon.tracking.gtrbase;
// McTrack

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import org.lcsim.recon.tracking.trfbase.Surface;

/**
 * This class describes a propagated Monte Carlo track.
 * It is made up of a set of McTrackStates along the trajectory
 * of the track.
 *<p>
 * We use a SortedSet to guarantee ordering in s.
 *<p>
 * The first state is the start of the trajectory (production
 * or entering detector volume) and the last is the end of the
 * trajectory (decay or volume exit).
 *<p>
 * The VTrack's represent the changing state of the track.
 *<p>
 * Users may fetch the entire set of states or may request the
 * state at a specified surface.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class McTrack
{   
    // attributes
    
    private static McTrackState BadState = new McTrackState();
    
    // Version number
    private int _version;
    
    // List of states.
    private SortedSet _states;
    
    // MC Particle Id
    // Id of the MC particle which created the states of this track
    int _mctrackid;
    
    // PDG particle type
    int _pdgid;
    
    // Particle Parentage
    // Packed word containing information about particles heritage
    int _parentword;
    
    // methods
    
    // Return the index for the first matching state.
    // Return -1 if there is no match.
    private int find( Surface srf)
    {
        int count = 0;
        for ( Iterator ista=_states.iterator(); ista.hasNext(); )
            if ( ((McTrackState)ista.next()).track().surface().pureEqual(srf) ) return count++;
        return -1;
        
    }
    // methods
    
    /**
     *Construct a default instance.
     * Leaves object in invalid state.
     *
     */
    public McTrack()
    {
    }
    
    /**
     *Construct a replica of the McTrack (copy constructor).
     *
     * @param   mct The McTrack to replicate.
     */
    public McTrack(McTrack mct)
    {
        _states = new TreeSet();
        for(Iterator it = mct._states.iterator(); it.hasNext(); )
        {
            _states.add(it.next());
        }
        _mctrackid = mct._mctrackid;
        _parentword = mct._parentword;
        _pdgid = mct._pdgid;
    }
    
    /**
     * Construct n instance from a list of states, the MC Track ID, and a
     * packed parent word (see gtrbase/McParent).
     * Size must be nonzero and all states must be valid or
     * assertion is thrown and all states are discarded.
     *
     * @param   states A list of track states.
     * @param   mcid   The MC track ID.
     * @param   parentword A packed parent word.
     * @param   pdgid   The pdgid associated with this track.
     */
    public McTrack( Set states, int mcid, int parentword, int pdgid)
    {
        _states = new TreeSet();
        for(Iterator it = states.iterator(); it.hasNext(); )
        {
            _states.add(it.next());
        }
        _mctrackid = mcid;
        _parentword = parentword;
        _pdgid = pdgid;
        // Check that all states are valid.
        int ok_count = 0;
        for ( Iterator ista=_states.iterator(); ista.hasNext(); )
        {
            if ( ! ((McTrackState)ista.next()).isValid() ) break;
            ++ok_count;
        }
        if( ok_count != _states.size() ) throw new IllegalArgumentException("McTrackStates not valid!");
        if ( ok_count != _states.size() )
            _states.clear();
        
    }
    
    /**
     *Test track validity.
     *
     * @return true if this track has one or more states.
     */
    public boolean isValid()
    {
        if(_states == null) return false;
        return _states.size()!=0;
    }
    
    /**
     *Return the list of states.
     *
     * @return The list of states comprising this track.
     */
    public Set states()
    {
        TreeSet states = new TreeSet();
        for(Iterator it = _states.iterator(); it.hasNext(); )
        {
            states.add(it.next());
        }
        return states;
    }
    
    /**
     *Return whether a surface can be matched.
     *
     * @param   srf The surface to check.
     * @return true whether this track contains a state at the surface srf.
     */
    public boolean hasSurface( Surface srf)
    {
        return find(srf) != -1;
    }
    
    /**
     *Return the state for a particular surface.
     *The first match is returned.
     * Surface bounds are not required to match.
     * A reference to an invalid state is returned if the surface
     * cannot be matched.
     *
     * @param   srf The surface for which the state is requested.
     * @return The McTrackState at the surface srf.
     */
    public McTrackState state( Surface srf)
    {
        for ( Iterator ista=_states.iterator(); ista.hasNext(); )
        {
            McTrackState tmp = (McTrackState)ista.next();
            if ( tmp.track().surface().pureEqual(srf) ) return new McTrackState(tmp);
        }
        return BadState;
    }
    
    /**
     *Add an McTrackState to this track.
     *
     * @param   state The McTrackState to add.
     */
    public void addState(McTrackState state)
    {
        if(_states == null)  _states = new TreeSet();
        if(_states.contains(state)) throw new IllegalArgumentException("McTrack already contains this state!");
        _states.add(new McTrackState(state));
    }
    
    /**
     *Return the MC Id for the Track creating this track's states.
     *
     * @return The MC track ID.
     */
    public int mcTrackId()
    {
        return _mctrackid;
    }
    
    /**
     *Set the MC Id for the Track creating this track's states.
     *
     * @param   mctrackid The MC track ID.
     */
    public void setMcTrackId( int mctrackid)
    {
        _mctrackid = mctrackid;
    }
    
    
    /**
     *Return the PDG particle Id for the Track creating this track's states.
     *
     * @return The MC track pdg ID.
     */
    public int pdgId()
    {
        return _pdgid;
    }
    
    /**
     *Set the PDG particle Id for the Track creating this track's states
     *
     * @param   pdgid The MC track pdg ID.
     */
    public void setPdgId( int pdgid)
    {
        _pdgid = pdgid;
    }
    
    
    /**
     *Return the parentage of this particle.
     *
     * @return The MCParent for this track's MC track.
     */
    public McParent parent()
    {
        return new McParent(_parentword);
    }
    
    /**
     *Set the MC parentage of this particle.
     *
     * @param   parent The MCParent for this track's MC track.
     */
    public void setParent(McParent parent)
    {
        _parentword = parent.parentWord();
    }
    
    /**
     *Set the MC parentage of this particle
     *
     * @param   parentword The packed MC parentage.
     */
    public void setParent(int parentword)
    {
        _parentword = parentword;
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb;
        if(isValid())
        {
            sb = new StringBuffer("McTrack of type ");
            sb.append(pdgId() + " and track ID " + mcTrackId()+"\n");
            sb.append( "with " +_states.size() + " MC track states\n\n");
            for(Iterator it = _states.iterator(); it.hasNext(); )
            {
                sb.append(it.next());
            }
        }
        else
        {
            sb = new StringBuffer("Invalid McTrack");
        }
        return sb.toString();
    }
 
    /**
     *Equality
     *
     * @param   mct The McTrack to test.
     * @return true if equal.
     */
    public boolean equals(McTrack mct)
    {
        return _states.equals(mct._states);
    }
    
    
    /**
     *Test inequality.
     *
     * @param   mct The McTrack to test.
     * @return true if not equal.
     */
    public boolean notEquals(McTrack mct)
    {
        return !equals(mct);
    }
    
}
