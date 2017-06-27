package org.lcsim.geometry.compact.converter.lcdd;

import java.util.ArrayList;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Polyhedra;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.compact.converter.lcdd.util.ZPlane;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

/**
 * This class implements the LCDD binding for a calorimeter with a cone hollow section,
 * e.g. for Muon Collider calorimeter designs.
 */
public class PolyhedraEndcapCalorimeter3 extends LCDDSubdetector
{
    public PolyhedraEndcapCalorimeter3(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {    	
    	// Get important references from LCDD.
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Material air = lcdd.getMaterial("Air");
        
        // Get subdetector name and id.
        String subdetectorName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();        
        
        // Get the basic parameters from the XML.
        Element dimensions = node.getChild("dimensions");
        int numsides = dimensions.getAttribute("numsides").getIntValue();
        double zmin = dimensions.getAttribute("zmin").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        double rmax = dimensions.getAttribute("rmax").getDoubleValue();
        rmax = rmax*Math.cos(Math.PI/numsides); // Compute G4 rmax from dimension value.
        double innerAngle = dimensions.getAttribute("angle").getDoubleValue();
        
        // Make layering to get thickness.
        LayerStack layers = Layering.makeLayering(this.node).getLayerStack();
        
        // Get thickness of calorimeter in z dimension determined by layering.
        double thickness = layers.getTotalThickness();
                         
        // Compute radius at last zplane of envelope.
        double rmax2 = rmin + thickness * Math.tan(innerAngle);               
        
        // Setup envelope volume so that rmin increases along +z.
        ArrayList<ZPlane> zplanes = new ArrayList<ZPlane>();
        ZPlane frontZPlane = new ZPlane(rmin, rmax, -thickness/2);
        ZPlane backZPlane = new ZPlane(rmax2, rmax, thickness/2);
        zplanes.add(frontZPlane);
        zplanes.add(backZPlane);
        Polyhedra envelopePolyhedra = new Polyhedra(
                subdetectorName + "_envelope_polyhedra",
                numsides,
                zplanes);        
        solids.addSolid(envelopePolyhedra);
        
        // Make the calorimeter envelope volume.
        Volume envelopeVolume = new Volume(subdetectorName + "_volume", envelopePolyhedra, air);        
        
        // Compute z rotation for envelope with flat side down.        
        double moduleInnerAngle = (Math.PI * 2) / numsides;
        double leftover = (Math.PI / 2) % moduleInnerAngle;
        double zrot = moduleInnerAngle / 2 - leftover;
                
        // Set the z coordinate of the layer front face for layer loop.
        double layerZ = -thickness/2;
        
        // The rmin of the layer which will decrease along z.
        double layerRmin = rmin;
                        
        // Loop over and build the layers into the detector envelope.
        for (int i=0, l=layers.getNumberOfLayers(); i<l; i++)
        {                                   
            // Get this layer's thickness.
            double layerThickness = layers.getLayer(i).getThickness();
            
            // Compute change in layer inner radius.
            double layerdy = Math.tan(innerAngle) * layerThickness;
            
            // Add dx to inner radius.
            layerRmin += layerdy;
                                                                           
            // Make the layer's polyhedra.
            ArrayList<ZPlane> layerZplanes = new ArrayList<ZPlane>();
            ZPlane layerFrontZPlane = new ZPlane(layerRmin, rmax, -layerThickness/2);
            ZPlane layerBackZPlane = new ZPlane(layerRmin, rmax, layerThickness/2);
            layerZplanes.add(layerFrontZPlane);
            layerZplanes.add(layerBackZPlane);
            Polyhedra layerPolyhedra = new Polyhedra(
                    subdetectorName + "_layer" + i + "_polyhedra",
                    numsides,
                    layerZplanes);
            
            // Add layer volume to solids.
            solids.addContent(layerPolyhedra);
            
            // Make layer volume.            
            Volume layerVolume = 
                new Volume(subdetectorName + "_layer" + i + "_volume", layerPolyhedra, air);                     
            
            // Build slices into the layer volume.
            double sliceZ = -layerThickness/2;
            int sliceCount = 0;
            for (LayerSlice slice : layers.getLayer(i).getSlices())
            {                
            	// Get the slice's thickness.
            	double sliceThickness = slice.getThickness();
            	
            	// Make the slice's polyhedra shape.
            	ArrayList<ZPlane> sliceZplanes = new ArrayList<ZPlane>();
            	ZPlane sliceFrontZPlane = new ZPlane(layerRmin, rmax, -sliceThickness/2);
            	ZPlane sliceBackZPlane = new ZPlane(layerRmin, rmax, sliceThickness/2);
            	sliceZplanes.add(sliceFrontZPlane);
            	sliceZplanes.add(sliceBackZPlane);
            	Polyhedra slicePolyhedra = new Polyhedra(
            			subdetectorName + "_layer" + i + "_slice" + sliceCount + "_polyhedra",
            			numsides,
            			sliceZplanes
            			);
            	
            	// Add slice volume to solids.
            	solids.addContent(slicePolyhedra);
            	
            	// Make slice volume.
            	Volume sliceVolume = 
            		new Volume(
            				subdetectorName + "_layer" + i + "_slice" + sliceCount + "_volume",
            				slicePolyhedra,
            				lcdd.getMaterial(slice.getMaterial().getName()));
            	structure.addContent(sliceVolume);
            	
            	// Set slice volume's sensitive detector.
            	if (slice.isSensitive())
            		sliceVolume.setSensitiveDetector(sens);
            	
            	// Make slice physical volume.
            	PhysVol slicePhysVol = new PhysVol(sliceVolume);
            	slicePhysVol.setZ(sliceZ + sliceThickness/2);
            	slicePhysVol.addPhysVolID("slice", sliceCount);
            	
            	// Add slice physical volume to layer.
            	layerVolume.addPhysVol(slicePhysVol);
            	
            	// Increment slice count.
            	++sliceCount;
            	
            	// Add thickness to get Z for next slice.
            	sliceZ += sliceThickness;
            }                                    
            
            // Add layer volume after slices are built.
            structure.addVolume(layerVolume);
            
            // Make layer placement into envelope.
            PhysVol layerPhysVol = new PhysVol(layerVolume);
            layerPhysVol.addPhysVolID("layer", i);
            layerPhysVol.setZ(layerZ + layerThickness/2);
            envelopeVolume.addPhysVol(layerPhysVol);              
            
            // Set z to edge of next layer.
            layerZ += layerThickness;
        }
        
        // Add envelope's logical volume to structure.
        structure.addVolume(envelopeVolume);
        
        // Make rotation for positive endcap.
        Rotation positiveEndcapRotation = new Rotation(subdetectorName + "_positive");
        positiveEndcapRotation.setZ(zrot);
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
        negativeEndcapRotation.setZ(zrot);
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
    
    public boolean isCalorimeter()
    {
    	return true;
    }
}