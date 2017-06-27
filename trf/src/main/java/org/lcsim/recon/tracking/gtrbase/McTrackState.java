package org.lcsim.recon.tracking.gtrbase;
// McTrackState

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackVector;

/**
 * This class represents the state of a Monte Carlo track at one
 * point along its trajectory.  It consists of the path distance and
 * a VTrack. It can be sorted by the path distance.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class McTrackState implements Comparable
{
    
    // The path distance.
    private double _s;
    
    // The track.
    private VTrack _trv;
    
    // The detector element id
    
    private int _id;
    
  
    /**
     *Construct a default instance.
     *
     */
    public McTrackState()
    {
    }
   
    /**
     *Construct an instance from a path distance and track.
     *
     * @param   s The path distance to this state..
     * @param   trv The VTrack at this state.
     */
    public McTrackState(double s, VTrack trv)
    {
        _s = s;
        _trv = new VTrack(trv);
        _id = 0;
    }
    
    /**
     *Construct an instance replicating the McTrackState (copy constructor).
     *
     * @param   ts The McTrackState to replicate.
     */
    public McTrackState( McTrackState ts)
    {
        _s = ts._s;
        _trv = new VTrack(ts._trv);
        _id = ts._id;
    }
   
    /**
     *Construct an instance from the track distance, a surface and a track vector.
     *
     * @param   s The path distance to this state.
     * @param   srf The surface for this state.
     * @param   vec The VTrack at this state.
     */
    public McTrackState(double s, Surface srf, TrackVector vec)
    {
        _s = s;
        _trv = new VTrack(srf,vec);
        _id = 0;
    }
   
    /**
     *Check this state's validity.
     *
     * @return true if the track is valid.
     */
    public boolean isValid()
    {
        if(_trv==null) return false;
        return _trv.isValid();
    }
   
    /**
     *Return the path distance for this McTrackState.
     *
     * @return The path distance.
     */
    public double s()
    { 
        return _s; 
    }
    
    /**
     * Return the track.
     *
     * @return The VTrack at this state.
     */
    public  VTrack track()
    { 
        return new VTrack(_trv); 
    }
   
    /**
     *Return the detector id for this state.
     *
     * @return The detector id for this state.
     */
    public DetectorID detectorId()
    {
        return new DetectorID(_id);
    }
  
    /**
     * Set the detector id for this state.
     *
     * @param   id The detector id for this state.
     */
    public void setDetectorId( DetectorID id)
    {
        _id=id.detectorId();
    }
    
    
    /**
     * Test equality.
     *
     * @param   ts The McTrackState to test.
     * @return true if the states are equal.
     */
    public boolean equals(McTrackState ts)
    {
        return (_s == ts._s) && (track().equals(ts.track()) );
    }
  
    /**
     * Test object equality.
     *
     * @param   ts The Object to test.
     * @return true if objects are equal.
     */
    public boolean equals(Object ts)
    {
        if(!(ts instanceof McTrackState)) return false;
        return (_s == ((McTrackState)ts)._s) && (track().equals(((McTrackState)ts).track()) );
    }
  
    /**
     * Test ordering.
     *
     * @param ts McTrackState to test.
     * @return true if detid is larger.
     */
    public boolean lessThan(McTrackState ts)
    {
        return _s<ts._s;
    }
    
    // print
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("McTrackState ");
        if ( isValid() )
        {
            sb.append("Path distance is " +_s + ".\n");
            sb.append("Detector Element Id is " + detectorId() +".\n");
            sb.append(track());
            sb.append("\n");
        }
        else
        {
            sb.append("Invalid state.");
        }
        return sb.toString();
    }
 
    /**
     * Comparable interface
     * @param o Object to compare to.
     * @return -1 if less, 0 if equal, 1 if greater.
     */    
    public int compareTo(Object o)
    {
        double s = ( (McTrackState) o)._s;
        return (_s < s ? -1 : (_s == s ? 0 : 1));
    }
    
}
