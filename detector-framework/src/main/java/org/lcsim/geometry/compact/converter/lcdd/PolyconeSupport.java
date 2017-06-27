/*
 * PolyconeSupport.java
 *
 * Created on May 3, 2005, 2:55 PM
 */
package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;

import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Polycone;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 * -reorganize and rename 090220 JM
 * 
 * @author jeremym
 */
public class PolyconeSupport extends LCDDSubdetector {

    private Element node;

    /** Creates a new instance of PolyconeSupport */
    public PolyconeSupport(Element node) throws JDOMException {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException {
        
        // Get the lcdd data structures.
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();

        // Name of support structure.
        String supportName = node.getAttributeValue("name");

        // polycone solid
        Polycone pc = new Polycone(supportName + "_envelope_polycone", 0, 2. * Math.PI, node);
        solids.addSolid(pc);

        // Material is required.
        Element matElem = node.getChild("material");
        if (matElem == null) {
            throw new JDOMException("Required material element not found.");
        }
        Material mat = lcdd.getMaterial(matElem.getAttributeValue("name"));

        // Material was not found.
        if (mat == null) {
            throw new JDOMException("Material not found in compact file.");
        }

        // Create the volume.
        Volume vol = new Volume(supportName + "_envelope_volume");
        vol.setMaterial(mat);
        vol.setSolid(pc);
        
        // Set region.
        setRegion(lcdd, mat, vol);

        // Set the volume display.
        setVisAttributes(lcdd, node, vol);

        // Add the volume to lcdd.
        structure.addVolume(vol);

        // Let lcdd pick the mother volume.
        Volume motherVolume = lcdd.pickMotherVolume(this);

        // Finally, make the physical volume. (supports have no id)
        PhysVol physvol = new PhysVol(vol);
        motherVolume.addPhysVol(physvol);
    }
}