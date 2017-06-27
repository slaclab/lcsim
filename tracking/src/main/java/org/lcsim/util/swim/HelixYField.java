package org.lcsim.util.swim;

import hep.physics.matrix.BasicMatrix;
import hep.physics.vec.Hep3Vector;

import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;

/**
 * Helix for swimming in Y B-field.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class HelixYField extends Helix {

    public HelixYField(Hep3Vector org, double r, double p, double lambda) {
        super(org, r, p, lambda);
    }

    public SpacePoint getPointAtDistance(double alpha) {
        double darg = alpha * cosLambda / radius - phi;
        double x = xCenter + radius * Math.sin(darg);
        double y = yCenter + radius * Math.cos(darg);
        double z = origin.z() + alpha * sinLambda;
        CartesianPoint trkpoint = new CartesianPoint(x, y, z);
        CartesianPoint labpoint = getLabPoint(trkpoint);
      //  System.out.println(trkpoint.toString() + labpoint.toString());
        return labpoint;
    }

    private CartesianPoint getLabPoint(CartesianPoint pos) {
        BasicMatrix detToTrk;
        detToTrk = new BasicMatrix(3, 3);
        detToTrk.setElement(0, 2, 1);
        detToTrk.setElement(1, 0, 1);
        detToTrk.setElement(2, 1, 1);
        BasicMatrix _trkToDet;
        _trkToDet = new BasicMatrix(3, 3);
        _trkToDet.setElement(0, 1, 1);
        _trkToDet.setElement(1, 2, 1);
        _trkToDet.setElement(2, 0, 1);
        double[] trk = { 0, 0, 0 };
        trk[0] = _trkToDet.e(0, 0) * pos.x() + _trkToDet.e(0, 1) * pos.y() + _trkToDet.e(0, 2) * pos.z();
        trk[1] = _trkToDet.e(1, 0) * pos.x() + _trkToDet.e(1, 1) * pos.y() + _trkToDet.e(1, 2) * pos.z();
        trk[2] = _trkToDet.e(2, 0) * pos.x() + _trkToDet.e(2, 1) * pos.y() + _trkToDet.e(2, 2) * pos.z();
        CartesianPoint retpt = new CartesianPoint(trk[0], trk[1], trk[2]);
        return retpt;
    }
}
