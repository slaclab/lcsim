package org.lcsim.recon.tracking.trfxyp;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.ETrack;

//cng changed public for component tests
/**
 * Describes a v-z measurement on a XYPlane.
 * avz = wv*v + wz*z
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the avz of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitXYPlane1 extends  Hit
{
    
    // Only ClusXYPlane1 is allowed to construct HitXYPlane1 objects.
    // package protection
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "HitXYPlane1"; }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    // prediction for avz
    private double _avz_pre;
    
    // error matrix for avz
    private double _eavz_pre;
    
    
    /**
     * Test equality.
     * Hits are equal if they have the same parent cluster.
     *
     * @param   hit The Hit to test against.
     * @return true if the Hits are the same.
     */
    protected boolean equal( Hit hit)
    {
        Assert.assertTrue( type().equals(hit.type()) );
        return cluster().equals(hit.cluster());
    }
    
    // constructor
    HitXYPlane1(double avz, double eavz)
    {
        _avz_pre = avz;
        _eavz_pre = eavz;
    }
    
    
    
    /**
     *Construct an instance replicating the HitXYPlane1 ( copy constructor ).
     *
     * @param   hit The Hit to replicate.
     */
    public HitXYPlane1(  HitXYPlane1 hit)
    {
        super(hit);
        _avz_pre = hit._avz_pre;
        _eavz_pre = hit._eavz_pre;
    }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType(); }
    
    /**
     *Return the dimension of a v-z measurement on an xy plane.
     *avz = wv*v + wz*z
     *The value is always one.
     *
     * @return The dimension of this hit (1).
     */
    public int size()
    { return 1; }
    
    /**
     *Return the measured hit vector.
     *
     * @return The HitVector for this hit.
     */
    public HitVector measuredVector()
    {
        return new HitVector(fullCluster().aVZ());
    }
    
    /**
     *Return the measured hit error.
     *
     * @return The HitError for this hit.
     */
    public HitError measuredError()
    {
        double davz = fullCluster().daVZ();
        return new HitError(davz*davz);
    }
    
    /**
     *Return the predicted hit vector.
     *
     * @return The HitVector for the prediction.
     */
    public HitVector predictedVector()
    {
        return new HitVector(_avz_pre);
    }
    
    /**
     *Return the predicted hit error.
     *
     * @return The HitError for the prediction.
     */
    public HitError predictedError()
    {
        return new HitError(_eavz_pre);
    }
    
    /**
     *Return the hit derivative with respect to a track on this surface.
     *
     * @return The HitDerivative for a track on this surface.
     */
    public HitDerivative dHitdTrack()
    {
        double values[] =
        { 0.0, 0.0, 0.0, 0.0, 0.0 };
        values[0] = fullCluster().wV();
        values[1] = fullCluster().wZ();
        return new HitDerivative(1, values);
    }
    
    /**
     *Return the difference between prediction and measurement.
     *
     * @return The HitVector for the difference between the hit prediction and measurement.
     */
    public HitVector differenceVector()
    {
        return new HitVector(_avz_pre-fullCluster().aVZ());
    }
    
    /**
     *Update the prediction (measurement and derivative do not change).
     *
     * @param   tre The ETrack for which to predict this hit measurement.
     */
    public void update( ETrack tre)
    {
        
        double v_track = tre.vector().get(SurfXYPlane.IV);
        double z_track = tre.vector().get(SurfXYPlane.IZ);
        double evv_track = tre.error().get(SurfXYPlane.IV,SurfXYPlane.IV);
        double evz_track = tre.error().get(SurfXYPlane.IV,SurfXYPlane.IZ);
        double ezz_track = tre.error().get(SurfXYPlane.IZ,SurfXYPlane.IZ);
        
        double cl_wv = fullCluster().wV();
        double cl_wz = fullCluster().wZ();
        
        _avz_pre = cl_wv*v_track + cl_wz*z_track;
        _eavz_pre =   evv_track*cl_wv*cl_wv +
                2.*evz_track*cl_wv*cl_wz +
                ezz_track*cl_wz*cl_wz;
    }
    
    /**
     *Return a ClusXYPlane1 reference to the cluster.
     *
     * @return The hit as a ClusZPlane1 object.
     */
    public  ClusXYPlane1 fullCluster()
    { return (  ClusXYPlane1) _pclus; }
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        if ( _pclus != null )
        {
            return "HitXYPlane1 prediction for " +_pclus;
        }
        else return "No parent cluster.";
    }
    
    
}
