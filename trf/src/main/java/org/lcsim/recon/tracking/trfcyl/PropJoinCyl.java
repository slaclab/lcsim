package org.lcsim.recon.tracking.trfcyl;


import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * PropJoinCyl propagates tracks from a starting surface to a cylinder and
 * then to the final surface.  The radius of the intermediate
 * cylinder is calculated from two parameters rmin and rfac and
 * the starting radius r0:
 * if rfac*r0 < rmin, then r = rmin, otherwise r = rfac*r0.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class PropJoinCyl extends PropDirected
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropJoinCyl";
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
    
    // attributes
    
    // parameters to calculate radius
    private double _rmin;
    private double _rfac;
    
    // The propagators.
    private Propagator _prop1;
    private Propagator _prop2;
    
    //methods
    
    
    //
    
    /**
     *Construct an instance from the two constituent propagators.
     *
     * @param   rmin The minimum radius.
     * @param   rfac The maximum radius.
     * @param   prop1 The first Propagator.
     * @param   prop2 The second Propagator.
     */
    public PropJoinCyl(double rmin, double rfac, Propagator prop1,
            Propagator prop2)
    {
        _rmin = rmin;
        _rfac = rfac;
        _prop1 = prop1.newPropagator();
        _prop2 = prop2.newPropagator();
    }
    
    //
    
    /**
     *Construct a clone of this instance
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropJoinCyl(_rmin, _rfac, _prop1.newPropagator(), _prop2.newPropagator());
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
    
    //
    
    /**
     *Return the minimum radius.
     *
     * @return The minimum radius.
     */
    public double minRadius()
    { return _rmin;
    }
    
    /**
     *Return the maximum radius.
     *
     * @return The maximum radius.
     */
    public double maxRadius()
    { return _rfac;
    }
    
    /**
     * Return the first Propagator.
     *
     * @return The first Propagator.
     */
    public  Propagator prop1()
    { return _prop1.newPropagator();
    }
    
    /**
     * Return the second Propagator.
     *
     * @return The second Propagator.
     */
    public  Propagator prop2()
    { return _prop2.newPropagator();
    }
    
    //
    
    /**
     *Propagate a track without error in the specified direction.
     *
     * @param   trv The Vtrack to propagate.
     * @param   srf The surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @return The propagation status.
     */
    public PropStat vecDirProp(VTrack trv,  Surface srf, PropDir dir)
    {
        TrackDerivative der = null;
        return vecDirProp(trv,srf,dir,der);
    }
    
    //
    
    /**
     *Propagate a track without error in the specified direction
     * and update the track derivatives at the final surface.
     *
     * @param   trv The Vtrack to propagate.
     * @param   srf The surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   der the track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat vecDirProp(VTrack trv,  Surface srf,
            PropDir dir, TrackDerivative der )
    {
        // Construct intermediate derivatives.
        TrackDerivative tmpder1 = null;
        TrackDerivative tmpder2 = null;
        if (der != null)
        {
            tmpder1 = new TrackDerivative();
            tmpder2 = new TrackDerivative();
        }
        
        //System.out.println("In PropJoinCyl!");
        // Propagate from starting surface to cylinder.
        double r = _rfac*trv.spacePoint().rxy();
        if ( r < _rmin ) r = _rmin;
        PropStat pstat = _prop1.vecDirProp(trv, new SurfCylinder(r),
                PropDir.NEAREST, tmpder1);
        if ( ! pstat.success() )
        {
            return pstat;
        }
        
        // Propagate from cylinder to final surface.
        pstat = _prop2.vecDirProp(trv, srf, dir, tmpder2);
        if ( ! pstat.success() )
        {
            return pstat;
        }
        
        // Calculate the overall derivative matrix.
        if ( der != null )
        {
            der = new TrackDerivative( tmpder2.times(tmpder1) );
        }
        
        // Return the final status.
        return pstat;
        
    }
    //
    
    /**
     *Propagate a track with error in the specified direction.
     *
     * @param   trv The Etrack to propagate.
     * @param   srf The surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @return The propagation status.
     */
    public PropStat errDirProp(ETrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative der = null;
        return errDirProp(trv,srf,dir,der);
    }
    
    //
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   trv The Etrack to propagate.
     * @param   srf The surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   der the track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat errDirProp(ETrack trv, Surface srf,
            PropDir dir, TrackDerivative der)
    {
        TrackDerivative tmpder1 = null;
        TrackDerivative tmpder2 = null;
        if (der != null)
        {
            // Construct intermediate derivatives.
            tmpder1 = new TrackDerivative();
            tmpder2 = new TrackDerivative();
        }
        
        // Propagate from starting surface to cylinder.
        double r = _rfac*trv.spacePoint().rxy();
        
        if ( r < _rmin ) r = _rmin;
        PropStat pstat = _prop1.errDirProp(trv, new SurfCylinder(r),
                PropDir.NEAREST, tmpder1 );
        if ( ! pstat.success() )
        {
            return pstat;
        }
        
        // Propagate from cylinder to final surface.
        pstat = _prop2.errDirProp(trv, srf, dir, tmpder2);
        if ( ! pstat.success() )
        {
            return pstat;
        }
        // Calculate the overall derivative matrix.
        if ( der != null )
        {
            der = new TrackDerivative( tmpder2.times(tmpder1) );
        }
        // Return the final status.
        return pstat;
        
    }
    
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Propagator using intermediate cylinder.\n");
        sb.append("rmin = " + _rmin + "; rfac = " + _rfac + "\n");
        sb.append("First propagator: " + _prop1 + "\n");
        sb.append("Second propagator: " + _prop2 + "\n");
        return sb.toString();
    }
}