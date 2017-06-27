package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class PhysVol extends Element
{
    private static int n = 0;
    /** Creates a new instance of Physvol */
    public PhysVol(Volume volume, Volume mother, Position p, Rotation r)
    {
        super("physvol");
        Element volumeref = new Element("volumeref");
        if (volume != null) volumeref.setAttribute("ref",volume.getRefName());
        addContent(volumeref);

        Element positionref = new Element("positionref");
        positionref.setAttribute("ref","identity_pos");
        addContent(positionref);

        Element rotationref = new Element("rotationref");
        rotationref.setAttribute("ref","identity_rot");
        addContent(rotationref);
        
        setPosition(p);
        
        setRotation(r);
        
        mother.addPhysVol(this);
    }
    
    public PhysVol()
    {
        this(null);
    }
    public PhysVol(Volume volume)
    {
        super("physvol");
        Element volumeref = new Element("volumeref");
        if (volume != null) volumeref.setAttribute("ref",volume.getRefName());
        addContent(volumeref);

        Element positionref = new Element("positionref");
        positionref.setAttribute("ref","identity_pos");
        addContent(positionref);

        Element rotationref = new Element("rotationref");
        rotationref.setAttribute("ref","identity_rot");
        addContent(rotationref);
    }
    public void setVolume(Volume volume)
    {
        getChild("volumeref").setAttribute("ref",volume.getRefName());
    }

    public void setZ(double z)
    {
        Element positionref = getChild("positionref");
        if (positionref != null) this.removeContent(positionref);
        Position position = (Position) getChild("position");
        if (position == null)
        {
            position = new Position("volpos_"+(n++));
            addContent(1,position);
        }
        position.setZ(z);
    }

    public void setPosition(Position pos)
    {
        Element positionRef = getChild("positionref");
        positionRef.setAttribute("ref", pos.getRefName());
    }

    public void setRotation(Rotation rot)
    {
        Element rotationRef = getChild("rotationref");
        rotationRef.setAttribute("ref",rot.getRefName());
    }
    public void addPhysVolID(String name, int value)
    {
        Element element = new Element("physvolid");
        element.setAttribute("field_name",name);
        element.setAttribute("value",String.valueOf(value));
        addContent(element);
    }

    public void addPhysVolID(PhysVolID id)
    {
        addContent(id);
    }
}
