package org.lcsim.recon.tracking.spacegeom;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: SphericalPointTensor.java,v 1.1 2011/07/06 17:22:35 ngraf Exp $
 */
public class SphericalPointTensor extends SpacePointTensor
{
// Construct a spherical tensor from space point and direction.

    public SphericalPointTensor(SpacePoint spt,
            double trr, double trt, double trp,
            double ttr, double ttt, double ttp,
            double tpr, double tpt, double tpp)
    {
        super(spt);
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double arx = trr * s_th * c_phi + trt * c_th * c_phi - trp * s_phi;
        double ary = trr * s_th * s_phi + trt * c_th * s_phi + trp * c_phi;
        double arz = trr * c_th - trt * s_th;
        double atx = ttr * s_th * c_phi + ttt * c_th * c_phi - ttp * s_phi;
        double aty = ttr * s_th * s_phi + ttt * c_th * s_phi + ttp * c_phi;
        double atz = ttr * c_th - ttt * s_th;
        double apx = tpr * s_th * c_phi + tpt * c_th * c_phi - tpp * s_phi;
        double apy = tpr * s_th * s_phi + tpt * c_th * s_phi + tpp * c_phi;
        double apz = tpr * c_th - tpt * s_th;
        _txx = arx * s_th * c_phi + atx * c_th * c_phi - apx * s_phi;
        _txy = ary * s_th * c_phi + aty * c_th * c_phi - apy * s_phi;
        _txz = arz * s_th * c_phi + atz * c_th * c_phi - apz * s_phi;
        _tyx = arx * s_th * s_phi + atx * c_th * s_phi + apx * c_phi;
        _tyy = ary * s_th * s_phi + aty * c_th * s_phi + apy * c_phi;
        _tyz = arz * s_th * s_phi + atz * c_th * s_phi + apz * c_phi;
        _tzx = arx * c_th - atx * s_th;
        _tzy = ary * c_th - aty * s_th;
        _tzz = arz * c_th - atz * s_th;
    }

// Construct a spherical tensor from coordinates and direction.
    public SphericalPointTensor(double r, double phi, double theta,
            double trr, double trt, double trp,
            double ttr, double ttt, double ttp,
            double tpr, double tpt, double tpp)
    {
        super(new SphericalPoint(r, phi, theta));
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double arx = trr * s_th * c_phi + trt * c_th * c_phi - trp * s_phi;
        double ary = trr * s_th * s_phi + trt * c_th * s_phi + trp * c_phi;
        double arz = trr * c_th - trt * s_th;
        double atx = ttr * s_th * c_phi + ttt * c_th * c_phi - ttp * s_phi;
        double aty = ttr * s_th * s_phi + ttt * c_th * s_phi + ttp * c_phi;
        double atz = ttr * c_th - ttt * s_th;
        double apx = tpr * s_th * c_phi + tpt * c_th * c_phi - tpp * s_phi;
        double apy = tpr * s_th * s_phi + tpt * c_th * s_phi + tpp * c_phi;
        double apz = tpr * c_th - tpt * s_th;
        _txx = arx * s_th * c_phi + atx * c_th * c_phi - apx * s_phi;
        _txy = ary * s_th * c_phi + aty * c_th * c_phi - apy * s_phi;
        _txz = arz * s_th * c_phi + atz * c_th * c_phi - apz * s_phi;
        _tyx = arx * s_th * s_phi + atx * c_th * s_phi + apx * c_phi;
        _tyy = ary * s_th * s_phi + aty * c_th * s_phi + apy * c_phi;
        _tyz = arz * s_th * s_phi + atz * c_th * s_phi + apz * c_phi;
        _tzx = arx * c_th - atx * s_th;
        _tzy = ary * c_th - aty * s_th;
        _tzz = arz * c_th - atz * s_th;
    }

}
