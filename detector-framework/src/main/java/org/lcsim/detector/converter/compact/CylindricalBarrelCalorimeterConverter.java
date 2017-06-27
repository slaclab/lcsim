package org.lcsim.detector.converter.compact;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
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
import org.lcsim.geometry.subdetector.CylindricalBarrelCalorimeter;

public class CylindricalBarrelCalorimeterConverter extends AbstractSubdetectorConverter implements
        ISubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        // Cast to subtype.
        CylindricalBarrelCalorimeter cal = ( CylindricalBarrelCalorimeter ) subdet;

        // Get the world volume.
        IPhysicalVolume world = detector.getWorldVolume();

        // Get the world volume fill material.
        IMaterial matWorld = world.getLogicalVolume().getMaterial();

        // Create the Subdetector's envelope LogicalVolume.
        ILogicalVolume envelope = buildEnvelope( cal, matWorld );

        // Create the PhysicalVolume.
        new PhysicalVolume( null, cal.getName(), envelope, world.getLogicalVolume(), subdet.getSystemID() );

        // Setup the geometry.
        IPhysicalVolumeNavigator nav = PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();
        IPhysicalVolumePath path = nav.getPath( cal.getName() );

        // Create the Subdetector's DetectorElement.
        ( ( DetectorElement ) cal.getDetectorElement() ).setSupport( path );

        // Build the layers.
        buildLayers( cal, envelope );
    }

    private ILogicalVolume buildEnvelope( CylindricalBarrelCalorimeter cal, IMaterial material )
    {
        Tube tube = new Tube( cal.getName() + "_envelope_tube", cal.getInnerRadius(), cal.getOuterRadius(), cal
                .getZMax() );

        LogicalVolume logvol = new LogicalVolume( cal.getName() + "_envelope", tube, material );

        return logvol;
    }

    private void buildLayers( CylindricalBarrelCalorimeter cal, ILogicalVolume envelope )
    {
        IIdentifierHelper helper = cal.getDetectorElement().getIdentifierHelper();

        Layering layering = cal.getLayering();
        String name = cal.getName();

        double zHalfLength = cal.getZMax();

        int layerNumber = 0;

        for ( int i = 0; i < layering.getNumberOfLayers(); i++ )
        {
            Layer layer = layering.getLayer( i );

            double layerInnerRadius = layering.getDistanceToLayer( i );

            String layerName = "layer" + i;

            Tube tubeLayer = new Tube( name + "_layer" + i + "_tube", layerInnerRadius, layerInnerRadius + layer
                    .getThickness(), zHalfLength );

            LogicalVolume lvLayer = new LogicalVolume( name + "_layer" + i, tubeLayer, envelope.getMaterial() );

            new PhysicalVolume( null, layerName, lvLayer, envelope, i );

            double sliceInnerRadius = layerInnerRadius;

            int sensorNumber = 0;
            for ( int j = 0; j < layer.getSlices().size(); j++ )
            {
                LayerSlice slice = layer.getSlice( j );

                double sliceThickness = slice.getThickness();

                String materialName = slice.getMaterial().getName();
                IMaterial material = MaterialStore.getInstance().get( materialName );

                String sliceName = "slice" + j;

                Tube sliceLayer = new Tube( name + "_layer" + i + "_slice" + j + "_tube",
                                            sliceInnerRadius,
                                            sliceInnerRadius + sliceThickness,
                                            zHalfLength );

                LogicalVolume lvSlice = new LogicalVolume( name + "_layer" + i + "_slice" + j, sliceLayer, material );

                PhysicalVolume pvSlice = new PhysicalVolume( null, sliceName, lvSlice, lvLayer, j );

                // Setup a DE for each sensitive slice.
                // TODO: needs identifier
                if ( slice.isSensitive() )
                {
                    // FIXME: Hack to fix old detectors like sid02. Remove when these are
                    // retired.
                    if ( sensorNumber > 0 && !helper.hasField( "slice" ) )
                    {
                        // System.out.println("Not creating second sensor, because id is missing a slice field!");
                        break;
                    }

                    pvSlice.setSensitive( true );

                    // Path to the PhysicalVolume of this sensor.
                    String sensorPath = "/" + cal.getName() + "/" + layerName + "/" + sliceName;

                    // Make an id for the sensor.
                    IExpandedIdentifier expid = new ExpandedIdentifier( helper.getIdentifierDictionary()
                            .getNumberOfFields() );
                    if ( helper.hasField( "system" ) )
                    {
                        expid.setValue( helper.getFieldIndex( "system" ), cal.getSystemID() );
                        expid.setValue( helper.getFieldIndex( "barrel" ), BarrelEndcapFlag.BARREL.getFlag() );
                        expid.setValue( helper.getFieldIndex( "layer" ), i );
                        if ( helper.hasField( "slice" ) )
                            expid.setValue( helper.getFieldIndex( "slice" ), j );
                        IIdentifier id = helper.pack( expid );

                        new DetectorElement( cal.getName() + "_layer" + layerNumber + "_sensor" + sensorNumber, cal
                                .getDetectorElement(), sensorPath, id );
                    }
                    // Increment the number of sensors.
                    ++sensorNumber;
                }

                sliceInnerRadius += sliceThickness;
            }
            // Increment layer number..
            layerNumber += 1;
        }
    }

    public void makeIdentifierContext( Subdetector subdet )
    {
        /*
         * IIdentifierDictionary iddict =
         * subdet.getDetectorElement().getIdentifierHelper().getIdentifierDictionary();
         * 
         * int systemIndex = iddict.getFieldIndex("system"); int barrelIndex =
         * iddict.getFieldIndex("barrel"); int layerIndex = iddict.getFieldIndex("layer");
         * 
         * IdentifierContext systemContext = new IdentifierContext(new int[]
         * {systemIndex}); IdentifierContext subdetContext = new IdentifierContext(new
         * int[] {systemIndex,barrelIndex}); IdentifierContext layerContext = new
         * IdentifierContext(new int[] {systemIndex,barrelIndex,layerIndex});
         * 
         * iddict.addIdentifierContext("system", systemContext);
         * iddict.addIdentifierContext("subdetector", subdetContext);
         * iddict.addIdentifierContext("layer", layerContext);
         * 
         * IDDecoder decoder = subdet.getIDDecoder(); if ( decoder instanceof
         * SegmentationBase) { if (decoder instanceof NonprojectiveCylinder) { int
         * phiIndex = iddict.getFieldIndex("phi"); int zIndex = iddict.getFieldIndex("z");
         * IdentifierContext cellContext = new IdentifierContext(new int[]
         * {systemIndex,barrelIndex,layerIndex,phiIndex,zIndex});
         * iddict.addIdentifierContext("cell", cellContext); } else if (decoder instanceof
         * ProjectiveCylinder) { // TODO } else if (decoder instanceof GridXYZ) { // TODO
         * } else if (decoder instanceof ProjectiveZPlane) { // TODO } }
         */
    }

    public Class getSubdetectorType()
    {
        return CylindricalBarrelCalorimeter.class;
    }
}
