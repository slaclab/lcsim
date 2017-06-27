package org.lcsim.recon.tracking.trfbase;
import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;

/**Abstract interface for hits.
 *

 * A hit provides a measurement vector and error matrix, a predicted
 * vector and error matrix and the derivative of the prediction with
 * respect to the input track.  If the hit is N-dimensional, then the
 * error matrix is NxN symmetric and the derivative is Nx5.
 *<p>
 * The hit also maintains a reference-counting pointer to its parent hit.
 * This must be supplied when generating the list of predictions.
 *<p>
 * The hit prediction has one non-const method update which takes a
 * track as input.  This track is used to recalculate the data.  The
 * data should remain the same if the track is the same as that used
 * to generate the hit prediction.  If the track vector or its error
 * change, then any of the data may change.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class Hit
{
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return  String representation of the type
     * Included for completeness with C++ version
     */
    public static String typeName()
    { return "Hit"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return  String representation of the type
     * Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // pointer to the parent cluster
    protected Cluster _pclus;
    
    
    // equality
    // Return true if and only if the two predictions would return the same
    // parameters for any input track.
    // The current track (and thus the current sets of parameters) do
    // not have to be the same.
    // It has already been verified that this and hp are of the same type
    // and have equal clusters.
    protected abstract boolean equal( Hit hp);
    
    //
    
    /**
     *constructor
     *
     */
    public Hit()
    {
    }
    
    //
    
    /**
     * copy constructor
     *
     * @param   hp  Hit to replicate
     */
    public Hit(Hit hp)
    {
    }
    
    //
    
    /**
     *Return the generic type.
     *
     * @return  String representation of the type
     * Included for completeness with C++ version
     *
     */
    public String genericType()
    { return staticType(); }
    
    /**
     *Return the type.
     *
     * @return  String representation of the type
     * Included for completeness with C++ version
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     * Register the parent cluster.
     * This should only be called once.
     *
     * @param   clus The Cluster which creates this Hit
     */
    public void setParentPointer( Cluster clus)
    {
        Assert.assertTrue(_pclus == null || _pclus == clus);
        _pclus = clus;
    }
    
    //
    
    /**
     *return the parent cluster
     *
     * @return the Cluster which created this Hit
     */
    public Cluster cluster()
    {return _pclus; }
    
    //
    
    /**
     *Return the parent cluster surface.
     *
     * @return the Surface at which this Hit's originating Cluster is defined
     */
    public Surface surface()
    {
        return cluster().surface();
    }
    
    //
    
    /**
     *return the dimension of the hit vector
     *
     * @return the dimensionality of this Hit
     */
    public abstract int size();
    
    //
    
    /**
     *return the measured hit vector
     *
     * @return the measured HitVector
     */
    public abstract HitVector measuredVector();
    
    //
    
    /**
     *return the measured hit error
     *
     * @return the measurement HitError
     */
    public abstract HitError measuredError();
    
    //
    
    /**
     *return the predicted hit vector
     *
     * @return the predicted Hit
     */
    public abstract HitVector predictedVector();
    
    //
    
    /**
     *return the predicted hit error
     *
     * @return the predicted Hit error
     */
    public abstract HitError predictedError();
    
    //
    
    /**
     *return the Nx5 derivative dhit_dtrack.
     *
     * @return the erivative for this Hit
     */
    public abstract HitDerivative dHitdTrack();
    
    //
    
    /**
     *return the difference between the prediction and the measurement
     * this is not trivial because of circular variables (e.g. angles)
     *
     * @return the difference between the prediction and measurement
     */
    public abstract HitVector differenceVector();
    
    //
    
    /**
     *Update the measurement and prediction using new track parameters.
     * If update is called with the same track, then the measurement,
     * prediction and derivative should not change.
     *
     * @param   tre the ETrack to use for updating 
     */
    public abstract void update( ETrack tre);
    
    //
    
    /**
     *Return the ID's of MC tracks contributing to the parent cluster.
     *
     * @return a List of MC Id's of tracks contributing to the parent cluster
     */
    public List mcIds()
    { return _pclus.mcIds(); }
    
    //
    
    /**
     *equality
     * false if the hits are of different type
     * otherwise compare with virtual function equal
     *
     * @param   hit Hit to compare
     * @return false if the hits are of different type
     * otherwise compare with virtual function equal
     */
    public boolean equals( Hit hit)
    {
        if( !type().equals(hit.type()) ) return false;
        if( cluster() != hit.cluster() ) return false;
        return equal(hit);
    }
    
    //
    
    /**
     *inequality
     *
     * @param   hit Hit to compare
     * @return true if Hits are not equal
     */
    public boolean notEquals( Hit hit)
    { return ! equals(hit); }
    
}
