package  org.lcsim.recon.tracking.trfzp;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Describes a one dimensional xy measurement on a ZPlane.
 * axy = wx*x + wy*y
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the axy of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
//cng changed to public for component tests
public class HitZPlane1 extends Hit
{
    
    // Only ClusZPlane1 is allowed to construct HitZPlane1 objects.
    // therefore package protection
    
    
    //
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "HitZPlane1";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    
    // prediction for axy
    private double _axy_pre;
    
    // error matrix for axy
    private double _eaxy_pre;
    
    
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
    
    // constructor (package access for ClusZPlane1)
    HitZPlane1(double axy, double eaxy)
    {
        _axy_pre = axy;
        _eaxy_pre = eaxy;
    }
    
    //
    
    /**
     *Construct an instance replicating the HitZPlane1 ( copy constructor ).
     *
     * @param   hit The Hit to replicate.
     */
    public HitZPlane1( HitZPlane1 hit)
    {
        super(hit);
        _axy_pre = hit._axy_pre;
        _eaxy_pre = hit._eaxy_pre;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();
    }
    
    //
    
    /**
     *Return the dimension of an xy measurement on a z plane.
     *axy = wx*x + wy*y
     *The value is always one.
     *
     * @return The dimension of this hit (1).
     */
    public int size()
    { return 1;
    }
    
    //
    
    /**
     *Return the measured hit vector.
     *
     * @return The HitVector for this hit.
     */
    public HitVector measuredVector()
    {
        return new HitVector( fullCluster().aXY() );
    }
    
    //
    
    /**
     *Return the measured hit error.
     *
     * @return The HitError for this hit.
     */
    public HitError measuredError()
    {
        double daxy = fullCluster().daXY();
        return new HitError( daxy*daxy );
    }
    
    //
    
    /**
     *Return the predicted hit vector.
     *
     * @return The HitVector for the prediction.
     */
    public HitVector predictedVector()
    {
        return new HitVector( _axy_pre );
    }
    
    //
    
    /**
     *Return the predicted hit error.
     *
     * @return The HitError for the prediction.
     */
    public HitError predictedError()
    {
        return new HitError( _eaxy_pre );
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
        { 0.0, 0.0, 0.0, 0.0, 0.0 };
        values[0] = fullCluster().wX();
        values[1] = fullCluster().wY();
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
        return new HitVector( _axy_pre-fullCluster().aXY() );
    }
    
    //
    
    /**
     *Update the prediction (measurement and derivative do not change).
     *
     * @param   tre The ETrack for which to predict this hit measurement.
     */
    public void update(  ETrack tre)
    {
        
        double x_track = tre.vector().get(SurfZPlane.IX);
        double y_track = tre.vector().get(SurfZPlane.IY);
        double exx_track = tre.error().get(SurfZPlane.IX,SurfZPlane.IX);
        double exy_track = tre.error().get(SurfZPlane.IX,SurfZPlane.IY);
        double eyy_track = tre.error().get(SurfZPlane.IY,SurfZPlane.IY);
        
        double cl_wx = fullCluster().wX();
        double cl_wy = fullCluster().wY();
        
        _axy_pre = cl_wx*x_track + cl_wy*y_track;
        _eaxy_pre =   exx_track*cl_wx*cl_wx +
        2.*exy_track*cl_wx*cl_wy +
        eyy_track*cl_wy*cl_wy;
    }
    
    //
    
    /**
     *Return a ClusZPlane1 reference to the cluster.
     *
     * @return The hit as a ClusZPlane1 object.
     */
    public ClusZPlane1 fullCluster()
    {
        return (ClusZPlane1) _pclus;
    }
    
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        if( _pclus != null )
        {
            return "HitZPlane1 from " + _pclus;
        }
        else
        {
            return "HitZPlane1 with no parent clusters";
        }
    }
}
