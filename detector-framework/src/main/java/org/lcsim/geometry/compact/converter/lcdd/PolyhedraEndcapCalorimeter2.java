package org.lcsim.geometry.compact.converter.lcdd;

import java.util.Iterator;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.PolyhedraRegular;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

public class PolyhedraEndcapCalorimeter2 extends LCDDSubdetector
{
    public PolyhedraEndcapCalorimeter2(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Material air = lcdd.getMaterial("Air");
        Define define = lcdd.getDefine();
        Rotation identityRotation = define.getRotation("identity_rot");
        Element staves = node.getChild("staves");
        
        String subdetectorName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();
        
        if (node.getChild("dimensions") == null)
            throw new RuntimeException("PolhedraEndcapCalorimeter2 " + subdetectorName
                    + " is missing required <dimensions> element.");        
        
        Element dimensions = node.getChild("dimensions");
        double zmin = dimensions.getAttribute("zmin").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        double rmax = dimensions.getAttribute("rmax").getDoubleValue();
        int numsides = dimensions.getAttribute("numsides").getIntValue();
        
        LayerStack layers = Layering.makeLayering(this.node).getLayerStack();
    
        // Geant4 interprets rmax as the distance to the polygonal flat.
        // We want to interpret this as the distance to the point.
        rmax = rmax*Math.cos(Math.PI/numsides);
                
        double subdetectorThickness = org.lcsim.geometry.layer.LayerFromCompactCnv.computeDetectorTotalThickness(node);                        

        PolyhedraRegular envelopeSolid = new PolyhedraRegular(subdetectorName + "_envelope", numsides, rmin, rmax, subdetectorThickness);
        solids.addSolid(envelopeSolid);
        Volume envelopeVolume = new Volume(subdetectorName + "_volume", envelopeSolid, air);
                                
        int layerNumber = 0;
        int layerType = 0;
        double layerZ = -subdetectorThickness/2;
        for (Iterator<Element> i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layerElement = i.next();
            double layerThickness = layers.getLayer(layerNumber).getThickness();
            String layerTypeName = subdetectorName + "_layer" + layerType;            
            if (layerElement.getAttribute("repeat") == null)
            {
                throw new RuntimeException("Missing the repeat number in layering for " + subdetectorName);
            }
            PolyhedraRegular layerSolid = new PolyhedraRegular(layerTypeName + "_solid", numsides, rmin, rmax, layerThickness);
            solids.addSolid(layerSolid);
            Volume layerVolume = new Volume(layerTypeName + "_volume", layerSolid, air);
               
            int sliceNumber = 0;
            double sliceZ = -layerThickness/2;
            for (Iterator j = layerElement.getChildren("slice").iterator(); j.hasNext();)
            {
                String sliceName = layerTypeName + "_slice" + sliceNumber;
                Element sliceElement = (Element) j.next();
                if (sliceElement.getAttribute("thickness") == null)
                    throw new RuntimeException("Missing thickness attribute.");
                double sliceThickness = sliceElement.getAttribute("thickness").getDoubleValue();
                if (sliceElement.getAttribute("material") == null)
                    throw new RuntimeException("Missing material attribute.");
                String materialName = sliceElement.getAttribute("material").getValue();
                Material sliceMaterial = lcdd.getMaterial(materialName);
                PolyhedraRegular sliceSolid = new PolyhedraRegular(sliceName + "_solid", numsides, rmin, rmax, sliceThickness);
                solids.addSolid(sliceSolid);
                Volume sliceVolume = new Volume(sliceName + "_volume", sliceSolid, sliceMaterial);
                if (sliceElement.getAttribute("sensitive") != null)
                {
                	if (sliceElement.getAttribute("sensitive").getBooleanValue() == true)
                	{
                		sliceVolume.setSensitiveDetector(sens);
                	}
                }
                
                // Visualization of slice.
                this.setVisAttributes(lcdd, sliceElement, sliceVolume);
                //if (sliceElement.getAttribute("vis") != null)
                //{
                //    sliceVolume.setVisAttributes(lcdd.getVisAttributes(.getAttributeValue("vis")));
                //}
                structure.addVolume(sliceVolume);
                sliceZ += sliceThickness/2;
                Position slicePosition = new Position(sliceName + "_position", 0, 0, sliceZ);
                define.addPosition(slicePosition);
                sliceZ += sliceThickness/2;
                PhysVol slicePhysVol = new PhysVol(sliceVolume, layerVolume, slicePosition, identityRotation);
                slicePhysVol.addPhysVolID("slice", sliceNumber);
                sliceNumber++;
            }
            
            // Visualization of layer.
            if (layerElement.getAttribute("vis") != null)
            {
                layerVolume.setVisAttributes(lcdd.getVisAttributes(layerElement.getAttributeValue("vis")));
            }            
            
            sliceNumber = 0;
            structure.addVolume(layerVolume);        
            int repeat = (int)layerElement.getAttribute("repeat").getDoubleValue();
            if (repeat <= 0)
                throw new RuntimeException("The repeat value of " + repeat + " is invalid.");          
            for ( int j=0; j<repeat; j++)
            {
                String physLayerName = subdetectorName + "_layer" + layerNumber;
                layerZ += layerThickness/2;
                Position layerPosition = new Position(physLayerName + "_position", 0, 0, layerZ);
                define.addPosition(layerPosition);
                PhysVol layerPhysVol = new PhysVol(layerVolume, envelopeVolume, layerPosition, identityRotation);
                layerPhysVol.addPhysVolID("layer", layerNumber);
                layerZ += layerThickness/2;
                ++layerNumber;
            }
            ++layerType;            
        }
                
        // The envelope volume is complete.        
        structure.addVolume(envelopeVolume);
        
        // Set envelope volume attributes.
        setAttributes(lcdd, node, envelopeVolume);
                
        // Positive endcap.
        
        double zrot = Math.PI / numsides;
        PhysVol physvol = new PhysVol(envelopeVolume);
        physvol.setZ(zmin+subdetectorThickness/2);
        physvol.addPhysVolID("system",id);
        physvol.addPhysVolID("barrel",1);        
        Rotation positiveEndcapRotation = new Rotation(subdetectorName + "_positive");
        positiveEndcapRotation.setZ(zrot);
        lcdd.getDefine().addRotation(positiveEndcapRotation);
        physvol.setRotation(positiveEndcapRotation);        
        motherVolume.addPhysVol(physvol);
        
        // Negative endcap.
        
        // Assume 2 endcaps but check for setting.
        boolean reflect = true;
        if (node.getAttribute("reflect") != null )
        {
        	reflect = node.getAttribute("reflect").getBooleanValue();
        }
        	
        if (reflect)
        {
            PhysVol physvol2 = new PhysVol(envelopeVolume);
            physvol2.setZ(-zmin-subdetectorThickness/2);
            Rotation negativeEndcapRotation = new Rotation(subdetectorName + "_negative");
            lcdd.getDefine().addRotation(negativeEndcapRotation);
            negativeEndcapRotation.setX(Math.PI);
            negativeEndcapRotation.setZ(zrot);
            
            physvol2.setRotation(negativeEndcapRotation);
            physvol2.addPhysVolID("system",id);
            physvol2.addPhysVolID("barrel",2);
            motherVolume.addPhysVol(physvol2);
        }
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }    
}
