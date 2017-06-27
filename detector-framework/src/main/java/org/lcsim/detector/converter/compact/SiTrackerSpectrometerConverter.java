package org.lcsim.detector.converter.compact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Trd;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.converter.SiTrackerModuleComponentParameters;
import org.lcsim.geometry.compact.converter.SiTrackerModuleParameters;
import org.lcsim.geometry.subdetector.SiTrackerSpectrometer;

/**
 * An LCDD converter for a Silicon tracker model for a fixed target 
 * with a gap between planes around y=0 to allow for beam particles to 
 * go through. 
 * designed so that z=beam direction, x=bend direction, y=non-bend direction
 * ...based off of SiTrackerFixedTarget2
 * 
 * @author mgraham
 */
public class SiTrackerSpectrometerConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter {

    Map<String, SiTrackerModuleParameters> moduleParameters = new HashMap<String, SiTrackerModuleParameters>();
    Map<String, LogicalVolume> modules = new HashMap<String, LogicalVolume>();
    IMaterial vacuum;

    public IIdentifierHelper makeIdentifierHelper(Subdetector subdetector, SystemMap systemMap) {
        return new SiTrackerIdentifierHelper(subdetector.getDetectorElement(),
                makeIdentifierDictionary(subdetector),
                systemMap);
    }

    public void convert(Subdetector subdet, Detector detector) {
        //System.out.println("SiTrackerSpectrometerConverter:  Converting...");
        try {
            Element node = subdet.getNode();
            String subdetName = node.getAttributeValue("name");
            vacuum = MaterialStore.getInstance().get("Air");

            boolean reflect = true;
            if (node.getAttribute("reflect") != null) {
                reflect = node.getAttribute("reflect").getBooleanValue();
            }


            boolean flipSA = false;
            if (node.getAttribute("flipSA") != null) {
                flipSA = node.getAttribute("flipSA").getBooleanValue();
            }

            IDetectorElement subdetDetElem = subdet.getDetectorElement();
            DetectorIdentifierHelper helper = (DetectorIdentifierHelper) subdetDetElem.getIdentifierHelper();
            int nfields = helper.getIdentifierDictionary().getNumberOfFields();
            IDetectorElement endcapPos = null;
            IDetectorElement endcapNeg = null;
            try {
                // Positive endcap DE
                IExpandedIdentifier endcapPosId = new ExpandedIdentifier(nfields);
                endcapPosId.setValue(helper.getFieldIndex("system"), subdet.getSystemID());
                endcapPosId.setValue(helper.getFieldIndex("barrel"), helper.getBarrelValue());
                endcapPos = new DetectorElement(subdet.getName() + "_positive", subdetDetElem);
                endcapPos.setIdentifier(helper.pack(endcapPosId));
                if (reflect) {
                    IExpandedIdentifier endcapNegId = new ExpandedIdentifier(nfields);
                    endcapNegId.setValue(helper.getFieldIndex("system"), subdet.getSystemID());
                    endcapNegId.setValue(helper.getFieldIndex("barrel"), helper.getBarrelValue());
                    endcapNeg = new DetectorElement(subdet.getName() + "_negative", subdetDetElem);
                    endcapNeg.setIdentifier(helper.pack(endcapNegId));
                }
            } catch (Exception x) {
                throw new RuntimeException(x);
            }

            for (Iterator i = node.getChildren("module").iterator(); i.hasNext();) {
                Element module = (Element) i.next();
                String moduleName = module.getAttributeValue("name");
                moduleParameters.put(moduleName, new SiTrackerModuleParameters(module));
                modules.put(moduleName, makeModule(moduleParameters.get(moduleName)));
            }

            for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();) {
                Element layerElement = (Element) i.next();

                int layerId = layerElement.getAttribute("id").getIntValue();

                // Positive endcap layer.
                IExpandedIdentifier layerPosId = new ExpandedIdentifier(nfields);
                layerPosId.setValue(helper.getFieldIndex("system"), subdet.getSystemID());
                // layerPosId.setValue(helper.getFieldIndex("barrel"),
                // helper.getEndcapPositiveValue());
                layerPosId.setValue(helper.getFieldIndex("barrel"), helper.getBarrelValue());
                layerPosId.setValue(helper.getFieldIndex("layer"), layerId);
                IDetectorElement layerPos = new DetectorElement(endcapPos.getName() + "_layer" + layerId,
                        endcapPos,
                        helper.pack(layerPosId));

                // Negative endcap layer.
                IDetectorElement layerNeg = null;
                if (reflect) {
                    IExpandedIdentifier layerNegId = new ExpandedIdentifier(nfields);
                    layerNegId.setValue(helper.getFieldIndex("system"), subdet.getSystemID());
                    layerNegId.setValue(helper.getFieldIndex("barrel"), helper.getBarrelValue());
                    layerNegId.setValue(helper.getFieldIndex("layer"), layerId);
                    layerNeg = new DetectorElement(endcapNeg.getName() + "_layer_reflected" + layerId,
                            endcapNeg,
                            helper.pack(layerNegId));
                }

                int moduleNumber = 0;
                for (Iterator j = layerElement.getChildren("quadrant").iterator(); j.hasNext();) {
                    Element ringElement = (Element) j.next();
                    double zLayer = ringElement.getAttribute("z").getDoubleValue();
                    double dz = ringElement.getAttribute("dz").getDoubleValue();
                    double xStart = ringElement.getAttribute("xStart").getDoubleValue();
                    double xStep = ringElement.getAttribute("xStep").getDoubleValue();
                    int nx = ringElement.getAttribute("nx").getIntValue();
                    double yStart = ringElement.getAttribute("yStart").getDoubleValue();
                    int ny = ringElement.getAttribute("ny").getIntValue();
                    double yStep = ringElement.getAttribute("yStep").getDoubleValue();

                    double phi0 = 0;
                    if (ringElement.getAttribute("phi0") != null) {
                        phi0 = ringElement.getAttribute("phi0").getDoubleValue();
                    }
                    String module = ringElement.getAttributeValue("module");
                    LogicalVolume moduleVolume = modules.get(module);
                    if (moduleVolume == null) {
                        throw new RuntimeException("Module " + module + " was not found.");
                    }

                    SiTrackerModuleParameters modPars = moduleParameters.get(module);

                    double x, y, z;
                    z = zLayer;
                    x = xStart;
                    //System.out.println("Making modules...nx=" + nx + ";ny=" + ny);
                    for (int k = 0; k < nx; k++) {
                        y = yStart;
                        for (int kk = 0; kk < ny; kk++) {
                            String moduleBaseName = subdetName + "_layer" + layerId + "_module" + moduleNumber;

                            // Positive endcap module.
                            Translation3D p = new Translation3D(x, y, z + dz);

                            RotationGeant rot = new RotationGeant(-Math.PI / 2, Math.PI / 2+phi0,0);
                            new PhysicalVolume(new Transform3D(p, rot), moduleBaseName, moduleVolume, detector.getTrackingVolume().getLogicalVolume(), 0);
                            String path = "/" + detector.getTrackingVolume().getName() + "/" + moduleBaseName;
                            IDetectorElement modulePos = new SiTrackerModule(moduleBaseName,
                                    layerPos,
                                    path,
                                    moduleNumber);
                            ++moduleNumber;
                            //System.out.println("Making module " + moduleBaseName + "  @ " + layerPos.toString());
                            if (reflect) {
                                Translation3D pr = new Translation3D(x, -y, z + dz);
                                double rphi0 = phi0;
                                if (flipSA)
                                    rphi0 = -rphi0;

                                RotationGeant rotr = new RotationGeant(-Math.PI / 2,  Math.PI / 2+rphi0,0);

                                String path2 = "/" + detector.getTrackingVolume().getName() + "/" + moduleBaseName + "_reflected";
                                new PhysicalVolume(new Transform3D(pr, rotr),
                                        moduleBaseName + "_reflected",
                                        moduleVolume,
                                        detector.getTrackingVolume().getLogicalVolume(),
                                        k);
                                new SiTrackerModule(moduleBaseName + "_reflected", layerNeg, path2, moduleNumber);
                                //System.out.println("Making module " + moduleBaseName + "  @ " + layerNeg.toString());
                            }

                            dz = -dz;
                            y += yStep;
                            ++moduleNumber;
                        }
                        x += xStep;
                    }
                }
            }
        } catch (JDOMException except) {
            throw new RuntimeException(except);
        }

        // Create DetectorElements for the sensors.
        setupSensorDetectorElements(subdet);
    }

    private LogicalVolume makeModule(SiTrackerModuleParameters params) {
        double thickness = params.getThickness();
        double dx1, dx2, dy1, dy2, dz;
        dy1 = dy2 = thickness / 2;
        dx1 = params.getDimension(0);
        dx2 = params.getDimension(1);
        dz = params.getDimension(2);
        Trd envelope = new Trd(params.getName() + "Trd", dx1, dx2, dy1, dy2, dz);
        LogicalVolume volume = new LogicalVolume(params.getName() + "Volume", envelope, vacuum);
        makeModuleComponents(volume, params);
        return volume;
    }

    private void makeModuleComponents(LogicalVolume moduleVolume, SiTrackerModuleParameters moduleParameters) {
        Trd trd = (Trd) moduleVolume.getSolid();

        double x1 = trd.getXHalfLength1();
        double x2 = trd.getXHalfLength2();
        double y1 = trd.getYHalfLength1();
        double z = trd.getZHalfLength();

        double posY = -y1;

        String moduleName = moduleVolume.getName();

        int sensor = 0;
        for (SiTrackerModuleComponentParameters component : moduleParameters) {
            double thickness = component.getThickness();

            IMaterial material = MaterialStore.getInstance().get(component.getMaterialName());
            if (material == null) {
                throw new RuntimeException("The material " + component.getMaterialName() + " does not exist in the materials database.");
            }
            boolean sensitive = component.isSensitive();
            int componentNumber = component.getComponentNumber();

            posY += thickness / 2;

            String componentName = moduleName + "_component" + componentNumber;

            Trd sliceTrd = new Trd(componentName + "_trd", x1, x2, thickness / 2, thickness / 2, z);

            LogicalVolume volume = new LogicalVolume(componentName, sliceTrd, material);

            double zrot = 0;
            if (sensitive) {
                if (sensor > 1) {
                    throw new RuntimeException("Exceeded maximum of 2 sensors per module.");
                }
                // Flip 180 deg for 1st sensor.
                if (sensor == 0) {
                    zrot = Math.PI;
                }
                ++sensor;
            }
            Translation3D position = new Translation3D(0., posY, 0);
            RotationGeant rotation = new RotationGeant(0, 0, zrot);
            PhysicalVolume pv = new PhysicalVolume(new Transform3D(position, rotation),
                    componentName,
                    volume,
                    moduleVolume,
                    componentNumber);
            pv.setSensitive(sensitive);

            posY += thickness / 2;
        }
    }

    private void setupSensorDetectorElements(Subdetector subdet) {
        SiTrackerIdentifierHelper helper = (SiTrackerIdentifierHelper) subdet.getDetectorElement().getIdentifierHelper();

        for (IDetectorElement endcap : subdet.getDetectorElement().getChildren()) {
            for (IDetectorElement layer : endcap.getChildren()) {
                for (IDetectorElement module : layer.getChildren()) {
                    IPhysicalVolume modulePhysVol = module.getGeometry().getPhysicalVolume();
                    IPhysicalVolumePath modulePath = module.getGeometry().getPath();
                    int sensorId = 0;
                    for (IPhysicalVolume pv : modulePhysVol.getLogicalVolume().getDaughters()) {
                        if (pv.isSensitive()) {
                            IIdentifierDictionary iddict = subdet.getDetectorElement().getIdentifierHelper().getIdentifierDictionary();

                            ExpandedIdentifier expId = new ExpandedIdentifier(iddict.getNumberOfFields());
                            expId.setValue(iddict.getFieldIndex("system"), subdet.getSystemID());

                            if (helper.isEndcapPositive(endcap.getIdentifier())) {
                                expId.setValue(iddict.getFieldIndex("barrel"), helper.getEndcapPositiveValue());
                            } else if (helper.isEndcapNegative(endcap.getIdentifier())) {
                                expId.setValue(iddict.getFieldIndex("barrel"), helper.getEndcapNegativeValue());
                            } else if (helper.isBarrel(endcap.getIdentifier())) {
                                expId.setValue(iddict.getFieldIndex("barrel"), helper.getBarrelValue());
                            } else {
                                throw new RuntimeException(endcap.getName() + " is not a positive or negative endcap!");
                            }
                            expId.setValue(iddict.getFieldIndex("layer"), layer.getIdentifierHelper().getValue(
                                    layer.getIdentifier(),
                                    "layer"));
                            expId.setValue(iddict.getFieldIndex("module"), ((SiTrackerModule) module).getModuleId());
                            expId.setValue(iddict.getFieldIndex("sensor"), sensorId);

                            IIdentifier id = iddict.pack(expId);

                            String sensorPath = modulePath.toString() + "/" + pv.getName();
                            String sensorName = module.getName() + "_sensor" + sensorId;

                            SiSensor sensor = new SiSensor(sensorId, sensorName, module, sensorPath, id);

                            // Set up SiStrips for the sensors
                            /*
                             * Trd sensor_solid =
                             * (Trd)sensor.getGeometry().getLogicalVolume().getSolid();
                             * 
                             * Polygon3D n_side = sensor_solid.getFacesNormalTo(new
                             * BasicHep3Vector(0,-1,0)).get(0); Polygon3D p_side =
                             * sensor_solid.getFacesNormalTo(new
                             * BasicHep3Vector(0,1,0)).get(0);
                             * 
                             * //System.out.println("Plane of p_side polygon has... ");
                             * //System
                             * .out.println("                        normal: "+p_side
                             * .getNormal());
                             * //System.out.println("                        distance: "
                             * +p_side.getDistance()); //for (Point3D point :
                             * p_side.getVertices()) //{ //
                             * System.out.println("      Vertex: "+point); //}
                             * 
                             * //System.out.println("Plane of n_side polygon has... ");
                             * //System
                             * .out.println("                        normal: "+n_side
                             * .getNormal());
                             * //System.out.println("                        distance: "
                             * +n_side.getDistance());
                             * 
                             * // Bias the sensor
                             * sensor.setBiasSurface(ChargeCarrier.HOLE,p_side);
                             * sensor.setBiasSurface(ChargeCarrier.ELECTRON,n_side);
                             * 
                             * double strip_angle =
                             * Math.atan2(sensor_solid.getXHalfLength2() -
                             * sensor_solid.getXHalfLength1(),
                             * sensor_solid.getZHalfLength() * 2);
                             * 
                             * ITranslation3D electrodes_position = new
                             * Translation3D(VecOp.mult(-p_side.getDistance(),new
                             * BasicHep3Vector(0,0,1))); // translate to outside of
                             * polygon IRotation3D electrodes_rotation = new
                             * RotationPassiveXYZ(-Math.PI/2,0,strip_angle); Transform3D
                             * electrodes_transform = new Transform3D(electrodes_position,
                             * electrodes_rotation);
                             * 
                             * // Free calculation of readout electrodes, sense electrodes
                             * determined thereon SiSensorElectrodes readout_electrodes =
                             * new
                             * SiStrips(ChargeCarrier.HOLE,0.050,sensor,electrodes_transform
                             * ); SiSensorElectrodes sense_electrodes = new
                             * SiStrips(ChargeCarrier
                             * .HOLE,0.025,(readout_electrodes.getNCells
                             * ()*2-1),sensor,electrodes_transform);
                             * 
                             * sensor.setSenseElectrodes(sense_electrodes);
                             * sensor.setReadoutElectrodes(readout_electrodes);
                             * 
                             * double[][] transfer_efficiencies = { {0.986,0.419} };
                             * sensor.setTransferEfficiencies(ChargeCarrier.HOLE,new
                             * BasicMatrix(transfer_efficiencies));
                             */
                            ++sensorId;
                        }
                    }
                }
            }
        }
    }

    public Class getSubdetectorType() {
        return SiTrackerSpectrometer.class;
    }
}
