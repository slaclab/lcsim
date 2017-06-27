package org.lcsim.geometry.subdetector;

import static java.lang.Math.PI;
import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * This class provides common implementation of methods for calorimeters with a polyhedra topology.
 * 
 * @see org.lcsim.geometry.Calorimeter
 * @see org.lcsim.geometry.subdetector.AbstractCalorimeter
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractPolyhedraCalorimeter.java,v 1.16 2012/01/30 13:43:47 jeremy Exp $
 */
// TODO Remove duplicate methods (e.g. methods added for Pandora output).
public class AbstractPolyhedraCalorimeter extends AbstractCalorimeter
{
    public AbstractPolyhedraCalorimeter( Element node) throws JDOMException
    {
        super( node );
        build( node );
    }

    private void build( Element node ) throws JDOMException
    {
        Element dimensions = node.getChild( "dimensions" );

        nsides = dimensions.getAttribute( "numsides" ).getIntValue();
        sectionPhi = (2. * PI) / ((double) nsides);
        
        // Additional parameters are read by subclasses.
    }
    
    protected final double computeBarrelOuterRadius()
    {
        return (getInnerRadius() + getLayering().getThickness())/(Math.cos(Math.PI/getNumberOfSides()));
    }

    public int getNumberOfSides()
    {
        return nsides;
    }

    public double getSectionPhi()
    {
        return sectionPhi;
    }

    public double getZLength()
    {
        return zlength;
    }

    public double getInnerZ()
    {
        return innerZ;
    }

    public double getOuterZ()
    {
        return outerZ;
    }

    public double getOuterRadius()
    {
        return outerRadius;
    }

    public double getInnerRadius()
    {
        return innerRadius;
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        // Let subclasses implement this.
    }

    // Old HepRep method. Keep here for reference. --JM
    /*
     * public void appendHepRep(HepRepFactory factory, HepRep heprep) { HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop("Detector","1.0"); HepRepTypeTree typeTree =
     * heprep.getTypeTree("DetectorType","1.0"); HepRepType barrel = typeTree.getType("Barrel");
     * 
     * HepRepType type = factory.createHepRepType(barrel, getName()); type.addAttValue("drawAs","Polygon");
     * 
     * setHepRepColor(type);
     * 
     * HepRepInstance instance = factory.createHepRepInstance(instanceTree, type);
     * 
     * // compute section (x,y) coordinates double rmin = getInnerR();
     * 
     * double x1 = rmin * tan(this.getHalfSectionPhi()); double y1 = rmin;
     * 
     * double x2 = -x1; double y2 = y1;
     * 
     * double z = this.getZLength() / 2;
     * 
     * double rmax = this.getOuterR();
     * 
     * double x4 = rmax * tan(this.getHalfSectionPhi()); double y4 = rmax;
     * 
     * double x3 = -x4; double y3 = y4;
     * 
     * // Place nsides sections by applying matrix transform to starting coordinates. double phi = 0; for ( int i=0; i<getNumberOfSides(); i++) { instance = factory.createHepRepInstance(instanceTree,
     * type);
     * 
     * double ix1 = x1 * cos(phi) - y1 * sin(phi); double iy1 = x1 * sin(phi) + y1 * cos(phi);
     * 
     * double ix2 = x2 * cos(phi) - y2 * sin(phi); double iy2 = x2 * sin(phi) + y2 * cos(phi);
     * 
     * double ix3 = x3 * cos(phi) - y3 * sin(phi); double iy3 = x3 * sin(phi) + y3 * cos(phi);
     * 
     * double ix4 = x4 * cos(phi) - y4 * sin(phi); double iy4 = x4 * sin(phi) + y4 * cos(phi);
     * 
     * // +z face factory.createHepRepPoint(instance, ix1, iy1, z); factory.createHepRepPoint(instance, ix2, iy2, z); factory.createHepRepPoint(instance, ix3, iy3, z);
     * factory.createHepRepPoint(instance, ix4, iy4, z);
     * 
     * // -z face factory.createHepRepPoint(instance, ix4, iy4, -z); factory.createHepRepPoint(instance, ix3, iy3, -z); factory.createHepRepPoint(instance, ix2, iy2, -z);
     * factory.createHepRepPoint(instance, ix1, iy1, -z);
     * 
     * phi += getSectionPhi(); } }
     * 
     * // Static version for subclasses. public static final void appendHepRep(AbstractPolyhedraCalorimeter cal, HepRepFactory factory, HepRep heprep) { HepRepInstanceTree instanceTree =
     * heprep.getInstanceTreeTop("Detector","1.0"); HepRepTypeTree typeTree = heprep.getTypeTree("DetectorType","1.0"); HepRepType barrel = typeTree.getType("Barrel");
     * 
     * HepRepType type = factory.createHepRepType(barrel, cal.getName()); type.addAttValue("drawAs","Polygon");
     * 
     * cal.setHepRepColor(type);
     * 
     * HepRepInstance instance = factory.createHepRepInstance(instanceTree, type);
     * 
     * // compute section (x,y) coordinates double rmin = cal.getInnerR();
     * 
     * double x1 = rmin * tan(cal.getHalfSectionPhi()); double y1 = rmin;
     * 
     * double x2 = -x1; double y2 = y1;
     * 
     * double z = cal.getZLength() / 2;
     * 
     * double rmax = cal.getOuterR();
     * 
     * double x4 = rmax * tan(cal.getHalfSectionPhi()); double y4 = rmax;
     * 
     * double x3 = -x4; double y3 = y4;
     * 
     * // Place nsides sections by applying matrix transform to starting coordinates. double phi = 0; for ( int i=0; i<cal.getNumberOfSides(); i++) { instance =
     * factory.createHepRepInstance(instanceTree, type);
     * 
     * double ix1 = x1 * cos(phi) - y1 * sin(phi); double iy1 = x1 * sin(phi) + y1 * cos(phi);
     * 
     * double ix2 = x2 * cos(phi) - y2 * sin(phi); double iy2 = x2 * sin(phi) + y2 * cos(phi);
     * 
     * double ix3 = x3 * cos(phi) - y3 * sin(phi); double iy3 = x3 * sin(phi) + y3 * cos(phi);
     * 
     * double ix4 = x4 * cos(phi) - y4 * sin(phi); double iy4 = x4 * sin(phi) + y4 * cos(phi);
     * 
     * // +z face factory.createHepRepPoint(instance, ix1, iy1, z); factory.createHepRepPoint(instance, ix2, iy2, z); factory.createHepRepPoint(instance, ix3, iy3, z);
     * factory.createHepRepPoint(instance, ix4, iy4, z);
     * 
     * // -z face factory.createHepRepPoint(instance, ix4, iy4, -z); factory.createHepRepPoint(instance, ix3, iy3, -z); factory.createHepRepPoint(instance, ix2, iy2, -z);
     * factory.createHepRepPoint(instance, ix1, iy1, -z);
     * 
     * phi += cal.getSectionPhi(); } }
     */
}