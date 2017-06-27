package org.lcsim.recon.tracking.trfcyl;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;

/**
 * Describes a measurement of both phi and z on a cylinder.
 *<p>
 * This hit produces a two-dimensional prediction of phi and z of the track.
 *<p>
 *Only ClusCylPhiZ2D objects are allowed to construct HitCylPhiZ2D objects.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
//**********************************************************************
//cng changed to public for component tests
public class HitCylPhiZ2D extends Hit
{
    
    // Only ClusCylPhiZ2D is allowed to construct HitCylPhiZ2D objects.
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "HitCylPhiZ2D"; }
    
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
    
    // prediction for z
    private double _z_pre;
    
    // error matrix for prediction
    private double _ephi_pre;
    private double _ez_pre;
    private double _ephiz_pre;
    
    // store the HitDerivative
    private static double values[] =
    { 1.0, 0.0, 0.0, 0.0, 0.0,
              0.0, 1.0, 0.0, 0.0, 0.0 };
              private static HitDerivative _deriv = new HitDerivative(2,values);
              
              
              //
              
              /**
               * Test equality.
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
              HitCylPhiZ2D(double phi, double ephi, double z, double ez, double ephiz)
              {
                  _phi_pre = phi;
                  _ephi_pre = ephi;
                  _z_pre = z;
                  _ez_pre = ez;
                  _ephiz_pre = ephiz;
              }
              
              //
              
              /**
               *Construct an instance replicating the HitCylPhiZ2D ( copy constructor ).
               *
               * @param   hcpz The Hit to replicate.
               */
              public HitCylPhiZ2D(HitCylPhiZ2D hcpz)
              {
                  super(hcpz);
                  _phi_pre = hcpz._phi_pre;
                  _ephi_pre = hcpz._ephi_pre;
                  _z_pre = hcpz._z_pre;
                  _ez_pre = hcpz._ez_pre;
                  _ephiz_pre = hcpz._ephiz_pre;
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
               *Return the dimension of a phi, z measurement on a cylinder.
               *The value is always two.
               *
               * @return The dimension of this hit (2).
               */
              public int size()
              { return 2; }
              
              //
              
              /**
               *Return the measured hit vector.
               *
               * @return The HitVector for this hit.
               */
              public HitVector measuredVector()
              {
                  return new HitVector( fullCluster().phi(), fullCluster().z() );
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
                  double dz = fullCluster().dZ();
                  double dphidz = fullCluster().dPhidZ();;
                  return new HitError( dphi*dphi, dphidz, dz*dz );
              }
              
              //
              
              /**
               *Return the predicted hit vector.
               *
               * @return The HitVector for the prediction.
               */
              public HitVector predictedVector()
              {
                  return new HitVector( _phi_pre, _z_pre );
              }
              
              //
              
              /**
               *Return the predicted hit error.
               *
               * @return The HitError for the prediction.
               */
              public HitError predictedError()
              {
                  return new HitError( _ephi_pre, _ephiz_pre, _ez_pre );
              }
              
              //
              
              /**
               *Return the hit derivative with respect to a track on this surface.
               *
               * @return The HitDerivative for a track on this surface.
               */
              public HitDerivative dHitdTrack()
              {
                  return _deriv;
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
                          TRFMath.fmod2(_phi_pre-fullCluster().phi(), TRFMath.TWOPI),
                          _z_pre-fullCluster().z() );
              }
              
              //
              
              /**
               *Update the prediction (measurement and derivative do not change).
               *
               * @param   tre The ETrack for which to predict this hit measurement.
               */
              public void update( ETrack tre)
              {
                  TrackVector vec =   tre.vector();
                  TrackError err =  tre.error() ;
                  
                  _phi_pre = vec.get(SurfCylinder.IPHI);
                  _ephi_pre = err.get(SurfCylinder.IPHI,SurfCylinder.IPHI);
                  
                  _z_pre = vec.get(SurfCylinder.IZ);
                  _ez_pre = err.get(SurfCylinder.IZ,SurfCylinder.IZ);
                  
                  _ephiz_pre = err.get(SurfCylinder.IPHI,SurfCylinder.IZ);
                  
              }
              
              //
              
              /**
               *Return a ClusCylPhiZ2D reference to the hit.
               *
               * @return The hit as a ClusCylPhiZ2D object.
               */
              public ClusCylPhiZ2D fullCluster()
              { return (ClusCylPhiZ2D) _pclus; };
              
              
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