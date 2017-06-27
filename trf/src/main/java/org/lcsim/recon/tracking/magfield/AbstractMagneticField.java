package org.lcsim.recon.tracking.magfield;

import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointTensor;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

/**
 * AbstractMagneticField is a general magnetic field class which returns values
 * interesting for tracking code. A magnetic field will generally have an
 * internal representation optimized for speed which will need to be updated
 * whenever the field is moved or rescaled. Important methods are:
 * 
 *
 * SpacePointVector field( SpacePoint p); SpacePointVector field( SpacePoint p,
 * SpacePointTensor g); return the field at p and calculates the gradient
 * tensor. The returned field and the gradient are in the form of a value at the
 * point p.
 *
 *
 * Note regarding gradient tensor:
 * 
 * The gradient tensor is a matrix of covariant derivates, which can be defined,
 * or its components extracted, in any standard coordinate system (namely,
 * Cartesian, cylindrical, spherical).
 * 
 * In Cartesian coordinates, covariant derivatives is the same as ordinary
 * partial derivatives:
 * 
 * Bi;j = Bi,j (i,j = x,y,z)
 * 
 * In Cylindrical coordinates, the physical components of the covariant gradient
 * tensor are as follows:
 * 
 * Br;r = Br,r 
 * Br;phi = (1/r)Br,phi - Bphi/r 
 * Br:z = Br,z 
 * Bphi;r = Bphi,r
 * Bphi;phi = (1/r)Bphi,phi + Br/r 
 * Bphi;z = Bphi,z 
 * Bz;r = Bz,r 
 * Bz;phi = (1/r)Bz,phi 
 * Bz;z = Bz,phi
 *
 *
 *
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public abstract class AbstractMagneticField
{
    // given a space position, return a field object

    public abstract SpacePointVector field(SpacePoint p);
    // given a space position, return a field object and calculate
    // the covariant gradient.

    public abstract SpacePointVector field(SpacePoint p, SpacePointTensor g);
}
