package org.lcsim.detector.converter.compact;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.IdentifierDictionaryManager;
import org.lcsim.detector.identifier.IdentifierUtil;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.Trd;
import org.lcsim.detector.solids.Tube;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.detector.tracker.silicon.SiTrackerLayer;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;

public class SiTrackerFixedTarget2ConverterBarrel extends AbstractSubdetectorConverter implements ISubdetectorConverter
{

    public IIdentifierHelper makeIdentifierHelper( Subdetector subdetector, SystemMap systemMap )
    {
        return new SiTrackerIdentifierHelper( subdetector.getDetectorElement(),
                                              makeIdentifierDictionary( subdetector ),
                                              systemMap );
    }

    public void convert( Subdetector subdet, Detector detector )
    {
        Map< String, ILogicalVolume > modules = buildModules( subdet );

        try
        {
            buildLayers( detector, subdet, modules );
        }
        catch ( DataConversionException x )
        {
            throw new RuntimeException( x );
        }

        setupSensorDetectorElements( subdet );
    }

    private Map< String, ILogicalVolume > buildModules( Subdetector subdet )
    {
        Map< String, ILogicalVolume > modules = new HashMap< String, ILogicalVolume >();

        Element subdetElement = subdet.getNode();

        for ( Iterator i = subdetElement.getChildren( "module" ).iterator(); i.hasNext(); )
        {
            Element module = ( Element ) i.next();
            String module_name = module.getAttributeValue( "name" );
            ILogicalVolume module_envelope;
            try
            {
                module_envelope = buildModule( subdetElement, module_name );
                modules.put( module_name, module_envelope );
            }
            catch ( Exception x )
            {
                throw new RuntimeException( x );
            }
        }

        return modules;
    }

    private ILogicalVolume buildModule( Element subdetElement, String module_name ) throws Exception
    {
        String subdetName = subdetElement.getAttributeValue( "name" );
        ILogicalVolume moduleLogVol = null;

        Element moduleElement = null;
        for ( Iterator i = subdetElement.getChildren( "module" ).iterator(); i.hasNext(); )
        {
            Element moduleCheck = ( Element ) i.next();
            if ( moduleCheck.getAttributeValue( "name" ).compareTo( module_name ) == 0 )
            {
                moduleElement = moduleCheck;
            }
        }
        if ( moduleElement == null )
        {
            throw new RuntimeException( "module <" + module_name + " was not found" );
        }

        Element moduleEnvelopeElement = moduleElement.getChild( "module_envelope" );

        // Create the module box.
        double moduleLength = moduleEnvelopeElement.getAttribute( "length" ).getDoubleValue();
        double moduleWidth = moduleEnvelopeElement.getAttribute( "width" ).getDoubleValue();
        double moduleThickness = moduleEnvelopeElement.getAttribute( "thickness" ).getDoubleValue();
        ISolid moduleBox = new Box( module_name + "_box", moduleWidth / 2, moduleLength / 2, moduleThickness / 2 );

        // Create the module logical volume.
        IMaterial air = MaterialStore.getInstance().get( "Air" );
        moduleLogVol = new LogicalVolume( module_name, moduleBox, air );

        int componentNumber = 0;
        for ( Iterator j = moduleElement.getChildren( "module_component" ).iterator(); j.hasNext(); ++componentNumber )
        {
            Element componentElement = ( Element ) j.next();

            boolean sensitive = ( ( componentElement.getAttribute( "sensitive" ) == null ) ? false : componentElement
                    .getAttribute( "sensitive" ).getBooleanValue() );

            String componentName = module_name + "_component" + componentNumber;

            // Create the box solid for the module component.
            double componentLength = componentElement.getAttribute( "length" ).getDoubleValue();
            double componentWidth = componentElement.getAttribute( "width" ).getDoubleValue();
            double componentThickness = componentElement.getAttribute( "thickness" ).getDoubleValue();
            ISolid componentBox = new Box( componentName,
                                           componentWidth / 2,
                                           componentLength / 2,
                                           componentThickness / 2 );

            IMaterial componentMaterial = MaterialStore.getInstance().get(
                    componentElement.getAttributeValue( "material" ) );

            // Create the volume for the module component.
            ILogicalVolume componentLogVol = new LogicalVolume( componentName, componentBox, componentMaterial );

            // Set component position.
            double px = 0, py = 0, pz = 0;

            if ( componentElement.getChild( "position" ) != null )
            {
                Element pos_elem = componentElement.getChild( "position" );

                if ( pos_elem.getAttribute( "x" ) != null )
                {
                    px = pos_elem.getAttribute( "x" ).getDoubleValue();
                }

                if ( pos_elem.getAttribute( "y" ) != null )
                {
                    py = pos_elem.getAttribute( "y" ).getDoubleValue();
                }

                if ( pos_elem.getAttribute( "z" ) != null )
                {
                    pz = pos_elem.getAttribute( "z" ).getDoubleValue();
                }
            }

            ITranslation3D pos = new Translation3D( px, py, pz );

            // Set component rotation.
            double rx = 0, ry = 0, rz = 0;

            if ( componentElement.getChild( "rotation" ) != null )
            {
                Element rot_elem = componentElement.getChild( "rotation" );

                if ( rot_elem.getAttribute( "x" ) != null )
                {
                    rx = rot_elem.getAttribute( "x" ).getDoubleValue();
                }

                if ( rot_elem.getAttribute( "y" ) != null )
                {
                    ry = rot_elem.getAttribute( "y" ).getDoubleValue();
                }

                if ( rot_elem.getAttribute( "z" ) != null )
                {
                    rz = rot_elem.getAttribute( "z" ).getDoubleValue();
                }
            }

            IRotation3D rot = new RotationPassiveXYZ( rx, ry, rz );

            // Make transform
            Transform3D componentTransform = new Transform3D( pos, rot );

            PhysicalVolume componentPhysVol = new PhysicalVolume( componentTransform,
                                                                  componentName,
                                                                  componentLogVol,
                                                                  moduleLogVol,
                                                                  componentNumber );

            if ( sensitive )
            {
                componentPhysVol.setSensitive( true );
            }

            ++componentNumber;
        }

        return moduleLogVol;
    }

    private void buildLayers( Detector detector, Subdetector subdet, Map< String, ILogicalVolume > modules ) throws DataConversionException
    {
        Element node = subdet.getNode();
        String detector_name = subdet.getName();

        // Build the layers.
        // int nlayer = 0;
        for ( Iterator i = node.getChildren( "layer" ).iterator(); i.hasNext(); )
        {
            // Get the next layer element.
            Element layer_element = ( Element ) i.next();

            int layern = layer_element.getAttribute( "id" ).getIntValue();

            // Get the reference to the module from the layer.
            String module_name = layer_element.getAttributeValue( "module" );

            // Get the logical volume for the module.
            ILogicalVolume moduleEnvelope = modules.get( module_name );

            // Get the barrel_envelope for this layer.
            Element barrel_envelope = layer_element.getChild( "barrel_envelope" );

            // Inner radius of layer.
            double ir = barrel_envelope.getAttribute( "inner_x" ).getDoubleValue();

            // Outer radius of layer.
            double or = barrel_envelope.getAttribute( "outer_x" ).getDoubleValue();

            // Full length in z of layer.
            double oz = barrel_envelope.getAttribute( "z_length" ).getDoubleValue();
            double oy = barrel_envelope.getAttribute( "y_length" ).getDoubleValue();

            // Name of this layer including layer number.
            String layer_name = detector_name + "_layer" + layern;

            // System.out.println("layer_name=" + layer_name);
            // make the trapazoid...
            double dx1, dx2;
            dx1 = dx2 = ( or - ir ) / 2;
            Trd layer_trd = new Trd( layer_name + "_trd", dx1, dx2, oy, oy, oz );
            Translation3D p = new Translation3D( ir, 0, 0 );
            RotationGeant rot = new RotationGeant( 0, 0, -Math.PI / 2 );
            Transform3D trans = new Transform3D( p, rot );
            Tube layer_tube = new Tube( layer_name + "_tube", ir, or, oz / 2 );

            // Create the layer envelope volume.
            IMaterial air = MaterialStore.getInstance().get( "Air" );
            ILogicalVolume layer_volume = new LogicalVolume( layer_name, layer_trd, air );

            // Layer PhysicalVolume.
            IPhysicalVolume layer_envelope_physvol = new PhysicalVolume( trans, layer_name, layer_volume, detector
                    .getTrackingVolume().getLogicalVolume(), layern );

            // Layer DE.
            String layerPath = "/tracking_region/" + layer_name;
            IDetectorElement layerDE = new SiTrackerLayer( layer_name, subdet.getDetectorElement(), layerPath, layern );

            // Get the layout element.
            Element layout = layer_element.getChild( "layout" );

            // angle with respect to x (beam) axis of first module.
            double xTilt = layout.getAttribute( "xTilt" ).getDoubleValue();

            // Number of modules in y.
            int ny = layout.getAttribute( "ny" ).getIntValue();
            assert ( ny > 0 );
            // Number of modules in z.
            int nz = layout.getAttribute( "nz" ).getIntValue();
            assert ( nz > 0 );

            // Radius of the module center.
            double xCent = layout.getAttribute( "xCent" ).getDoubleValue();

            // Radius of the module center.
            double zGap = layout.getAttribute( "zgap" ).getDoubleValue();

            // The delta radius of every other module.
            double dx = 0.0;
            if ( layout.getAttribute( "dx" ) != null )
            {
                dx = layout.getAttribute( "dx" ).getDoubleValue();
            }

            // y increment for one module.
            // double phi_incr = (Math.PI * 2) / nphi;

            // Phi of the module center.
            double phic = 0;
            // phic += phi0;

            // Z increment for module placement along Z axis.
            // Adjust for z0 at center of module rather than
            // the end of cylindrical envelope.
            // double z_incr = (2.0 * z0) / (nz - 1);

            // Starting z for module placement along Z axis.
            // double module_z = -z0;

            // DEBUG
            // System.out.println("layer ir=" + ir);
            // System.out.println("layer or=" + or);
            // System.out.println("layer oz=" + oz);
            // System.out.println("phi_tilt=" + phi_tilt);
            // System.out.println("rc=" + rc);
            // System.out.println("phi0=" + phi0);
            // System.out.println("module z_incr=" + z_incr);
            // System.out.println("module z0=" + z0);
            // System.out.println("module nz=" + nz);
            // System.out.println("module dr=" + dr);
            //

            // String module_lkp_name = layer.getAttributeValue("module");

            int moduleId = 0;

            // Loop over the number of modules in phi.
            // for (int phicount = 0; phicount < nphi; phicount++) {
            // Delta x of module position.
            // / double dx = z_dr * cos(phic + phi_tilt);

            // Delta y of module position.
            // double dy = z_dr * sin(phic + phi_tilt);

            // Basic x module position.
            // double x = rc * cos(phic);

            // Basic y module position.
            // double y = rc * sin(phic);

            // Loop over the number of modules in z.
            for ( int zcount = 0; zcount < nz; zcount++ )
            {
                // Create a unique name for the module in this logical volume, layer, phi,
                // and z.
                // String module_place_name = detector_name + "_layer" + layern + "_phi" +
                // phicount + "_z" + zcount;

                // double z = module_z;

                // DEBUG
                // System.out.println("module build...");
                // System.out.println("module nphi=" + ii);
                // System.out.println("module nz" + j);
                // System.out.println("module x=" + x);
                // System.out.println("module y=" + y);
                // System.out.println("module z=" + z);
                // DEBUG
                /*
                 * // Position of module. //Position module_position = new
                 * Position(module_place_name + "_position"); ITranslation3D
                 * module_position = new Translation3D(x, y, z);
                 * 
                 * /* from the LCDD converter
                 * 
                 * double rotx = Math.PI / 2; double roty = -((Math.PI / 2) - phic -
                 * phi_tilt); double rotz = 0;
                 * 
                 * /*
                 * 
                 * // Rotation of module.
                 * 
                 * // FIXME: The Y and Z rotations are switched around from // the LCDD /
                 * Geant4 convention. Seems like an // active versus passive problem.
                 * double rotx = Math.PI / 2; double roty = 0; double rotz = ((Math.PI /
                 * 2) - phic - phi_tilt);
                 * 
                 * 
                 * IRotation3D module_rotation = new RotationPassiveXYZ(rotx, roty, rotz);
                 * 
                 * //System.out.println("module rotx=" + rotx);
                 * //System.out.println("module roty=" + roty);
                 * //System.out.println("module rotz=" + rotz);
                 * 
                 * Transform3D moduleTransform = new Transform3D(module_position,
                 * module_rotation);
                 * 
                 * // Module PhysicalVolume. IPhysicalVolume module_physvol = new
                 * PhysicalVolume( moduleTransform, module_place_name, moduleEnvelope,
                 * layer_volume, moduleId);
                 * 
                 * String modulePath = "/tracking_region/" + layer_name + "/" +
                 * module_place_name;
                 * 
                 * new SiTrackerModule( module_place_name, layerDE, modulePath, moduleId);
                 * 
                 * // Increment the by-layer module number. ++moduleId;
                 * 
                 * // Adjust the x and y coordinates of the module. x += dx; y += dy;
                 * 
                 * // Flip sign of x and y adjustments. dx *= -1; dy *= -1;
                 * 
                 * // Add z increment to get next z placement pos. module_z += z_incr;
                 * 
                 * //System.out.println(); }
                 * 
                 * // Increment the phi placement of module. phic += phi_incr;
                 * 
                 * // Increment the center radius according to dr parameter. rc +=
                 * rphi_dr;
                 * 
                 * // Flip sign of dr parameter. rphi_dr *= -1;
                 * 
                 * // Reset the Z placement parameter for module. module_z = -z0;
                 */
            }
        }
    }

    public Class getSubdetectorType()
    {
        return SiTrackerFixedTarget2ConverterBarrel.class;
    }

    private void setupSensorDetectorElements( Subdetector subdet )
    {
        int moduleId = 0;
        for ( IDetectorElement layer : subdet.getDetectorElement().getChildren() )
        {
            for ( IDetectorElement module : layer.getChildren() )
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
                        expId.setValue( iddict.getFieldIndex( "barrel" ), 0 );

                        // Set the layer number.
                        expId.setValue( iddict.getFieldIndex( "layer" ), layer.getGeometry().getPath().getLeafVolume()
                                .getCopyNumber() );

                        // Set the module id from the DetectorElement.
                        expId.setValue( iddict.getFieldIndex( "module" ), ( ( SiTrackerModule ) module ).getModuleId() );

                        // Set the sensor id for double-sided.
                        expId.setValue( iddict.getFieldIndex( "sensor" ), sensorId );

                        // Create the packed id using util method.
                        // No IdentifierHelper is available yet.
                        IIdentifier id = iddict.pack( expId );

                        // System.out.println(pv.getName() + " is sens");
                        // System.out.println("path : " + modulePath.toString() + "/" +
                        // pv.getName());
                        String sensorPath = modulePath.toString() + "/" + pv.getName();
                        // String sensorName = subdet.getName() + "_layer" +
                        // layer.getGeometry().getPhysicalVolume().getCopyNumber() +
                        // "_module" + moduleId + "_sensor" + sensorId;
                        String sensorName = subdet.getName() + "_layer" + layer.getGeometry().getPhysicalVolume()
                                .getCopyNumber() + "_module" + ( ( SiTrackerModule ) module ).getModuleId() + "_sensor" + sensorId;

                        SiSensor sensor = new SiSensor( sensorId, sensorName, module, sensorPath, id );
                        sensor.setIdentifier( id );

                        /*
                         * 
                         * Comment out sensor setup and use lcsim Driver.
                         * 
                         * // Set up SiStrips for the sensors IPolyhedron sensor_solid =
                         * (IPolyhedron
                         * )sensor.getGeometry().getLogicalVolume().getSolid();
                         * 
                         * // Bias the sensor Polygon3D p_side =
                         * sensor_solid.getFacesNormalTo(new
                         * BasicHep3Vector(0,0,1)).get(0); //
                         * System.out.println("Plane of p_side polygon has... "); //
                         * System
                         * .out.println("                        normal: "+p_side.getNormal
                         * ()); //
                         * System.out.println("                        distance: "+
                         * p_side.getDistance()); // for (Point3D point :
                         * p_side.getClosedVertices()) // { //
                         * System.out.println("      Vertex: "+point); // }
                         * 
                         * 
                         * Polygon3D n_side = sensor_solid.getFacesNormalTo(new
                         * BasicHep3Vector(0,0,-1)).get(0); //
                         * System.out.println("Plane of n_side polygon has... "); //
                         * System
                         * .out.println("                        normal: "+n_side.getNormal
                         * ()); //
                         * System.out.println("                        distance: "+
                         * n_side.getDistance());
                         * 
                         * sensor.setBiasSurface(ChargeCarrier.HOLE,p_side);
                         * sensor.setBiasSurface(ChargeCarrier.ELECTRON,n_side);
                         * 
                         * // Add sense and readout electrodes ITranslation3D
                         * electrodes_position = new
                         * Translation3D(VecOp.mult(-p_side.getDistance
                         * (),p_side.getNormal())); // translate to p_side IRotation3D
                         * electrodes_rotation = new RotationPassiveXYZ(0.0,0.0,0.0); //
                         * no rotation (global x-y = local x-y for axial strips)
                         * Transform3D electrodes_transform = new
                         * Transform3D(electrodes_position, electrodes_rotation);
                         * 
                         * // Free calculation of readout electrodes, sense electrodes
                         * determined thereon SiSensorElectrodes readout_electrodes = new
                         * SiStrips(ChargeCarrier.HOLE,0.050,sensor,electrodes_transform);
                         * SiSensorElectrodes sense_electrodes = new
                         * SiStrips(ChargeCarrier
                         * .HOLE,0.025,(readout_electrodes.getNCells(
                         * )*2-1),sensor,electrodes_transform);
                         * 
                         * // Free calculation of sense electrodes, readout electrodes
                         * determined thereon // SiSensorElectrodes sense_electrodes = new
                         * SiStrips(ChargeCarrier.HOLE,0.025,sensor,electrodes_transform);
                         * // SiSensorElectrodes readout_electrodes = new
                         * SiStrips(ChargeCarrier
                         * .HOLE,0.050,(sense_electrodes.getNCells()+
                         * 1)/2,sensor,electrodes_transform);
                         * 
                         * sensor.setSenseElectrodes(sense_electrodes);
                         * sensor.setReadoutElectrodes(readout_electrodes);
                         * 
                         * double[][] transfer_efficiencies = { {0.986,0.419} };
                         * sensor.setTransferEfficiencies(ChargeCarrier.HOLE,new
                         * BasicMatrix(transfer_efficiencies));
                         */

                        // Incremenet sensorID for double-sided.
                        ++sensorId;

                        // SiSensorElectrodes sense_electrodes = new
                        // SiStrips(3679,0.025,sensor,electrodes_transform);
                        // SiSensorElectrodes readout_electrodes = new
                        // SiStrips(1840,0.050,sensor,electrodes_transform);
                        //
                        // double[][] transfer_efficiencies = { {0.986,0.419} };
                        // sensor.setSenseElectrodes(ChargeCarrier.HOLE,sense_electrodes);
                        // sensor.setReadoutElectrodes(ChargeCarrier.HOLE,readout_electrodes);
                        // sensor.setTransferEfficiencies(ChargeCarrier.HOLE,new
                        // BasicMatrix(transfer_efficiencies));

                    }
                }

                ++moduleId;
            }
        }
    }
}
