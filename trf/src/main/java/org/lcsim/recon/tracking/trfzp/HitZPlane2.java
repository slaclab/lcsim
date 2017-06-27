package  org.lcsim.recon.tracking.trfzp;


import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;

/**
 * Describes a two dimensional (x,y) measurement on a ZPlane.
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the (x,y) of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

//cng changed to public for component tests
public class HitZPlane2 extends Hit
{
    
    // Only ClusZPlane2 is allowed to construct HitZPlane2 objects.
    // therefore package level protection
    
    // store the derivative
    static double values[] =
    { 1.0, 0.0, 0.0, 0.0, 0.0,
      0.0, 1.0, 0.0, 0.0, 0.0 };
      static HitDerivative _deriv = new HitDerivative(2,values);
      
      //
      
      /**
       *Return a String representation of the class' type name.
       *Included for completeness with the C++ version.
       *
       * @return   A String representation of the class' type name.
       */
      public static String typeName()
      { return "HitZPlane2"; }
      
      //
      
      /**
       *Return a String representation of the class' type name.
       *Included for completeness with the C++ version.
       *
       * @return   A String representation of the class' type name.
       */
      public static String staticType()
      { return typeName(); }
      
      
      // prediction for hm
      private double _x;
      private double _y;
      
      // error matrix for hm
      private double _dx2;
      private double _dy2;
      private double _dxdy;
      
      // equality
      // Hits are equal if they have the same parent cluster.
      
      protected boolean equal( Hit hit)
      {
          Assert.assertTrue( type().equals(hit.type()) );
          return cluster().equals(hit.cluster());
      }
      
      // constructor (package access for ClusZPlane2)
      HitZPlane2(double x, double y, double dx2, double dy2, double dxdy)
      {
          _x = x;
          _y = y;
          _dx2 = dx2;
          _dy2 = dy2;
          _dxdy = dxdy;
      }
      
      //
      
      /**
       *Construct an instance replicating the HitZPlane2 ( copy constructor ).
       *
       * @param   hit The Hit to replicate.
       */
      public HitZPlane2( HitZPlane2 hit)
      {
          super(hit);
          _x = hit._x;
          _y = hit._y;
          _dx2 = hit._dx2;
          _dy2 = hit._dy2;
          _dxdy = hit._dxdy;
      }
      
      //
      
      /**
       *Return a String representation of the class' type name.
       *Included for completeness with the C++ version.
       *
       * @return   A String representation of the class' type name.
       */
      public String type()
      { return staticType();  }
      
      //
      
      /**
       *Return the dimension of an (x,y) measurement on a z plane.
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
          ClusZPlane2 clu =  fullCluster();
          return new HitVector( clu.x(), clu.y() );
      }
      
      //
      
      /**
       *Return the measured hit error.
       *
       * @return The HitError for this hit.
       */
      public HitError measuredError()
      {
          ClusZPlane2 clu =  fullCluster();
          return new HitError(clu.dX2(),clu.dXdY(), clu.dY2()   );
      }
      
      //
      
      /**
       *Return the predicted hit vector.
       *
       * @return The HitVector for the prediction.
       */
      public HitVector predictedVector()
      {
          return new HitVector( _x , _y );
      }
      
      //
      
      /**
       *Return the predicted hit error.
       *
       * @return The HitError for the prediction.
       */
      public HitError predictedError()
      {
          return new HitError( _dx2,_dxdy, _dy2 );
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
          
          ClusZPlane2 clu =  fullCluster();
          
          double diff_x = _x - clu.x();
          double diff_y = _y - clu.y();
          return new HitVector( diff_x, diff_y );
      }
      
      //
      
      /**
       *Update the prediction (measurement and derivative do not change).
       *
       * @param   tre The ETrack for which to predict this hit measurement.
       */
      public void update(  ETrack tre)
      {
          
          TrackVector vec =   tre.vector();
          
          _x = vec.get(SurfZPlane.IX);
          _y = vec.get(SurfZPlane.IY);
          
          TrackError err =  tre.error() ;
          
          _dx2 = err.get(SurfZPlane.IX,SurfZPlane.IX);
          _dxdy = err.get(SurfZPlane.IX,SurfZPlane.IY);
          _dy2 = err.get(SurfZPlane.IY,SurfZPlane.IY);
      }
      
      
      /**
       * Return the measured x position of the hit.
       *
       * @return The measured x position of the hit.
       */
      public double x()
      { return _x; }
      
      /**
       * Return the measured y position of the hit.
       *
       * @return The measured y position of the hit.
       */
      public double y()
      { return _y; }
      
      /**
       * Return the error matrix term for x.
       *
       * @return The error matrix term for x.
       */
      public double dX2()
      { return _dx2; }
      
      /**
       *  Return the error matrix term for y.
       *
       * @return The error matrix term for y.
       */
      public double dY2()
      { return _dy2; }
      
      /**
       * Return the error matrix covariance term.
       *
       * @return The error matrix covariance term.
       */
      public double dXdY()
      { return _dxdy; }
      
      //
      
      /**
       *Return a ClusZPlane2 reference to the cluster.
       *
       * @return The hit as a ClusZPlane2 object.
       */
      public  ClusZPlane2 fullCluster()
      { return ( ClusZPlane2) _pclus; }
      
      
      /**
       *output stream
       *
       * @return  A String representation of this instance.
       */
      public String toString()
      {
          if( _pclus != null )
          {
              return "HitZPlane2 from " + _pclus;
          }
          else
          {
              return "HitZPlane2 with no parent clusters";
          }
      }
      
}
