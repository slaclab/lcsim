package org.lcsim.geometry.compact.converter.lcdd.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.lcsim.geometry.compact.converter.lcdd.LCDDSubdetector;
import org.lcsim.material.XMLMaterialManager;

/**
 * 
 * @author tonyj
 */
public class LCDD extends Element {
    
    private Map materials = new HashMap();
    LCDDMaterialHelper matHelper = new LCDDMaterialHelper(XMLMaterialManager.getDefaultMaterialManager());

    public LCDD() {
        super("lcdd");
        build();
    }

    /**
     * Builds an empty LCDD XML skeleton into this object.
     */
    private void build() {
        
        addNamespaceDeclaration(Namespace.getNamespace("lcdd", "http://www.lcsim.org/schemas/lcdd/1.0"));

        setAttribute("noNamespaceSchemaLocation", "http://www.lcsim.org/schemas/lcdd/1.0/lcdd.xsd", 
                Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema-instance"));

        Header header = new Header();
        addContent(header);

        Element iddict = new Element("iddict");
        addContent(iddict);

        Element sensitiveDetectors = new Element("sensitive_detectors");
        addContent(sensitiveDetectors);

        Element limits = new Element("limits");
        addContent(limits);

        Element regions = new Element("regions");
        addContent(regions);

        Element display = new Element("display");
        addContent(display);
       
        Element gdml = new Element("gdml");
        addContent(gdml);
        gdml.addContent(new Define());
        gdml.addContent(new Element("materials"));
        Solids solids = new Solids();
        gdml.addContent(solids);
        Structure structure = new Structure();
        gdml.addContent(structure);

        Element setup = new Element("setup");
        setup.setAttribute("name", "Default");
        setup.setAttribute("version", "1.0");
        gdml.addContent(setup);

        Element fields = new Element("fields");
        addContent(fields);
    }

    private final Material getWorldMaterial() {
        Material m = null;
        try {
            // User-specified fill material.
            m = this.getMaterial("WorldMaterial");
        } catch (JDOMException x) {
            try {
                // Default fill material of air.
                m = this.getMaterial("Air");
            } catch (JDOMException x2) {
                // This should not happen!
                throw new RuntimeException(x);
            }
        }
        return m;
    }

    private final Material getTrackingMaterial() {
        Material m = null;
        try {
            // User-specified fill material.
            m = this.getMaterial("TrackingMaterial");
        } catch (JDOMException x) {
            try {
                // Default fill material of air.
                m = this.getMaterial("Air");
            } catch (JDOMException x2) {
                // This should not happen!
                throw new RuntimeException(x);
            }
        }
        return m;
    }

    public void cleanUp() throws JDOMException {
        Structure structure = getStructure();
        Volume trackingVolume = structure.getTrackingVolume();
        trackingVolume.setMaterial(getTrackingMaterial());

        // Move tracking volume definition to end of XML block.
        structure.removeContent(trackingVolume);
        structure.addContent(trackingVolume);

        Region trackingRegion = new Region("TrackingRegion");
        trackingRegion.setThreshold(1);
        trackingRegion.setStoreSecondaries(true);
        addRegion(trackingRegion);
        trackingVolume.setRegion(trackingRegion);

        Volume worldVolume = structure.getWorldVolume();
        worldVolume.setMaterial(getWorldMaterial());

        // Move world volume definition to end of XML block.
        structure.removeContent(worldVolume);
        structure.addContent(worldVolume);

        // Set the world volume to invisible.
        VisAttributes worldVis = new VisAttributes("WorldVis");
        worldVis.setVisible(false);
        this.getWorldVolume().setVisAttributes(worldVis);
        this.add(worldVis);

        // Set the tracking volume to invisible.
        VisAttributes trackingVis = new VisAttributes("TrackingVis");
        trackingVis.setVisible(false);
        this.getTrackingVolume().setVisAttributes(trackingVis);
        this.add(trackingVis);
    }

    public Solids getSolids() {
        return (Solids) getChild("gdml").getChild("solids");
    }
    
    public Element getSetup() {
        return getChild("gdml").getChild("setup");
    }

    public Solid getSolid(String name) {
        Solid solid = null;
        for (Object object : getSolids().getChildren()) {
            solid = (Solid) object;
            if (solid != null) {
                if (solid.getAttributeValue("name").compareTo(name) == 0) {
                    return solid;
                }
            }
        }
        return null;
    }

    public void add(Constant constant) {
        getDefine().addConstant(constant);
    }
    
    public void add(Matrix matrix) {
        getDefine().addMatrix(matrix);
    }

    public void add(Position position) {
        getDefine().addPosition(position);
    }

    public void add(Rotation rotation) {
        getDefine().addRotation(rotation);
    }

    public void add(Solid solid) {
        getSolids().addSolid(solid);
    }

    public void add(Volume volume) {
        getStructure().addVolume(volume);
    }

    public void add(Region region) {
        getChild("regions").addContent(region);
    }

    public void add(IDSpec spec) {
        getChild("iddict").addContent(spec);
    }

    public void add(LimitSet limitset) {
        getChild("limits").addContent(limitset);
    }

    public void add(VisAttributes vis) {
        getChild("display").addContent(vis);
    }

    public VisAttributes getVisAttributes(String name) {
        VisAttributes vis = null;
        for (Iterator i = getChild("display").getChildren("vis").iterator(); i.hasNext();) {
            VisAttributes thisvis = (VisAttributes) i.next();
            if (thisvis.getRefName().compareTo(name) == 0) {
                vis = thisvis;
                break;
            }
        }
        return vis;
    }

    public Structure getStructure() {
        return (Structure) getChild("gdml").getChild("structure");
    }

    public Material getMaterial(String name) throws JDOMException {
        Material mat = (Material) materials.get(name);

        /**
         * This may be a material that was not defined in the materials block. Attempt to look it up using the
         * global materials manager.  If this fails, the material reference is probably invalid/undefined.
         * 
         */
        if (mat == null) {
            // This call will push material XML references into the LCDD object. (???)
            matHelper.resolveLCDDMaterialReference(name, this);
        }

        // Retry the materials lookup as above call should have added it if found.
        mat = (Material) materials.get(name);

        if (mat == null) {
            // Material lookup failed!
            throw new JDOMException("Material " + name + " was not found.");
        }

        return mat;
    }

    public Define getDefine() {
        return (Define) getChild("gdml").getChild("define");
    }

    public Header getHeader() {
        return (Header) getChild("header");
    }

    public void addMaterial(Material material) {
        if (materials.get(material.getRefName()) == null) {
            getChild("gdml").getChild("materials").addContent(material);
            materials.put(material.getRefName(), material);
        }
    }

    public void addElement(Element element) {
        if (getElement(element.getAttributeValue("name")) == null) {
            getChild("gdml").getChild("materials").addContent(element);
        }
    }

    public Element getElement(String elemName) {
        Element e = null;
        for (Object o : getChild("gdml").getChild("materials").getChildren("element")) {
            Element ee = (Element) o;
            if (ee.getAttributeValue("name").contentEquals(elemName)) {
                e = ee;
                break;
            }
        }
        return e;
    }

    public void addIDSpec(IDSpec spec) {
        getChild("iddict").addContent(spec);
    }

    public void addSensitiveDetector(SensitiveDetector det) {
        getChild("sensitive_detectors").addContent(det);
    }

    public void add(Field field) {
        getChild("fields").addContent(field);
    }

    public void setGlobalField(Field field) {
        Element fields = getChild("fields");
        fields.addContent(field);

        Element fieldRef = new Element("fieldref");
        fieldRef.setAttribute("ref", field.getRefName());

        Element globalField = new Element("global_field");
        globalField.addContent(fieldRef);
        fields.addContent(globalField);
    }

    public void addRegion(Region region) {
        getChild("regions").addContent(region);
    }

    public Element getRegions() {
        return getChild("regions");
    }

    public Region getRegion(String name) {
        Region region = null;
        for (Iterator i = getChild("regions").getChildren("region").iterator(); i.hasNext();) {
            Region thisregion = (Region) i.next();
            if (thisregion.getRefName().compareTo(name) == 0) {
                region = thisregion;
                break;
            }
        }
        return region;
    }

    /**
     * Pick the world or tracking volume for a subdetector's mother volume, depending on the value of the
     * insideTrackingVolume attribute.
     * 
     * If insideTrackingVolume is not set, trackers go into the tracking volume and calorimeters go into the
     * world volume.
     * 
     * @param subdet LCDD subdetector
     */
    public Volume pickMotherVolume(LCDDSubdetector subdet) {
        Attribute insideAttrib = subdet.getElement().getAttribute("insideTrackingVolume");
        boolean inside = false;

        try {
            if (insideAttrib == null) {
                if (subdet.isTracker()) {
                    inside = true;
                } else {
                    inside = false;
                }
            } else {
                inside = insideAttrib.getBooleanValue();
            }
        } catch (org.jdom.DataConversionException dce) {
            throw new RuntimeException("Error converting insideTrackingVolume attribute.", dce);
        }

        Volume motherVolume = (inside ? getStructure().getTrackingVolume() : getStructure().getWorldVolume());

        if (motherVolume == null) {
            throw new RuntimeException("Picked a null mother volume.");
        }

        return motherVolume;
    }

    public void addLimitSet(LimitSet limitset) {
        getChild("limits").addContent(limitset);
    }

    public LimitSet getLimitSet(String name) {
        LimitSet limitset = null;
        for (Iterator i = getChild("limits").getChildren("limitset").iterator(); i.hasNext();) {
            LimitSet thislimitset = (LimitSet) i.next();
            if (thislimitset.getRefName().compareTo(name) == 0) {
                limitset = thislimitset;
                break;
            }
        }
        return limitset;
    }

    public Volume getWorldVolume() {
        return this.getStructure().getWorldVolume();
    }

    public Volume getTrackingVolume() {
        return this.getStructure().getTrackingVolume();
    }

    public Volume getVolume(String name) {
        for (Iterator i = getChild("structure").getChildren("volume").iterator(); i.hasNext();) {
            Volume vol = (Volume) i.next();
            if (vol.getRefName().compareTo(name) == 0) {
                return vol;
            }
        }
        return null;
    }

    /**
     * Merge an existing GDML file into this LCDD document.
     * 
     * @param in InputStream from a GDML data source.
     */
    public void mergeGDML(InputStream in) {
        
        // Build the GDML input document.
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(in);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }

        Element root = doc.getRootElement();

        if (!root.getName().equals("gdml")) {
            throw new RuntimeException("Document is not a valid GDML file.");
        }

        Element gdml = getChild("gdml");

        // Find the world and tracking volumes in the target document.
        Element targetWorld = null;
        Element targetTracking = null;
        for (Object o : gdml.getChild("structure").getChildren()) {
            Element e = (Element) o;
            if (e.getAttributeValue("name").equals("world_volume")) {
                targetWorld = e;
            } else if (e.getAttributeValue("name").equals("tracking_volume")) {
                targetTracking = e;
            }
        }

        // Process top level sections in the source GDML document.
        for (Object o1 : root.getChildren()) {
            Element section = (Element) o1;
            
            // Ignore the setup section of the source document.
            if (!section.getName().equals("setup")) {
                Element target = gdml.getChild(section.getName());

                // Process children in this section.
                for (Object o2 : section.getChildren()) {
                    Element element = (Element) o2;

                    // Check if physvols need to be merged into the target tracking or world volumes.
                    if (element.getName().equals("volume") && (element.getAttributeValue("name").equals("world_volume") 
                            || element.getAttributeValue("name").equals("tracking_volume"))) {
                        Element targetVol = null;

                        if (element.getAttributeValue("name").equals("world_volume")) {
                            targetVol = targetWorld;
                        } else if (element.getAttributeValue("name").equals("tracking_volume")) {
                            targetVol = targetTracking;
                        }

                        for (Object o : element.getChildren("physvol")) {
                            Element physvol = (Element) o;
                            boolean skip = false;
                            if (targetTracking != null 
                                    && physvol.getChild("volumeref").getAttributeValue("ref").equals("tracking_volume"))
                                skip = true;
                            if (!skip)
                                targetVol.addContent((Element) physvol.clone());
                        }
                    }
                    // Generic merge-in of this element into target section, checking for duplicates.
                    else {
                        // Check for dup names in target section.
                        List targetElements = target.getChildren(element.getName());
                        boolean dup = false;
                        for (Object o : targetElements) {
                            Element targetElement = (Element) o;
                            if (targetElement.getAttributeValue("name").equals(element.getAttributeValue("name"))) {
                                dup = true;
                                break;
                            }
                        }

                        if (!dup)
                            target.addContent((Element) element.clone());
                    }
                }
            }
        }
    }
}
