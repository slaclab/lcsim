package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Volume extends RefElement
{
    public Volume(String name, Solid solid, Material material)
    {
        super("volume",name);

        Element materialref = new Element("materialref");
        addContent(materialref);
        Element solidref = new Element("solidref");
        addContent(solidref);

        setSolid(solid);
        setMaterial(material);
    }


    /** Creates a new instance of Volume */
    public Volume(String name)
    {
        super("volume",name);
        Element materialref = new Element("materialref");
        addContent(materialref);
        Element solidref = new Element("solidref");
        addContent(solidref);
    }
    public void setMaterial(Material mat)
    {
        getChild("materialref").setAttribute("ref",mat.getRefName());
    }
    public void setSolid(Solid solid)
    {
        getChild("solidref").setAttribute("ref",solid.getRefName());
    }
    public void setSensitiveDetector(SensitiveDetector sens)
    {
        Element sdref = new Element("sdref");
        sdref.setAttribute("ref",sens.getRefName());
        addContent(sdref);
    }

    public void setLimitSet(LimitSet limitset)
    {
    	if (limitset == null)
    		return;
        Element limitsetref = new Element("limitsetref");
        limitsetref.setAttribute("ref",limitset.getRefName());
        addContent(limitsetref);
    }

    public void addPhysVol(PhysVol vol)
    {
        addContent(vol);
    }
    public void setRegion(Region region)
    {
    	if (region == null)
    		return;
        Element regionref = new Element("regionref");
        regionref.setAttribute("ref",region.getRefName());
        addContent(regionref);
    }

    public void setVisAttributes(VisAttributes vis)
    {
    	if (vis == null)
    		return;
        Element visref = new Element("visref");
        visref.setAttribute("ref",vis.getRefName());
        addContent(visref);
    }

    public String getSolidRef()
    {
        return getChild("solidref").getAttributeValue("ref");
    }

    public String getVolumeName()
    {
        return getAttributeValue("name");
    }
    
    public String getMaterialRef()
    {
        return getAttributeValue("materialref");
    }
}
