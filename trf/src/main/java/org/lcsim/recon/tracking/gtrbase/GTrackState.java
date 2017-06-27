package org.lcsim.recon.tracking.gtrbase;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Miss;

/**
 * This class represents the state of a reconstructed track at one
 * point along its trajectory.  It consists of
 *<p>
 * <li>1. the relative path distance s
 * <li>2. a fit at a surface (ETrack)
 * <li>3. the fit status (invalid, optimal, ...)
 * <li>4. a chi-square for that fit
 * <li>5. smoothing data           * not yet implemented
 * <li>6. an optional cluster
 * <li>7. an optional Miss
 *<p>
 * The fit at any surface should be an optimal one accounting for
 * all the preceeding and following clusters.  It is *not* a
 * partial fit including only the preceeding clusters.
 *<p>
 * In principle one can obtain an optimal fit for any point outside
 * the first or last cluster and immediately between any intermediate
 * adjacent clusters for which smoothing is defined.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */


public class GTrackState implements Comparable
{
    
    
    // The path distance.
    private double _s;
    
    // The track.
    private ETrack _tre;
    
    // The fit status.
    private FitStatus _fit_status;
    
    // Chi-square.
    private double _chi_square;
    
    // Optional smoothing data.
    //  private Smooth _smooth;
    
    // Optional cluster.
    private Cluster _clu;
    
    // Optional miss.
    private Miss _miss;
    
    
    /**
     *Construct a default instance.
     * This  leaves the state invalid.
     *
     */
    public GTrackState()
    {
        _s = 0.0;
        _tre = new ETrack();
        _fit_status = FitStatus.BADSTATE;
        _chi_square = 0.0;
        _clu = null;
        _miss = null;
    }
    
    /**
     *Construct an instance from the path distance only.
     * Fit is set invalid and no cluster or miss is assigned.
     * This is useful for searching sets using s as a comparator.
     *
     * @param   s The path distance to this state.
     */
    public  GTrackState(double s)
    {
        _s = s;
        _tre = new ETrack();
        _fit_status = FitStatus.INVALID;
        _chi_square = 0.0;
        _clu = null;
        _miss = null;
    }
    
    /**
     * Construct an instance from the path distance and the track fit.
     * No cluster or miss.
     *
     * @param   s    The path distance to this state.
     * @param   tre  The fit ETRack.
     * @param   fit_status The fit status.
     * @param   chi_square The chi-square of the fit.
     */
    public GTrackState(double s, ETrack tre, FitStatus fit_status,
    double chi_square)
    {
        _s = s;
        _tre = new ETrack(tre);
        _fit_status = fit_status;
        _chi_square = chi_square;
        _clu = null;
        _miss = null;
    }
  
    /**
     *Construct an instance from the path distance, the track fit and a cluster.
     *
     * @param   s      The path distance to this state.
     * @param   tre    The fit ETRack.
     * @param   fit_status The fit status.
     * @param   chi_square The chi-square of the fit.
     * @param   clu   The cluster associated with this track state.
     */
    public GTrackState(double s, ETrack tre, FitStatus fit_status,
    double chi_square, Cluster clu)
    {
        _s = s;
        _tre = new ETrack(tre);
        _fit_status = fit_status;
        _chi_square = chi_square;
        _clu = clu;
        _miss = null;
    }
   
    /**
     *Construct an instance from the path distance, the track fit and a miss.
     *
     * @param   s      The path distance to this state.
     * @param   tre    The fit ETRack.
     * @param   fit_status The fit status.
     * @param   chi_square The chi-square of the fit.
     * @param   miss The miss associated with this track state.
     */
    public GTrackState(double s, ETrack tre, FitStatus fit_status,
    double chi_square, Miss miss)
    {
        _s = s;
        _tre = new ETrack(tre);
        _fit_status = fit_status;
        _chi_square = chi_square;
        _clu = null;
        _miss = miss;
    }
   
    /**
     *Construct an instance replicating the GTrackState (copy constructor).
     *
     * @param   gts The GTrackState to replicate.
     */
    public GTrackState( GTrackState gts)
    {
        _s= gts._s;
        _tre = new ETrack(gts._tre);
        _fit_status = gts._fit_status;
        _chi_square = gts._chi_square;
        _miss = gts._miss;
    }
    
    /**
     * Drop the fit from the state.
     * The fit, error and chi-square are zeroed.
     * The fit status is set INVALID.
     *
     */
    public void dropFit()
    {
        TrackVector vec = new TrackVector();
        for(int i = 0; i<5; ++i) vec.set(i,0.0);
        TrackError err = new TrackError();
        for(int i = 0; i<5; ++i)
        {
            for(int j = 0; j<5; ++j)
            {
                err.set(i, j, 0.0);
            }
        }
        _tre = new ETrack(_tre.surface(), vec, err);
        _chi_square = 0.0;
        _fit_status = FitStatus.INVALID;
    }
   
    /**
     * Return if this state is valid.
     * 
     *
     * @return true if the track is valid.
     */
    public boolean isValid()
    {
        return _fit_status != FitStatus.BADSTATE;
    }
  
    /**
     *Return whether the state is valid and has a valid fit.
     *
     * @return true if the track and fit is valid.
     */
    public boolean hasValidFit()
    {
        return isValid() && _fit_status != FitStatus.INVALID;
    }
    
    /**
     *Return the path distance to this state.
     *
     * @return The path distance.
     */
    public double s()
    {
        return _s;
    }
 
    /**
     *Return the track t this state.
     *
     * @return The ETrack representing the fit at this state.
     */
    public  ETrack track()
    {
        return _tre;
    }
   
    /**
     * Return the fit status.
     * For most applications, anything but OPTIMAL is suspect.
     *
     * @return The status of the fit at this state.
     */
    public FitStatus fitStatus()
    {
        return _fit_status;
    }
    
    /**
     *Return the chi-square of the fit.
     *
     * @return The fit chi-square.
     */
    public double chiSquared()
    {
        return _chi_square;
    }
   
    /**
     * Return the cluster
     *
     * @return The cluster associated with this state.
     */
    public Cluster cluster()
    {
        return _clu;
    }
   
    /**
     *Return the miss.
     *
     * @return The miss associated with this state.
     */
    public  Miss miss()
    {
        return _miss;
    }
   
    /**
     *Test equality.
     * Requires complete match.
     *
     * @param   gts The GTrackState to test.
     * @return true if the states are equal.
     */
    public boolean equals( GTrackState gts)
    {
        if ( _s != gts.s() ) return false;
        if ( _fit_status != gts.fitStatus() ) return false;
        if ( _fit_status != FitStatus.INVALID )
        {
            if ( !_tre.equals(gts.track()) ) return false;
            if ( _chi_square != gts.chiSquared() ) return false;
        }
        if ( _clu == null && gts._clu != null) return false;
        if ( _clu != null && gts._clu == null) return false;
        if ( _clu != null && gts._clu != null)
        {
            if ( !_clu.equals(gts.cluster()) ) return false;
        }
        if ( _miss == null && gts._miss != null) return false;
        if ( _miss != null && gts._miss == null) return false;
        if ( _miss != null && gts._miss != null)
        {
            if ( !_miss.equals(gts.miss()) ) return false;
        }
        return true;
    }
  
    /**
     * Test nequality.
     *
     * @param   gts The GTrackState to test.
     * @return true if the states are not equal.
     */
    public  boolean notEquals( GTrackState gts)
    {
        return ! (equals(gts));
    }
    

    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("GTrackState: \n");
        if ( isValid() )
        {
            sb.append( "Path distance is " + _s + ".\n" );
            sb.append( track() + "\n");
            sb.append( "Fit status is " + fitStatus() + ".\n");
            sb.append( "Chi-square is " + chiSquared() + ".\n" );
            if ( cluster()!=null ) sb.append( cluster() + "\n");
            else sb.append( "No cluster defined.\n" );
            if ( miss()!=null ) sb.append( miss() + "\n");
            else sb.append( "No miss defined." );
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
    public int compareTo( Object o )
    {
        // Make sure o is GTrackState
        double s = ( (GTrackState) o).s();
        // Sort on path length
        //
        return( _s < s ? -1 : ( _s == s ? 0 : 1 ));
    }
    
}




