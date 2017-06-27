package org.lcsim.detector.converter.compact;

import static java.lang.Math.PI;
import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.ITranslation3D;
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
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.Trd;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter2;

/**
 * 
 * This converter makes a detailed geometry for the
 * {@link org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter2} subdetector type.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyhedraBarrelCalorimeter2Converter.java,v 1.1 2010/05/03 18:01:40
 *          jeremy Exp $
 */
public class PolyhedraBarrelCalorimeter2Converter extends AbstractSubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        // Subdetector parameters.
        int sysId = subdet.getSystemID();
        String subdetName = subdet.getName();
        PolyhedraBarrelCalorimeter2 cal = ( PolyhedraBarrelCalorimeter2 ) subdet;
        int nsides = cal.getNumberOfSides();
        double innerR = cal.getInnerRadius();
        Layering layering = cal.getLayering();
        double thickness = layering.getThickness();
        double z = cal.getZLength();

        // Gap size.
        double gap = 0;
        if ( subdet.getNode().getAttribute( "gap" ) != null )
        {
            try
            {
                gap = subdet.getNode().getAttribute( "gap" ).getDoubleValue();
            }
            catch ( DataConversionException e )
            {
                throw new RuntimeException( e );
            }
        }

        // Get the gap material or use air as the default.
        IMaterial air = MaterialStore.getInstance().get( "Air" );
        IMaterial gapMaterial = air;
        if ( subdet.getNode().getAttribute( "material" ) != null )
        {
            gapMaterial = MaterialStore.getInstance().get( subdet.getNode().getAttributeValue( "material" ) );
        }

        // Parameters for trd stave.
        double innerAngle = Math.PI * 2 / nsides;
        double halfInnerAngle = innerAngle / 2;
        double innerFaceLength = innerR * tan( halfInnerAngle ) * 2;
        double rmax = innerR + thickness;
        double outerFaceLength = rmax * tan( halfInnerAngle ) * 2;
        double sectCenter = innerR + thickness / 2;
        double layerOuterAngle = ( PI - innerAngle ) / 2;
        double layerInnerAngle = ( PI / 2 - layerOuterAngle );

        // Make outer stave trd.
        Trd staveTrdOuter = new Trd( subdet.getName() + "_stave_trap_outer",
                                     innerFaceLength / 2,
                                     outerFaceLength / 2,
                                     z / 2,
                                     z / 2,
                                     thickness / 2 );

        // Make outer stave volume.
        ILogicalVolume staveOuterLogicalVolume = new LogicalVolume( subdetName + "_stave_outer",
                                                                    staveTrdOuter,
                                                                    gapMaterial );

        // Make inner stave trd.
        Trd staveTrdInner = new Trd( subdet.getName() + "_stave_trap_inner",
                                     innerFaceLength / 2 - gap,
                                     outerFaceLength / 2 - gap,
                                     z / 2,
                                     z / 2,
                                     thickness / 2 );

        // Make inner stave volume.
        ILogicalVolume staveInnerLogicalVolume = new LogicalVolume( subdetName + "_stave_inner", staveTrdInner, air );

        // Place inner stave inside of outer.
        new PhysicalVolume( new Transform3D(), subdetName, staveInnerLogicalVolume, staveOuterLogicalVolume, 0 );

        // Layer parameters.
        double staveThickness = thickness;
        int layerNumber = 0;
        double layerZ = -( staveThickness / 2 );
        double layerX = staveTrdInner.getXHalfLength1() * 2;

        // Loop over layer elements.
        for ( Iterator i = subdet.getNode().getChildren( "layer" ).iterator(); i.hasNext(); )
        {
            Element layer_element = ( Element ) i.next();

            // Get the layer from the layering engine.
            Layer layer = layering.getLayer( layerNumber );

            // Get number of times to repeat this layer.
            int repeat;
            try
            {
                repeat = ( int ) layer_element.getAttribute( "repeat" ).getDoubleValue();
            }
            catch ( Exception x )
            {
                throw new RuntimeException( x );
            }

            // Loop over repeats for this layer.
            for ( int j = 0; j < repeat; j++ )
            {
                // Name of the layer.
                String layerName = subdetName + "_stave_layer" + layerNumber;

                // Layer thickness, which is Z in the trd coordinate system.
                double layerThickness = layer.getThickness();

                // Layer position in Z within the stave.
                layerZ += layerThickness / 2;

                // Position of layer.
                ITranslation3D layer_position = new Translation3D( 0, 0, layerZ );

                // Layer box.
                Box layerBox = new Box( layerName + "_box", layerX / 2, z / 2, layerThickness / 2 );

                // Layer volume.
                ILogicalVolume layerVolume = new LogicalVolume( layerName, layerBox, air );

                // Starting Z of slice.
                double sliceZ = -( layerThickness / 2 );

                // Slice ID.
                int sliceNumber = 0;

                // Create the slices within the layer.
                for ( Iterator k = layer_element.getChildren( "slice" ).iterator(); k.hasNext(); )
                {
                    Element sliceElement = ( Element ) k.next();

                    String sliceName = layerName + "_slice" + sliceNumber;

                    // Sensitivity.
                    boolean sensitive = false;
                    try
                    {
                        if ( sliceElement.getAttribute( "sensitive" ) != null )
                        {
                            Attribute s = sliceElement.getAttribute( "sensitive" );
                            sensitive = s != null && s.getBooleanValue();
                        }
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    // Thickness of slice.
                    double sliceThickness;
                    try
                    {
                        sliceThickness = sliceElement.getAttribute( "thickness" ).getDoubleValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    // Increment Z position to place next slice.
                    sliceZ += sliceThickness / 2;

                    // Get the slice material.
                    IMaterial sliceMaterial = MaterialStore.getInstance().get(
                            sliceElement.getAttributeValue( "material" ) );

                    // Slice position.
                    ITranslation3D slicePosition = new Translation3D( 0, 0, sliceZ );
                    ITransform3D sliceTrans = new Transform3D( slicePosition );

                    // Slice box.
                    Box sliceBox = new Box( sliceName + "_box", layerX / 2, z / 2, sliceThickness / 2 );

                    // Slice volume.
                    ILogicalVolume sliceVolume = new LogicalVolume( sliceName, sliceBox, sliceMaterial );

                    // Slice placement.
                    PhysicalVolume slicePlacement = new PhysicalVolume( sliceTrans,
                                                                        sliceName,
                                                                        sliceVolume,
                                                                        layerVolume,
                                                                        sliceNumber );

                    // Set sensitivity.
                    if ( sensitive )
                        slicePlacement.setSensitive( true );

                    // Increment z position.
                    sliceZ += sliceThickness / 2;

                    // Increment slice ID.
                    ++sliceNumber;
                }

                // Layer PhysicalVolume.
                new PhysicalVolume( new Transform3D( layer_position ),
                                    layerName,
                                    layerVolume,
                                    staveInnerLogicalVolume,
                                    layerNumber );

                // Increment the layer X dimension.
                layerX += layerThickness * tan( layerInnerAngle ) * 2;

                // Increment the layer Z position.
                layerZ += layerThickness / 2;

                // Increment the layer number.
                ++layerNumber;
            }
        }

        // Place staves and make DetectorElements for them.
        for ( int i = 0; i < nsides; i++ )
        {
            // Compute stave position and rotation parameters.
            double phi = -( 2 * Math.PI * ( ( double ) i ) / nsides - Math.PI / 2 );
            double zc = -phi + Math.PI / 2;
            double x = sectCenter * Math.cos( phi );
            double y = sectCenter * Math.sin( phi );

            // Make stave position and rotation.
            ITranslation3D trans = new Translation3D( x, y, 0 );
            IRotation3D rotate = new RotationPassiveXYZ( Math.PI / 2, 0, zc );
            ITransform3D transform = new Transform3D( trans, rotate );

            // Stave placement.
            String name = subdetName + "_module" + i;
            new PhysicalVolume( transform, name, staveOuterLogicalVolume, detector.getDetectorElement().getGeometry()
                    .getLogicalVolume(), i );

            // Stave DetectorElement.
            new DetectorElement( subdet.getName() + "_module" + i, subdet.getDetectorElement(), "/" + name );
        }

        // Get the identifier helper for the subdetector for ID packing.
        IIdentifierHelper helper = cal.getDetectorElement().getIdentifierHelper();

        // Create DetectorElements for sensitive slices.

        // Loop over outer staves.
        for ( IDetectorElement outerStave : subdet.getDetectorElement().getChildren() )
        {
            // Loop over inner staves (just one per outer stave).
            for ( IPhysicalVolume innerStave : outerStave.getGeometry().getLogicalVolume().getDaughters() )
            {
                // Get the stave number.
                int staveNum = outerStave.getGeometry().getPhysicalVolume().getCopyNumber();

                // Loop over the layer placements.
                for ( IPhysicalVolume layer : innerStave.getLogicalVolume().getDaughters() )
                {
                    // Loop over the slice placements.
                    for ( IPhysicalVolume slice : layer.getLogicalVolume().getDaughters() )
                    {
                        // Create a new expanded identifier with correct number of fields.
                        IExpandedIdentifier expId = new ExpandedIdentifier( helper.getIdentifierDictionary()
                                .getNumberOfFields() );

                        // Fill in the identifier field values from the geometry values.
                        expId.setValue( helper.getFieldIndex( "system" ), sysId );
                        expId.setValue( helper.getFieldIndex( "barrel" ), 0 );
                        expId.setValue( helper.getFieldIndex( "module" ), outerStave.getGeometry().getPhysicalVolume()
                                .getCopyNumber() );
                        expId.setValue( helper.getFieldIndex( "layer" ), layer.getCopyNumber() );
                        expId.setValue( helper.getFieldIndex( "slice" ), slice.getCopyNumber() );

                        // Pack the identifier.
                        IIdentifier id = helper.pack( expId );

                        // Make a DetectorElement if the slice is sensitive.
                        if ( slice.isSensitive() )
                        {
                            new DetectorElement( subdetName + "_module" + staveNum + "_layer" + layer.getCopyNumber() + "_slice" + slice
                                                         .getCopyNumber(),
                                                 outerStave,
                                                 "/" + outerStave.getGeometry().getPhysicalVolume().getName() + "/" + innerStave
                                                         .getName() + "/" + layer.getName() + "/" + slice.getName(),
                                                 id );
                        }
                    }
                }
            }
        }

    }

    public Class getSubdetectorType()
    {
        return PolyhedraBarrelCalorimeter2.class;
    }
}