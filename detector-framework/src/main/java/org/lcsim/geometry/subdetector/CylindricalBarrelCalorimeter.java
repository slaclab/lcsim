package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * 
 * @author Tony Johnson
 */
public class CylindricalBarrelCalorimeter extends CylindricalCalorimeter
{
    public CylindricalBarrelCalorimeter( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    public boolean isBarrel()
    {
        return true;
    }

    private void build( Element node ) throws JDOMException
    {
        Element dimensions = node.getChild( "dimensions" );

        outerR = innerR + getLayering().getThickness();
        maxZ = dimensions.getAttribute( "outer_z" ).getDoubleValue();
        minZ = -maxZ;
        zlength = maxZ * 2;

        getLayering().setOffset( innerR );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, 1, false, getVisAttributes().getColor() );
        /*
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );
        HepRepType barrel = typeTree.getType( "Barrel" );

        HepRepType type = factory.createHepRepType( barrel, getName() );
        type.addAttValue( "drawAs", "Cylinder" );
        //type.addAttValue( "color", getVisAttributes().getColor() );

        setHepRepColor( type );

        HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
        instance.addAttValue( "radius", getInnerRadius() );
        factory.createHepRepPoint( instance, 0, 0, getZMin() );
        factory.createHepRepPoint( instance, 0, 0, getZMax() );

        HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
        instance2.addAttValue( "radius", getOuterRadius() );
        factory.createHepRepPoint( instance2, 0, 0, getZMin() );
        factory.createHepRepPoint( instance2, 0, 0, getZMax() );
        */
    }

    public double getZLength()
    {
        return this.maxZ * 2;
    }
}
