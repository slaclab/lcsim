package org.lcsim.recon.tracking.trffit;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Propagator;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.PropDir;

import java.util.*;
/**
 * This class describes a track as an ordered list of hits plus
 * an ETrack (fitted vector and error matrix at a surface) and
 * the fit chi-square.
 *<p>
 * Hits are ordered as they were fit.
 *<p>
 * The access to the list of hits is minimal.  Users are expected to
 * construct a new object to do more than update the fit or add a
 * hit to the end of the list.
 *<p>
 * The method is_fit returns true if the track has been fit with all its hits.
 * This state is set by calling set_fit and is unset when hits are added.
 * No hits are required to be on the track (so one can store a fit without
 * any hits).
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HTrack
{
    // Number of hits used in the current fit.
    // -1 for a track which has never been fit
    private int _nfit;
    
    
    // list of hits
    protected List _hits;
    
    // fitted track (or starting track)
    protected ETrack _tre;
    
    // fit chi-square
    protected double _chisq;
    
    
    //
    
    /**
     * Construct an instance that contains no hits and whose fit is invalid.
     *
     */
    public HTrack()
    {
        _tre = new ETrack();
        _chisq = 0.0;
        _nfit = -1;
        _hits = new ArrayList();
    }
    
    //
    
    /**
     *Construct and instance from a track and its error.
     * There are no hits on the track.
     *
     * @param   tre  The ETrack from which to construct this instance.
     */
    public HTrack( ETrack tre)
    {
        _tre = new ETrack(tre);
        _chisq = 0.0;
        _nfit = 0;
        _hits = new ArrayList();
    }
    
    //
    
    /**
     *Construct an instance which replicates its argument (Copy Constructor).
     *
     * @param   ht  The HTrack to replicate.
     */
    public HTrack( HTrack ht)
    {
        _tre = new ETrack(ht._tre);
        _chisq = ht._chisq;
        _nfit = ht._nfit;
        _hits = new ArrayList(ht._hits);
    }
    
    
    //
    
    /**
     *Add a hit to the back of the list.
     *
     * @param   hit The Hit to add to this track.
     */
    public void addHit( Hit hit)
    {_hits.add(hit);
    }
    
    //
    
    /**
     *Drop the last hit on the list. This does not affect the fit status.
     *
     */
    public  void dropHit()
    {
        _hits.remove(_hits.size()-1); // is there no lastElement in List?
    }
    
    //
    
    /**
     *Drop all hits. This does not affect the fit status.
     *
     */
    public  void dropHits()
    {
        _hits.clear();
    }
    
    //
    
    
    /**
     *Set the fit parameters for the current list of hits.
     *Be sure to add all relevant hits before invocation.
     * It is assumed the track has been fit with all of its hits.
     * The track and chi-square are assigned the specified value and
     * the number of fitted hits is set to the current number of hits.
     *
     * @param   tre The ETrack which represents the fit parameters and errors.
     * @param   chisq The chi-square of the fit.
     */
    public void setFit( ETrack tre, double chisq)
    {
        _tre = new ETrack(tre);
        _chisq = chisq;
        _nfit = _hits.size();
    }
    
    //
    
    
    /**
     * Unset the fit.
     * The number of hits is set to 0 and the chi-square is set to zero.
     * The track is not modified (the last fit becomes the starting track).
     *
     */
    public void unsetFit()
    {
        _nfit = -1;
        _chisq = 0.0;
    }
    
    //
    
    /**
     *Unset the fit and reset the starting track.
     * The fit is unset and the track is set to the specified value.
     *
     * @param   tre The ETrack which represents the track parameters and errors.
     */
    public void unsetFit( ETrack tre)
    {
        unsetFit();
        _tre = new ETrack(tre);
    }
    
    //
    
    /**
     *Return the list of hits.
     *
     * @return The list of hits on this track.
     */
    public List hits()
    {
        return new ArrayList(_hits);
    }
    
    //
    
    /**
     *Return a copy of the fit track parameters and errors.
     *
     * @return The ETrack which represents the track parameters and errors.
     */
    public ETrack newTrack()
    {
        return new ETrack(_tre);
    }
    
    //
    
    /**
     * Fetch a handle to the fit track parameters and errors.
     * @return The ETrack which represents the track parameters and errors.
     */
    public ETrack track()
    {
        return _tre;
    }
    
    //
    
    /**
     *Return the chi-square of the track fit.
     *
     * @return The chi-square of the track fit.
     */
    public double chisquared()
    { return _chisq;
    }
    
    //
    
    /**
     *Return the number of measurements on this track.
     *This is the number of hits times the dimension of the hit measurement.
     *
     * @return The number of measurements on this track.
     */
    public int numberOfMeasurements()
    {
        int nmeas = 0;
        for (Iterator it = _hits.iterator(); it.hasNext(); )
        {
            nmeas+= ((Hit)it.next()).size();
        }
        return nmeas;
    }
    
    //
    
    /**
     *Return the MC track information for hits on this track.
     *
     * @return The TrackMcHitInfo object encapsulating the MC track information for hits on this track.
     */
    public TrackMcHitInfo mcInfo()
    {
        List idlist = new ArrayList();
        Map idmap = new HashMap();
        int nhits = _hits.size();
        //Loop over the hits and fill the mcids list with all the McIds
        List mcids = new ArrayList();
//        for (Iterator it = _hits.iterator(); it.hasNext(); )
//        {
//            System.out.println( ((Hit) it.next()).mcIds().size() );
//            //	 	mcids.addAll( ((Hit) it.next()).get_mc_ids() );
//        }
        //Iterate over the list and fill the frequency map idmap
        for (Iterator it = mcids.iterator(); it.hasNext(); )
        {
            Integer freq = (Integer) it.next();
            idmap.put( freq, (freq==null ? new Integer(1) : new Integer(freq.intValue()+1)) );
        }
        // Return a TrackMcHitInfo object
        return new TrackMcHitInfo(nhits, idmap);
    }
    
    //
    
    /**
     *Return the fit status: true if track has one or more hits
     * and has been fit with all of its hits.
     *
     * @return true if the track contains a valid fit.
     */
    public boolean isFit()
    {
        return _tre.isValid() && ( _nfit == _hits.size() );
    }
    
    //
    
    
    /**
     *Propagate the fit track.
     * Note that this does not change the fit status because the propagation
     * should be reversible.
     *
     * @param   prop The Propagator to use for propagation.
     * @param   srf The Surface to propagate to.
     * @return The propagation status.
     */
    public PropStat propagate( Propagator prop,  Surface srf)
    {
        return prop.errProp(_tre, srf);
    }
    
    //
    
    
    /**
     *Propagate the fit track in a specified direction.
     * Note that this does not change the fit status because the propagation
     * should be reversible.
     *
     * @param   prop The Propagator to use for propagation.
     * @param   srf The Surface to propagate to.
     * @param   dir The direction in which to propagate.
     * @return The propagation status.
     */
    public PropStat propagate( Propagator prop,  Surface srf, PropDir dir)
    {
        return prop.errDirProp(_tre,srf,dir);
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this class.
     */
    public String toString()
    {
        StringBuffer sb =  new StringBuffer(getClass().getName()+" is ");
        if( ! isFit()) sb.append("not ");
        sb.append("fit: \n" + _tre +"\n");
        sb.append("Chi-square: "+_chisq+"\n");
        sb.append("Track has "+_hits.size()+" hit");
        if(_hits.size() != 1) sb.append("s");
        if (_hits.size() > 0 )
        {
            sb.append(":\n");
            for (Iterator it = _hits.iterator(); it.hasNext(); )
            {
                sb.append( ((Hit) it.next()+"\n") );
            }
        }
        return sb.toString();
    }
    
    
    /**
     *Test equality of this object against another HTrack.
     *
     * @param   ht The HTrack against which to compare.
     * @return true if the two tracks are equal.
     */
    public boolean equals( HTrack ht)
    {
        if( ! hits().equals(ht.hits()) ) return false;
        if( ! newTrack().equals(ht.newTrack()) ) return false;
        if(   chisquared() != ht.chisquared() ) return false;
        if(   isFit()     != ht.isFit() ) return false;
        return true;
    }
    
    
    /**
     * Test inequality of this object against another HTrack.
     *
     * @param   ht The HTrack against which to compare.
     * @return true if the two tracks are equal.
     */
    public boolean notEquals( HTrack ht)
    {
        return ! equals(ht);
    }
}


