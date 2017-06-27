package org.lcsim.geometry.layer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A stack of @see Layer objects representing layers in a subdetector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: LayerStack.java,v 1.13 2010/04/14 18:24:54 jeremy Exp $
 */
public class LayerStack {        
    
    List<Layer> layers;
    
    /** Creates a new instance of LayerStack */
    public LayerStack() 
    {
        layers = new ArrayList<Layer>();        
    }        
    
    public void addLayer(Layer l)
    {
        layers.add(l);
    }
    
    public Layer getLayer(int i)
    {
        return layers.get(i);
    }
    
    public double getTotalThickness()
    {
        return getSectionThickness(0, layers.size() - 1);        
    }
    
    public double getSectionThickness(int is, int ie) throws IllegalArgumentException
    {
        double thick = 0.0;
                
        if ( is > ie) 
        {
            throw new java.lang.IllegalArgumentException("First index must be <= second index.");
        }
        
        if ( is < 0 || is > getNumberOfLayers() - 1 ) 
        {
            throw new java.lang.IllegalArgumentException("First index out of range: "+is+", #layers="+getNumberOfLayers());
        }
        
        if ( ie < 0 || ie > getNumberOfLayers() - 1 ) 
        {
            throw new java.lang.IllegalArgumentException("Second index out of range: "+ie+", #layers="+getNumberOfLayers());
        }
                
        for ( int i = is; i <= ie; i++ )
        {            
            Layer layer = getLayer(i); 
            thick += getLayer(i).getThicknessWithPreOffset();  
        }
        
        return thick;
    }
    
    public double getThicknessToLayerBack(int i)
    {
        return getSectionThickness(0, i);
    }
    
    public double getThicknessToLayerMid(int i)
    {
        return getThicknessToLayerBack(i) - layers.get(i).getThickness() / 2;
    }
        
    public double getThicknessToLayerFront(int i)
    {
        return getThicknessToLayerBack(i) - getLayer(i).getThickness();
    }
        
    public int getNumberOfLayers()
    {
        return layers.size();
    }        
    
    public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    
    	int layern=0;
    	for (Layer layer : layers)
    	{    		
    		sb.append("Layer: " + layern +'\n');
    		sb.append("inner_radius="+layer.getPreOffset()+'\n');
    		sb.append("thickness="+layer.getThickness()+'\n');
    		sb.append('\n');
    		
    		int slicen=0;
    		for (LayerSlice slice : layer.getSlices())
    		{
    			assert(slice.getMaterial() != null);
    			sb.append("    " + slicen + ", " + slice.getMaterial().getName() + ", " + slice.getThickness());
    			sb.append('\n');
    			++slicen;
    		}
    		
    		sb.append('\n');
    		
    		++layern;
    	}
    	
    	return sb.toString();
    }
}
