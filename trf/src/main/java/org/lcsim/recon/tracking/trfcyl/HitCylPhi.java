package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;

/**
 * Describes a phi measurement on a cylinder.
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the phi of the track.
 *<p>
 *Only ClusCylPhi objects are allowed to construct HitCylPhi objects.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
//**********************************************************************
//cng changed to public for component testing... needs to be checked.
public class HitCylPhi extends Hit
{
    
    // Only ClusCylPhi is allowed to construct HitCylPhi objects.
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    {
        return "HitCylPhi";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    {
        return typeName();
    }
    
    // attributes
    
    // prediction for phi
    private double _phi_pre;
    
    // error matrix for phi
    private double _ephi_pre;
    
    // methods
    
    //
    
    
    /**
     * Test equality.
     * Hits are equal if they have the same parent cluster.
     *
     * @param   hit The Hit to test against.
     * @return true if the Hits are the same.
     */
    public boolean equal( Hit hit)
    {
        Assert.assertTrue( type().equals(hit.type()) );
        return cluster().equals(hit.cluster());
    }
    
    // constructor (package access for ClusCylPhi)
    HitCylPhi(double phi, double ephi)
    {
        _phi_pre = phi;
        _ephi_pre = ephi;
    }
    
    // methods
    
    //
    
    /**
     *Construct an instance replicating the HitCylPhi ( copy constructor ).
     *
     * @param   hcp The Hit to replicate.
     */
    public  HitCylPhi( HitCylPhi hcp)
    {
        super(hcp);
        _phi_pre = hcp._phi_pre;
        _ephi_pre = hcp._ephi_pre;
    }
    
    //
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    {
        return staticType();
    }
    
    //
    
    /**
     *Return the dimension of a phi measurement on a cylinder.
     *The value is always one.
     *
     * @return The dimension of this hit (1).
     */
    public int size()
    {
        return 1;
    }
    
    //
    
    /**
     *Return the measured hit vector.
     *
     * @return The HitVector for this hit.
     */
    public HitVector measuredVector()
    {
        return new HitVector(fullCluster().phi());
    }
    
    //
    
    /**
     *Return the measured hit error.
     *
     * @return The HitError for this hit.
     */
    public HitError measuredError()
    {
        double dphi = fullCluster().dPhi();
        return new HitError(dphi*dphi);
    }
    
    //
    
    /**
     *Return the predicted hit vector.
     *
     * @return The HitVector for the prediction.
     */
    public HitVector predictedVector()
    {
        return new HitVector(_phi_pre);
    }
    
    //
    
    /**
     *Return the predicted hit error.
     *
     * @return The HitError for the prediction.
     */
    public HitError predictedError()
    {
        return new HitError(_ephi_pre);
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
        return new HitDerivative(1, values);
    }
    
    //
    
    /**
     *Return the difference between prediction and measurement.
     *
     * @return The HitVector for the difference between the hit prediction and measurement.
     */
    public HitVector differenceVector()
    {
        return new HitVector(TRFMath.fmod2(_phi_pre - fullCluster().phi(),TRFMath.TWOPI));
    }
    
    //
    
    /**
     *Update the prediction (measurement and derivative do not change).
     *
     * @param   tre The ETrack for which to predict this hit measurement.
     */
    public void update( ETrack tre)
    {
        _phi_pre = tre.vector().get(0);
        _ephi_pre = tre.error().get(0,0);
    }
    
    //
    
    /**
     *Return a ClusCylPhi reference to the hit.
     *
     * @return The hit as a ClusCylPhi object.
     */
    public  ClusCylPhi fullCluster()
    {
        return (ClusCylPhi) _pclus;
    }
    
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        if ( _pclus != null )
        {
            return "HitCylPhi prediction for " +_pclus;
        }
        else return "No parent cluster.";
    }
}
