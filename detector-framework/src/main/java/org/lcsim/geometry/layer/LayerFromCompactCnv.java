/*
 * LayerBuilder.java
 *
 * Created on April 1, 2005, 4:23 PM
 */

package org.lcsim.geometry.layer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * Converts from a compact detector description to a @see LayerStack object.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class LayerFromCompactCnv
{
    private LayerFromCompactCnv()
    {}
    
    public static LayerStack makeLayerStackFromCompact(Element e) throws JDOMException, DataConversionException
    {        
        if ( e == null )
        {
            throw new JDOMException("LayerFromCompactCnv.makeLayerStackFromCompact() - got null element");
        }
        
        LayerStack layerStack = null;
        
        if ( e.getChildren("layer") != null)
        {
            layerStack = new LayerStack();
            
            for (Iterator i = e.getChildren("layer").iterator(); i.hasNext();)
            {
                Element lyrElem = (Element) i.next();
                
                // Start a new layer.
                List lyrList = new ArrayList();
                
                // Default to one repetition.
                int repeat = 1;
                
                // Get repeat value from element if exists.
                Attribute repAttrib = lyrElem.getAttribute("repeat");
                if ( repAttrib != null )
                {
                    repeat = repAttrib.getIntValue();
                }
               
                for (Iterator j = lyrElem.getChildren("slice").iterator(); j.hasNext(); )
                {
                    Element slice = (Element) j.next();
                    
                    Attribute matAttrib = slice.getAttribute("material");
                    
                    if ( matAttrib == null )
                    {
                        throw new JDOMException("No material attribute found in this slice.");
                    }
                    
                    String matName = slice.getAttributeValue("material");
                    
                    Attribute thickness = slice.getAttribute("thickness");
                    
                    if ( thickness == null )
                    {
                        throw new JDOMException("No thickness attribute found in this slice.");
                    }
                    
                    Attribute sensAttrib = slice.getAttribute("sensitive");
                    
                    /* Default to not sensitive. */
                    boolean isSens = false;
                    
                    if ( sensAttrib != null )
                    {
                        isSens = sensAttrib.getBooleanValue();
                    }
                    
                    // Make a new slice. Ctor will look up material. 
                    LayerSlice s = new LayerSlice(matName,
                            thickness.getDoubleValue(),
                            isSens);
                    
                    // Add slice to current layer.                    
                    lyrList.add(s);
                }
                
                Layer lyr = new Layer(lyrList);                
                
                // Add layer to stack once for each repetition.
                for (int k = 0; k < repeat; k++)
                {
                    layerStack.addLayer(lyr);
                }
            }
        }
        else
        {
            System.err.println("LayerFromCompactCnv.makeLayerStackFromCompact() - no layer child elements to build");
        }
        
        return layerStack;
    }
    
    /** 
     * Compute the thickness of a single layer, ignoring the repeat attribute. 
     */
    public static double computeSingleLayerThickness(Element node) throws JDOMException
    {
        if ( node.getName() != "layer" )
        {
            throw new JDOMException("LayerCompactCnv.computeLayerThickness() takes layer element, not " + node.getName() );
        }
        
        double thickness = 0.0;
        for ( Object o : node.getChildren("slice") )
        {
            Element sliceElem = (Element) o;
            thickness += sliceElem.getAttribute("thickness").getDoubleValue();
        }
        return thickness;
    }
    
    public static double computeDetectorTotalThickness(Element node) throws JDOMException
    {
        double totalThickness = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {            
            Element layer = (Element) i.next();
            int repeat = 1;
            if (layer.getAttribute("repeat") != null)
            {
            	repeat = (int)layer.getAttribute("repeat").getDoubleValue();
            }
            for ( int j=0; j<repeat; j++)
            {
                for ( Iterator k = layer.getChildren("slice").iterator(); k.hasNext();)
                {
                    Element slice = (Element) k.next();
                    totalThickness += slice.getAttribute("thickness").getDoubleValue();
                }
            }
        }
        return totalThickness;
    }
}
