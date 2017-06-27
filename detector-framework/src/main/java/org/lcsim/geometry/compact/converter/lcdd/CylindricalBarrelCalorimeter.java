package org.lcsim.geometry.compact.converter.lcdd;

import java.util.Iterator;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 *
 * Class for converting a compact detector element of type
 * CylindricalBarrelCalorimeter to the LCDD format.
 *
 * @author tonyj
 * @version $id: $
 */
class CylindricalBarrelCalorimeter extends LCDDSubdetector
{
    CylindricalBarrelCalorimeter(Element node) throws JDOMException
    {
        super(node);
    }
    
    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        String detectorName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();
        
        Material air = lcdd.getMaterial("Air");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        
        Element dimensions = node.getChild("dimensions");
        double z = dimensions.getAttribute("outer_z").getDoubleValue();
        double rmin = dimensions.getAttribute("inner_r").getDoubleValue();
        double r = rmin;
        
        Tube envelope = new Tube(detectorName+"_envelope");
        Volume envelopeVolume = new Volume(detectorName+"_envelope_volume");
        envelopeVolume.setMaterial(air);
        envelopeVolume.setSolid(envelope);
                
        int n = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layer = (Element) i.next();
            int repeat = (int)layer.getAttribute("repeat").getDoubleValue();
            for (int ll=0; ll<repeat; ll++)
            {
                double rlayer = r;
                
                String name1 = detectorName+"_layer"+n;
                Tube tube1 = new Tube(name1);
                Volume volume1 = new Volume(name1+"_volume");
                volume1.setMaterial(air);
                volume1.setSolid(tube1);                               
                
                int m = 0;
                for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext(); m++)
                {
                    Element slice = (Element) j.next();
                    double w = slice.getAttribute("thickness").getDoubleValue();
                    Attribute s = slice.getAttribute("sensitive");
                    boolean sensitive = s != null && s.getBooleanValue();
                    
                    String name = detectorName+"_layer"+n+"_slice"+m;
                    Tube tube = new Tube(name);
                    tube.setZ(2 * z);
                    tube.setRMin(r);
                    r += w;
                    tube.setRMax(r);
                    tube.setDeltaPhi(2*Math.PI);
                    solids.addSolid(tube);
                    
                    Volume volume = new Volume(name+"_volume");
                    volume.setMaterial(lcdd.getMaterial(slice.getAttributeValue("material")));
                    volume.setSolid(tube);
                    if (sensitive) volume.setSensitiveDetector(sens);                    
                    
                    setAttributes(lcdd, slice, volume);
                    
                    structure.addVolume(volume);
                    volume1.addPhysVol(new PhysVol(volume));                    
                }
                
                setVisAttributes(lcdd, node, volume1);
                
                tube1.setZ(2*z);
                tube1.setRMin(rlayer);
                tube1.setRMax(r);
                tube1.setDeltaPhi(2*Math.PI);
                
                PhysVol physvol = new PhysVol(volume1);
                physvol.addPhysVolID("layer",n);
                envelopeVolume.addPhysVol(physvol);
                structure.addVolume(volume1);
                solids.addSolid(tube1);
                n++;                                
            }
        }
        
        envelope.setZ(2*z);
        envelope.setRMin(rmin);
        envelope.setRMax(r);
        envelope.setDeltaPhi(Math.PI*2);
        PhysVol physvol = new PhysVol(envelopeVolume);
        physvol.addPhysVolID("system",id);
        physvol.addPhysVolID("barrel",0);
        motherVolume.addPhysVol(physvol);
        
        solids.addSolid(envelope);
        
        // Set envelope volume attributes.
        setAttributes(lcdd, node, envelopeVolume);
        
        structure.addVolume(envelopeVolume);                       
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }
}
