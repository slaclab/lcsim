package org.lcsim.detector.converter.compact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.IdentifierDictionaryManager;
import org.lcsim.detector.identifier.IdentifierUtil;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.Trap;
import org.lcsim.detector.solids.Trd;
import org.lcsim.detector.solids.Tube;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.converter.SiTrackerModuleComponentParameters;
import org.lcsim.geometry.compact.converter.SiTrackerModuleParameters;
import org.lcsim.geometry.subdetector.SiTrackerEndcap;

/**
 * Converter for SiTrackerEndcap.
 * 
 * @author Jeremy McCormick, Tim Nelson
 * @version $Id: SiTrackerEndcapConverter.java,v 1.38 2011/02/25 03:09:38 jeremy Exp $
 */

public class SiTrackerEndcapConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{
    public SiTrackerEndcapConverter()
    {
    }

    public IIdentifierHelper makeIdentifierHelper( Subdetector subdetector, SystemMap systemMap )
    {
        return new SiTrackerIdentifierHelper( subdetector.getDetectorElement(),
                                              makeIdentifierDictionary( subdetector ),
                                              systemMap );
    }

    public void convert( Subdetector subdet, Detector detector )
    {
        Map< String, SiTrackerModuleParameters > moduleParameters = new HashMap< String, SiTrackerModuleParameters >();

        IPhysicalVolume trackingPV = detector.getTrackingVolume();
        ILogicalVolume trackingLV = trackingPV.getLogicalVolume();

        Element node = subdet.getNode();

        IDetectorElement endcapDE = subdet.getDetectorElement();

        DetectorIdentifierHelper helper = ( DetectorIdentifierHelper ) endcapDE.getIdentifierHelper();
        int nfields = helper.getIdentifierDictionary().getNumberOfFields();

        IDetectorElement endcapPosDE = null;
        IDetectorElement endcapNegDE = null;
        try
        {
            // Positive endcap DE
            IExpandedIdentifier endcapPosId = new ExpandedIdentifier( nfields );
            endcapPosId.setValue( helper.getFieldIndex( "system" ), subdet.getSystemID() );
            endcapPosId.setValue( helper.getFieldIndex( "barrel" ), helper.getEndcapPositiveValue() );
            endcapPosDE = new DetectorElement( subdet.getName() + "_positive", endcapDE );
            endcapPosDE.setIdentifier( helper.pack( endcapPosId ) );

            // Negative endcap DE.
            IExpandedIdentifier endcapNegId = new ExpandedIdentifier( nfields );
            endcapNegId.setValue( helper.getFieldIndex( "system" ), subdet.getSystemID() );
            endcapNegId.setValue( helper.getFieldIndex( "barrel" ), helper.getEndcapNegativeValue() );
            endcapNegDE = new DetectorElement( subdet.getName() + "_negative", endcapDE );
            endcapNegDE.setIdentifier( helper.pack( endcapNegId ) );

        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }

        // Set static module parameters.
        for ( Object n : node.getChildren( "module" ) )
        {
            Element e = ( Element ) n;
            moduleParameters.put( e.getAttributeValue( "name" ), new SiTrackerModuleParameters( e ) );
        }

        for ( Object o : node.getChildren( "layer" ) )
        {
            Element layerElement = ( Element ) o;

            int nwedges;
            try
            {
                nwedges = layerElement.getAttribute( "nwedges" ).getIntValue();
            }
            catch ( DataConversionException x )
            {
                throw new RuntimeException( x );
            }

            double innerR, outerR, innerZ, thickness;
            int layern;
            try
            {
                layern = layerElement.getAttribute( "id" ).getIntValue();
                innerR = layerElement.getAttribute( "inner_r" ).getDoubleValue();
                outerR = layerElement.getAttribute( "outer_r" ).getDoubleValue();
                innerZ = layerElement.getAttribute( "inner_z" ).getDoubleValue();
                thickness = layerElement.getAttribute( "thickness" ).getDoubleValue();
            }
            catch ( DataConversionException x )
            {
                throw new RuntimeException( x );
            }

            ILogicalVolume layerLV = makeLayer(
                    detector,
                    subdet,
                    innerR,
                    outerR,
                    thickness,
                    nwedges,
                    layerElement,
                    moduleParameters );

            double layerZ = innerZ + thickness / 2;

            // Positive endcap layer.
            ITransform3D layerTrans = new Transform3D( new Translation3D( 0, 0, layerZ ) );
            String layerName = layerLV.getName() + "_positive";
            new PhysicalVolume( layerTrans, layerName, layerLV, trackingLV, layern );
            String layerPath = "/" + trackingPV.getName() + "/" + layerName;
            IDetectorElement layerDE = new DetectorElement( layerLV.getName(), endcapPosDE, detector.getNavigator()
                    .getPath( layerPath ) );

            // Negative endcap layer.
            ITransform3D layerTransReflect = new Transform3D( new Translation3D( 0, 0, -layerZ ),
                                                              new RotationPassiveXYZ( 0, Math.PI, 0 ) );
            String layerNameReflect = layerLV.getName() + "_negative";
            new PhysicalVolume( layerTransReflect, layerNameReflect, layerLV, trackingLV, layern );
            String layerPathReflect = "/" + trackingPV.getName() + "/" + layerNameReflect;
            IDetectorElement layerDEReflect = new DetectorElement( layerNameReflect, endcapNegDE, detector
                    .getNavigator().getPath( layerPathReflect ) );

            // Wedge DE.
            for ( IPhysicalVolume wedge : layerLV.getDaughters() )
            {
                // Positive endcap.
                String wedgePath = layerPath + "/" + wedge.getName();
                String wedgeName = layerName + "_wedge" + wedge.getCopyNumber();
                IDetectorElement wedgeDE = new DetectorElement( wedgeName, layerDE, detector.getNavigator().getPath(
                        wedgePath ) );

                // Negative endcap.
                String wedgePathReflect = layerPathReflect + "/" + wedge.getName();
                String wedgeNameReflect = layerNameReflect + "_wedge" + wedge.getCopyNumber();
                IDetectorElement wedgeDEReflect = new DetectorElement( wedgeNameReflect, layerDEReflect, detector
                        .getNavigator().getPath( wedgePathReflect ) );

                // Module DEs.
                for ( IPhysicalVolume module : wedge.getLogicalVolume().getDaughters() )
                {
                    // Positive endcap module.
                    String modulePath = wedgePath + "/" + module.getName();
                    String moduleName = wedgeName + "_module" + module.getCopyNumber();
                    IDetectorElement moduleDE = new SiTrackerModule( moduleName, wedgeDE, detector.getNavigator()
                            .getPath( modulePath ).toString(), module.getCopyNumber() );

                    // Negative endcap module.
                    String modulePathReflect = wedgePathReflect + "/" + module.getName();
                    String moduleNameReflect = wedgeNameReflect + "_module" + module.getCopyNumber();
                    IDetectorElement moduleDEReflect = new SiTrackerModule( moduleNameReflect, wedgeDEReflect, detector
                            .getNavigator().getPath( modulePathReflect ).toString(), module.getCopyNumber() );
                }
            }
        }

        try
        {
            setupSensorDetectorElements( subdet );
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }
    }

    private ILogicalVolume makeWedge( Detector detector,
            Subdetector subdet,
            double innerR,
            double outerR,
            double thickness,
            int nwedges,
            int layern )
    {
        IMaterial material = detector.getTrackingVolume().getLogicalVolume().getMaterial();

        String name = subdet.getName() + "_layer" + layern + "_wedge";

        double wedge_margin = 0.0001;
        // double wedge_margin = 2.0;

        double dz = ( outerR - innerR ) / 2;
        double dy1, dy2;
        dy1 = dy2 = thickness / 2;
        double dx1, dx2;
        double dphi = Math.PI / nwedges;
        dx1 = innerR * Math.tan( dphi ) - wedge_margin / Math.cos( dphi );
        dx2 = outerR * Math.tan( dphi ) - wedge_margin / Math.cos( dphi );

        Trd wedgeTrd = new Trd( name, dx1, dx2, dy1, dy2, dz );

        ILogicalVolume wedgeLV = new LogicalVolume( name, wedgeTrd, material );

        return wedgeLV;
    }

    private ILogicalVolume makeLayer( Detector detector,
            Subdetector subdet,
            double innerR,
            double outerR,
            double thickness,
            int nwedges,
            Element layerElement,
            Map< String, SiTrackerModuleParameters > moduleParameters )
    {
        int layern;
        try
        {
            layern = layerElement.getAttribute( "id" ).getIntValue();
        }
        catch ( DataConversionException x )
        {
            throw new RuntimeException( x );
        }

        IMaterial material = detector.getTrackingVolume().getLogicalVolume().getMaterial();
        String layerName = subdet.getName() + "_layer" + layern;

        double dphi = Math.PI / nwedges;

        double tubeInnerR, tubeOuterR;
        tubeInnerR = innerR;
        tubeOuterR = outerR / Math.cos( dphi );

        Tube layerTube = new Tube( layerName, tubeInnerR, tubeOuterR, thickness / 2 );

        // Make the layer LV.
        ILogicalVolume layerLV = new LogicalVolume( layerName, layerTube, material );

        // Make the wedge LV.
        ILogicalVolume wedgeLV = makeWedge( detector, subdet, innerR, outerR, thickness, nwedges, layern );

        Attribute moduleref = layerElement.getAttribute( "module" );

        if ( moduleref == null )
            throw new RuntimeException( "module reference is missing for layer number " + layern );

        SiTrackerModuleParameters module = moduleParameters.get( moduleref.getValue() );

        // Make the modules in the wedge.
        makeModules( subdet, wedgeLV, layerElement.getChild( "module_parameters" ), module, layern );

        // Place the wedges in the layer.
        double r = ( innerR + outerR ) / 2;

        for ( int i = 0; i < nwedges; i++ )
        {
            double phi = i * 2 * Math.PI / nwedges;
            double x = r * Math.cos( phi );
            double y = r * Math.sin( phi );

            Translation3D p = new Translation3D( x, y, 0 );
            IRotation3D rot = new RotationGeant( -Math.PI / 2, -Math.PI / 2 - phi, 0. );
            Transform3D transform = new Transform3D( p, rot );
            new PhysicalVolume( transform, "wedge" + i, wedgeLV, layerLV, i );
        }

        return layerLV;
    }

    private void makeModules( Subdetector subdet,
            ILogicalVolume wedgeLV,
            Element moduleElement,
            SiTrackerModuleParameters module,
            int layern )
    {
        double r_size;
        try
        {
            r_size = moduleElement.getAttribute( "r_size" ).getDoubleValue();
        }
        catch ( DataConversionException x )
        {
            throw new RuntimeException( x );
        }

        double phi_size_max;
        try
        {
            phi_size_max = moduleElement.getAttribute( "phi_size_max" ).getDoubleValue();
        }
        catch ( DataConversionException x )
        {
            throw new RuntimeException( x );
        }

        Trd wedgeTrd = ( Trd ) wedgeLV.getSolid();
        double dz = ( ( Trd ) wedgeLV.getSolid() ).getZHalfLength();
        double dx1 = wedgeTrd.getXHalfLength1();
        double dx2 = wedgeTrd.getXHalfLength2();
        double dy = wedgeTrd.getYHalfLength1();
        double deltax = dx2 - dx1;
        double side_slope = deltax / ( 2 * dz );

        double module_margin = 0.0001;
        // double module_margin = 1.0;
        double box_margin = module_margin;
        double side_angle = Math.atan( side_slope );
        double trap_margin_1 = module_margin * ( 1.0 / Math.cos( side_angle ) - Math.sin( side_angle ) );
        double trap_margin_2 = module_margin * ( 1.0 / Math.cos( side_angle ) + Math.sin( side_angle ) );

        List< Double > zcenters = new ArrayList< Double >();
        List< Double > zsizes = new ArrayList< Double >();
        List< Double > xsizes1 = new ArrayList< Double >();
        List< Double > xsizes2 = new ArrayList< Double >();

        {
            double zcurr = dz;
            while ( zcurr - r_size > -dz )
            {
                double zmax = zcurr;
                double zmin = zcurr - r_size;
                zcenters.add( ( zmin + zmax ) / 2 );
                zsizes.add( ( zmax - zmin ) / 2 );

                double xsize1 = dx1 + side_slope * ( zmin + dz );
                double xsize2 = dx1 + side_slope * ( zmax + dz );

                xsizes1.add( xsize1 );
                xsizes2.add( xsize2 );

                zcurr -= r_size;
            }
            double zmax = zcurr;
            double zmin = -dz;
            zcenters.add( ( zmin + zmax ) / 2 );
            zsizes.add( ( zmax - zmin ) / 2 );
            double xsize1 = dx1 + side_slope * ( zmin + dz );
            double xsize2 = dx1 + side_slope * ( zmax + dz );
            xsizes1.add( xsize1 );
            xsizes2.add( xsize2 );
        }

        IMaterial sliceMaterial = wedgeLV.getMaterial();

        double zsize_last = 0.0;
        double xsize1_min = phi_size_max / 2;
        double xsize_box = 0.0;
        int nboxes = 0;

        int imodule = 0;

        for ( int i = zcenters.size() - 1; i >= 0; i-- )
        {

            if ( zsizes.get( i ) != zsize_last )
            {
                zsize_last = zsizes.get( i );
                xsize1_min = phi_size_max / 2;
                xsize_box = 0.0;
                nboxes = 0;
            }

            int ntraps = ( int ) Math.ceil( 2 * ( xsizes1.get( i ) - nboxes * xsize_box ) / phi_size_max );

            // Squares to fill extra space
            if ( ntraps > 2 )
            {
                double delta_x = xsizes2.get( i ) - xsizes1.get( i );

                if ( phi_size_max > delta_x )
                {
                    xsize_box = delta_x * ( int ) Math.floor( phi_size_max / delta_x );
                }
                else
                {
                    xsize_box = delta_x / ( int ) Math.floor( delta_x / phi_size_max );
                }

                if ( xsize_box > 0.0 )
                {
                    nboxes = ( int ) Math.floor( ( xsizes1.get( i ) - 2 * xsize1_min ) / xsize_box );
                }
                ntraps = 2;
            }

            double xmin = -nboxes * xsize_box;
            double xmax = xmin + 2 * xsize_box;

            for ( int ibox = 0; ibox < nboxes; ibox++ )
            {

                double xcenter = ( xmin + xmax ) / 2;
                xmin += 2 * xsize_box;
                xmax += 2 * xsize_box;

                String sliceName = "module" + imodule;

                Box sliceBox = new Box( sliceName, xsize_box - box_margin, dy, zsizes.get( i ) - box_margin );

                ILogicalVolume sliceLV = new LogicalVolume( sliceName, sliceBox, sliceMaterial );

                // Build the module substructure.
                makeBoxModule( sliceLV, module, imodule );

                Transform3D trans = new Transform3D( new Translation3D( xcenter, 0, zcenters.get( i ) ) );

                new PhysicalVolume( trans, sliceName, sliceLV, wedgeLV, imodule );

                imodule++;
            }

            // Small symmetric trapezoids
            if ( ntraps == 1 )
            {
                String sliceName = "module" + imodule;

                Trd sliceTrd = new Trd( sliceName,
                                        xsizes1.get( i ) - trap_margin_1,
                                        xsizes2.get( i ) - trap_margin_2,
                                        dy,
                                        dy,
                                        zsizes.get( i ) - box_margin );

                ILogicalVolume sliceLV = new LogicalVolume( sliceName, sliceTrd, sliceMaterial );

                // Build the module substructure.
                makeTrdModule( sliceLV, module, imodule );

                Transform3D trans = new Transform3D( new Translation3D( 0, 0, zcenters.get( i ) ) );

                new PhysicalVolume( trans, sliceName, sliceLV, wedgeLV, imodule );

                imodule++;
            }

            // Split trapezoids
            if ( ntraps == 2 )
            {

                double xoffset = xsize_box * nboxes;

                double average_margin_1 = ( box_margin + trap_margin_1 ) / 2;
                double average_margin_2 = ( box_margin + trap_margin_2 ) / 2;

                double xsize1 = ( xsizes1.get( i ) - xoffset ) / ntraps - average_margin_1;
                if ( xsize1_min == 0.0 )
                    xsize1_min = xsize1;
                double xsize2 = ( xsizes2.get( i ) - xoffset ) / ntraps - average_margin_2;

                double xcenter = ( xsize1 + xsize2 ) / 2 + xoffset + box_margin;
                double theta = Math.abs( Math.atan( side_slope / 2 ) );

                for ( int ix = -1; ix <= 1; ix = ix + 2 )
                {

                    String sliceName = "module" + imodule;

                    Trap sliceTrap = new Trap( sliceName,
                                               zsizes.get( i ) - box_margin,
                                               theta * ix,
                                               0.0,
                                               dy,
                                               xsize1,
                                               xsize1,
                                               0.0,
                                               dy,
                                               xsize2,
                                               xsize2,
                                               0.0 );

                    ILogicalVolume sliceLV = new LogicalVolume( sliceName, sliceTrap, sliceMaterial );

                    makeTrapModule( sliceLV, module, imodule );

                    Transform3D trans = new Transform3D( new Translation3D( ix * xcenter, 0, zcenters.get( i ) ) );

                    new PhysicalVolume( trans, sliceName, sliceLV, wedgeLV, imodule );

                    imodule++;
                }
            }
        }
    }

    public void makeBoxModule( ILogicalVolume module, SiTrackerModuleParameters moduleParameters, int moduleTypeId )
    {
        Box moduleBox = ( Box ) module.getSolid();
        double posY = -moduleBox.getYHalfLength();
        double moduleX = moduleBox.getXHalfLength();
        double moduleZ = moduleBox.getZHalfLength();

        // pull corners in by 0.5 microns to eliminate overlaps.
        // moduleX -= 0.0005;
        // moduleZ -= 0.0005;

        for ( SiTrackerModuleComponentParameters component : moduleParameters )
        {
            double thickness = component.getThickness();
            IMaterial material = MaterialStore.getInstance().get( component.getMaterialName() );
            boolean sensitive = component.isSensitive();
            int componentNumber = component.getComponentNumber();

            posY += thickness / 2;

            String componentName = "module" + moduleTypeId + "_component" + componentNumber;

            Box sliceBox = new Box( componentName + "_box", moduleX, thickness / 2, moduleZ );

            ILogicalVolume volume = new LogicalVolume( componentName, sliceBox, material );

            Transform3D trans = new Transform3D( new Translation3D( 0, posY, 0 ) );

            PhysicalVolume pv = new PhysicalVolume( trans, componentName, volume, module, componentNumber );
            pv.setSensitive( sensitive );

            posY += thickness / 2;
        }
    }

    public void makeTrdModule( ILogicalVolume module, SiTrackerModuleParameters moduleParameters, int moduleTypeId )
    {
        Trd trd = ( Trd ) module.getSolid();
        double x1 = trd.getXHalfLength1();
        double x2 = trd.getXHalfLength2();
        double y1 = trd.getYHalfLength1();
        double z = trd.getZHalfLength();

        // pull corners in by 0.5 microns to eliminate overlaps.
        // x1 -= 0.0005;
        // x2 -= 0.0005;
        // z -= 0.0005;

        double posY = -y1;

        for ( SiTrackerModuleComponentParameters component : moduleParameters )
        {
            double thickness = component.getThickness();
            IMaterial material = MaterialStore.getInstance().get( component.getMaterialName() );
            boolean sensitive = component.isSensitive();
            int componentNumber = component.getComponentNumber();

            posY += thickness / 2;

            String componentName = "module" + moduleTypeId + "_component" + componentNumber;

            Trd sliceTrd = new Trd( componentName, x1, x2, thickness / 2, thickness / 2, z );

            ILogicalVolume volume = new LogicalVolume( componentName, sliceTrd, material );

            Transform3D trans = new Transform3D( new Translation3D( 0, posY, 0 ) );

            PhysicalVolume pv = new PhysicalVolume( trans, componentName, volume, module, componentNumber );
            pv.setSensitive( sensitive );

            posY += thickness / 2;
        }
    }

    public void makeTrapModule( ILogicalVolume module, SiTrackerModuleParameters moduleParameters, int moduleTypeId )
    {
        Trap trap = ( Trap ) module.getSolid();
        double a1 = trap.getAlpha1();
        double a2 = trap.getAlpha2();
        double x1 = trap.getXHalfLength1();
        double x2 = trap.getXHalfLength2();
        double x3 = trap.getXHalfLength3();
        double x4 = trap.getXHalfLength4();
        double y1 = trap.getYHalfLength1();
        double z = trap.getZHalfLength();
        double theta = trap.getTheta();
        double phi = trap.getPhi();
        double posY = -y1;

        // pull corners in by 0.5 microns to eliminate overlaps.
        // x1 -= 0.0005;
        // x2 -= 0.0005;
        // x3 -= 0.0005;
        // x4 -= 0.0005;
        // z -= 0.0005;

        for ( SiTrackerModuleComponentParameters component : moduleParameters )
        {
            double thickness = component.getThickness();
            IMaterial material = MaterialStore.getInstance().get( component.getMaterialName() );
            boolean sensitive = component.isSensitive();
            int componentNumber = component.getComponentNumber();

            posY += thickness / 2;

            String componentName = "module" + moduleTypeId + "_component" + componentNumber;

            Trap sliceTrap = new Trap( componentName,
                                       z,
                                       theta,
                                       phi,
                                       thickness / 2,
                                       x1,
                                       x2,
                                       a1,
                                       thickness / 2,
                                       x3,
                                       x4,
                                       a2 );

            ILogicalVolume volume = new LogicalVolume( componentName, sliceTrap, material );

            Transform3D trans = new Transform3D( new Translation3D( 0, posY, 0 ) );

            PhysicalVolume pv = new PhysicalVolume( trans, componentName, volume, module, componentNumber );
            pv.setSensitive( sensitive );

            posY += thickness / 2;
        }
    }

    public Class getSubdetectorType()
    {
        return SiTrackerEndcap.class;
    }

    // TODO: Need to have SiTrackerIdentifierHelper available before this is called.
    private void setupSensorDetectorElements( Subdetector subdet ) throws Exception
    {
        SiTrackerIdentifierHelper id_helper = ( SiTrackerIdentifierHelper ) subdet.getDetectorElement()
                .getIdentifierHelper();

        if ( id_helper == null )
            throw new RuntimeException( "helper is null!!!!!!!!!" );

        int moduleId = 0;

        for ( IDetectorElement endcap : subdet.getDetectorElement().getChildren() )
        {
            for ( IDetectorElement layer : endcap.getChildren() )
            {
                int nwedges = layer.getChildren().size();
                for ( IDetectorElement wedge : layer.getChildren() )
                {
                    for ( IDetectorElement module : wedge.getChildren() )
                    {
                        IPhysicalVolumePath modulePath = module.getGeometry().getPath();

                        IPhysicalVolume modulePhysVol = modulePath.getLeafVolume();

                        int sensorId = 0;
                        for ( IPhysicalVolume pv : modulePhysVol.getLogicalVolume().getDaughters() )
                        {
                            // Create the identifier for this sensor.
                            if ( pv.isSensitive() )
                            {
                                IIdentifierDictionary iddict = IdentifierDictionaryManager.getInstance()
                                        .getIdentifierDictionary( subdet.getReadout().getName() );

                                ExpandedIdentifier expId = new ExpandedIdentifier( iddict.getNumberOfFields() );

                                // Set the System ID.
                                expId.setValue( iddict.getFieldIndex( "system" ), subdet.getSystemID() );

                                // Set the barrel-endcap flag.
                                if ( id_helper.isEndcapPositive( endcap.getIdentifier() ) )
                                {
                                    expId.setValue( iddict.getFieldIndex( "barrel" ), id_helper
                                            .getEndcapPositiveValue() );
                                }
                                else if ( id_helper.isEndcapNegative( endcap.getIdentifier() ) )
                                {
                                    expId.setValue( iddict.getFieldIndex( "barrel" ), id_helper
                                            .getEndcapNegativeValue() );
                                }
                                else
                                {
                                    throw new RuntimeException( endcap.getName() + " - not pos or neg endcap!" );
                                }

                                // System.out.println("barrel flag set to " +
                                // expId.getValue(iddict.getFieldIndex("barrel")));

                                // Set the layer number.
                                expId.setValue( iddict.getFieldIndex( "layer" ), layer.getGeometry().getPath()
                                        .getLeafVolume().getCopyNumber() );

                                // Set the wedge number.
                                expId.setValue( iddict.getFieldIndex( "wedge" ), wedge.getGeometry().getPath()
                                        .getLeafVolume().getCopyNumber() );

                                // Set the module id from the DetectorElement.
                                expId.setValue( iddict.getFieldIndex( "module" ), ( ( SiTrackerModule ) module )
                                        .getModuleId() );

                                // Set the sensor id for double-sided.
                                expId.setValue( iddict.getFieldIndex( "sensor" ), sensorId );

                                // Create the packed id using util method.
                                // No IdentifierHelper is available yet.
                                IIdentifier id = iddict.pack( expId );

                                String sensorPath = modulePath.toString() + "/" + pv.getName();
                                String sensorName = endcap.getName() + "_layer" + layer.getGeometry()
                                        .getPhysicalVolume().getCopyNumber() + "_wedge" + wedge.getGeometry().getPath()
                                        .getLeafVolume().getCopyNumber() + "_module" + ( ( SiTrackerModule ) module )
                                        .getModuleId() + "_sensor" + sensorId;

                                // System.out.println(sensorName + " -> " + expId);
                                // System.out.println(sensorName + " -> " + id);
                                // System.out.println();

                                SiSensor sensor = new SiSensor( sensorId, sensorName, module, sensorPath, id );

                                /*
                                 * // Set up SiStrips for the sensors IPolyhedron
                                 * sensor_solid =
                                 * (IPolyhedron)sensor.getGeometry().getLogicalVolume
                                 * ().getSolid();
                                 * 
                                 * Polygon3D inner_surface =
                                 * sensor_solid.getFacesNormalTo(new
                                 * BasicHep3Vector(0,-1,0)).get(0); Polygon3D
                                 * outer_surface = sensor_solid.getFacesNormalTo(new
                                 * BasicHep3Vector(0,1,0)).get(0); // double
                                 * strip_angle_magnitude = Math.PI/nwedges;
                                 * 
                                 * Polygon3D p_side; Polygon3D n_side; int side;
                                 * 
                                 * if (sensorId == 0) // inner sensor { p_side =
                                 * inner_surface; n_side = outer_surface; side = 1; //
                                 * strip_angle = strip_angle_magnitude; } else // outer
                                 * sensor { p_side = outer_surface; n_side =
                                 * inner_surface; side = -1; // strip_angle =
                                 * -1*strip_angle_magnitude; }
                                 * 
                                 * 
                                 * 
                                 * 
                                 * //System.out.println("Plane of p_side polygon has... ");
                                 * /
                                 * /System.out.println("                        normal: "+
                                 * p_side.getNormal());
                                 * //System.out.println("                        distance: "
                                 * +p_side.getDistance()); //for (Point3D point :
                                 * p_side.getVertices()) //{ //
                                 * System.out.println("      Vertex: "+point); //}
                                 * 
                                 * 
                                 * 
                                 * 
                                 * //System.out.println("Plane of n_side polygon has... ");
                                 * /
                                 * /System.out.println("                        normal: "+
                                 * n_side.getNormal());
                                 * //System.out.println("                        distance: "
                                 * +n_side.getDistance());
                                 * 
                                 * // Bias the sensor
                                 * sensor.setBiasSurface(ChargeCarrier.HOLE,p_side);
                                 * sensor.setBiasSurface(ChargeCarrier.ELECTRON,n_side);
                                 * 
                                 * double strip_angle = Math.PI/nwedges;
                                 * 
                                 * 
                                 * 
                                 * 
                                 * //System.out.println("                        side = : "
                                 * +side);
                                 * 
                                 * ITranslation3D electrodes_position = new
                                 * Translation3D(VecOp.mult(-p_side.getDistance(),new
                                 * BasicHep3Vector(0,0,1))); // translate to outside of
                                 * polygon IRotation3D electrodes_rotation = new
                                 * RotationPassiveXYZ(side*(Math.PI/2),0,strip_angle); //
                                 * Transform3D electrodes_transform = new
                                 * Transform3D(electrodes_position, electrodes_rotation);
                                 * 
                                 * // Free calculation of readout electrodes, sense
                                 * electrodes determined thereon SiSensorElectrodes
                                 * readout_electrodes = new
                                 * SiStrips(ChargeCarrier.HOLE,0.050
                                 * ,sensor,electrodes_transform); SiSensorElectrodes
                                 * sense_electrodes = new
                                 * SiStrips(ChargeCarrier.HOLE,0.025
                                 * ,(readout_electrodes.getNCells
                                 * ()*2-1),sensor,electrodes_transform);
                                 * 
                                 * sensor.setSenseElectrodes(sense_electrodes);
                                 * sensor.setReadoutElectrodes(readout_electrodes);
                                 * 
                                 * double[][] transfer_efficiencies = { {0.986,0.419} };
                                 * sensor.setTransferEfficiencies(ChargeCarrier.HOLE,new
                                 * BasicMatrix(transfer_efficiencies));
                                 */

                                // Increment sensorID for double-sided.
                                ++sensorId;
                            }
                        }
                        ++moduleId;
                    }
                }
            }
        }
    }

    /*
     * public static class ModuleComponentParameters { String materialName; double
     * thickness; boolean sensitive; int componentNumber;
     * 
     * public ModuleComponentParameters(double thickness, String materialName, int
     * componentNumber, boolean sensitive) { this.thickness = thickness; this.materialName
     * = materialName; this.sensitive = sensitive; this.componentNumber = componentNumber;
     * }
     * 
     * public double getThickness() { return thickness; }
     * 
     * public String getMaterialName() { return materialName; }
     * 
     * public boolean isSensitive() { return sensitive; }
     * 
     * public int getComponentNumber() { return componentNumber; } }
     * 
     * public static class ModuleParameters extends ArrayList<ModuleComponentParameters> {
     * double thickness=0.; String name; public ModuleParameters(Element element) { name =
     * element.getAttributeValue("name"); int cntr=0; for (Object o :
     * element.getChildren("module_component")) { try {
     * 
     * Element e = (Element)o;
     * 
     * double thickness = e.getAttribute("thickness").getDoubleValue();
     * 
     * String materialName = e.getAttributeValue("material");
     * 
     * boolean sensitive = false; if (e.getAttribute("sensitive") != null) sensitive =
     * e.getAttribute("sensitive").getBooleanValue(); add(new
     * ModuleComponentParameters(thickness, materialName, cntr, sensitive)); } catch
     * (JDOMException x) { throw new RuntimeException(x); } ++cntr; }
     * calculateThickness(); }
     * 
     * public void calculateThickness() { thickness = 0.; // reset thickness for
     * (ModuleComponentParameters p : this) { thickness += p.getThickness(); } }
     * 
     * public double getThickness() { return thickness; } }
     */
}
