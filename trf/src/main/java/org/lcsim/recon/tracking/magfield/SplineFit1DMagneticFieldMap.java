package org.lcsim.recon.tracking.magfield;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.lcsim.recon.tracking.spacegeom.CartesianPointVector;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointTensor;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class SplineFit1DMagneticFieldMap extends AbstractMagneticField
{

    private SplineInterpolator _interpolator = new SplineInterpolator();
    private PolynomialSplineFunction _spline;

    private COORDINATE _coord;
    private BVAL _bval;

    private double _scaleFactor = 1.0;

    public enum COORDINATE
    {

        X(0, "x"), Y(1, "y"), Z(2, "z");
        private int _numVal;
        private String _stringVal;

        COORDINATE(int numVal, String stringVal)
        {
            this._numVal = numVal;
            this._stringVal = stringVal;
        }

        public int numVal()
        {
            return _numVal;
        }

        public String stringVal()
        {
            return _stringVal;
        }
    }

    public enum BVAL
    {

        BX(0, "Bx"), BY(1, "By"), BZ(2, "Bz");
        private int _numVal;
        private String _stringVal;

        BVAL(int numVal, String stringVal)
        {
            this._numVal = numVal;
            this._stringVal = stringVal;
        }

        public int numVal()
        {
            return _numVal;
        }

        public String stringVal()
        {
            return _stringVal;
        }

    }

    public SplineFit1DMagneticFieldMap(COORDINATE coord, double[] pos, BVAL bval, double[] val)
    {
        this(coord, pos, bval, val, 1.);

    }

    public SplineFit1DMagneticFieldMap(COORDINATE coord, double[] pos, BVAL bval, double[] val, double scale)
    {
        _coord = coord;
        _bval = bval;
        _spline = _interpolator.interpolate(pos, val);
        _scaleFactor = scale;
    }

    public void setScalefactor(double scale)
    {
        _scaleFactor = scale;
    }

    @Override
    public SpacePointVector field(SpacePoint p)
    {
        double[] b = {0., 0., 0.};
        double v = p.getCartesianArray()[_coord.numVal()];
        int index = _bval.numVal();
        try {
            b[index] = _spline.value(v);
        } catch (ArgumentOutsideDomainException ex) {
            ex.printStackTrace();
        }
        return new CartesianPointVector(p, _scaleFactor*b[0], _scaleFactor*b[1], _scaleFactor*b[2]);
    }

    @Override
    public SpacePointVector field(SpacePoint p, SpacePointTensor g)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toString()
    {
        return "1D magnetic field returning " + _bval.stringVal() + " as a function of " + _coord.stringVal()+" with scale factor "+_scaleFactor;
    }

}
