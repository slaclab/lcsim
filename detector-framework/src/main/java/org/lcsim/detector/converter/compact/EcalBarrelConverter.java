package org.lcsim.detector.converter.compact;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationGeant;
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
import org.lcsim.geometry.layer.LayerFromCompactCnv;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.EcalBarrel;
import org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter;

/**
 * Implementation of the EcalBarrel detailed geometry.
 * 
 * @author jeremym
 */
public class EcalBarrelConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{
    public void convert( Subdetector subdet, Detector detector )
    {
        Element node = subdet.getNode();
        int sysId = subdet.getSystemID();
        PolyhedraBarrelCalorimeter cal = ( PolyhedraBarrelCalorimeter ) subdet;
        int nsides = cal.getNumberOfSides();
        double innerR = cal.getInnerRadius();
        Layering layering = cal.getLayering();
        LayerStack layers = layering.getLayerStack();
        double z = cal.getZLength();
        String name = subdet.getName();

        // Compute the delta phi per section.
        double dphiModule = PI * 2.0 / nsides;
        double hphi = dphiModule / 2;

        double moduleY1 = z;
        double moduleY2 = moduleY1;

        // Compute the total thickness of the subdetector.
        double moduleZ = 0.;
        try
        {
            moduleZ = LayerFromCompactCnv.computeDetectorTotalThickness( node );
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }

        // Compute the center Y offset of a single module.
        double moduleYOffset = innerR + moduleZ / 2;

        // Compute the outer radius.
        double outerR = innerR + moduleZ;

        // Compute trapezoid measurements.
        double bo = tan( hphi ) * outerR;
        double bi = tan( hphi ) * innerR;

        // Compute the dx per layer, using side
        // triangle calculations (from Norman Graf).
        double gamma = ( PI * 2 ) / nsides;
        double dx = moduleZ / sin( gamma );

        // The offset of a stave, derived from the dx term.
        double moduleXOffset = dx / 2.0;

        // Compute the top and bottom face measurements.
        double moduleX2 = 2 * bo - dx;
        double moduleX1 = 2 * bi + dx;

        // Create the trapezoid for the stave.
        Trd moduleTrd = new Trd( name + "_module_trd", moduleX1 / 2, // Outer side, i.e.
                                 // the "short" X
                                 // side.
                                 moduleX2 / 2, // Inner side, i.e. the "long" X side.
                                 moduleY1 / 2, // Corresponds to subdetector (or module)
                                 // Z.
                                 moduleY2 / 2, // "
                                 moduleZ / 2 ); // Thickness, in Y for top stave, when
        // rotated.

        LogicalVolume moduleVolume = new LogicalVolume( name + "_module", moduleTrd, MaterialStore.getInstance().get(
                "Air" ) );
        //
        // Build module.
        //

        // double layerZ = moduleTrd.getYHalfLength1();
        double trdZ = moduleTrd.getZHalfLength();

        //
        // Parameters for computing the layer X dimension,
        // e.g. trapezoid's X1 value.
        //

        // Adjacent angle of triangle.
        double adj = ( moduleTrd.getXHalfLength1() - moduleTrd.getXHalfLength2() ) / 2;

        // Hypotenuse of triangle.
        double hyp = sqrt( trdZ * trdZ + adj * adj );

        // Lower-right angle of triangle.
        double beta = acos( adj / hyp );

        // Primary coefficient for figuring X.
        double tan_beta = tan( beta );

        double subdetectorThickness = 0;
        try
        {
            subdetectorThickness = LayerFromCompactCnv.computeDetectorTotalThickness( node );
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }

        double layerPositionZ = -( subdetectorThickness / 2 );

        // Delta phi.
        double dphiLayer = PI * 2.0 / nsides;

        // Half delta phi.
        // double hphiLayer = dphiLayer / 2;

        // Starting X dimension for the layer.
        double layerDimX = moduleTrd.getXHalfLength1();

        int layerNumber = 0;
        for ( Iterator i = node.getChildren( "layer" ).iterator(); i.hasNext(); )
        {
            Element layer_element = ( Element ) i.next();
            int repeat = 0;
            try
            {
                repeat = ( int ) layer_element.getAttribute( "repeat" ).getDoubleValue();
            }
            catch ( Exception x )
            {
                throw new RuntimeException( x );
            }

            // Loop over number of repeats for this layer.
            for ( int j = 0; j < repeat; j++ )
            {
                // Compute this layer's thickness.
                double layerThickness = layers.getLayer( layerNumber ).getThickness();

                // Increment the Z position to place this layer.
                layerPositionZ += layerThickness / 2;

                // Name of the layer.
                String layerName = name + "_layer" + layerNumber;

                // Position of the layer.
                Translation3D layerPosition = new Translation3D( 0, 0, layerPositionZ );

                layerPositionZ += layerThickness / 2;

                // Compute the X dimension for this layer.
                double xcut = ( layerThickness / tan_beta );
                layerDimX -= xcut;

                Box layerBox = new Box( layerName + "_box", layerDimX, z / 2, layerThickness / 2 );

                LogicalVolume layerVolume = new LogicalVolume( layerName, layerBox, MaterialStore.getInstance().get(
                        "Air" ) );

                int sliceNumber = 0;
                double slicePositionZ = -( layerThickness / 2 );
                for ( Iterator k = layer_element.getChildren( "slice" ).iterator(); k.hasNext(); )
                {
                    Element sliceElement = ( Element ) k.next();

                    // Name of the slice.
                    String sliceName = layerName + "_slice" + sliceNumber;

                    // Sensitivity.
                    Attribute s = sliceElement.getAttribute( "sensitive" );
                    boolean sensitive = false;
                    try
                    {
                        sensitive = s != null && s.getBooleanValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    // Thickness of slice.
                    double sliceThickness = 0;

                    try
                    {
                        sliceThickness = sliceElement.getAttribute( "thickness" ).getDoubleValue();
                    }
                    catch ( Exception x )
                    {
                        throw new RuntimeException( x );
                    }

                    // Increment Z position of slice.
                    slicePositionZ += sliceThickness / 2;

                    Translation3D slicePosition = new Translation3D( 0, 0, slicePositionZ );

                    slicePositionZ += sliceThickness / 2;

                    Box sliceBox = new Box( sliceName + "_box", layerDimX, z / 2, sliceThickness / 2 );

                    IMaterial sliceMaterial = MaterialStore.getInstance().get(
                            sliceElement.getAttributeValue( "material" ) );

                    LogicalVolume sliceVolume = new LogicalVolume( sliceName, sliceBox, sliceMaterial );

                    PhysicalVolume slicePhysVol = new PhysicalVolume( new Transform3D( slicePosition,
                                                                                       new RotationGeant( 0, 0, 0 ) ),
                                                                      sliceName,
                                                                      sliceVolume,
                                                                      layerVolume,
                                                                      sliceNumber );
                    if ( sensitive )
                    {
                        slicePhysVol.setSensitive( true );
                    }

                    ++sliceNumber;
                }

                ITransform3D layerTransform = new Transform3D( layerPosition, new RotationGeant( 0, 0, 0 ) );

                PhysicalVolume layerPhysVol = new PhysicalVolume( layerTransform,
                                                                  layerName,
                                                                  layerVolume,
                                                                  moduleVolume,
                                                                  layerNumber );

                ++layerNumber;
            }

        }

        //
        // End build module.
        //

        // 
        // Start place modules.
        //

        // Phi start for a stave.
        double phi = 0;

        // Create DetectorElements for modules.
        for ( int i = 0; i < nsides; i++ )
        {
            int moduleNumber = i;

            RotationGeant rotation = new RotationGeant( PI * 0.5, phi, 0 );

            double modulePositionX = moduleXOffset * cos( phi ) - moduleYOffset * sin( phi );
            double modulePositionY = moduleXOffset * sin( phi ) + moduleYOffset * cos( phi );
            double modulePositionZ = 0;

            ITranslation3D trans = new Translation3D( modulePositionX, modulePositionY, modulePositionZ );
            ITransform3D transform = new Transform3D( trans, rotation );
            String moduleName = name + "_module" + i;
            new PhysicalVolume( transform, moduleName, moduleVolume, detector.getDetectorElement().getGeometry()
                    .getLogicalVolume(), moduleNumber );
            new DetectorElement( moduleName, subdet.getDetectorElement(), "/" + moduleName );

            // increment phi
            phi -= dphiLayer;
        }

        //
        // End place modules.
        //

        //
        // Start build DetectorElements.
        //
        IIdentifierHelper helper = cal.getDetectorElement().getIdentifierHelper();
        for ( IDetectorElement module : subdet.getDetectorElement().getChildren() )
        {
            int sensorNum = 0;
            for ( IPhysicalVolume layer : module.getGeometry().getLogicalVolume().getDaughters() )
            {
                IDetectorElement deLayer = new DetectorElement( name + "_module" + module.getGeometry()
                        .getPhysicalVolume().getCopyNumber() + "_layer" + layer.getCopyNumber(), module, "/" + module
                        .getGeometry().getPhysicalVolume().getName() + "/" + layer.getName() );
                for ( IPhysicalVolume slice : layer.getLogicalVolume().getDaughters() )
                {
                    if ( slice.isSensitive() )
                    {
                        IExpandedIdentifier expId = new ExpandedIdentifier( helper.getIdentifierDictionary()
                                .getNumberOfFields() );

                        expId.setValue( helper.getFieldIndex( "system" ), sysId );
                        expId.setValue( helper.getFieldIndex( "barrel" ), 0 );
                        expId.setValue( helper.getFieldIndex( "module" ), module.getGeometry().getPhysicalVolume()
                                .getCopyNumber() );
                        expId.setValue( helper.getFieldIndex( "layer" ), layer.getCopyNumber() );
                        expId.setValue( helper.getFieldIndex( "slice" ), slice.getCopyNumber() );

                        IIdentifier sensorId = helper.pack( expId );
                        String sliceName = module.getName() + "_layer" + layer.getCopyNumber() + "_sensor" + sensorNum;
                        IDetectorElement sensor = new DetectorElement( sliceName,
                                                                       deLayer,
                                                                       "/" + module.getName() + "/" + layer.getName() + "/" + slice
                                                                               .getName(),
                                                                       sensorId );
                        ++sensorNum;
                    }
                }

            }
        }
        //
        // End build DetectorElements.
        //
    }

    public Class getSubdetectorType()
    {
        return EcalBarrel.class;
    }
}
