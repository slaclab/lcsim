package org.lcsim.recon.tracking.trfxyp;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
/**
 * Describes a two dimensional (v,z) measurement on an XYPlane.
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the (v,z) of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
//cng changed to public for component tests
public class HitXYPlane2 extends Hit
{
    
    // Only ClusXYPlane2 is allowed to construct HitXYPlane2 objects.
    // package protection
    
    // store the HitDerivative
    private static double values[] =
    { 1.0, 0.0, 0.0, 0.0, 0.0,
              0.0, 1.0, 0.0, 0.0, 0.0 };
              private static HitDerivative _deriv = new HitDerivative(2,values);
              
              /**
               *Return a String representation of the class' type name.
               *Included for completeness with the C++ version.
               *
               * @return   A String representation of the class' type name.
               */
              public static String typeName()
              { return "HitXYPlane2"; }
              
              /**
               *Return a String representation of the class' type name.
               *Included for completeness with the C++ version.
               *
               * @return   A String representation of the class' type name.
               */
              public static String staticType()
              { return typeName(); }
              
              // prediction for hm
              private double _v;
              private double _z;
              
              // error matrix for hm
              private double _dv2;
              private double _dz2;
              private double _dvdz;
              
              
              // equality
              // Hits are equal if they have the same parent cluster.
              protected boolean equal( Hit hit)
              {
                  Assert.assertTrue( type().equals(hit.type()) );
                  return cluster().equals(hit.cluster());
              }
              
              // constructor
              HitXYPlane2(double v, double z, double dv2, double dz2, double dvdz)
              {
                  _v = v;
                  _z = z;
                  _dv2 = dv2;
                  _dz2 = dz2;
                  _dvdz = dvdz;
              }
              
              /**
               *Construct an instance replicating the HitXYPlane2 ( copy constructor ).
               *
               * @param   hit The Hit to replicate.
               */
              public HitXYPlane2( HitXYPlane2 hit)
              {
                  super(hit);
                  _v = hit._v;
                  _z = hit._z;
                  _dv2 = hit._dv2;
                  _dz2 = hit._dz2;
                  _dvdz = hit._dvdz;
              }
              
              /**
               *Return the dimension of a (v,z) measurement on an xy plane.
               *The value is always two.
               *
               * @return The dimension of this hit (2).
               */
              public int size()
              { return 2; }
              
              /**
               *Return the measured hit vector.
               *
               * @return The HitVector for this hit.
               */
              public HitVector measuredVector()
              {
                  ClusXYPlane2 clu =  fullCluster();
                  return new HitVector( clu.v(), clu.z() );
              }
              
              /**
               *Return the measured hit error.
               *
               * @return The HitError for this hit.
               */
              public HitError measuredError()
              {
                  ClusXYPlane2 clu =  fullCluster();
                  return new HitError(clu.dV2(),clu.dVdZ(), clu.dZ2()   );
              }
              
              /**
               *Return the predicted hit vector.
               *
               * @return The HitVector for the prediction.
               */
              public HitVector predictedVector()
              {
                  return new HitVector( _v, _z );
              }
              
              /**
               *Return the predicted hit error.
               *
               * @return The HitError for the prediction.
               */
              public HitError predictedError()
              {
                  return new HitError( _dv2,_dvdz,_dz2 );
              }
              
              /**
               *Return the hit derivative with respect to a track on this surface.
               *
               * @return The HitDerivative for a track on this surface.
               */
              public HitDerivative dHitdTrack()
              {
                  return _deriv;
              }
              
              /**
               *Return the difference between prediction and measurement.
               *
               * @return The HitVector for the difference between the hit prediction and measurement.
               */
              public HitVector differenceVector()
              {
                  
                  ClusXYPlane2 clu =  fullCluster();
                  
                  
                  double diff_v = _v - clu.v();
                  double diff_z = _z - clu.z();
                  return new HitVector( diff_v, diff_z );
              }
              
              /**
               *Update the prediction (measurement and derivative do not change).
               *
               * @param   tre The ETrack for which to predict this hit measurement.
               */
              public void update(  ETrack tre)
              {
                  TrackVector vec =   tre.vector();
                  
                  _v = vec.get(SurfXYPlane.IV);
                  _z = vec.get(SurfXYPlane.IZ);
                  
                  TrackError err =  tre.error() ;
                  
                  _dv2 = err.get(SurfXYPlane.IV,SurfXYPlane.IV);
                  _dvdz = err.get(SurfXYPlane.IV,SurfXYPlane.IZ);
                  _dz2 = err.get(SurfXYPlane.IZ,SurfXYPlane.IZ);
                  
              }
              
              /**
               *Return a ClusXYPlane2 reference to the cluster.
               *
               * @return The hit as a ClusXYPlane2 object.
               */
              public  ClusXYPlane2 fullCluster()
              { return ( ClusXYPlane2) _pclus; }
              
              /**
               * Return the measured v position of the hit.
               *
               * @return The measured v position of the hit.
               */
              public double v()
              { return _v; }
              /**
               * Return the measured z position of the hit.
               *
               * @return The measured z position of the hit.
               */
              public double z()
              { return _z; }
              /**
               *  Return the error matrix term for v.
               *
               * @return The error matrix term for v.
               */
              public double dV2()
              { return _dv2; }
              /**
               *  Return the error matrix term for z.
               *
               * @return The error matrix term for z.
               */
              public double dZ2()
              { return _dz2; }
              /**
               * Return the error matrix covariance term.
               *
               * @return The error matrix covariance term.
               */
              public double dVdZ()
              { return _dvdz; }
              
              /**
               *Return a String representation of the class' type name.
               *Included for completeness with the C++ version.
               *
               * @return   A String representation of the class' type name.
               */
              public String type()
              { return staticType();  };
              
              /**
               *output stream
               *
               * @return  A String representation of this instance.
               */
              public String toString()
              {
                  if ( _pclus != null )
                  {
                      return "HitXYPlane2 prediction for " +_pclus;
                  }
                  else return "HitXYPlane2 with no parent cluster.";
              }
              
}
