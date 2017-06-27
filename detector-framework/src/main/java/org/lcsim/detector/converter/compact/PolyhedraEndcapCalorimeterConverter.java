package org.lcsim.detector.converter.compact;

import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IRotation3D;
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
import org.lcsim.detector.solids.Trd;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.subdetector.PolyhedraEndcapCalorimeter;

public class PolyhedraEndcapCalorimeterConverter extends AbstractSubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        Element node = subdet.getNode();

        String detName = node.getAttributeValue( "name" );

        int sysId = subdet.getSystemID();

        boolean reflect = true;
        try
        {
            if ( node.getAttribute( "reflect" ) != null )
            {
                reflect = node.getAttribute( "reflect" ).getBooleanValue();
            }
        }
        catch ( Exception t )
        {
            throw new RuntimeException( t );
        }

        Element dimensions = node.getChild( "dimensions" );
        double zmin, rmin, rmax;
        int numsides;

        try
        {
            zmin = dimensions.getAttribute( "zmin" ).getDoubleValue();
            rmin = dimensions.getAttribute( "rmin" ).getDoubleValue();
            rmax = dimensions.getAttribute( "rmax" ).getDoubleValue();
            numsides = dimensions.getAttribute( "numsides" ).getIntValue();
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }

        double subdetectorThickness;
        try
        {
            subdetectorThickness = org.lcsim.geometry.layer.LayerFromCompactCnv.computeDetectorTotalThickness( node );
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }

        double innerAngle = Math.PI * 2 / numsides;
        double halfInnerAngle = innerAngle / 2;
        double innerFaceLength = rmin * tan( halfInnerAngle ) * 2;
        double outerFaceLength = rmax * tan( halfInnerAngle ) * 2;
        double radialThickness = rmax - rmin;

        DetectorElement endcapPositive = new DetectorElement( subdet.getName() + "_positive", subdet
                .getDetectorElement() );

        DetectorElement endcapNegative = null;
        if ( reflect )
        {
            endcapNegative = new DetectorElement( subdet.getName() + "_negative", subdet.getDetectorElement() );
        }

        Trd sectTrd = new Trd( detName + "_stave_trapezoid",
                               innerFaceLength / 2,
                               outerFaceLength / 2,
                               subdetectorThickness / 2,
                               subdetectorThickness / 2,
                               radialThickness / 2 );

        IMaterial air = MaterialStore.getInstance().get( "Air" );

        ILogicalVolume sectVolume = new LogicalVolume( detName + "_stave", sectTrd, air );

        LayerStack layers = subdet.getLayering().getLayerStack();

        int layerNumber = 0;
        double layerPositionY = subdetectorThickness / 2;

        for ( Iterator i = node.getChildren( "layer" ).iterator(); i.hasNext(); )
        {
            Element layerElement = ( Element ) i.next();

            int repeat;
            try
            {
                repeat = ( int ) layerElement.getAttribute( "repeat" ).getDoubleValue();
            }
            catch ( Exception x )
            {
                throw new RuntimeException( x );
            }

            for ( int j = 0; j < repeat; j++ )
            {
                String layerName = detName + "_stave_layer" + layerNumber;

                double layerThickness = layers.getLayer( layerNumber ).getThickness();

                layerPositionY -= layerThickness / 2;

                Translation3D layer_position = new Translation3D( 0, layerPositionY, 0 );

                double layerInnerFaceLength = innerFaceLength;// -LAYER_ENVELOPE_TOLERANCE;
                double layerOuterFaceLength = outerFaceLength;// -LAYER_ENVELOPE_TOLERANCE;
                double layerRadialThickness = radialThickness;// -LAYER_ENVELOPE_TOLERANCE;

                // Layer trapezoid.
                Trd layerTrd = new Trd( layerName + "_trapezoid",
                                        layerInnerFaceLength,
                                        layerOuterFaceLength,
                                        layerThickness,
                                        layerThickness,
                                        layerRadialThickness );

                ILogicalVolume layerVolume = new LogicalVolume( layerName, layerTrd, air );

                int sliceNumber = 0;
                double slicePositionY = layerThickness / 2;
                for ( Iterator k = layerElement.getChildren( "slice" ).iterator(); k.hasNext(); )
                {
                    Element slice_element = ( Element ) k.next();

                    String sliceName = layerName + "_slice" + sliceNumber;

                    Attribute s = slice_element.getAttribute( "sensitive" );
                    boolean sensitive = false;
                    try
                    {
                        sensitive = s != null && s.getBooleanValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    double sliceThickness;
                    try
                    {
                        sliceThickness = slice_element.getAttribute( "thickness" ).getDoubleValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    // Apply tolerance factor to given slice thickness.
                    // slice_thickness -= SLICE_TOLERANCE;

                    slicePositionY -= sliceThickness / 2;

                    Translation3D slicePosition = new Translation3D( 0, slicePositionY, 0 );

                    double sliceInnerFaceLength = layerInnerFaceLength;// -
                    // SLICE_ENVELOPE_TOLERANCE;
                    double sliceOuterFaceLength = layerOuterFaceLength;// -
                    // SLICE_ENVELOPE_TOLERANCE;
                    double sliceRadialThickness = layerRadialThickness;// -
                    // SLICE_ENVELOPE_TOLERANCE;

                    Trd sliceTrd = new Trd( sliceName + "_trapezoid",
                                            sliceInnerFaceLength / 2,
                                            sliceOuterFaceLength / 2,
                                            sliceThickness / 2,
                                            sliceThickness / 2,
                                            sliceRadialThickness / 2 );

                    ILogicalVolume sliceVolume = new LogicalVolume( sliceName, sliceTrd, MaterialStore.getInstance()
                            .get( slice_element.getAttributeValue( "material" ) ) );

                    PhysicalVolume slicePhysVol = new PhysicalVolume( new Transform3D( slicePosition ),
                                                                      sliceName,
                                                                      sliceVolume,
                                                                      layerVolume,
                                                                      sliceNumber );

                    if ( sensitive )
                        slicePhysVol.setSensitive( true );

                    // The slice thickness is the original, NOT adjusted for tolerance,
                    // so that the center of the slice is in the right place with
                    // tolerance
                    // gaps on either side.
                    slicePositionY -= sliceThickness / 2;

                    // Increment the slice counter.
                    ++sliceNumber;
                }

                new PhysicalVolume( new Transform3D( layer_position ), layerName, layerVolume, sectVolume, layerNumber );

                layerPositionY -= layerThickness / 2;

                // DEBUG
                // layer_position_y -= INTER_LAYER_GAP;

                ++layerNumber;
            }
            // DEBUG - Uncomment to build only one layer.
            // break;
        }

        ILogicalVolume motherVolume = detector.getDetectorElement().getGeometry().getLogicalVolume();

        double sectCenter = rmin + radialThickness / 2;
        double sectZ = zmin + subdetectorThickness / 2;

        for ( int i = 0; i < numsides; i++ )
        {
            double phi = -( 2 * Math.PI * ( ( double ) i ) / numsides - Math.PI / 2 );
            // double phi = 2*Math.PI*((double)i)/numsides;
            double zc = -phi + Math.PI / 2;
            // double zc = -phi + Math.PI/2;
            double x = sectCenter * Math.cos( phi );
            double y = sectCenter * Math.sin( phi );

            ITranslation3D position = new Translation3D( x, y, sectZ );
            IRotation3D rotation = new RotationPassiveXYZ( Math.PI / 2, 0, zc );

            IPhysicalVolume sectPhysVol = new PhysicalVolume( new Transform3D( position, rotation ),
                                                              detName + "_stave_positive" + i,
                                                              sectVolume,
                                                              motherVolume,
                                                              i );

            new DetectorElement( sectPhysVol.getName(), endcapPositive, "/" + sectPhysVol.getName() );

            if ( reflect )
            {
                IRotation3D reflectRotation = new RotationPassiveXYZ( -Math.PI / 2, 0, zc + Math.PI );
                ITranslation3D reflectPosition = new Translation3D( x, y, -zmin - subdetectorThickness / 2 );

                IPhysicalVolume physVolReflect = new PhysicalVolume( new Transform3D( reflectPosition, reflectRotation ),
                                                                     detName + "_stave_negative" + i,
                                                                     sectVolume,
                                                                     motherVolume,
                                                                     i );

                new DetectorElement( physVolReflect.getName(), endcapNegative, "/" + physVolReflect.getName() );
            }
        }

        for ( IDetectorElement endcap : subdet.getDetectorElement().getChildren() )
        {
            // System.out.println("endcap: " + endcap.getName());
            IIdentifierHelper helper = endcap.getIdentifierHelper();
            for ( IDetectorElement module : endcap.getChildren() )
            {
                for ( IPhysicalVolume layer : module.getGeometry().getLogicalVolume().getDaughters() )
                {
                    int sensorNum = 0;
                    for ( IPhysicalVolume slice : layer.getLogicalVolume().getDaughters() )
                    {
                        if ( slice.isSensitive() )
                        {
                            IExpandedIdentifier expId = new ExpandedIdentifier( helper.getIdentifierDictionary()
                                    .getNumberOfFields() );

                            expId.setValue( helper.getFieldIndex( "system" ), sysId );

                            int endcapFlag = BarrelEndcapFlag.ENDCAP_NORTH.getFlag();
                            if ( endcap.getName().contains( "negative" ) )
                            {
                                endcapFlag = BarrelEndcapFlag.ENDCAP_SOUTH.getFlag();;
                            }
                            expId.setValue( helper.getFieldIndex( "barrel" ), endcapFlag );
                            expId.setValue( helper.getFieldIndex( "module" ), module.getGeometry().getPhysicalVolume()
                                    .getCopyNumber() );
                            expId.setValue( helper.getFieldIndex( "layer" ), layer.getCopyNumber() );
                            expId.setValue( helper.getFieldIndex( "slice" ), slice.getCopyNumber() );

                            IIdentifier id = helper.pack( expId );

                            String sliceDetElemName = endcap.getName() + "_module" + module.getGeometry()
                                    .getPhysicalVolume().getCopyNumber() + "_layer" + layer.getCopyNumber() + "_sensor" + sensorNum;

                            IDetectorElement sensor = new DetectorElement( sliceDetElemName, module, "/" + module
                                    .getName() + "/" + layer.getName() + "/" + slice.getName(), id );
                            /*
                             * IDetectorElement x = sensor;
                             * System.out.println("---print sensor---"); while (x != null)
                             * { System.out.println(x.getName()); x = x.getParent(); }
                             * System.out.println("------------");
                             */

                            ++sensorNum;
                        }
                    }
                }
            }
        }
    }

    public Class getSubdetectorType()
    {
        return PolyhedraEndcapCalorimeter.class;
    }
}
