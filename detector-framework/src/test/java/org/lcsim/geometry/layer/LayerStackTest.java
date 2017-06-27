/*
 * LayerStackTest.java
 *
 * Created on June 6, 2005, 10:26 AM
 */

package org.lcsim.geometry.layer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.lcsim.material.XMLMaterialManager;

/**
 * Tests that LayerStack's methods work correctly.
 * @author jeremym
 */
public class LayerStackTest extends TestCase
{
 
    private LayerStack layerStack;
    private static final double layerThickness = 2.0;    
    private static final int numLayers = 10;        
    private static final double layerStackThickness = layerThickness * numLayers; 
    
    private static final double[] sliceThick = { 1.0, 0.5, 0.25, 0.25 };
    private static final String[] sliceMat = { "Iron", "Silicon", "Chlorine", "Nitrogen"};
        
    /** 
     * Creates a new instance of LayerStackTest 
     **/
    public LayerStackTest(String name)
    {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception
    {    
        // Need to make sure static material classes are setup 
        // before settings up layers.
        XMLMaterialManager.setup();
        XMLMaterialManager.getDefaultMaterialManager();
        
        layerStack = new LayerStack();
        
        LayerSlice s1 = new LayerSlice(sliceMat[0], sliceThick[0], false);
        LayerSlice s2 = new LayerSlice(sliceMat[1], sliceThick[1], false);
        LayerSlice s3 = new LayerSlice(sliceMat[2], sliceThick[2], true);
        LayerSlice s4 = new LayerSlice(sliceMat[3], sliceThick[3], false);
        
        for ( int i = 0; i < numLayers; i++)
        {                       
        	List<LayerSlice> list = new ArrayList<LayerSlice>();
            
            list.add(s1);
            list.add(s2);
            list.add(s3);
            list.add(s4);
           
            Layer l = new Layer(list);
            
            layerStack.addLayer(l);
        }
        
        //System.out.println("layerStack.getTotalThickness = " + layerStack.getTotalThickness());
    }
            
    public void testLayerStack()
    {   
        // Number of layers    	
        assertEquals(layerStack.getNumberOfLayers(), numLayers);        
        
        // Total thickness. 
        assertEquals(layerStack.getTotalThickness(), layerStackThickness);
                
        // A single layer's total thickness.
        assertEquals(layerStack.getLayer(0).getThickness(), layerThickness);                        
        
        // Section thickness.
        assertEquals(layerStack.getSectionThickness(0,4), layerStackThickness/2);
        
        // Section thickness with same layer number.
        assertEquals(layerStack.getSectionThickness(0,0), layerThickness);

        // Thickness to layer midpoint. 
        assertEquals(layerStack.getThicknessToLayerMid(1), layerThickness + layerThickness/2);
        
        // Thickness to layer front face with layer number 0.
        assertEquals(layerStack.getThicknessToLayerMid(0), layerThickness/2);
        
        // Thickness to layer back face.
        assertEquals(layerStack.getThicknessToLayerBack(0), layerThickness);
        
        // Thickness to layer front face. 
        assertEquals(layerStack.getThicknessToLayerFront(0), 0.0);                               
    }
}
