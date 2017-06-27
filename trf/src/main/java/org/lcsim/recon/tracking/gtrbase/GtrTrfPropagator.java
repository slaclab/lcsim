package org.lcsim.recon.tracking.gtrbase;

// GtrTrfPropagator

import org.lcsim.recon.tracking.trfbase.Propagator;

import org.lcsim.recon.tracking.trfbase.PropDispatch;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.PropNull;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;
import org.lcsim.recon.tracking.trfcyl.PropCyl;
import org.lcsim.recon.tracking.trfcyl.PropJoinCyl;
import org.lcsim.recon.tracking.trfzp.SurfZPlane;
import org.lcsim.recon.tracking.trfzp.PropZZ;
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;
import org.lcsim.recon.tracking.trfxyp.PropXYXY;
import org.lcsim.recon.tracking.trfcylplane.PropZXY;
import org.lcsim.recon.tracking.trfcylplane.PropXYZ;
import org.lcsim.recon.tracking.trfcylplane.PropZCyl;
import org.lcsim.recon.tracking.trfcylplane.PropXYCyl;
import org.lcsim.recon.tracking.trfcylplane.PropCylZ;
import org.lcsim.recon.tracking.trfcylplane.PropCylXY;
import org.lcsim.recon.tracking.trfdca.SurfDCA;
import org.lcsim.recon.tracking.trfdca.PropCylDCA;
import org.lcsim.recon.tracking.trfdca.PropDCACyl;

/**
 * Class which provides access to a trf propagator which can be
 * used with any combination of the following surface types:
 * SurfCylinder, SurfZplane, SurfXYPlane and SurfDCA.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class GtrTrfPropagator
{
    
    // The propagator.
    private Propagator _prop;
    
    // methods
    
    //
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public  GtrTrfPropagator(double bfield)
    {
        // Extract the types.
        String tcyl = SurfCylinder.staticType();
        String tzpl = SurfZPlane.staticType();
        String txyp = SurfXYPlane.staticType();
        String tdca = SurfDCA.staticType();
        
        // Construct the constituent propagators.
        double rmin = 1.0;
        double rfac = 1.1;
        Propagator  pcylcyl = new PropCyl(bfield);
        Propagator  pzplzpl = new PropZZ(bfield);
        Propagator  pxypxyp = new PropXYXY(bfield);
        Propagator  pzplxyp = new PropZXY(bfield);
        Propagator  pxypzpl = new PropXYZ(bfield);
        Propagator  pcylzpl = new PropCylZ(bfield);
        Propagator  pcylxyp = new PropCylXY(bfield);
        Propagator  pzplcyl = new PropZCyl(bfield);
        Propagator  pxypcyl = new PropXYCyl(bfield);
        Propagator  pcyldca = new PropCylDCA(bfield);
        Propagator  pdcacyl = new PropDCACyl(bfield);
        Propagator  pzpldca = new PropJoinCyl(rmin,rfac,pzplcyl,pcyldca);
        Propagator  pxypdca = new PropJoinCyl(rmin,rfac,pxypcyl,pcyldca);
        Propagator  pdcazpl = new PropJoinCyl(rmin,rfac,pdcacyl,pcylzpl);
        Propagator  pdcaxyp = new PropJoinCyl(rmin,rfac,pdcacyl,pcylxyp);
        Propagator  pdcadca = new PropNull();
        
        // Construct the dispatch propagator.
        PropDispatch _pdispatch = new PropDispatch();
        _prop = _pdispatch;
        _pdispatch.addPropagator(tcyl,tcyl,pcylcyl);
        _pdispatch.addPropagator(tzpl,tzpl,pzplzpl);
        _pdispatch.addPropagator(txyp,txyp,pxypxyp);
        _pdispatch.addPropagator(tzpl,txyp,pzplxyp);
        _pdispatch.addPropagator(txyp,tzpl,pxypzpl);
        _pdispatch.addPropagator(tcyl,tzpl,pcylzpl);
        _pdispatch.addPropagator(tcyl,txyp,pcylxyp);
        _pdispatch.addPropagator(tzpl,tcyl,pzplcyl);
        _pdispatch.addPropagator(txyp,tcyl,pxypcyl);
        _pdispatch.addPropagator(tcyl,tdca,pcyldca);
        _pdispatch.addPropagator(tdca,tcyl,pdcacyl);
        _pdispatch.addPropagator(tzpl,tdca,pzpldca);
        _pdispatch.addPropagator(txyp,tdca,pxypdca);
        _pdispatch.addPropagator(tdca,tzpl,pdcazpl);
        _pdispatch.addPropagator(tdca,txyp,pdcaxyp);
        _pdispatch.addPropagator(tdca,tdca,pdcadca);
        
    }
    
    //
    
    /**
     * Return the propagator.
     *
     * @return The underlying propagator.
     */
    public   Propagator propagator()
    {
        return _prop;
    }
    
    //
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "GtrTrfPropagator"+_prop;
    }
    
}

