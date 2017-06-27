package org.lcsim.detector.converter.compact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorFactory;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Polycone;
import org.lcsim.detector.solids.Polycone.ZPlane;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.PolyconeSupport;

/**
 * Convert a {@link org.lcsim.geometry.subdetector.PolyconeSupport} to the
 * {@link org.lcsim.detector} detailed detector representation.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyconeSupportConverter.java,v 1.4 2011/02/09 01:28:15 jeremy Exp $
 */

public class PolyconeSupportConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{
    /**
     * This type is only used for dead material and has no IdentifierHelper.
     */
    public IIdentifierHelper makeIdentifierHelper( Subdetector subdetector, SystemMap systemMap )
    {
        return null;
    }

    public void convert( Subdetector subdet, Detector detector )
    {        
        List< ZPlane > zplanes = new ArrayList< ZPlane >();
        Element node = subdet.getNode();
        for ( Iterator i = node.getChildren( "zplane" ).iterator(); i.hasNext(); )
        {
            try
            {
                Element zplane = ( Element ) i.next();
                zplanes.add( new ZPlane( zplane.getAttribute( "rmin" ).getDoubleValue(), zplane.getAttribute( "rmax" )
                        .getDoubleValue(), zplane.getAttribute( "z" ).getDoubleValue() ) );
            }
            catch ( DataConversionException dce )
            {
                throw new RuntimeException( "bad values for zplane from the xml file", dce );
            }
        }

        Polycone polycone = new Polycone( subdet.getName(), zplanes );

        IMaterial material = MaterialStore.getInstance().get( node.getChild( "material" ).getAttributeValue( "name" ) );

        ILogicalVolume lvPolycone = new LogicalVolume( subdet.getName(), polycone, material );

        IPhysicalVolume mom = null;
        String path = "/";
        if ( subdet.isInsideTrackingVolume() )
        {
            mom = detector.getTrackingVolume();
            path = mom.getName() + "/";
        }
        else
        {
            mom = detector.getWorldVolume();
        } 

        IPhysicalVolume pv = DetectorFactory.getInstance().createPhysicalVolume(
                null,
                subdet.getName(),
                lvPolycone,
                mom.getLogicalVolume(),
                0 );
        
        path += pv.getName();
        
        //System.out.println("making PolyconeSupport DE with path: " + path);
                               
        new DetectorElement(subdet.getName() + "_placement", subdet.getDetectorElement(), path);
    }

    public Class getSubdetectorType()
    {
        return PolyconeSupport.class;
    }
}