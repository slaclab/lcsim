package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Cone;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

public class CylindricalEndcapCalorimeter2 extends LCDDSubdetector
{
    CylindricalEndcapCalorimeter2(Element c) throws JDOMException
    {
        super(c);
    }

    void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        // Get subdetector id and name.
        int id = node.getAttribute("id").getIntValue();
        String subdetectorName = node.getAttributeValue("name");

        // Get important references from LCDD.
        Material air = lcdd.getMaterial("Air");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);

        // Get the subdetector dimensions.
        Element dimensions = node.getChild("dimensions");
        double zmin = dimensions.getAttribute("zmin").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        double rmax = dimensions.getAttribute("rmax").getDoubleValue();
        double innerAngle = dimensions.getAttribute("angle").getDoubleValue();

        // Make layering object.
        LayerStack layers = Layering.makeLayering(this.node).getLayerStack();

        // Get thickness of calorimeter in z dimension determined by layering.
        double thickness = layers.getTotalThickness();

        // Compute radius at last zplane of envelope.
        double rmin2 = rmin + (thickness * Math.tan(innerAngle));

        // Make the subdetector's envelope cone.
        // Note: Z measurement is full length, which will be divided by two for half-length by GDML.
        Cone envelopeCone = new Cone(subdetectorName + "_envelope_cone", rmin, rmin2, rmax, rmax, thickness);
        solids.addSolid(envelopeCone);

        // Make the calorimeter envelope volume.
        Volume envelopeVolume = new Volume(subdetectorName + "_volume", envelopeCone, air);

        // Set the absolute z coordinate of the layer front face for layer loop.
        double layerZ = -thickness / 2;

        // The rmin of the layer which will decrease along z.
        double layerRmin = rmin;
                
        // Loop over and build the layers into the detector envelope.
        for (int i = 0, l = layers.getNumberOfLayers(); i < l; i++)
        {            
            // Get this layer's thickness.
            double layerThickness = layers.getLayer(i).getThickness();

            // Compute change in layer inner radius.
            double layerdx = Math.tan(innerAngle) * layerThickness;

            // Add dx to inner radius.
            layerRmin += layerdx;

            // Make layer tube.
            // Note: This shape wants the z half-length instead of full z length. 
            Tube layerTube = new Tube(subdetectorName + "_layer" + i + "_tube", layerRmin, rmax, layerThickness / 2);
            solids.addSolid(layerTube);

            // Make layer volume.
            Volume layerVolume = new Volume(subdetectorName + "_layer" + i + "_volume", layerTube, air);

            // Slice loop.
            double sliceZ = -layerThickness / 2;
            int sliceCount = 0;
            for (LayerSlice slice : layers.getLayer(i).getSlices())
            {                
                // Get slice thickness.
                double sliceThickness = slice.getThickness();

                // Make slice tube.
                Tube sliceTube = 
                    new Tube(subdetectorName + "_layer" + i + "_slice" + sliceCount + "_tube", layerRmin, rmax, sliceThickness / 2);
                solids.addSolid(sliceTube);
                
                // Make slice volume.
                Volume sliceVolume = new Volume(subdetectorName + "_layer" + i + "_slice" + sliceCount + "_volume", sliceTube, lcdd.getMaterial(slice.getMaterial().getName()));
                structure.addVolume(sliceVolume);
                
                // Set volume sensitivity.
                if (slice.isSensitive())
                    sliceVolume.setSensitiveDetector(sens);
                
                // Make slice placement.
                PhysVol slicePhysVol = new PhysVol(sliceVolume);
                slicePhysVol.setZ(sliceZ + sliceThickness / 2);
                slicePhysVol.addPhysVolID("slice", sliceCount);
                layerVolume.addPhysVol(slicePhysVol);
                
                // Add slice's thickness to slice Z position.
                sliceZ += sliceThickness;
                
                // Increment slice count.
                ++sliceCount;
            }
             
            // Add layer volume to structure.
            structure.addVolume(layerVolume);

            // Make layer placement into envelope.
            PhysVol layerPhysVol = new PhysVol(layerVolume);
            layerPhysVol.addPhysVolID("layer", i);
            layerPhysVol.setZ(layerZ + layerThickness / 2);  
            envelopeVolume.addPhysVol(layerPhysVol);

            // Set z to edge of next layer.
            layerZ += layerThickness;                
        }

        // Add envelope's logical volume to structure.
        structure.addVolume(envelopeVolume);

        // Positive endcap placement.
        PhysVol endcapPositivePhysVol = new PhysVol(envelopeVolume);
        endcapPositivePhysVol.addPhysVolID("system", id);
        endcapPositivePhysVol.addPhysVolID("barrel", 1);        
        endcapPositivePhysVol.setZ(zmin + thickness / 2);
        motherVolume.addPhysVol(endcapPositivePhysVol);
        
        // Negative endcap placement.
        PhysVol endcapNegativePhysVol = new PhysVol(envelopeVolume);
        endcapNegativePhysVol.addPhysVolID("system", id);
        endcapNegativePhysVol.addPhysVolID("barrel", 2);
        endcapNegativePhysVol.setZ(-zmin - thickness / 2);
        Rotation negativeEndcapRotation = new Rotation(subdetectorName + "_negative_rotation");
        lcdd.getDefine().addRotation(negativeEndcapRotation);
        negativeEndcapRotation.setX(Math.PI);
        endcapNegativePhysVol.setRotation(negativeEndcapRotation);
        motherVolume.addPhysVol(endcapNegativePhysVol);
    }

    public boolean isCalorimeter()
    {
        return true;
    }
}