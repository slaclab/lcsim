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
import org.lcsim.geometry.segmentation.GridXYZ;

/**
 * 
 * @author Tony Johnson
 */
public class CylindricalEndcapCalorimeter extends CylindricalCalorimeter
{
    CylindricalEndcapCalorimeter( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    public boolean isEndcap()
    {
        return true;
    }

    private void build( Element node ) throws JDOMException
    {
        Element dimensions = node.getChild( "dimensions" );

        /** Define subclass-specific outerR, zmin and zmax. */
        innerR = dimensions.getAttribute( "inner_r" ).getDoubleValue();
        outerR = dimensions.getAttribute( "outer_r" ).getDoubleValue();
        minZ = dimensions.getAttribute( "inner_z" ).getDoubleValue();
        maxZ = minZ + getLayering().getLayers().getTotalThickness();
        zlength = maxZ - minZ;

        getLayering().setOffset( minZ );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {        
        DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, 2, true, getVisAttributes().getColor() );
        /*
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );
        HepRepType endcap = typeTree.getType( "Endcap" );

        HepRepType type = factory.createHepRepType( endcap, getName() );
        type.addAttValue( "drawAs", "Cylinder" );
        //type.addAttValue( "color", getVisAttributes().getColor() );

        setHepRepColor( type );

        double flip = 1;
        for ( ;; )
        {
            HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
            instance.addAttValue( "radius", getInnerRadius() );
            factory.createHepRepPoint( instance, 0, 0, flip * getZMin() );
            factory.createHepRepPoint( instance, 0, 0, flip * getZMax() );

            HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
            instance2.addAttValue( "radius", getOuterRadius() );
            factory.createHepRepPoint( instance2, 0, 0, flip * getZMin() );
            factory.createHepRepPoint( instance2, 0, 0, flip * getZMax() );

            if ( !getReflect() || flip < 0 )
                break;
            flip = -1;
        }
        */
    }

    /**
     * FIXME: This has an implicit assumption that the localPos is that of the layer.
     */
    public double[] transformLocalToGlobal( double[] localPos )
    {
        double[] globPos = { localPos[ 0 ], localPos[ 1 ], localPos[ 2 ] };
        GridXYZ gridSeg = ( GridXYZ ) getReadout().getSegmentation();

        if ( gridSeg != null )
        {
            globPos[ 2 ] += gridSeg.getDistanceToSensitive( gridSeg.getLayer() );

            // int ecFlag = gridSeg.getValue("barrel");
            if ( gridSeg.getBarrelEndcapFlag().isEndcapSouth() )
            {
                // if (ecFlag == 2)
                // {
                globPos[ 2 ] = -globPos[ 2 ];
            }
        }

        return globPos;
    }

    public double getZLength()
    {
        return this.maxZ - this.minZ;
    }
}
