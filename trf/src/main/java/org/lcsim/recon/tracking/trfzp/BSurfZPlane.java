package org.lcsim.recon.tracking.trfzp;

import org.lcsim.recon.tracking.trfutil.Assert;

import  org.lcsim.recon.tracking.spacegeom.SpacePoint;
import  org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import  org.lcsim.recon.tracking.spacegeom.SpacePath;
import  org.lcsim.recon.tracking.spacegeom.CartesianPath;

import org.lcsim.recon.tracking.trfbase.Surface;

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.BoundedStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;

/**
 * BSurfZPlane is a ZPlane with rectangular boundaries in (x,y).
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class BSurfZPlane extends SurfZPlane
{
    
    // Static methods
    
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "BSurfZPlane";
    }
    
    
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    // Attributes
    
    // lower x-limit
    private double _xmin;
    
    // upper x-limit
    private double _xmax;
    
    // lower y-limit
    private double _ymin;
    
    // upper y-limit
    private double _ymax;
    
    // Methods
    
    /** Equality */
    protected boolean safeBoundEqual(Surface srf)
    {
        BSurfZPlane bxyp = (BSurfZPlane) srf;
        return super.safePureEqual(srf) &&
        _xmin == bxyp._xmin  && _xmax == bxyp._xmax &&
        _ymin == bxyp._ymin  && _ymax == bxyp._ymax;
    }
    
    // Calculate crossing status from VTrack and error in (x,y)_track
    // This is used by both the public status methods.
    private CrossStat status(VTrack trv, double dxtrk, double dytrk)
    {
        CrossStat xstat = super.pureStatus(trv);
        // If track is not at surface exit with bounds flags undefined.
        if ( !xstat.at() ) return xstat;
        Assert.assertTrue( ! xstat.boundsChecked() );
        // Calculate whether x track +/- nsigma is in and/or out of bounds.
        double xtrk = trv.vector().get(SurfZPlane.IX);
        double xtrk1 = xtrk - dxtrk;
        double xtrk2 = xtrk + dxtrk;
        
        // Calculate whether y track +/- nsigma is in and/or out of bounds.
        double ytrk = trv.vector().get(SurfZPlane.IY);
        double ytrk1 = ytrk - dytrk;
        double ytrk2 = ytrk + dytrk;
        
        // fully out of bounds
        if( (xtrk2 < _xmin || xtrk1 > _xmax ) ||
        (ytrk2 < _ymin || ytrk1 > _ymax ) )
            return new CrossStat(BoundedStat.OUT_OF_BOUNDS);
        // fully in bounds
        if( (xtrk1 > _xmin  &&  xtrk2 < _xmax ) &&
        (ytrk1 > _ymin  &&  ytrk2 < _ymax ) )
            return new CrossStat(BoundedStat.IN_BOUNDS);
        // must be both
        return new CrossStat(BoundedStat.BOTH_BOUNDS);
    }
    
    /**
     *Construct an instance specifying the  z location of the plane and
     *the bounds in x and y.
     * @param zpos Z Position of ZPlane
     * @param xmin Minimum x position of bound
     * @param xmax Maximum x position of bound
     * @param ymin Minimum y position of bound
     * @param ymax Maximum y position of bound
     */
    public BSurfZPlane(double zpos,
    double xmin, double xmax,
    double ymin, double ymax)
    {
        super(zpos);
        _xmin = xmin;
        _xmax = xmax;
        _ymin = ymin;
        _ymax = ymax;
        Assert.assertTrue( _ymax > _ymin );
        Assert.assertTrue( _xmax > _xmin );
    }
    
    /**
     * Return the lower bound in x.
     * @return The lower bound in x.
     */
    
    public double xMin()
    { return _xmin;
    }
    
    /**
     * Return the upper bound in x.
     * @return The upper bound in x.
     */
    
    public double xMax()
    { return _xmax;
    }
    
     /**
     * Return the lower bound in y.
     * @return The lower bound in y.
     */
    
    public double yMin()
    { return _ymin;
    }
    
     /**
     * Return the upper bound in y.
     * @return The upper bound in y.
     */
    
    public double yMax()
    { return _ymax;
    }
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return  staticType();
    }
    
    /**
     * Clone this BSurfZPlane.
     * @return a clone of this BSurfZPlane.
     */
    
    public Surface newSurface()
    {
        double zpos = parameter(SurfZPlane.ZPOS);
        return new BSurfZPlane( zpos, _xmin, _xmax, _ymin, _ymax );
    }
    
    /**
     * Calculate the crossing status for a track without error.
     * @param   trv The VTrack to test.
     * @return The crossing status.
     */
    
    public CrossStat status(VTrack trv)
    {
        double prec = CrossStat.staticPrecision();
        double dxtrk = prec;
        double dytrk = prec;
        return status(trv,dxtrk,dytrk);
    }
    
    /**
     * Calculate the crossing status for a track with error.
     * @param   tre The ETrack to test.
     * @return The crossing status.
     */
    
    public  CrossStat status(ETrack tre)
    {
        double nsigma = CrossStat.staticNSigma();
        double prec = CrossStat.staticPrecision();
        double dxtrk = nsigma*
        Math.sqrt(tre.error().get(SurfZPlane.IX,SurfZPlane.IX)) + prec;
        double dytrk = nsigma*
        Math.sqrt(tre.error().get(SurfZPlane.IY,SurfZPlane.IY)) + prec;
        return status(tre,dxtrk,dytrk);
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(", xmin " + _xmin + " and xmax " + _xmax
        + ", ymin " + _ymin + " and ymax " + _ymax);
        return sb.toString();
    }
}