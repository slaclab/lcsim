package org.lcsim.recon.tracking.spacegeom;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: SpacePointTensor.java,v 1.2 2011/07/06 20:21:01 ngraf Exp $
 */
public class SpacePointTensor extends SpacePoint
{
    // The tensor.

    double _txx, _txy, _txz;
    double _tyx, _tyy, _tyz;
    double _tzx, _tzy, _tzz;
// The rotation from Cartesian to cylindrical:
// |  cos(phi)  sin(phi)   0  |
// | -sin(phi)  cos(phi)   0  |
// |     0         0       1  |
//
// The rotation from cylindrical to Cartesian:
// |  cos(phi) -sin(phi)   0  |
// |  sin(phi)  cos(phi)   0  |
// |     0         0       1  |
//
// The rotation from Cartesian to spherical:
// |  sin(tht)*cos(phi)  sin(tht)*sin(phi)    cos(tht)  |
// |  cos(tht)*cos(phi)  cos(tht)*sin(phi)   -sin(tht)  |
// |          -sin(phi)           cos(phi)       0      |
//
// The rotation from spherical to Cartesian:
// |  sin(tht)*cos(phi)  cos(tht)*cos(phi)   -sin(phi)  |
// |  sin(tht)*sin(phi)  cos(tht)*sin(phi)    cos(phi)  |
// |  cos(tht)          -sin(tht)                0      |
//
//
// For tensor A and rotation matrix O (above):
// A' = OAOt

// Default constructor.
// Initial point is the origin with phi = theta = 0.
// Tensor is zero.
    public SpacePointTensor()
    {
    }

// Constructor from a space point.
// Tensor is zero.
    public SpacePointTensor(SpacePoint spt)
    {
        super(spt);
    }

    // Copy Constructor
    public SpacePointTensor(SpacePointTensor spt)
    {
        super((SpacePoint) spt);
        _txx = spt._txx;
        _txy = spt._txy;
        _txz = spt._txz;
        _tyx = spt._tyx;
        _tyy = spt._tyy;
        _tyz = spt._tyz;
        _tzx = spt._tzx;
        _tzy = spt._tzy;
        _tzz = spt._tzz;
    }

    // Cartesian components.
    public double t_x_x()
    {
        return _txx;
    }

    public double t_x_y()
    {
        return _txy;
    }

    public double t_x_z()
    {
        return _txz;
    }

    public double t_y_x()
    {
        return _tyx;
    }

    public double t_y_y()
    {
        return _tyy;
    }

    public double t_y_z()
    {
        return _tyz;
    }

    public double t_z_x()
    {
        return _tzx;
    }

    public double t_z_y()
    {
        return _tzy;
    }

    public double t_z_z()
    {
        return _tzz;
    }

// Return the rxy, rxy cylindrical component.
    public double t_rxy_rxy()
    {
        double c = cosPhi();
        double s = sinPhi();
        return _txx * c * c + _tyy * s * s + (_tyx + _txy) * c * s;
    }
//**********************************************************************

// Return the rxy, phi cylindrical component.
    public double t_rxy_phi()
    {
        double c = cosPhi();
        double s = sinPhi();
        return (_tyy - _txx) * c * s - _tyx * s * s + _txy * c * c;
    }

//**********************************************************************
// Return the rxy, z cylindrical component.
    public double t_rxy_z()
    {
        double c = cosPhi();
        double s = sinPhi();
        return _txz * c + _tyz * s;
    }

//**********************************************************************
// Return the phi, rxy cylindrical or spherical component.
    public double t_phi_rxy()
    {
        double c = cosPhi();
        double s = sinPhi();
        return (_tyy - _txx) * c * s + _tyx * c * c - _txy * s * s;
    }

//**********************************************************************
// Return the phi, phi cylindrical component.
    public double t_phi_phi()
    {
        double c = cosPhi();
        double s = sinPhi();
        return _txx * s * s + _tyy * c * c - (_tyx + _txy) * c * s;
    }

//**********************************************************************
// Return the phi, z cylindrical component.
    public double t_phi_z()
    {
        double c = cosPhi();
        double s = sinPhi();
        return -_txz * s + _tyz * c;
    }

//**********************************************************************
// Return the z, rxy cylindrical component.
    public double t_z_rxy()
    {
        double c = cosPhi();
        double s = sinPhi();
        return _tzx * c + _tzy * s;
    }

//**********************************************************************
// Return the z, phi cylindrical component.
    public double t_z_phi()
    {
        double c = cosPhi();
        double s = sinPhi();
        return -_tzx * s + _tzy * c;
    }

//**********************************************************************
// Return the rxyz, rxyz spherical component.
    public double t_rxyz_rxyz()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double arx = _txx * s_th * c_phi + _tyx * s_th * s_phi + _tzx * c_th;
        double ary = _txy * s_th * c_phi + _tyy * s_th * s_phi + _tzy * c_th;
        double arz = _txz * s_th * c_phi + _tyz * s_th * s_phi + _tzz * c_th;
        return arx * s_th * c_phi + ary * s_th * s_phi + arz * c_th;
    }

//**********************************************************************
// Return the rxyz, theta spherical component.
    public double t_rxyz_theta()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double arx = _txx * s_th * c_phi + _tyx * s_th * s_phi + _tzx * c_th;
        double ary = _txy * s_th * c_phi + _tyy * s_th * s_phi + _tzy * c_th;
        double arz = _txz * s_th * c_phi + _tyz * s_th * s_phi + _tzz * c_th;
        return arx * c_th * c_phi + ary * c_th * s_phi - arz * s_th;
    }

//**********************************************************************
// Return the rxyz, phi spherical component.
    public double t_rxyz_phi()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double arx = _txx * s_th * c_phi + _tyx * s_th * s_phi + _tzx * c_th;
        double ary = _txy * s_th * c_phi + _tyy * s_th * s_phi + _tzy * c_th;
        return -arx * s_phi + ary * c_phi;
    }

//**********************************************************************
// Return the theta, rxyz spherical component.
    public double t_theta_rxyz()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double atx = _txx * c_th * c_phi + _tyx * c_th * s_phi - _tzx * s_th;
        double aty = _txy * c_th * c_phi + _tyy * c_th * s_phi - _tzy * s_th;
        double atz = _txz * c_th * c_phi + _tyz * c_th * s_phi - _tzz * s_th;
        return atx * s_th * c_phi + aty * s_th * s_phi + atz * c_th;
    }

//**********************************************************************
// Return the theta, theta spherical component.
    public double t_theta_theta()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double atx = _txx * c_th * c_phi + _tyx * c_th * s_phi - _tzx * s_th;
        double aty = _txy * c_th * c_phi + _tyy * c_th * s_phi - _tzy * s_th;
        double atz = _txz * c_th * c_phi + _tyz * c_th * s_phi - _tzz * s_th;
        return atx * c_th * c_phi + aty * c_th * s_phi - atz * s_th;
    }

//**********************************************************************
// Return the theta, phi spherical component.
    public double t_theta_phi()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double atx = _txx * c_th * c_phi + _tyx * c_th * s_phi - _tzx * s_th;
        double aty = _txy * c_th * c_phi + _tyy * c_th * s_phi - _tzy * s_th;
        return -atx * s_phi + aty * c_phi;
    }

//**********************************************************************
// Return the phi, rxyz spherical component.
    public double t_phi_rxyz()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double apx = -_txx * s_phi + _tyx * c_phi;
        double apy = -_txy * s_phi + _tyy * c_phi;
        double apz = -_txz * s_phi + _tyz * c_phi;
        return apx * s_th * c_phi + apy * s_th * s_phi + apz * c_th;
    }

//**********************************************************************
// Return the phi, theta spherical component.
    public double t_phi_theta()
    {
        double c_phi = cosPhi();
        double s_phi = sinPhi();
        double c_th = cosTheta();
        double s_th = sinTheta();
        double apx = -_txx * s_phi + _tyx * c_phi;
        double apy = -_txy * s_phi + _tyy * c_phi;
        double apz = -_txz * s_phi + _tyz * c_phi;
        return apx * c_th * c_phi + apy * c_th * s_phi - apz * s_th;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Point: \n " + super.toString());
        sb.append("Tensor:" + "\n");
        sb.append(t_x_x() + "\n");
        sb.append(t_x_y() + "\n");
        sb.append(t_x_z() + "\n");
        sb.append("\n");
        sb.append(t_y_x() + "\n");
        sb.append(t_y_y() + "\n");
        sb.append(t_y_z() + "\n");
        sb.append("\n");
        sb.append(t_z_x() + "\n");
        sb.append(t_z_y() + "\n");
        sb.append(t_z_z() + "\n");

        return sb.toString();

    }

    public boolean equals(SpacePointTensor spt)
    {

        if (!super.equals(spt))
            return false;
        if (_txx != spt._txx)
            return false;
        if (_txy != spt._txy)
            return false;
        if (_txz != spt._txz)
            return false;
        if (_tyx != spt._tyx)
            return false;
        if (_tyy != spt._tyy)
            return false;
        if (_tyz != spt._tyz)
            return false;
        if (_tzx != spt._tzx)
            return false;
        if (_tzy != spt._tzy)
            return false;
        if (_tzz != spt._tzz)
            return false;

        return true;
    }
}
