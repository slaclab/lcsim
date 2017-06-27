package org.lcsim.recon.tracking.spacegeom;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: CylindricalPointTensor.java,v 1.1 2011/07/06 17:22:36 ngraf Exp $
 */
public class CylindricalPointTensor extends SpacePointTensor
{
// Construct a cylindrical tensor from space point and direction.

    public CylindricalPointTensor(SpacePoint spt,
            double trr, double trp, double trz,
            double tpr, double tpp, double tpz,
            double tzr, double tzp, double tzz)
    {
        super(spt);
        double c = cosPhi();
        double s = sinPhi();
        _txx = trr * c * c + tpp * s * s - (trp + tpr) * c * s;
        _txy = (trr - tpp) * c * s - tpr * s * s + trp * c * c;
        _txz = trz * c - tpz * s;
        _tyx = (trr - tpp) * c * s + tpr * c * c - trp * s * s;
        _tyy = trr * s * s + tpp * c * c + (tpr + trp) * c * s;
        _tyz = trz * s + tpz * c;
        _tzx = tzr * c - tzp * s;
        _tzy = tzr * s + tzp * c;
        _tzz = tzz;
    }

// Construct a cylindrical tensor from coordinates and direction.
    public CylindricalPointTensor(double r, double phi, double z,
            double trr, double trp, double trz,
            double tpr, double tpp, double tpz,
            double tzr, double tzp, double tzz)
    {
        super(new CylindricalPoint(r, phi, z));
        double c = cosPhi();
        double s = sinPhi();
        _txx = trr * c * c + tpp * s * s - (trp + tpr) * c * s;
        _txy = (trr - tpp) * c * s - tpr * s * s + trp * c * c;
        _txz = trz * c - tpz * s;
        _tyx = (trr - tpp) * c * s + tpr * c * c - trp * s * s;
        _tyy = trr * s * s + tpp * c * c + (tpr + trp) * c * s;
        _tyz = trz * s + tpz * c;
        _tzx = tzr * c - tzp * s;
        _tzy = tzr * s + tzp * c;
        _tzz = tzz;
    }
}
