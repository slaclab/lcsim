package org.lcsim.recon.tracking.trffit;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Hit;
import java.util.*;

/**
 * Full track fit using Kalman filter.  The propagator is specified
 * when the fitter is constructed.  The starting surface, vector and
 * error matrix are taken from the input track.  Errors should be
 * increased appropriately if the fitter is applied repeatedly to
 * a single track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/
public class FullFitKalman extends FullFitter
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' the type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public static String typeName()
    { return "FullFitKalman"; }
    
    //
    
    /**
     *Return a String representation of the class' the type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // The propagator.
    private Propagator _pprop;
    
    // The add fitter.
    private AddFitKalman _addfit;
    
    //
    
    /**
     *Construct an instance specifying a propagator.
     *
     * @param   prop The Propagator to be used during the fit.
     */
    public FullFitKalman(Propagator prop)
    {
        _pprop = prop;
        _addfit = new AddFitKalman();
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *Return the propagator.
     *
     * @return The Propagator used in the fit.
     */
    public  Propagator propagator()
    { return _pprop; }
    
    //
    
    /**
     *Fit the specified track.
     *
     * @param   trh The HTrack to fit.
     * @return 0 if successful.
     */
    public int fit(HTrack trh)
    {
        // Copy the hits from the track.
        List hits = trh.hits();
        //System.out.println("Hits has "+hits.size()+" elements");
        // Delete the list of hits from the track.
        while ( trh.hits().size()>0 ) trh.dropHit();
        //System.out.println("Hits has "+hits.size()+" elements");
        
        // Set direction to be nearest.
        PropDir dir = PropDir.NEAREST;
        
        // Loop over hits and fit.
        int icount = 0;
        for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
        {
            
            // Extract the next hit pointer.
            Hit hit = (Hit)ihit.next();
            //System.out.println("Hit "+icount+" is: \n"+hit);
            // propagate to the surface
            PropStat pstat = trh.propagate(_pprop,hit.surface(),dir);
            if ( ! pstat.success() ) return icount;
            
            // fit track
            //System.out.println("trh= \n"+trh+", hit= \n"+hit);
            //System.out.println("_addfit= "+_addfit);
            int fstat = _addfit.addHit(trh,hit);
            if ( fstat>0 ) return 10000 + 1000*fstat + icount;
            
        }
        return 0;
        
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return getClass().getName();
    }
    
}
