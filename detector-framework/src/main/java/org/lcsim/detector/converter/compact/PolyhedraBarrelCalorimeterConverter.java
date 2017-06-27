package org.lcsim.detector.converter.compact;

import static java.lang.Math.PI;
import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
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
import org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter;

public class PolyhedraBarrelCalorimeterConverter extends AbstractSubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        // subdetector parameters
        int sysId = subdet.getSystemID();
        String subdetName = subdet.getName();
        PolyhedraBarrelCalorimeter cal = ( PolyhedraBarrelCalorimeter ) subdet;
        int nsides = cal.getNumberOfSides();
        double innerR = cal.getInnerRadius();
        Layering layering = cal.getLayering();
        double thickness = layering.getThickness();
        double z = cal.getZLength();

        // parameters for trapezoid section
        double innerAngle = Math.PI * 2 / nsides;
        double halfInnerAngle = innerAngle / 2;
        double innerFaceLength = innerR * tan( halfInnerAngle ) * 2;
        double rmax = innerR + thickness;
        double outerFaceLength = rmax * tan( halfInnerAngle ) * 2;
        double sectCenter = innerR + thickness / 2;
        double layerOuterAngle = ( PI - innerAngle ) / 2;
        double layerInnerAngle = ( PI / 2 - layerOuterAngle );

        Trd sectTrd = new Trd( subdet.getName() + "_stave_trapezoid",
                               innerFaceLength / 2,
                               outerFaceLength / 2,
                               z / 2,
                               z / 2,
                               thickness / 2 );
        IMaterial air = MaterialStore.getInstance().get( "Air" );
        ILogicalVolume sectLV = new LogicalVolume( subdetName + "_stave", sectTrd, air );

        double stave_thickness = thickness;

        int layer_number = 0;
        double layer_position_z = -( stave_thickness / 2 );

        double layer_dim_x = innerFaceLength;

        for ( Iterator i = subdet.getNode().getChildren( "layer" ).iterator(); i.hasNext(); )
        {
            Element layer_element = ( Element ) i.next();

            // Get the layer from the layering engine.
            Layer layer = layering.getLayer( layer_number );

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
                String layer_name = subdetName + "_stave_layer" + layer_number;

                // Layer thickness.
                double layer_thickness = layer.getThickness();

                int nslices = layer_element.getChildren( "slices" ).size();

                // Layer position in Z within the stave.
                layer_position_z += layer_thickness / 2;

                // Position of layer.
                ITranslation3D layer_position = new Translation3D( 0, 0, layer_position_z );

                // Layer box.
                Box layer_box = new Box( layer_name + "_box", layer_dim_x / 2, z / 2, layer_thickness / 2 );

                ILogicalVolume layer_volume = new LogicalVolume( layer_name, layer_box, air );

                // Create the slices (sublayers) within the layer.
                double slice_position_z = -( layer_thickness / 2 );

                int slice_number = 0;
                for ( Iterator k = layer_element.getChildren( "slice" ).iterator(); k.hasNext(); )
                {
                    Element slice_element = ( Element ) k.next();

                    String slice_name = layer_name + "_slice" + slice_number;

                    boolean sensitive = false;
                    try
                    {
                        if ( slice_element.getAttribute( "sensitive" ) != null )
                        {
                            Attribute s = slice_element.getAttribute( "sensitive" );
                            sensitive = s != null && s.getBooleanValue();
                        }
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    double slice_thickness;
                    try
                    {
                        slice_thickness = slice_element.getAttribute( "thickness" ).getDoubleValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    slice_position_z += slice_thickness / 2;

                    IMaterial slice_material = MaterialStore.getInstance().get(
                            slice_element.getAttributeValue( "material" ) );

                    ITranslation3D slice_position = new Translation3D( 0, 0, slice_position_z );
                    Box slice_box = new Box( slice_name + "_box", layer_dim_x / 2, z / 2, slice_thickness / 2 );
                    ILogicalVolume slice_volume = new LogicalVolume( slice_name, slice_box, slice_material );
                    ITransform3D sliceTrans = new Transform3D( slice_position );
                    PhysicalVolume slice_physvol = new PhysicalVolume( sliceTrans,
                                                                       slice_name,
                                                                       slice_volume,
                                                                       layer_volume,
                                                                       slice_number );
                    if ( sensitive )
                        slice_physvol.setSensitive( true );

                    slice_position_z += slice_thickness / 2;
                    ++slice_number;
                }

                // Layer PhysicalVolume.
                new PhysicalVolume( new Transform3D( layer_position ), layer_name, layer_volume, sectLV, layer_number );

                // Increment the layer X dimension.
                layer_dim_x += layer_thickness * tan( layerInnerAngle ) * 2;

                // Increment the layer Z position.
                layer_position_z += layer_thickness / 2;

                // Increment the layer number.
                ++layer_number;
            }
        }

        // Create DetectorElements for modules.
        for ( int i = 0; i < nsides; i++ )
        {
            double phi = -( 2 * Math.PI * ( ( double ) i ) / nsides - Math.PI / 2 );
            double zc = -phi + Math.PI / 2;
            double x = sectCenter * Math.cos( phi );
            double y = sectCenter * Math.sin( phi );

            ITranslation3D trans = new Translation3D( x, y, 0 );
            IRotation3D rotate = new RotationPassiveXYZ( Math.PI / 2, 0, zc );

            ITransform3D transform = new Transform3D( trans, rotate );

            String name = subdetName + "_module" + i;
            new PhysicalVolume( transform,
                                name,
                                sectLV,
                                detector.getDetectorElement().getGeometry().getLogicalVolume(),
                                i );
            new DetectorElement( subdet.getName() + "_module" + i, subdet.getDetectorElement(), "/" + name );
        }

        IIdentifierHelper helper = cal.getDetectorElement().getIdentifierHelper();

        // Create DetectorElements for sensitive slices.
        for ( IDetectorElement section : subdet.getDetectorElement().getChildren() )
        {
            int sectNum = section.getGeometry().getPhysicalVolume().getCopyNumber();
            for ( IPhysicalVolume layer : section.getGeometry().getLogicalVolume().getDaughters() )
            {
                for ( IPhysicalVolume slice : layer.getLogicalVolume().getDaughters() )
                {
                    IExpandedIdentifier expId = new ExpandedIdentifier( helper.getIdentifierDictionary()
                            .getNumberOfFields() );

                    expId.setValue( helper.getFieldIndex( "system" ), sysId );
                    expId.setValue( helper.getFieldIndex( "barrel" ), 0 );
                    expId.setValue( helper.getFieldIndex( "module" ), section.getGeometry().getPhysicalVolume()
                            .getCopyNumber() );
                    expId.setValue( helper.getFieldIndex( "layer" ), layer.getCopyNumber() );
                    expId.setValue( helper.getFieldIndex( "slice" ), slice.getCopyNumber() );
                    IIdentifier id = helper.pack( expId );

                    if ( slice.isSensitive() )
                    {
                        new DetectorElement( subdetName + "_module" + sectNum + "_layer" + layer.getCopyNumber() + "_slice" + slice
                                                     .getCopyNumber(),
                                             section,
                                             "/" + section.getGeometry().getPhysicalVolume().getName() + "/" + layer
                                                     .getName() + "/" + slice.getName(),
                                             id );
                    }
                }
            }
        }

    }

    public Class getSubdetectorType()
    {
        return PolyhedraBarrelCalorimeter.class;
    }
}