package org.lcsim.detector.converter.compact;

import java.util.Iterator;

import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.RightRegularPolyhedron;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.subdetector.PolyhedraEndcapCalorimeter2;

public class PolyhedraEndcapCalorimeter2Converter extends AbstractSubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        Element node = subdet.getNode();

        PolyhedraEndcapCalorimeter2 polyEndcap = ( PolyhedraEndcapCalorimeter2 ) subdet;

        Layering layering = subdet.getLayering();
        double thickness = layering.getThickness();

        IMaterial air = MaterialStore.getInstance().get( "Air" );

        RightRegularPolyhedron subdetEnvelopePoly = new RightRegularPolyhedron( subdet.getName() + "_envelope_poly",
                                                                                polyEndcap.getNumberOfSides(),
                                                                                polyEndcap.getInnerRadius(),
                                                                                polyEndcap.getOuterRadius(),
                                                                                -thickness / 2,
                                                                                thickness / 2 );

        ILogicalVolume motherVolume = detector.getDetectorElement().getGeometry().getLogicalVolume();

        double posZ = polyEndcap.getInnerZ() + subdet.getLayering().getThickness() / 2;

        String envelopeName = subdet.getName() + "_envelope";

        // Endcap envelope volume.
        ILogicalVolume subdetLv = new LogicalVolume( envelopeName, subdetEnvelopePoly, air );

        // Positive endcap envelope placement.
        IPhysicalVolume envelopePv = new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, posZ ) ),
                                                         envelopeName + "_pos",
                                                         subdetLv,
                                                         motherVolume,
                                                         0 );

        // Positive endcap DetectorElement.
        new DetectorElement( envelopePv.getName(), subdet.getDetectorElement(), "/" + envelopePv.getName() );

        // Negative endcap envelope placement.
        IPhysicalVolume envelopePvNeg = new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, -posZ ),
                                                                             new RotationPassiveXYZ( 0., Math.PI, 0. ) ),
                                                            envelopeName + "_neg",
                                                            subdetLv,
                                                            motherVolume,
                                                            0 );

        // Negative endcap DetectorElement.
        new DetectorElement( envelopePvNeg.getName(), subdet.getDetectorElement(), "/" + envelopePvNeg.getName() );

        double layerZPosition = -subdet.getLayering().getThickness() / 2;

        int layerNumber = 0;

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
                // Get the layer from the layering engine.
                Layer layer = layering.getLayer( layerNumber );

                // Layer thickness.
                double layerThickness = layer.getThickness();

                // System.out.println("layerThickness = " + layerThickness);

                String layerName = subdet.getName() + "_layer" + layerNumber;

                RightRegularPolyhedron layerPoly = new RightRegularPolyhedron( layerName + "_poly",
                                                                               polyEndcap.getNumberOfSides(),
                                                                               polyEndcap.getInnerRadius(),
                                                                               polyEndcap.getOuterRadius(),
                                                                               -layerThickness / 2,
                                                                               layerThickness / 2 );

                LogicalVolume layerLv = new LogicalVolume( layerName, layerPoly, air );

                layerZPosition += layerThickness / 2;

                // Layer placement.
                new PhysicalVolume( new Transform3D( new Translation3D( 0, 0, layerZPosition ) ),
                                    subdet.getName() + "_layer" + layerNumber,
                                    layerLv,
                                    subdetLv,
                                    layerNumber );

                layerZPosition += layerThickness / 2;

                /*
                 * IDetectorElement layerDe = new DetectorElement( subdet.getName() +
                 * "_layer" + layerNumber, subdet.getDetectorElement(), "/" +
                 * envelopePv.getName() + "/" + layerPv.getName());
                 */

                int sliceNumber = 0;
                double sliceZPosition = -layerThickness / 2;
                for ( Iterator k = layerElement.getChildren( "slice" ).iterator(); k.hasNext(); )
                {
                    Element sliceElement = ( Element ) k.next();

                    LayerSlice slice = layer.getSlice( sliceNumber );
                    double sliceThickness = slice.getThickness();

                    String sliceName = layerName + "_slice" + sliceNumber;

                    RightRegularPolyhedron slicePoly = new RightRegularPolyhedron( sliceName + "_poly",
                                                                                   polyEndcap.getNumberOfSides(),
                                                                                   polyEndcap.getInnerRadius(),
                                                                                   polyEndcap.getOuterRadius(),
                                                                                   -sliceThickness / 2,
                                                                                   sliceThickness / 2 );

                    LogicalVolume sliceLv = new LogicalVolume( sliceName + "_poly", slicePoly, MaterialStore
                            .getInstance().get( slice.getMaterial().getName() ) );

                    sliceZPosition += sliceThickness / 2;

                    PhysicalVolume slicePv = new PhysicalVolume( new Transform3D( new Translation3D( 0,
                                                                                                     0,
                                                                                                     sliceZPosition ) ),
                                                                 sliceName,
                                                                 sliceLv,
                                                                 layerLv,
                                                                 sliceNumber );

                    if ( slice.isSensitive() )
                        slicePv.setSensitive( true );

                    sliceZPosition += sliceThickness / 2;

                    /*
                     * new DetectorElement( sliceName, layerDe, "/" + envelopePv.getName()
                     * + "/" + layerPv.getName() + "/" + slicePv.getName());
                     */

                    ++sliceNumber;
                }

                ++layerNumber;
            }
        }

        // Get the necessary identifier information before making sensor DetectorElements.
        IIdentifierHelper helper = subdet.getDetectorElement().getIdentifierHelper();
        IIdentifierDictionary dict = helper.getIdentifierDictionary();
        int fcount = dict.getNumberOfFields();
        int systemIdx = dict.getFieldIndex( "system" );
        int barrelIdx = dict.getFieldIndex( "barrel" );
        int layerIdx = dict.getFieldIndex( "layer" );
        int sliceIdx = dict.getFieldIndex( "slice" );

        // Setup sensor DetectorElements now that geometry is constructed.
        for ( IDetectorElement endcap : subdet.getDetectorElement().getChildren() )
        {
            for ( IPhysicalVolume layer : endcap.getGeometry().getLogicalVolume().getDaughters() )
            {
                for ( IPhysicalVolume slice : layer.getLogicalVolume().getDaughters() )
                {
                    if ( slice.isSensitive() )
                    {
                        int endcapFlag = BarrelEndcapFlag.ENDCAP_NORTH.getFlag();
                        if ( endcap.getGeometry().getPosition().z() < 0 )
                        {
                            endcapFlag = BarrelEndcapFlag.ENDCAP_SOUTH.getFlag();
                        }

                        // Make an identifier for the sensor.
                        ExpandedIdentifier expId = new ExpandedIdentifier( fcount );
                        expId.setValue( systemIdx, subdet.getSystemID() );
                        expId.setValue( barrelIdx, endcapFlag );
                        expId.setValue( layerIdx, layer.getCopyNumber() );
                        expId.setValue( sliceIdx, slice.getCopyNumber() );

                        // Make a DetectorElement for the sensor.
                        new DetectorElement( slice.getName(), endcap, "/" + endcap.getGeometry().getPhysicalVolume()
                                .getName() + "/" + layer.getName() + "/" + slice.getName(), helper.pack( expId ) );

                    }
                }
            }
        }
    }

    public Class getSubdetectorType()
    {
        return PolyhedraEndcapCalorimeter2.class;
    }
}
