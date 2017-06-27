package org.lcsim.geometry.compact;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.PhysicalVolumeNavigator;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.material.MaterialManager;
import org.lcsim.material.XMLMaterialManager;

import Jama.Matrix;

/**
 * Default class created to represent the parsed detector.
 * 
 * @author tonyj
 * @author jeremym
 */
public class Detector {
    
    private Header header;
    private Map<String, Constant> constants = new HashMap<String, Constant>();
    private Map<String, Subdetector> subdetectors = new HashMap<String, Subdetector>();
    private Map<String, Readout> readouts = new HashMap<String, Readout>();
    private Map<String, Field> fields = new HashMap<String, Field>();
    private Map<String, LimitSet> limits = new HashMap<String, LimitSet>();
    private Map<String, Region> regions = new HashMap<String, Region>();
    private Map<String, VisAttributes> display = new HashMap<String, VisAttributes>();
    private Map<String, Matrix> matrices = new HashMap<String, Matrix>();
    private XMLMaterialManager materialMgr; // Setup externally by CompactReader.
    private List<URL> gdmlReferences = new ArrayList<URL>();

    private SystemIDMap idmap = new SystemIDMap();

    private IPhysicalVolume worldVolume;
    private IPhysicalVolume trackingVolume;
    private IPhysicalVolumeNavigator navigator;

    private IDetectorElement de;

    String name;

    /**
     * Called by the reader to create a new Detector
     * 
     * @param element The JDOM element corresponding to the detector definition in the XML file.
     */
    protected Detector(Element element) {
        if (element != null) {
            name = element.getChild("info").getAttributeValue("name");
        }
    }

    protected void setXMLMaterialManager(XMLMaterialManager xmat) {
        materialMgr = xmat;
    }

    public String getName() {
        return name;
    }

    public IPhysicalVolume getWorldVolume() {
        return worldVolume;
    }

    public void setWorldVolume(IPhysicalVolume worldVolume) {
        this.worldVolume = worldVolume;
        navigator = new PhysicalVolumeNavigator("world", worldVolume);
        trackingVolume = navigator.getPath("/tracking_region").getLeafVolume();
    }

    public IPhysicalVolume getTrackingVolume() {
        return trackingVolume;
    }

    public IPhysicalVolumeNavigator getNavigator() {
        return navigator;
    }

    /**
     * Called by the reader to associate a header with this detector
     * 
     * @param header The header.
     */
    protected void setHeader(Header header) {
        this.header = header;
    }

    /**
     * Get the header associated with this detector.
     * 
     * @return The header
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Get the detector name from the header.
     * 
     * @return the detector name
     */
    public String getDetectorName() {
        return getHeader().getDetectorName();
    }

    /**
     * Called by the reader to add a constant to this detector.
     * 
     * @param c The constant to add.
     */
    protected void addConstant(Constant c) {
        constants.put(c.getName(), c);
    }

    /**
     * Get the constants associated with this detector.
     * 
     * @return A map of containing all of the constants, indexed by name.
     */
    public Map<String, Constant> getConstants() {
        return constants;
    }
    
    /**
     * Add a matrix associated with this Detector, e.g. for material properties.
     * @param name The name of the matrix.
     * @param matrix The matrix object.
     */
    public void addMatrix(String name, Matrix matrix) {
        this.matrices.put(name, matrix);
    }
    
    /**
     * Get a matrix associated with this Detector.
     * @param name The name of the matrix.
     * @return The matrix or null if it does not exist.
     */
    public Matrix getMatrix(String name) {
        return this.matrices.get(name);
    }
    
    /**
     * Get the map of named matrices.
     * @return The map of matrices for this detector.
     */
    public Map<String, Matrix> getMatrices() {
        return this.matrices;
    }

    /**
     * Called by the reader to add a new Readout to this detector.
     * 
     * @param r The readout to add.
     */
    protected void addReadout(Readout r) {
        // System.out.println("geometry.compact.Detector.addReadout() - " + r.getName() );
        readouts.put(r.getName(), r);
    }

    /**
     * Get the readouts associated with this detector.
     * 
     * @return A map of containing all of the readouts, indexed by name.
     */
    public Map<String, Readout> getReadouts() {
        return readouts;
    }

    /**
     * Convenience method to get readout by name.
     **/
    public Readout getReadout(String rn) {
        return readouts.get(rn);
    }

    /**
     * Called by the reader to add a sub-detector to this detector.
     * 
     * @param sub The sub-detector to add.
     */
    protected void addSubdetector(Subdetector sub) {
        // Check for a duplicate name.
        if (subdetectors.get(sub.getName()) != null) {
            throw new RuntimeException("The subdetector " + sub.getName() + " in detector " + this.getDetectorName() + " has the same name as another subdetector, which is not allowed.");
        }
        subdetectors.put(sub.getName(), sub);

        // Check for a duplicate system id.
        if (sub.getSystemID() != 0 && idmap.get(sub.getSystemID()) != null) {
            throw new RuntimeException("The subdetector " + sub.getName() + " in detector " + this.getDetectorName() + " duplicates the system ID " + sub.getSystemID() + " of the existing subdetector " + idmap.get(sub.getSystemID()).getName());
        }

        idmap.add(sub.getSystemID(), sub);
    }

    /**
     * Get a Subdetector by system ID.
     * 
     * @param sysid The system identifier.
     */
    public Subdetector getSubdetector(int sysid) {
        return idmap.get(sysid);
    }

    /**
     * Convenience method to retrieve subdetector by name.
     * 
     * @param subdetector with this name (null if doesn't exist)
     */
    public Subdetector getSubdetector(String name) {
        return subdetectors.get(name);
    }

    /**
     * Convenience method to retrieve set of detector names.
     * 
     * @return set of subdetector name strings
     */
    public Set<String> getSubdetectorNames() {
        return getSubdetectors().keySet();
    }

    /**
     * Get the sub-detectors associated with this detector.
     * 
     * @return A map of containing all of the sub-detectors, indexed by name.
     */
    public Map<String, Subdetector> getSubdetectors() {
        return subdetectors;
    }

    protected void addField(Field field) {
        fields.put(field.getName(), field);
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public final XMLMaterialManager getXMLMaterialManager() {
        return materialMgr;
    }

    public MaterialManager getMaterialManager() {
        return MaterialManager.instance();
    }

    public void addLimitSet(LimitSet limitset) {
        limits.put(limitset.getName(), limitset);
    }

    public LimitSet getLimitSet(String name) {
        return limits.get(name);
    }

    public Map<String, LimitSet> getLimits() {
        return limits;
    }

    public void addRegion(Region region) {
        regions.put(region.getName(), region);
    }

    public Region getRegion(String name) {
        return regions.get(name);
    }

    public Map<String, Region> getRegions() {
        return regions;
    }

    public void addVisAttributes(VisAttributes vis) {
        display.put(vis.getName(), vis);
    }

    public Map<String, VisAttributes> getVisAttributes() {
        return display;
    }

    public IDetectorElement getDetectorElement() {
        return de;
    }

    public void setDetectorElement(IDetectorElement de) {
        this.de = de;
    }

    public void addGDMLReference(URL url) {
        gdmlReferences.add(url);
    }

    public List<URL> getGDMLReferences() {
        return gdmlReferences;
    }

    public Calorimeter getCalorimeterByType(Calorimeter.CalorimeterType calType) {
        for (Subdetector subdet : this.subdetectors.values()) {
            if (subdet.isCalorimeter()) {
                Calorimeter cal = (Calorimeter) subdet;
                if (cal.getCalorimeterType() == calType)
                    return (Calorimeter) subdet;
            }
        }
        return null;
    }

    public List<Subdetector> getSubdetectorList() {
        return new ArrayList<Subdetector>(getSubdetectors().values());
    }
}
