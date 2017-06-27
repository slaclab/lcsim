package org.lcsim.recon.tracking.vsegment.hitmaking.hitmakers;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.units.clhep.SystemOfUnits;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.base.TrackerHitPoint;
import org.lcsim.recon.tracking.vsegment.hit.base.TrackerHitSegment;
import org.lcsim.recon.tracking.vsegment.hitmaking.TrackerHitMaker;
import org.lcsim.recon.tracking.vsegment.transform.CartesianToCylindrical;
import org.lcsim.recon.tracking.vsegment.transform.Rotation3D;
import org.lcsim.recon.tracking.vsegment.transform.Transformation3D;

/**
 * Simplistic <tt>TrackerHitMaker</tt>.
 * Trajectory is ignored by all methods.
 * Hit position is calculated as signal-weighted average of all channels.
 * For strips, the length and V-position of the hit are the length and V-position
 * of the shortest strip.
 *
 * @author D. Onoprienko
 * @version $Id: TrackerHitMakerBasic.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class TrackerHitMakerBasic implements TrackerHitMaker {
  
// -- Constructors :  ----------------------------------------------------------
  
  public TrackerHitMakerBasic() {
    set("ERROR_PIXELS", 0.005);
    set("ERROR_STRIPS", 0.007);
  }
  
// -- Setters :  ---------------------------------------------------------------

  /**
   * Set algorithm parameters. 
   * The following parameters can be set with this method :
   * <p><dl>
   * <dt>"ERROR_PIXELS"</dt> <dd>The <tt>values</tt> argument should be a double value
   *           giving the minimal error to be assigned to hits in pixels, in millimeters.
   *           <br>Default: <tt>0.005</tt> (no smearing).</dd>
   * <dt>"ERROR_STRIPS"</dt> <dd>The <tt>values</tt> argument should be a double value
   *           giving the minimal error to be assigned to hits in stripss, in millimeters.
   *           <br>Default: <tt>0.007</tt> (no smearing).</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param values  Zero or more objects or numbers defining the value of the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   */
  public void set(String name, Object... values) {
    Object value = values.length == 0 ? null : values[0];
    try {
      if (name.equalsIgnoreCase("ERROR_PIXELS")) {
        _err2Pixels = ((Double) values[0]) * SystemOfUnits.mm;
        _err2Pixels *= _err2Pixels;
      } else if (name.equalsIgnoreCase("ERROR_STRIPS")) {
        _err2Strips = ((Double) values[0]) * SystemOfUnits.mm;
        _err2Strips *= _err2Strips;
      } else {
        throw new NoSuchParameterException(name, this.getClass());
      }
    } catch (ClassCastException x) {
      throw new IllegalArgumentException(x);
    } catch (ArrayIndexOutOfBoundsException xx){
      throw new IllegalArgumentException(xx);
    }
  }
  
// -- Making and updating hits :  ----------------------------------------------
  
  /**
   * Makes a new <tt>TrackerHit</tt>.
   */
  public TrackerHit make(TrackerCluster cluster) {

    Sensor sensor = cluster.getSensor();
    SensorType sType = sensor.getType();
    double signal = 0;
    double[] minDim = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    int[] minDimChannel = new int[3];
    double[] posMean = new double[3];
//    double[] pos2Mean = new double[3];
    for (DigiTrackerHit digiHit : cluster.getDigiHits()) {
      double s = digiHit.getSignal();
      signal += s;
      int channel = digiHit.getChannel();
      double[] pos = sType.getChannelPosition(channel).v();
      double[] dim = sType.getChannelDimensions(channel).v();
      for (int i=0; i<3; i++) {
        posMean[i] += pos[i]*s;
//        pos2Mean[i] += pos[i]*pos[i]*s;
        if (dim[i] < minDim[i]) {
          minDim[i] = dim[i];
          minDimChannel[i] = channel;
        }
      }
    }

//    double[] pos = new double[3];
//    double[] error = new double[3];
    for (int i=0; i<3; i++) {
      posMean[i] /= signal;
//      pos2Mean[i] /= signal;
//      double err2 = pos2Mean[i] - posMean[i]*posMean[i];
//      error[i] = (err2 > _err2Min) ? err2 : (minDim[i]*minDim[i])/12.;
//      pos[i] = posMean[i];
    }
    
    Transformation3D trans = sensor.getRotation();
    double cU = 1.;
    if (trans instanceof CartesianToCylindrical) {
      cU = posMean[2];
    } else if (trans instanceof Rotation3D) {
      cU = 1.;
    } else {
      throw new RuntimeException("TrackerHitMakerBasic is only intended to work with flat or cylindrical sensors");
    }

    TrackerHit hit;
    if (sType.getHitDimension() == 1) { // Strips
      posMean[1] = sType.getChannelPosition(minDimChannel[1]).y();
      double cUU = _err2Strips / (cU*cU);
      double length = minDim[1];
      double cVV = (length*length)/12.;
      SymmetricMatrix cov = new SymmetricMatrix(3, new double[]{cUU, 0., cVV,0., 0., 0.}, true);
      hit = new TrackerHitSegment(cluster, new BasicHep3Vector(posMean), length, cov, true);
    } else {  // Pixels
      double cUU = _err2Pixels / (cU*cU);
      SymmetricMatrix cov = new SymmetricMatrix(3, new double[]{cUU, 0., _err2Pixels,0., 0., 0.}, true);
      hit = new TrackerHitPoint(cluster, new BasicHep3Vector(posMean), cov, true);
    }
    return hit;
  }
  
// -- Helper methods:  ---------------------------------------------------------

  private Hep3Vector sqr(Hep3Vector v) {
    double[] a = v.v();
    for (int i=0; i<3; i++) a[i] = a[i]*a[i];
    return new BasicHep3Vector(a);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private double _err2Pixels;
  private double _err2Strips;
}
