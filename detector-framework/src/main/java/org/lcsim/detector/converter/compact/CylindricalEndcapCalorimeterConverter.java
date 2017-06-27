package org.lcsim.detector.converter.compact;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.subdetector.CylindricalEndcapCalorimeter;

public class CylindricalEndcapCalorimeterConverter extends AbstractSubdetectorConverter implements
        ISubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        CylindricalEndcapCalorimeter cal = ( CylindricalEndcapCalorimeter ) subdet;

        IPhysicalVolume world = detector.getWorldVolume();

        ILogicalVolume envelope = buildEnvelope( cal, world.getLogicalVolume().getMaterial() );

        double width = ( cal.getZMax() - cal.getZMin() ) / 2;
        double zcenter = cal.getZMin() + width;

        // Positive endcap.
        IPhysicalVolume pv = new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, zcenter ) ),
                                                 cal.getName() + "_positive",
                                                 envelope,
                                                 world.getLogicalVolume(),
                                                 subdet.getSystemID() );

        double negz = -cal.getZMin() - width;

        // DE for positive endcap.
        DetectorElement endcap = new DetectorElement( cal.getName() + "_positive", subdet.getDetectorElement() );
        endcap.setSupport( cal.getName() + "_positive" );

        // DE for negative endcap.
        DetectorElement reflectedEndcap = null;
        if ( cal.getReflect() )
        {
            // IRotation3D reflect = new RotationPassiveEuler(Math.PI,0,0);
            IRotation3D reflect = new RotationPassiveXYZ( 0, Math.PI, 0 );

            new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, negz ), reflect ),
                                cal.getName() + "_negative",
                                envelope,
                                world.getLogicalVolume(),
                                subdet.getSystemID() );

            reflectedEndcap = new DetectorElement( cal.getName() + "_negative", subdet.getDetectorElement() );
            reflectedEndcap.setSupport( cal.getName() + "_negative" );
        }

        // Build the layers into the logical volume.
        buildLayers( cal, envelope );

        // Build the DetectorElements for positive and negative endcaps.
        buildDetectorElements( cal, endcap, reflectedEndcap );
    }

    private void buildDetectorElements( CylindricalEndcapCalorimeter cal,
            DetectorElement endcap,
            DetectorElement reflectedEndcap )
    {
        IIdentifierHelper helper = cal.getDetectorElement().getIdentifierHelper();

        int layerNumber = 0;

        for ( int i = 0; i < cal.getLayering().getNumberOfLayers(); i++ )
        {
            Layer layer = cal.getLayering().getLayer( i );
            String layerName = "layer" + i;
            int sensorNumber = 0;
            for ( int j = 0; j < layer.getSlices().size(); j++ )
            {
                String sliceName = "slice" + j;
                LayerSlice slice = layer.getSlice( j );
                if ( slice.isSensitive() )
                {
                    // FIXME: Hack to fix old detectors like sid02. Remove when these are
                    // retired.
                    if ( sensorNumber > 0 && !helper.hasField( "slice" ) )
                    {
                        // System.out.println("Not creating second sensor, because id is missing a slice field!");
                        break;
                    }

                    // Create an endcap sensor.
                    String pathName = "/" + cal.getName() + "_positive" + "/" + layerName + "/" + sliceName;

                    // Make an id for the endcap north sensor.
                    if ( helper.hasField( "system" ) )
                    {
                        IExpandedIdentifier expid = new ExpandedIdentifier( helper.getIdentifierDictionary()
                                .getNumberOfFields() );
                        if ( helper.hasField( "system" ) )
                            expid.setValue( helper.getFieldIndex( "system" ), cal.getSystemID() );
                        expid.setValue( helper.getFieldIndex( "barrel" ), BarrelEndcapFlag.ENDCAP_NORTH.getFlag() );
                        expid.setValue( helper.getFieldIndex( "layer" ), i );
                        if ( helper.hasField( "slice" ) )
                            expid.setValue( helper.getFieldIndex( "slice" ), j );
                        IIdentifier id = helper.pack( expid );

                        new DetectorElement( cal.getName() + "_positive_layer" + layerNumber + "_sensor" + sensorNumber,
                                             endcap,
                                             pathName,
                                             id );
                    }

                    // Create the reflected endcap sensor.
                    if ( reflectedEndcap != null )
                    {
                        String reflectedPathName = "/" + cal.getName() + "_negative" + "/" + layerName + "/" + sliceName;

                        // Make an id for the endcap south sensor.

                        if ( helper.hasField( "system" ) )
                        {
                            IExpandedIdentifier reflectedExpid = new ExpandedIdentifier( helper
                                    .getIdentifierDictionary().getNumberOfFields() );
                            if ( helper.hasField( "system" ) )
                                reflectedExpid.setValue( helper.getFieldIndex( "system" ), cal.getSystemID() );
                            reflectedExpid.setValue( helper.getFieldIndex( "barrel" ), BarrelEndcapFlag.ENDCAP_SOUTH
                                    .getFlag() );
                            reflectedExpid.setValue( helper.getFieldIndex( "layer" ), i );
                            if ( helper.hasField( "slice" ) )
                                reflectedExpid.setValue( helper.getFieldIndex( "slice" ), j );
                            IIdentifier idReflect = helper.pack( reflectedExpid );

                            new DetectorElement( cal.getName() + "_negative_layer" + layerNumber + "_sensor" + sensorNumber,
                                                 reflectedEndcap,
                                                 reflectedPathName,
                                                 idReflect );
                        }
                    }

                    // Increment the sensor number.
                    ++sensorNumber;
                }
            }

            // Increment the layer number.
            ++layerNumber;
        }
    }

    private ILogicalVolume buildEnvelope( CylindricalEndcapCalorimeter cal, IMaterial material )
    {
        String name = cal.getName();
        Tube tube = new Tube( name + "envelope_tube", cal.getInnerRadius(), cal.getOuterRadius(), ( cal.getZMax() - cal
                .getZMin() ) / 2 );

        ILogicalVolume lv = new LogicalVolume( name + "_envelope", tube, material );

        return lv;
    }

    private void buildLayers( CylindricalEndcapCalorimeter cal, ILogicalVolume envelope )
    {
        Layering layering = cal.getLayering();

        double innerRadius = cal.getInnerRadius();
        double outerRadius = cal.getOuterRadius();

        String name = cal.getName();

        double totalThickness = cal.getZMax() - cal.getZMin();
        totalThickness = -totalThickness;
        double zLayer = totalThickness / 2;

        for ( int i = 0; i < layering.getNumberOfLayers(); i++ )
        {
            Layer layer = layering.getLayer( i );

            Tube tubeLayer = new Tube( name + "layer" + i + "_tube", innerRadius, outerRadius, layer.getThickness() / 2 );

            ILogicalVolume lvLayer = new LogicalVolume( name + "_layer" + i, tubeLayer, envelope.getMaterial() );

            new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, zLayer + layer.getThickness() / 2 ) ),
                                "layer" + i,
                                lvLayer,
                                envelope,
                                i );

            // double zSlice = zLayer;
            double zSlice = -layer.getThickness() / 2;

            for ( int j = 0; j < layer.getNumberOfSlices(); j++ )
            {
                LayerSlice slice = layer.getSlice( j );

                Tube tubeSlice = new Tube( cal.getName() + "_layer" + i + "_slice" + j, cal.getInnerRadius(), cal
                        .getOuterRadius(), slice.getThickness() / 2 );

                ILogicalVolume lvSlice = new LogicalVolume( cal.getName() + "_layer" + i + "_slice" + j,
                                                            tubeSlice,
                                                            MaterialStore.getInstance().get(
                                                                    slice.getMaterial().getName() ) );

                zSlice += slice.getThickness() / 2;

                PhysicalVolume pvSlice = new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, zSlice ) ),
                                                             "slice" + j,
                                                             lvSlice,
                                                             lvLayer,
                                                             j );

                if ( slice.isSensitive() )
                {
                    pvSlice.setSensitive( true );
                }

                zSlice += slice.getThickness() / 2;
            }

            zLayer += layer.getThickness();
        }
    }

    public Class getSubdetectorType()
    {
        return CylindricalEndcapCalorimeter.class;
    }
}
