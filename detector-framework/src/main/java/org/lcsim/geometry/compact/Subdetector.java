package org.lcsim.geometry.compact;

import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.layer.Layering;

/**
 *
 * Basic implementation of org.lcsim.geometry.Subdetector.
 *
 * @author Tony Johnson <tonyj@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * 
 * @version $Id: Subdetector.java,v 1.25 2010/12/03 01:23:07 jeremy Exp $
 */
public class Subdetector implements org.lcsim.geometry.Subdetector
{
    // FIXME: Why is reflect a general attribute of Subdetector?  It only makes
    //        sense for endcaps.
    // Make default setting true so that when it isn't set, we get two endcaps as expected.
    private boolean reflect = true;

    private Readout readout;
    private String name;
    private int systemID;
    //private ParameterSet _parameters;
    private VisAttributes vis;
    private Element node;
    private IDetectorElement de;
    private boolean insideTrackingVolume = false;
    String digiCollectionName = null;

    protected Subdetector(Element element) throws JDOMException
    {
        // XML node.
        node = element;
        
        // Subdetector name.
        name = element.getAttributeValue("name");

        // Reflect attribute.
        Attribute r = element.getAttribute("reflect");
        reflect = r != null && r.getBooleanValue();

        // Don't need a null check on id, as the compact schema include a default of 0.
        systemID = element.getAttribute("id").getIntValue();

        // Check if inside tracking volume.
        Attribute insideAttrib = node.getAttribute("insideTrackingVolume");
        try
        {
            if (insideAttrib == null)
            {
                if (isTracker())
                {
                    insideTrackingVolume = true;
                } 
                else
                {
                    insideTrackingVolume = false;
                }
            } else
            {
                insideTrackingVolume = insideAttrib.getBooleanValue();
            }
        } 
        catch (org.jdom.DataConversionException dce)
        {
            throw new RuntimeException("Error converting insideTrackingVolume attribute.", dce);
        }
        
        // Set default digitized hits collection name.
        digiCollectionName = this.getName().replace("Hits","DigiHits");
    }

    protected void setReadout(Readout r)
    {
        // Require a valid system id.
        if (systemID == 0)
            throw new RuntimeException("The detector " + getName() + " cannot have a readout" + " because it does not have a valid system id.");
        this.readout = r;
    }

    /**
     * Get the readout associated with this detector.
     * 
     * @return The readout, or <CODE>null</CODE> if no readout is associated with the detector.
     */
    public Readout getReadout()
    {
        return readout;
    }

    /*
    public void setParameters(ParameterSet parameters)
    {
        _parameters = parameters;
    }
    
    public ParameterSet getParameterSet()
    {
        return _parameters;
    }
     */

    public IDDecoder getIDDecoder()
    {
        return getReadout().getIDDecoder();
    }

    public String getName()
    {
        return name;
    }

    public int getSystemID()
    {
        return (systemID);
    }

    public boolean isBarrel()
    {
        return false;
    }

    public boolean isEndcap()
    {
        return false;
    }

    public boolean isCalorimeter()
    {
        return false;
    }

    public boolean isTracker()
    {
        return false;
    }

    public boolean isLayered()
    {
        return false;
    }

    public double[] transformLocalToGlobal(double[] locPos)
    {
        return locPos;
    }

    public boolean getReflect()
    {
        return reflect;
    }

    public Layering getLayering()
    {
        throw new RuntimeException("layers not implemented");
    }

    public void setVisAttributes(VisAttributes vis)
    {
        this.vis = vis;
    }

    public VisAttributes getVisAttributes()
    {
        // Make a default VisAttributes if it is null.
        if ( vis == null )
        {
            vis = new VisAttributes( getName() + "_vis" );
            
            // Set color to white so it will show in Wired and Geant4.
            vis.setColor( Color.WHITE );
        }
        return vis;
    }

    public Element getNode()
    {
        return node;
    }

    public IDetectorElement getDetectorElement()
    {
        return de;
    }

    public void setDetectorElement(IDetectorElement de)
    {
        this.de = de;
    }

    public boolean isInsideTrackingVolume()
    {
        return insideTrackingVolume;
    }
    
    public String getHitsCollectionName()
    {
        return getReadout().getName();
    }
    
    /**
     * Allow setting of digi collection name by external digitization Driver.
     * This will override the default setting based on digisim's default.
     * @param digiCollecionName The new name for digitized hits collection.
     */
    public void setDigiHitsCollectionName(String digiCollectionName)
    {
        this.digiCollectionName = digiCollectionName;
    }
    
    /**
     * Get the name of the digitized hits collection.
     * @return The digitized hits collection name.
     */
    public String getDigiHitsCollectionName()
    {
        return digiCollectionName;
    }
}