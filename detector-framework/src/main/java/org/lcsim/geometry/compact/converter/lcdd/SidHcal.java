package org.lcsim.geometry.compact.converter.lcdd;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.Layering;

/**
 * This class generates the LCDD for the SidHcal design.
 * 
 * TODO Add URL for design reference.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: SidHcal.java,v 1.2 2010/04/27 17:39:11 jeremy Exp $
 */
public class SidHcal extends LCDDSubdetector
{
    public SidHcal(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        // Get some important LCDD references.
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Material air = lcdd.getMaterial("Air");
        Define define = lcdd.getDefine();
        
        // Subdetector name and ID.
        String detName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();
        
        // Create layering object for this subdetector.
        Layering layering = Layering.makeLayering(this.node);

        // Total thickness of subdetector.
        double staveThickness = layering.getLayerStack().getTotalThickness();
        
        // Subdetector envelope dimensions.
        Element dimensions = node.getChild("dimensions");
        double detZ = dimensions.getAttribute("z").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        int nsides = dimensions.getAttribute("numsides").getIntValue();
        double gap = dimensions.getAttribute("gap").getDoubleValue();
        
        // Rotation of the envelope to make flat side down.
        double zrot = PI / nsides;
        Rotation rot = new Rotation(detName + "_rotation");
        rot.setZ(zrot);
        define.addRotation(rot);
                        
        double innerAngle = PI * 2 / nsides;
        double halfInnerAngle = innerAngle / 2;
        double innerFaceLength = rmin * tan(halfInnerAngle) * 2;
        
        // Box stave outer solid.
        Trapezoid sectATrdOuter = new Trapezoid(detName + "_staveA_outer");
        sectATrdOuter.setY2(detZ/2);
        sectATrdOuter.setY1(detZ/2);
        sectATrdOuter.setZ(staveThickness/2);
        sectATrdOuter.setX1(innerFaceLength/2);
        sectATrdOuter.setX2(innerFaceLength/2);
        solids.addSolid(sectATrdOuter);
                
        // Box stave outer volume.        
        Volume staveAVolumeOuter = new Volume(detName + "_staveA_outer_vol");
        staveAVolumeOuter.setMaterial(air);
        staveAVolumeOuter.setSolid(sectATrdOuter);        
        
        // Box stave inner solid.
        Trapezoid sectATrdInner = new Trapezoid(detName + "_staveA_inner");
        sectATrdInner.setY2(detZ/2);
        sectATrdInner.setY1(detZ/2);
        sectATrdInner.setZ(staveThickness/2);
        sectATrdInner.setX1(innerFaceLength/2 - gap);
        sectATrdInner.setX2(innerFaceLength/2 - gap);
        solids.addSolid(sectATrdInner);
        
        // Box stave inner volume.        
        Volume staveAVolumeInner = new Volume(detName + "_staveA_inner_vol");
        staveAVolumeInner.setMaterial(air);
        staveAVolumeInner.setSolid(sectATrdInner);
        structure.addVolume(staveAVolumeInner);
        
        // Place inner box stave inside of outer to make skin.
        PhysVol staveAPhysVol = new PhysVol(staveAVolumeInner);
        staveAVolumeOuter.addPhysVol(staveAPhysVol);
        
        // Add box stave outer volume to structure.
        structure.addVolume(staveAVolumeOuter);
                       
        // Trap stave outer solid.        
        double trdBottomHalf = rmin * tan(PI/nsides);
        double trdTopHalf = rmin * tan(PI/nsides) + staveThickness * sin (PI/nsides * 2);
        double trdHalfThickness = (staveThickness * cos((PI/nsides) * 2)) / 2;
        Trapezoid sectBTrdOuter = new Trapezoid(detName + "_staveB_outer");
        sectBTrdOuter.setY2(detZ/2);
        sectBTrdOuter.setY1(detZ/2);
        sectBTrdOuter.setZ(trdHalfThickness);
        sectBTrdOuter.setX1(trdBottomHalf);
        sectBTrdOuter.setX2(trdTopHalf);
        solids.addSolid(sectBTrdOuter);
        
        // Trap stave outer volume.        
        Volume staveBVolumeOuter = new Volume(detName + "_staveB_outer_vol");
        staveBVolumeOuter.setMaterial(air);
        staveBVolumeOuter.setSolid(sectBTrdOuter);        
        
        // Box stave inner solid.
        Trapezoid sectBTrdInner = new Trapezoid(detName + "_staveB_inner");
        sectBTrdInner.setY2(detZ/2);
        sectBTrdInner.setY1(detZ/2);
        sectBTrdInner.setZ(trdHalfThickness);
        sectBTrdInner.setX1(trdBottomHalf - gap);
        sectBTrdInner.setX2(trdTopHalf - gap);
        solids.addSolid(sectBTrdInner);
        
        // Trap stave inner volume.
        Volume staveBVolumeInner = new Volume(detName + "_staveB_inner_vol");
        staveBVolumeInner.setMaterial(air);
        staveBVolumeInner.setSolid(sectBTrdInner);
        structure.addVolume(staveBVolumeInner);
        
        // Place inner box stave inside of outer to make skin.
        PhysVol staveBPhysVol = new PhysVol(staveBVolumeInner);
        staveBVolumeOuter.addPhysVol(staveBPhysVol);
        
        // Add trap stave outer volume to structure.
        structure.addVolume(staveBVolumeOuter);
                        
        // Place the box staves.
        placeStaves(motherVolume, define, detName, rmin + staveThickness / 2, nsides, innerAngle, staveAVolumeOuter, 0);
        
        // Place the trap staves.
        placeStaves(motherVolume, define, detName, rmin + trdHalfThickness, nsides, innerAngle, staveBVolumeOuter, 2 * PI / nsides);
    }

    private void placeStaves(Volume motherVolume, Define define, String detName, double rMid, int nsides, double innerAngle, Volume staveVolume, double startRot)
    {
        // Setup initial parameters for box stave placement and rotation.
        double rotY = startRot;
        double rotX = PI / 2;
        double posX = -rMid * sin(rotY);
        double posY = rMid * cos(rotY);
        int moduleNumber = 0;
        
        // Place box staves.
        for (int i=0; i<nsides; i++)
        {            
            Position position = new Position(staveVolume.getVolumeName() + "_module" + moduleNumber + "_position");
            position.setX(posX);
            position.setY(posY);

            Rotation rotation = new Rotation(staveVolume.getVolumeName() + "_module" + moduleNumber + "_rotation");
            rotation.setX(rotX);
            rotation.setY(rotY);

            define.addPosition(position);
            define.addRotation(rotation);
            
            PhysVol sectPhysVol = new PhysVol(staveVolume);
            sectPhysVol.setPosition(position);
            sectPhysVol.setRotation(rotation);

            motherVolume.addPhysVol(sectPhysVol);
            sectPhysVol.addPhysVolID("stave", 0);
            sectPhysVol.addPhysVolID("module", moduleNumber);
                                 
            // Increment the parameters for next stave.
            rotY -= innerAngle * 2;
            posX = -rMid * sin(rotY);
            posY = rMid * cos(rotY);            
            moduleNumber += 2;            
        }
    }
}