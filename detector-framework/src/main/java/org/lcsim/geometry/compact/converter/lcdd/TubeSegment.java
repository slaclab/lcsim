package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 * LCDD implementation of a TubeSegment for beampipe elements.
 *
 * @author Jeremy McCormick
 * @version $Id: TubeSegment.java,v 1.3 2009/12/07 23:01:00 jeremy Exp $
 */
public class TubeSegment
extends LCDDSubdetector
{
    public TubeSegment(Element node) throws JDOMException
    {
        super(node);
    }
    
    void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        // Find the mother volume
        Volume mother = null;
        if (isInsideTrackingVolume())
        {
            mother = lcdd.getTrackingVolume();
        }
        else
        {
            mother = lcdd.getWorldVolume();
        }
        
        // Name
        String name = node.getAttributeValue("name");
        
        // Id
        int id = -1;
        if (node.getAttribute("id") != null)
            id = node.getAttribute("id").getIntValue();
        
        // Material
        Material material = lcdd.getMaterial(node.getChild("material").getAttributeValue("name"));
        
        // Tube
        Tube tube = null;
        if (node.getChild("tubs") != null)
            tube = new Tube(name + "_tube", node.getChild("tubs"));
        else
            throw new RuntimeException("TubeSegment " + name + " is missing required tubs element");                   
        lcdd.add(tube);
        
        // Volume
        Volume volume = new Volume(name, tube, material);
        lcdd.add(volume);
        
        // Position
        Position position;
        if (node.getChild("position") != null)
            position = new Position(name + "_position", node.getChild("position"));
        else
            position = new Position(name + "_position");
        lcdd.add(position);
                
        // Rotation
        Rotation rotation = null;
        if (node.getChild("rotation") != null)
            rotation = new Rotation(name + "_rotation", node.getChild("rotation"));
        else
            rotation = new Rotation(name + "_rotation");
        lcdd.add(rotation);
        
        // Set the volume display.
        setVisAttributes(lcdd, node, volume);
        
        // Physical volume.
        PhysVol tubePV = new PhysVol(volume, mother, position, rotation);
        tubePV.addPhysVolID("id", id);
    }
}