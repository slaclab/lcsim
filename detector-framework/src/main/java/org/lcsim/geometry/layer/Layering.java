package org.lcsim.geometry.layer;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Provides access to layering information in a subdetector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class Layering 
{    
    protected LayerStack layerStack;
    protected double offset = 0;
    
    public static Layering makeLayering(Element e) throws JDOMException
    {
        return new Layering(e);
    }
    
    /** 
     * @param e The XML element representing the subdetector in the compact format.
     */
    public Layering(Element e) throws JDOMException
    {        
        // Create the LayerStack using static conversion function.
        layerStack = LayerFromCompactCnv.makeLayerStackFromCompact(e);
    }
    
    public Layering(LayerStack s)
    {
    	layerStack = s;
    }
    
    public double getDistanceToLayer(int layer)
    {
        return getLayerStack().getThicknessToLayerFront(layer) + offset;
    }
    
    public double getDistanceToLayerBack(int layer)
    {
    	return getLayerStack().getThicknessToLayerBack(layer) + offset;
    }
    
    /** alias to getLayerStack() */
    public LayerStack getLayers()
    {
        return getLayerStack();
    }

    public Layer getLayer(int i)
    {
        return getLayerStack().getLayer(i);
    }
    
    public double getThickness()
    {
        return getLayerStack().getTotalThickness();
    }
    
    public LayerStack getLayerStack()
    {
        return layerStack;
    }
    
    public int getLayerCount()
    {
        return getLayerStack().getNumberOfLayers();
    }       
    
    public int getNumberOfLayers()
    {
        return getLayerCount();
    }
    
    public int size()
    {
        return getLayerCount();
    }
    
    public double getDistanceToLayerSensorMid(int layer)
    {
        return getDistanceToLayer(layer) + 
               getLayerStack().getLayer(layer).getThicknessToSensitiveMid();
    }
    
    public double getDistanceToLayerSensorFront(int layer)
    {
    	return getDistanceToLayer(layer) + getLayerStack().getLayer(layer).getThicknessToSensitive();
    }
    
    public double getDistanceToLayerSensorBack(int layer)
    {    	
    	return getDistanceToLayerSensorFront(layer) + getLayer(layer).getSensorThickness();
    }
            
    public void setOffset(double o)
    {
        offset = o;
    }
    
    public double getOffset()
    {
        return offset;
    }
}