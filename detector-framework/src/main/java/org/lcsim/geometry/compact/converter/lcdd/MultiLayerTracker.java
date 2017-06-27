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
 * @author tonyj
 */
class MultiLayerTracker extends LCDDSubdetector
{  
    MultiLayerTracker(Element node) throws JDOMException
    {
        super(node); 
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        int id = -1;
        if (node.getAttribute("id") != null)
            id = node.getAttribute("id").getIntValue();
        String detectorName = node.getAttributeValue("name");

        Material air = lcdd.getMaterial("Air");      
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();

        Volume trackingVolume = lcdd.pickMotherVolume(this);

        int n = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext(); n++)
        {
            Element layer = (Element) i.next();
            String name1 = detectorName+"_layer"+n;
            Tube tube1 = new Tube(name1);        
            Volume volume1 = new Volume(name1+"_volume");
            volume1.setMaterial(air);
            volume1.setSolid(tube1);

            int m = 0;
            double z = layer.getAttribute("outer_z").getDoubleValue();
            double rmin = layer.getAttribute("inner_r").getDoubleValue();
            double r = rmin;

            for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext(); m++)
            {
                Element slice = (Element) j.next();
                double w = slice.getAttribute("thickness").getDoubleValue();
                Attribute s = slice.getAttribute("sensitive");
                boolean sensitive = s != null && s.getBooleanValue();

                String name = detectorName+"_layer"+n+"_slice"+m;
                Tube tube = new Tube(name);
                tube.setZ(2*z);
                tube.setRMin(r);
                r += w;
                tube.setRMax(r);
                tube.setDeltaPhi(Math.PI*2);
                solids.addContent(tube);

                Volume volume = new Volume(name+"_volume");
                volume.setMaterial(lcdd.getMaterial(slice.getAttributeValue("material")));
                volume.setSolid(tube);
                if (sensitive) volume.setSensitiveDetector(sens);

                // Set region of slice.
                setRegion(lcdd, slice, volume);
                
                // Set limits of slice.
                setLimitSet(lcdd, slice, volume);            
                
                // Set vis attributes of slice.
                setVisAttributes(lcdd, node, volume);

                structure.addContent(volume);
                PhysVol physvol = new PhysVol(volume);
                physvol.addPhysVolID("layer",n);
                volume1.addPhysVol(physvol);
            }

            // Set vis attributes of individual layers.
            this.setVisAttributes(lcdd, layer, volume1);
            
            tube1.setZ(2*z);
            tube1.setRMin(rmin);
            tube1.setRMax(r);
            tube1.setDeltaPhi(Math.PI*2);
            PhysVol physvol = new PhysVol(volume1);
            physvol.addPhysVolID("system",id);
            /* barrel is 0 from SubdetectorIDDecoder.BARREL */
            physvol.addPhysVolID("barrel",0);
            trackingVolume.addPhysVol(physvol);
            solids.addSolid(tube1);

            structure.addVolume(volume1);
        }

        setCombineHits(node, sens);
    }
    
    public boolean isTracker()
    {
        return true;
    }
}
