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
class DiskTracker extends LCDDSubdetector
{
    DiskTracker(Element node) throws JDOMException
    {
        super(node);
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        int id = -1;
        if (node.getAttribute("id") != null)
            id = node.getAttribute("id").getIntValue();
        String detectorName = node.getAttributeValue("name");
        boolean reflect = node.getAttribute("reflect").getBooleanValue();

        Material air = lcdd.getMaterial("Air");
        Rotation reflection = lcdd.getDefine().getRotation("reflect_rot");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();

        Volume trackingVolume = lcdd.pickMotherVolume(this);

        int n = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext(); n++)
        {
            Element layer = (Element) i.next();
            String layerName = detectorName+"_layer"+n;
            Tube tube1 = new Tube(layerName);
            Volume layerVolume = new Volume(layerName+"_volume");
            layerVolume.setMaterial(air);
            layerVolume.setSolid(tube1);

            int m = 0;
            double zmin = layer.getAttribute("inner_z").getDoubleValue();
            double rmin = layer.getAttribute("inner_r").getDoubleValue();
            double rmax = layer.getAttribute("outer_r").getDoubleValue();
            double z = zmin;

            double layerWidth = 0;
            for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext();)
            {
                Element slice = (Element) j.next();
                double w;
                if ( slice.getAttribute("thickness") != null )
                {
                    w = slice.getAttribute("thickness").getDoubleValue();
                }
                else
                {
                    throw new JDOMException("Disk Tracker " + detectorName +
                            " is missing required thickness attribute in layer #" + n + " .");
                }
                layerWidth += w;
            }

            for (Iterator j = layer.getChildren("slice").iterator(); j.hasNext(); m++)
            {
                Element slice = (Element) j.next();
                double w = slice.getAttribute("thickness").getDoubleValue();
                Attribute s = slice.getAttribute("sensitive");
                boolean sensitive = s != null && s.getBooleanValue();

                String sliceName = detectorName+"_layer"+n+"_slice"+m;
                Tube tube = new Tube(sliceName);
                tube.setZ(w);
                tube.setRMin(rmin);
                tube.setRMax(rmax);
                tube.setDeltaPhi(Math.PI*2);
                solids.addSolid(tube);

                Volume sliceVolume = new Volume(sliceName+"_volume");
                sliceVolume.setMaterial(lcdd.getMaterial(slice.getAttributeValue("material")));
                sliceVolume.setSolid(tube);
                if (sensitive) sliceVolume.setSensitiveDetector(sens);

                // Set slice region.
                setRegion(lcdd, slice, sliceVolume);
                
                // Set slice limits.
                setLimitSet(lcdd, slice, sliceVolume);
                
                // Set slice display.
                setVisAttributes(lcdd, slice, sliceVolume);

                PhysVol physvol = new PhysVol(sliceVolume);
                physvol.setZ(z-zmin -layerWidth/2 + w/2);
                physvol.addPhysVolID("layer",n);
                layerVolume.addPhysVol(physvol);
                structure.addVolume(sliceVolume);

                z += w;
            }
            
            // Set layer region.
            setRegion(lcdd, layer, layerVolume);
            
            // Set layer limits.
            setLimitSet(lcdd, layer, layerVolume);
            
            // Set layer display.
            setVisAttributes(lcdd, layer, layerVolume);

            tube1.setZ(layerWidth);
            tube1.setRMin(rmin);
            tube1.setRMax(rmax);
            tube1.setDeltaPhi(Math.PI*2);

            PhysVol physvol = new PhysVol(layerVolume);
            physvol.setZ(zmin+layerWidth/2);
            physvol.addPhysVolID("system",id);
            physvol.addPhysVolID("barrel",1);
            trackingVolume.addPhysVol(physvol);

            if (reflect)
            {
                PhysVol physvol2 = new PhysVol(layerVolume);
                physvol2.setZ(-zmin-layerWidth/2);
                physvol2.setRotation(reflection);
                physvol2.addPhysVolID("system",id);
                physvol2.addPhysVolID("barrel",2);
                trackingVolume.addPhysVol(physvol2);
            }
            solids.addContent(tube1);
            structure.addContent(layerVolume);
        }

        setCombineHits(node, sens);
    }

    public boolean isTracker()
    {
        return true;
    }
}