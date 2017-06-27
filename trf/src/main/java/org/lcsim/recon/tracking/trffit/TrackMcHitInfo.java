package org.lcsim.recon.tracking.trffit;

import org.lcsim.recon.tracking.trfbase.Hit;
import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;

/** TrackMcHitInfo handles information about the MC Id of
 * the hits on a track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class TrackMcHitInfo
{
    
    // The map of Mc Ids with their number of hits
    private Map _mcidmap;
    
    // The number of hits on this track
    private int _numhits;
    
    // The best mc id
    private int _bestid;
    
    // The purity
    private double _purity;
    
    // The list of all Mc Ids
    private List _mcidlist;
    
    //methods
    
    //
    
    /**
     *Default Constructor leaves the object incomplete
     *
     */
    public TrackMcHitInfo()
    {
    }
    
    //
    
    /**
     *Full Constructor
     *
     * @param   nhits  The number of hits on this track.
     * @param   mcidmap  The map of number of hits per MC ID keyed on id.
     */
    public TrackMcHitInfo(int nhits, Map mcidmap)
    {
        _mcidlist = new ArrayList();
        _mcidmap = new HashMap(mcidmap);
        _numhits = nhits;
        _bestid = 0;
        _purity = 0.;
        //Map contains the MC track id paired with the number of times it occurred
        //on the track
        int nentries = 0;
        // Loop over the map...
        for( Iterator it=mcidmap.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry e = (Map.Entry) it.next();
//            System.out.println( e.getKey()+ ": " + e.getValue() );
            _mcidlist.add( e.getKey() );
            if ( ((Integer)e.getValue()).intValue()>nentries )
            {
                nentries = ((Integer)e.getValue()).intValue();
                Assert.assertTrue(nentries<= nhits);// Can't have more occurrences than hits.
                _bestid = ( (Integer)e.getKey()).intValue();// Best id by definition
                _purity = (double)nentries/(double)_numhits; // Purity is fraction of possible hits
            }
        }
    }
    
    //
    
    /**
     *Return the best MC ID for this track.
     * This is defined as the MC ID with the most number of Hits (not measurements!)
     *
     * @return  The MC Track ID for this track.
     */
    public int bestMcId()
    {
        return _bestid;
    }
    
    //
    
    /**
     *Return the purity of this track.
     * This is defined as the ratio of number of hits with best id to total number of hits
     *
     * @return The purity of the track defined as the ratio of the number of
     * best MC ID its to the total number of hits on the track.
     */
    public double purity()
    {
        return _purity;
    }
    
    //
    
    /**
     *Return the number of hits on this track.
     *
     * @return The total number of hits on this track. Note that this
     * is not the number of measurements, or degrees of freedom.
     */
    public int numHits()
    {
        return _numhits;
    }
    
    //
    
    /**
     *Return the full list of MC IDs.
     *
     * @return  The list of MC IDs associated with this track.
     */
    public List mc_idlist()
    {
        return new ArrayList(_mcidlist);
    }
    
    //
    
    /**
     * Return a map of MC IDs with the number of hits for that ID.
     *
     * @return Map keyed on MC ID containing the number of hits for that ID.
     */
    public Map mcHitNumber()
    {
        return new HashMap(_mcidmap);
    }
    
    
    /**
     * Output stream
     *
     * @return String representation of the class
     */
    public String toString()
    {
        return getClass().getName();
    }
    
}

