package org.lcsim.geometry.compact.converter.lcdd;

import java.util.Iterator;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 *
 * @author tonyj
 */
class CylindricalEndcapCalorimeter extends LCDDSubdetector
{
    CylindricalEndcapCalorimeter(Element node) throws JDOMException
    {
        super(node);
    }
    
    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        int id = node.getAttribute("id").getIntValue();
        String detectorName = node.getAttributeValue("name");
        boolean reflect = node.getAttribute("reflect").getBooleanValue();
        
        Material air = lcdd.getMaterial("Air");
        Rotation reflection = lcdd.getDefine().getRotation("reflect_rot");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        
        Element dimensions = node.getChild("dimensions");
        double zmin = dimensions.getAttribute("inner_z").getDoubleValue();
        double rmin = dimensions.getAttribute("inner_r").getDoubleValue();
        double rmax = dimensions.getAttribute("outer_r").getDoubleValue();
        
        double totWidth = layers.getLayerStack().getTotalThickness();
        //LayerFromCompactCnv.computeDetectorThickness(node);
        double z = zmin;
        
        Tube envelope = new Tube(detectorName+"_envelope");
        solids.addSolid(envelope);
        Volume envelopeVolume = new Volume(detectorName+"_envelope_volume");
        envelopeVolume.setMaterial(air);
        envelopeVolume.setSolid(envelope);        
                
        int n = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layer = (Element) i.next();
            int repeat = (int)layer.getAttribute("repeat").getDoubleValue();
            
            double layerWidth = 0;
            for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext();)
            {
                Element slice = (Element) j.next();
                double w = slice.getAttribute("thickness").getDoubleValue();
                layerWidth += w;
            }
            
            for (int ll=0; ll<repeat; ll++)
            {
                double zlayer = z;
                String layerName = detectorName+"_layer"+n;
                Tube layerTube = new Tube(layerName);
                Volume layerVolume = new Volume(layerName+"_volume");
                layerVolume.setMaterial(air);
                layerVolume.setSolid(layerTube);
                
                int m = 0;
                for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext(); m++)
                {
                    Element slice = (Element) j.next();
                    double w = slice.getAttribute("thickness").getDoubleValue();
                    Attribute s = slice.getAttribute("sensitive");
                    boolean sensitive = s != null && s.getBooleanValue();
                    
                    String name = detectorName+"_layer"+n+"_slice"+m;
                    Tube tube = new Tube(name);
                    tube.setZ(w);
                    tube.setRMin(rmin);
                    tube.setRMax(rmax);
                    tube.setDeltaPhi(Math.PI*2);
                    solids.addSolid(tube);
                    
                    Volume sliceVolume = new Volume(name+"_volume");
                    sliceVolume.setMaterial(lcdd.getMaterial(slice.getAttributeValue("material")));
                    sliceVolume.setSolid(tube);
                    if (sensitive) sliceVolume.setSensitiveDetector(sens);
                    
                    setRegion(lcdd, slice, sliceVolume);                    
                    setLimitSet(lcdd, slice, sliceVolume);                    
                    setVisAttributes(lcdd, slice, sliceVolume);
                    
                    structure.addVolume(sliceVolume);
                    
                    PhysVol physvol = new PhysVol(sliceVolume);
                    physvol.setZ(z - zlayer - layerWidth/2 + w/2);
                    layerVolume.addPhysVol(physvol);
                    
                    z += w;
                }
                
                setVisAttributes(lcdd, layer, layerVolume);
                
                layerTube.setZ(layerWidth);
                layerTube.setRMin(rmin);
                layerTube.setRMax(rmax);
                layerTube.setDeltaPhi(Math.PI*2);
                
                PhysVol physvol = new PhysVol(layerVolume);
                physvol.setZ(zlayer - zmin - totWidth/2 + layerWidth/2);
                physvol.addPhysVolID("layer",n);
                envelopeVolume.addPhysVol(physvol);
                structure.addVolume(layerVolume);
                solids.addSolid(layerTube);
                n++;                
            }            
        }
        
        envelope.setZ(totWidth);
        envelope.setRMin(rmin);
        envelope.setRMax(rmax);
        envelope.setDeltaPhi(Math.PI*2);
        
        PhysVol physvol = new PhysVol(envelopeVolume);
        physvol.setZ(zmin+totWidth/2);
        physvol.addPhysVolID("system",id);
        physvol.addPhysVolID("barrel",1);
        motherVolume.addPhysVol(physvol);
        
        if (reflect)
        {
            PhysVol physvol2 = new PhysVol(envelopeVolume);
            physvol2.setZ(-zmin-totWidth/2);
            physvol2.setRotation(reflection);
            physvol2.addPhysVolID("system",id);
            physvol2.addPhysVolID("barrel",2);
            motherVolume.addPhysVol(physvol2);
        }
        
        // Set envelope volume attributes.
        setAttributes(lcdd, node, envelopeVolume);
        
        structure.addVolume(envelopeVolume);
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }
}
