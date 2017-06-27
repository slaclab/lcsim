package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.BoundedStat;
import org.lcsim.recon.tracking.trfbase.Surface;
/**
 **
 * Cylinder with boundaries in z.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class BSurfCylinder extends SurfCylinder
{
    
    // lower z-limit
    private double _zmin;
    
    // upper z-limit
    private double _zmax;
    
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "BSurfCylinder";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    
    //
    
    /**
     *Check the bounded equality of two surfaces.
     *
     * @param   srf The Surface to compare against.
     * @return true if the two surfaces are equl in their bounded extent.
     */
    public boolean safeBoundEqual( Surface srf)
    {
        
        BSurfCylinder bcy = (BSurfCylinder) srf;
        return safePureEqual(srf) &&
                _zmin == bcy._zmin  && _zmax == bcy._zmax;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();
    }
    
    
    // calculate crossing status from VTrack and error in z_track
    // This is used by both the public status methods.
    private CrossStat status( VTrack trv, double dztrk)
    {
        CrossStat xstat = pureStatus(trv);
        // If track is not at surface exit with bounds flags undefined.
        if ( ! xstat.at() ) return xstat;
        // needs work here
        //assert( ! xstat.bounds_checked() );
        // Calculate whether z track +/- nsigma is in and/or out of bounds.
        double ztrk = trv.vector().vector()[1];
        double ztrk1 = ztrk - dztrk;
        double ztrk2 = ztrk + dztrk;
        // fully out of bounds
        if ( ztrk2 < _zmin || ztrk1 > _zmax )
            return new CrossStat(BoundedStat.OUT_OF_BOUNDS);
        // fully in bounds
        if ( ztrk1 > _zmin  &&  ztrk2 < _zmax )
            return new CrossStat(BoundedStat.IN_BOUNDS);
        // must be both
        return new CrossStat(BoundedStat.BOTH_BOUNDS);
    }
    
    
    
    //
    
    /**
     *Construct an instance of a cylinder with radius, minimum and maximum z.
     *
     * @param   radius The radius of the bounded cylinder.
     * @param   zmin The minimum z extent of the bounded cylinder.
     * @param   zmax The maximum z extent of the bounded cylinder.
     */
    public BSurfCylinder(double radius, double zmin, double zmax)
    {
        super(radius);
        if(zmin>zmax) throw new IllegalArgumentException("BSurfCylinder zmin>zmax");
        
        _zmin = zmin;
        _zmax = zmax;
    }
    
    
    //
    
    /**
     *Return the minimum z extent for this bounded cylinder.
     *
     * @return The minimum z extent for this bounded cylinder.
     */
    public double zMin()
    { return _zmin;
    }
    
    //
    
    /**
     *Return the maximum z extent for this bounded cylinder.
     *
     * @return The maximum z extent for this bounded cylinder.
     */
    public double zMax()
    { return _zmax;
    }
    
    //
    
    /**
     *Calculate crossing status for a VTrack.
     *
     * @param   trv  The VTrack to check.
     * @return The crossing status for the track tre.
     */
    public CrossStat status( VTrack trv)
    {
        double prec = CrossStat.staticPrecision();
        double dztrk = prec;
        return status(trv,dztrk);
    }
    
    //
    
    /**
     *Calculate crossing status for an ETrack.
     *
     * @param   tre The ETrack to check.
     * @return The crossing status for the track tre.
     */
    public CrossStat status( ETrack tre)
    {
        double nsigma = CrossStat.staticNSigma();
        double prec = CrossStat.staticPrecision();
        double dztrk = nsigma*Math.sqrt(tre.error().matrix()[1][1]) + prec;
        return status(tre,dztrk);
    }
    
    /**
     * A String representation of this instance.
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return super.toString()+ " zmin= "+_zmin+" zmax= "+_zmax;
    }
    
}

