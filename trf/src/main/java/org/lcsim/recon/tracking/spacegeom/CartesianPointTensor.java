package org.lcsim.recon.tracking.spacegeom;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: CartesianPointTensor.java,v 1.1 2011/07/06 17:22:35 ngraf Exp $
 */
public class CartesianPointTensor extends SpacePointTensor
{
    // Constructor from space point and direction.

    CartesianPointTensor(SpacePoint spt,
            double txx, double txy, double txz,
            double tyx, double tyy, double tyz,
            double tzx, double tzy, double tzz)
    {
        super(spt);
        _txx = txx;
        _txy = txy;
        _txz = txz;
        _tyx = tyx;
        _tyy = tyy;
        _tyz = tyz;
        _tzx = tzx;
        _tzy = tzy;
        _tzz = tzz;
    }

// Construct a Cartesian tensor from coordinates and direction.
    public CartesianPointTensor(double x, double y, double z,
            double txx, double txy, double txz,
            double tyx, double tyy, double tyz,
            double tzx, double tzy, double tzz)
    {
        super(new CartesianPoint(x, y, z));

        _txx = txx;
        _txy = txy;
        _txz = txz;
        _tyx = tyx;
        _tyy = tyy;
        _tyz = tyz;
        _tzx = tzx;
        _tzy = tzy;
        _tzz = tzz;
    }
}
