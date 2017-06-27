package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Polycone;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.VisAttributes;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

/**
 * A cylindrical endcap calorimeter that has a tapered interior section.
 * 
 * @author jeremym
 */
public class TaperedCylindricalEndcapCalorimeter01 extends LCDDSubdetector
{
    public TaperedCylindricalEndcapCalorimeter01(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {       
        // Get important references from LCDD.
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
                
        // Get subdetector name and id.
        String subdetectorName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();      
        
        // Get the basic parameters from the XML.
        Element dimensions = node.getChild("dimensions");
        double zmin = dimensions.getAttribute("zmin").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        double rmax = dimensions.getAttribute("rmax").getDoubleValue();
        double innerAngle = dimensions.getAttribute("angle").getDoubleValue();
        
        // Make layering to get thickness.
        LayerStack layers = Layering.makeLayering(this.node).getLayerStack();
        
        // Get thickness of calorimeter in z dimension determined by layering.
        double thickness = layers.getTotalThickness();
                
        // Compute radius at last zplane of envelope.
        double rmin2 = rmin + thickness * Math.tan(innerAngle);
        
        // Create Polycone and ZPlanes.
        Polycone pc = new Polycone(subdetectorName + "_polycone");
        pc.addZPlane(rmin, rmax, -thickness/2);
        pc.addZPlane(rmin2, rmax, thickness/2);        
        lcdd.add(pc);
        
        // Create envelope volume.        
        Volume envelopeVolume = new Volume(subdetectorName + "_envelope", pc, lcdd.getMaterial("Air"));
        
        // Set the z coordinate of the layer front face for layer loop.
        double layerZ = -thickness/2;
        
        // The rmin of the layer which will decrease along z.
        double layerRmin = rmin;
        
        // Loop over and build the layers into the detector envelope.               
        for (int i=0, l=layers.getNumberOfLayers(); i<l; i++)
        {              
            //System.out.println(i+" : layerZ="+layerZ);
            
            // Get this layer's thickness.
            double layerThickness = layers.getLayer(i).getThickness();
            
            // Compute change in layer inner radius.
            double layerdy = Math.tan(innerAngle) * layerThickness;
            
            // Add dx to inner radius.
            layerRmin += layerdy;     
            
            String layerName = subdetectorName + "_layer" + i;
            
            Tube layerTube = new Tube(layerName + "_tube", layerRmin, rmax, layerThickness/2);
            lcdd.add(layerTube);

            // Make layer volume.            
            Volume layerVolume = 
                new Volume(layerName + "_volume", layerTube, lcdd.getMaterial("Air"));
            
            // Build slices into the layer volume.            
            double sliceZ = -layerThickness/2;
            int sliceCount = 0;
            for (LayerSlice slice : layers.getLayer(i).getSlices())
            {                
                double sliceThickness = slice.getThickness();
                
                String sliceName = layerName + "_slice" + sliceCount;
                
                Tube sliceTube = new Tube(sliceName + "_tube", layerRmin, rmax, sliceThickness/2);
                lcdd.add(sliceTube);
                
                Volume sliceVolume = new Volume(sliceName + "volume", sliceTube, lcdd.getMaterial("Air"));
                structure.addVolume(sliceVolume);
                
                // Set slice volume's sensitive detector.
                if (slice.isSensitive())
                    sliceVolume.setSensitiveDetector(sens);
                
                PhysVol slicePhysVol = new PhysVol(sliceVolume);                
                slicePhysVol.setZ(sliceZ + sliceThickness/2);
                slicePhysVol.addPhysVolID("slice", sliceCount);
                layerVolume.addPhysVol(slicePhysVol);
                                               
                sliceZ += sliceThickness;
                ++sliceCount;
            }            
            
            // Add layer volume after slices are built.
            lcdd.getStructure().addVolume(layerVolume);           
            
            // Make layer placement into envelope.
            PhysVol layerPhysVol = new PhysVol(layerVolume);
            layerPhysVol.addPhysVolID("layer", i);
            layerPhysVol.setZ(layerZ + layerThickness/2);
            envelopeVolume.addPhysVol(layerPhysVol);              
            
            // Set z to edge of next layer.
            layerZ += layerThickness;       
        }

        // Add envelope volume after layer construction.
        lcdd.add(envelopeVolume);
        
        // Make rotation for positive endcap.
        Rotation positiveEndcapRotation = new Rotation(subdetectorName + "_positive");
        lcdd.getDefine().addRotation(positiveEndcapRotation);
                
        // Positive endcap placement.
        PhysVol endcapPositivePhysVol = new PhysVol(envelopeVolume);
        endcapPositivePhysVol.setZ(zmin + thickness/2);
        endcapPositivePhysVol.addPhysVolID("system",id);
        endcapPositivePhysVol.addPhysVolID("barrel",1);                     
        endcapPositivePhysVol.setRotation(positiveEndcapRotation);                       
        motherVolume.addPhysVol(endcapPositivePhysVol);
                
        // Make rotation for negative endcap.        
        Rotation negativeEndcapRotation = new Rotation(subdetectorName + "_negative");
        negativeEndcapRotation.setY(Math.PI);
        lcdd.getDefine().addRotation(negativeEndcapRotation);
        
        // Negative endcap placement.
        PhysVol endcapNegativePhysVol = new PhysVol(envelopeVolume);
        endcapNegativePhysVol.setZ(-zmin - thickness/2);
        endcapNegativePhysVol.addPhysVolID("system",id);
        endcapNegativePhysVol.addPhysVolID("barrel",2);                     
        endcapNegativePhysVol.setRotation(negativeEndcapRotation);                       
        motherVolume.addPhysVol(endcapNegativePhysVol);                            
    }

    public boolean isEndcap()
    {
        return true;
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }
}