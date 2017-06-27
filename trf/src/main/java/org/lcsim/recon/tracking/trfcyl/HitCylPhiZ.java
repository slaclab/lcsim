package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;

/**
 * Describes a phiz measurement on a cylinder. This would correspond, for instance,
 *to a stereo measurement on a cylindrical surface, where the phi measurement is a function
 *of the z position, viz.
 *phiz = phi + stereo*z.
 *<p>
 * This is a simple hit.  It produces one prediction with fixed
 * measurement which is simply the phi of the track.
 *<p>
 *Only ClusCylPhiZ objects are allowed to construct HitCylPhiZ objects.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
//**********************************************************************
//cng changed to public for component testing...
public class HitCylPhiZ extends Hit
{
    
    // Only ClusCylPhiZ is allowed to construct HitCylPhiZ objects.
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "HitCylPhiZ"; }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // prediction for phi
    private double _phi_pre;
    
    // error matrix for phi
    private double _ephi_pre;
    
    //
    
    /**
     *equality
     * Hits are equal if they have the same parent cluster.
     *
     * @param   hit The Hit to test against.
     * @return true if the Hits are the same.
     */
    public boolean equal(Hit hit)
    {
        Assert.assertTrue( type().equals(hit.type()) );
        return cluster().equals(hit.cluster());
    }
    
    // constructor
    HitCylPhiZ(double phi, double ephi)
    {
        _phi_pre = phi;
        _ephi_pre = ephi;
    }
    
    //
    
    /**
     *Construct an instance replicating the HitCylPhiZ ( copy constructor ).
     *
     * @param   hcpz The Hit to replicate.
     */
    public HitCylPhiZ(HitCylPhiZ hcpz)
    {
        super(hcpz);
        _phi_pre = hcpz._phi_pre;
        _ephi_pre = hcpz._ephi_pre;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *Return the dimension of a phi measurement on a cylinder.
     *The value is always one.
     *
     * @return The dimension of this hit (1).
     */
    public int size()
    { return 1; }
    
    //
    
    /**
     *Return the measured hit vector.
     *
     * @return The HitVector for this hit.
     */
    public HitVector measuredVector()
    {
        return new HitVector( fullCluster().phiZ() );
    }
    
    //
    
    /**
     *Return the measured hit error.
     *
     * @return The HitError for this hit.
     */
    public HitError measuredError()
    {
        double dphi = fullCluster().dPhiZ();
        return new HitError( dphi*dphi );
    }
    
    //
    
    /**
     *Return the predicted hit vector.
     *
     * @return The HitVector for the prediction.
     */
    public HitVector predictedVector()
    {
        return new HitVector( _phi_pre );
    }
    
    //
    
    /**
     *Return the predicted hit error.
     *
     * @return The HitError for the prediction.
     */
    public HitError predictedError()
    {
        return new HitError( _ephi_pre );
    }
    
    //
    
    /**
     *Return the hit derivative with respect to a track on this surface.
     *
     * @return The HitDerivative for a track on this surface.
     */
    public HitDerivative dHitdTrack()
    {
        double values[] =
        { 1.0, 0.0, 0.0, 0.0, 0.0 };
        values[1] = fullCluster().stereo();
        HitDerivative deriv = new HitDerivative(1,values);
        return deriv;
        
    }
    
    //
    
    /**
     *Return the difference between prediction and measurement.
     *
     * @return The HitVector for the difference between the hit prediction and measurement.
     */
    public HitVector differenceVector()
    {
        return new HitVector(
                TRFMath.fmod2(_phi_pre-fullCluster().phiZ(), TRFMath.TWOPI) );
    }
    
    //
    
    /**
     *Update the prediction (measurement and derivative do not change).
     *
     * @param   tre The ETrack for which to predict this hit measurement.
     */
    public void update( ETrack tre)
    {
        double stereo = fullCluster().stereo();
        _phi_pre = tre.vector().get(0) + stereo*tre.vector().get(1);
        _ephi_pre = tre.error().get(0,0) + 2.0*stereo*tre.error().get(0,1) +
                stereo*stereo*tre.error().get(1,1);
        
    }
    
    //
    
    /**
     *Return a ClusCylPhiZ reference to the hit.
     *
     * @return The hit as a ClusCylPhiZ object.
     */
    public ClusCylPhiZ fullCluster()
    { return (ClusCylPhiZ) _pclus; };
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "hit from "+_pclus;
    }
}