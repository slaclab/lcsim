package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.LimitSet;
import org.lcsim.geometry.compact.converter.lcdd.util.Region;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Tracker;
import org.lcsim.geometry.compact.converter.lcdd.util.VisAttributes;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.Layering;

/**
 * 
 * @author tonyj
 */
public abstract class LCDDSubdetector extends Subdetector
{
    protected Element node;
    protected Layering layers;

    LCDDSubdetector(Element c) throws JDOMException
    {
        super(c);
        node = c;

        /* make compact layering descr for thickness calcs in addToLCDD */
        if (layers == null)
        {
        	try {
        		layers = org.lcsim.geometry.layer.Layering.makeLayering(c);
        	}
        	catch (JDOMException x)
        	{
        		System.err.println("WARNING: " + x.getMessage());
        	}
        }
    }

    abstract void addToLCDD(LCDD lcdd, SensitiveDetector sens)
            throws JDOMException;

    public Element getElement()
    {
        return node;
    }

    /**
	 * Set limits of an LCDD volume
	 * 
	 * @param lcdd
	 *            LCDD object currently being processed
	 * @param node
	 *            Current DOM node (e.g. detector, layer, slice, etc.)
	 * @param volume
	 *            The volume corresponding to the DOM node
	 */
    public final static void setLimitSet(LCDD lcdd, Element node, Volume volume)
    {
        if (node.getAttribute("limits") != null)
        {
            String limitref = node.getAttributeValue("limits");
            LimitSet limitset = lcdd.getLimitSet(limitref);
            if (limitset != null)
            {
                volume.setLimitSet(limitset);
            }
            else
            {
                throw new RuntimeException("limitset " + limitref
                        + " does not exist");
            }
        }
    }

    /**
	 * 
	 * Set the region of an LCDD volume
	 * 
	 * @param lcdd
	 *            LCDD object currently being processed
	 * @param node
	 *            Current DOM node (e.g. detector, layer, slice, etc.)
	 * @param volume
	 *            The volume corresponding to the DOM node
	 * 
	 */
    public final static void setRegion(LCDD lcdd, Element node, Volume volume)
    {
        if (node.getAttribute("region") != null)
        {
            String regionref = node.getAttributeValue("region");
            Region region = lcdd.getRegion(regionref);
            if (region != null)
            {
                volume.setRegion(region);
            }
            else
            {
                throw new RuntimeException("Region " + regionref
                        + " does not exist");
            }
        }
    }

    /**
	 * 
	 * Set combine_hits attribute of LCDD sensitive detector
	 * 
	 * @param node
	 *            detector XML node
	 * @param sens
	 *            sensitive detector which should be of type Tracker
	 */
    public final static void setCombineHits(Element node, SensitiveDetector sens)
    {
        if (node.getAttribute("combineHits") != null)
        {
        	//System.out.println("combineHits=" + node.getAttributeValue("combineHits"));
            try
            {
            	if (sens instanceof Tracker)
                {
            		if (node.getAttribute("combineHits").getBooleanValue() == true)
            		{                    
                        sens.setAttribute("combine_hits", "true");                 
            		}
            		else 
            		{
            			sens.setAttribute("combine_hits", "false");
            		}
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Utility method for checking and setting lcdd visualization parameters on a volume.
     * If the XML node does not have a visualization reference, the volume is turned off 
     * completely unless it is a detector; the showDaughters attribute is set to true
     * so that sub-volumes are not turned off even if they have visualization settings.  
     * Detectors are automatically added to the scene unless specifically turned off with 
     * visualization settings, so this default does not apply when the XML node is called
     * <detector>.
     * 
     * This method is used extensively by subclasses in their addToLCDD() methods for
     * assigning visualization attributes to detector envelopes, layer envelopes,
     * and layer slices.
     * 
     * @param lcdd The LCDD structure being built.
     * @param node The XML node with the possible visualization reference.
     * @param volume The volume to which visualization is being assigned.
     */
    public final static void setVisAttributes(LCDD lcdd, Element node, Volume volume)
    {
    	if (lcdd == null)
    		throw new IllegalArgumentException("The lcdd object points to null.");
    	
    	if (node == null)
    		throw new IllegalArgumentException("The node object points to null.");
    	
    	if (volume == null)
    		throw new IllegalArgumentException("The volume object points to null.");
    	
        if (node.getAttribute("vis") != null)
        {      	
            String visref = node.getAttributeValue("vis");
            VisAttributes vis = lcdd.getVisAttributes(visref);
            if (vis != null)
            {
                volume.setVisAttributes(vis);
            }
            else
            {
                throw new RuntimeException("The vis reference " + visref + " does not exist.");
            }
        }
        else
        {
        	// Slices are turned off by default.
        	if (node.getName().equals("slice"))
        		volume.setVisAttributes(lcdd.getVisAttributes("InvisibleNoDaughters"));
        	// Layers are also turned off by default but daughters are left possibly visible.
        	else if (node.getName().equals("layer"))
        		volume.setVisAttributes(lcdd.getVisAttributes("InvisibleWithDaughters"));
        	// Tracker modules are similar to layers.
        	else if (node.getName().equals("module"))
        		volume.setVisAttributes(lcdd.getVisAttributes("InvisibleWithDaughters"));
        	// Tracker module components are turned off by default.
        	else if (node.getName().equals("module_component"))
        		volume.setVisAttributes(lcdd.getVisAttributes("InvisibleNoDaughters"));
        }
    }
    
    public void setAttributes(LCDD lcdd, Element node, Volume volume)
    {
    	setRegion(lcdd, node, volume);
    	setLimitSet(lcdd, node, volume);
    	setVisAttributes(lcdd, node, volume);
    }
}