/*
 * RZFieldMap.java
 *
 * Created on September 1, 2005, 3:37 PM
 *
 */

package org.lcsim.geometry.compact.converter.lcdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import org.jdom.Attribute;
import org.jdom.Element;
import org.lcsim.util.cache.FileCache;

/**
 *
 * @author jeremym
 */
public class RZFieldMap extends LCDDField
{
    private Element node;
    
    public RZFieldMap(Element node)
    {
        super(node);
        this.node = node;
    }
    
    void addToLCDD(org.lcsim.geometry.compact.converter.lcdd.util.LCDD lcdd) throws org.jdom.JDOMException
    {    	
        org.lcsim.geometry.compact.converter.lcdd.util.RZFieldMap fmap =
                new org.lcsim.geometry.compact.converter.lcdd.util.RZFieldMap(node.getAttribute("name").getValue());
        
        fmap.setNumBinsR(node.getAttribute("numBinsR").getIntValue());
        fmap.setNumBinsZ(node.getAttribute("numBinsZ").getIntValue());
        fmap.setGridSizeZ(node.getAttribute("gridSizeZ").getDoubleValue());
        fmap.setGridSizeR(node.getAttribute("gridSizeR").getDoubleValue());        
        
        Attribute funit = node.getAttribute("funit");
        if ( funit != null )
        {
            fmap.setFieldUnit(funit.getValue());
        }
        
        Attribute lunit = node.getAttribute("lunit");
        if ( lunit != null )
        {
            fmap.setLengthUnit(lunit.getValue());
        }
        
        String location = node.getAttribute("url").getValue();
        
        try
        {
            FileCache cache = new FileCache();
            File file = cache.getCachedFile(new URL(location));
            
            URL url = new URL(location);
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            for (;;)
            {
                String line = reader.readLine();
                if (line == null) break;
                String[] chunks = line.trim().split(" +");
                
                if ( chunks.length != 4 )
                {
                    throw new IOException("Invalid RZ field map line: " + line);
                }
                
                double z = Double.valueOf(chunks[0]).doubleValue();
                double r = Double.valueOf(chunks[1]).doubleValue();
                double Bz = Double.valueOf(chunks[2]).doubleValue();
                double Br = Double.valueOf(chunks[3]).doubleValue();
                
                fmap.addRZBData(z, r, Bz, Br);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error reading field map data", e);
        }
        
        //lcdd.setGlobalField(fmap);
        lcdd.add(fmap);
    }
}