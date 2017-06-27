package org.lcsim.detector.converter.compact;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.DetectorStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.ParametersStore;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.Rotation3D;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.converter.lcdd.MaterialElementConverter;
import org.lcsim.detector.converter.lcdd.MaterialMixtureConverter;
import org.lcsim.detector.converter.lcdd.MaterialsConverter;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.Constant;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.PolyconeSupport;
import org.reflections.Reflections;

public class DetectorConverter implements IDetectorConverter {
    
    // Map of class to converter.
    private Map<Class, ISubdetectorConverter> subdetectorConverters = new HashMap<Class, ISubdetectorConverter>();

    // The parameters converter.
    private ParametersConverter paramCnv = new ParametersConverter();

    // Materials converters.
    private MaterialsConverter materialCnv = new MaterialsConverter();
    private MaterialElementConverter elemCnv = new MaterialElementConverter();
    private MaterialMixtureConverter matCnv = new MaterialMixtureConverter();

    // The SystemMap for setting up the IdentifierHelpers.
    private SystemMap sysMap;

    public IPhysicalVolume convert(Detector detector, Document doc) throws JDOMException, IOException {
        
        // Clear out old DetectorStore store before building new detector.
        DetectorStore.getInstance().clear();

        // Convert materials.
        convertMaterials("/org/lcsim/material/elements.xml");
        convertMaterials("/org/lcsim/material/materials.xml");
        convertMaterials(doc);

        // Construct the world volume.
        IPhysicalVolume pvWorld = buildWorldVolume(detector);

        // Make the default navigator.
        PhysicalVolumeNavigatorStore.getInstance().reset();
        PhysicalVolumeNavigatorStore.getInstance().createDefault(pvWorld);

        // Set the Detector's DetectorElement.
        IDetectorElement deDet = new DeDetector(detector);
        detector.setDetectorElement(deDet);

        // Construct the tracking volume.
        buildTrackingVolume(pvWorld.getLogicalVolume(), detector);

        // Set the world volume.
        detector.setWorldVolume(pvWorld);

        // Make the SystemMap.
        sysMap = makeSystemMap(detector);

        // Convert Subdetectors including creation of IdentifierHelpers.
        convertSubdetectors(detector);

        // Return the world volume.
        return pvWorld;
    }

    public DetectorConverter() {
    }

    public void registerSubdetectorConverters() {
        Reflections reflect = new Reflections("org.lcsim.detector.converter.compact");
        Set<Class<? extends AbstractSubdetectorConverter>> converters = reflect
                .getSubTypesOf(AbstractSubdetectorConverter.class);

        if (converters.size() == 0) {
            throw new RuntimeException("No subdetector converter classes were found.");
        }

        for (Class<? extends AbstractSubdetectorConverter> converter : converters) {
            try {
                if (!Modifier.isAbstract(converter.getModifiers())) {
                    this.addSubdetectorConverter(converter.newInstance());
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addSubdetectorConverter(ISubdetectorConverter s) {
        subdetectorConverters.put(s.getSubdetectorType(), s);
    }

    private ISubdetectorConverter getSubdetectorConverter(Class klass) {
        return subdetectorConverters.get(klass);
    }

    public IPhysicalVolume convert(Detector detector, String resource) throws JDOMException, IOException {
        return convert(detector, CompactDocumentBuilder.build(resource));
    }

    private void convertMaterials(Document doc) throws JDOMException {
        materialCnv.convert(doc);
    }

    private void convertMaterials(String resource) throws JDOMException, IOException {
        Document doc = CompactDocumentBuilder.build(resource);

        for (Object obj : doc.getRootElement().getChildren()) {
            Element e = (Element) obj;
            if (e.getName().equals("element")) {
                elemCnv.convert(e);
            } else if (e.getName().equals("material")) {
                matCnv.convert(e);
            }
        }
    }

    private void convertSubdetectors(Detector detector) {
        // Process all Subdetectors in the Detector.
        for (Subdetector subdetector : detector.getSubdetectors().values()) {

            // Find a converter for this type.
            ISubdetectorConverter cnv = getSubdetectorConverter(subdetector.getClass());

            if (cnv != null) {

                // cnv.getSubdetectorType().getCanonicalName());
                DetectorElement subdetectorDE = (DetectorElement) cnv.makeSubdetectorDetectorElement(detector, subdetector);

                // Make the IdentifierHelper for this Subdetector.
                IIdentifierHelper helper = cnv.makeIdentifierHelper(subdetector, sysMap);

                // Convert the parameters.
                try {
                    paramCnv.convert(subdetector.getNode());
                } catch (JDOMException x) {
                    throw new RuntimeException(x);
                }

                if (subdetectorDE != null) {
                    subdetectorDE.setIdentifierHelper(helper);
                }

                // Build the Subdetector's geometry and associated
                // DetectorElement(s).
                cnv.convert(subdetector, detector);

                // Get the top level Subdetector node back.
                DetectorElement subdetDE = (DetectorElement) subdetector.getDetectorElement();

                // Check if a DetectorElement was created. Some compact "detector" objects
                // are not really detectors but dead material so this check is necessary
                // to avoid errors.
                if (subdetDE != null) {
                    
                    // Make the Parameters from the compact detector element
                    // and assign to the Subdetector's DetectorElement.
                    subdetDE.setParameters(ParametersStore.getInstance().get(subdetector.getName()));

                    // Make the Subdetector IdentifierHelper from the IDDecoder
                    // and assign to the Subdetector's DetectorElement.
                    if (helper != null && subdetectorDE.getIdentifierHelper() == null) {
                        subdetDE.setIdentifierHelper(helper);
                    }

                    // Make the identifiers for this Subdetector.
                    cnv.makeIdentifiers(subdetector);
                }
                
                // Initialize the subdetector detector element (no op for most classes).
                subdetectorDE.initialize();
            }
        }
    }

    private void buildTrackingVolume(ILogicalVolume world, Detector detector) {
        
        Map<String, Constant> constants = detector.getConstants();

        if (constants.get("tracking_region_zmax") == null) {
            throw new RuntimeException("Missing required tracking_region_zmax parameter in compact.xml file.");
        }
        double zmax = constants.get("tracking_region_zmax").getValue();
        
        if (constants.get("tracking_region_radius") == null) {
            throw new RuntimeException("Missing required tracking_region_radius parameter in compact.xml file.");
        }              
        double radius = constants.get("tracking_region_radius").getValue();
        
        Tube trackingTube = new Tube("tracking_region_tube", 0, radius, zmax);

        LogicalVolume trackingLV = new LogicalVolume("tracking_region", trackingTube, MaterialStore.getInstance().get("Air"));
        
        double x, y, z;
        x = y = z = 0;
        if (constants.get("tracking_region_z") != null) {
            z = constants.get("tracking_region_z").getValue();
        }
        
        ITranslation3D pos = new Translation3D(x, y, z);
        IRotation3D rot = new Rotation3D();
        ITransform3D trans = new Transform3D(pos, rot);

        new PhysicalVolume(trans, "tracking_region", trackingLV, world, 0);
    }

    private IPhysicalVolume buildWorldVolume(Detector detector) {
        Map<String, Constant> constants = detector.getConstants();

        if (constants.get("world_x") == null || constants.get("world_y") == null || constants.get("world_z") == null) {
            throw new RuntimeException("Missing required constant for defining the world volume.");
        }

        double x = constants.get("world_x").getValue();
        double y = constants.get("world_y").getValue();
        double z = constants.get("world_z").getValue();

        IMaterial air = MaterialStore.getInstance().get("Air");

        Box boxWorld = new Box("world_box", x, y, z);

        LogicalVolume lvWorld = new LogicalVolume("world", boxWorld, air);

        PhysicalVolume pvWorld = new PhysicalVolume(null, "world", lvWorld, null, 0);

        return pvWorld;
    }

    // Converts the subdetectors in a detector to a map of subsystems to system id.
    public static final SystemMap makeSystemMap(Detector d) {
        SystemMap m = new SystemMap();

        for (Subdetector subdet : d.getSubdetectors().values()) {
            String name = subdet.getName();

            int sys = subdet.getSystemID();

            // Based on naming conventions from sid01 and sid02.
            if (!name.contains("Support") && !(subdet instanceof PolyconeSupport)) {
                if (name.contains("VertexBarrel")) {
                    m.put("vtxBarrel", sys);
                } else if (name.contains("VertexEndcap")) {
                    m.put("vtxEndcap", sys);
                } else if (name.contains("TrackerBarrel")) {
                    m.put("sitBarrel", sys);
                } else if (name.contains("TrackerEndcap")) {
                    m.put("sitEndcap", sys);
                } else if (name.contains("TrackerForward")) {
                    m.put("sitForward", sys);
                } else if (name.contains("TPC")) {
                    m.put("tpc", sys);
                } else if (name.contains("EMBarrel")) {
                    m.put("ecalBarrel", sys);
                } else if (name.contains("EMEndcap") && !name.contains("Forward")) {
                    m.put("ecalEndcap", sys);
                } else if (name.contains("HADBarrel")) {
                    m.put("hcalBarrel", sys);
                } else if (name.contains("HADEndcap")) {
                    m.put("hcalEndcap", sys);
                } else if (name.contains("MuonBarrel")) {
                    m.put("muonBarrel", sys);
                } else if (name.contains("MuonEndcap")) {
                    m.put("muonEndcap", sys);
                } else if (name.contains("LuminosityMonitor") || name.contains("LumiCal")) {
                    m.put("lumi", sys);
                } else if (name.contains("ForwardEMEndcap")) {
                    m.put("ecalForward", sys);
                }
            }
        }

        return m;
    }
}
