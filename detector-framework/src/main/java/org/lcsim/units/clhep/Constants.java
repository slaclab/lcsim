package org.lcsim.units.clhep;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Statically accessible version of CLHEP units and 
 * physical constants.  Two methods are forwarded
 * from Map.
 * 
 * @see #get(String)
 * @see #entrySet()
 * 
 * The method 
 * 
 * @see #getInstance()
 * 
 * should be used to retrieve a static reference
 * to this class.
 *  
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class Constants 
{
    Map<String, Double> constantsMap = new HashMap<String, Double>();
    
    private static Constants instance = new Constants();
    
    public double get(String key)
    {
        return constantsMap.get(key);
    }
    
    public Set<Entry<String,Double>> entrySet()
    {
        return constantsMap.entrySet();
    }
    
    public static Constants getInstance()
    {
        return instance;
    }
    
    private Constants()
    {
        setupSystemOfUnits();
        setupPhysicalConstants();
    }
    
    private void setupSystemOfUnits()
    {
        SystemOfUnits units = new SystemOfUnits();      
        Class<SystemOfUnits> klass = SystemOfUnits.class;
        Field[] fields = klass.getFields();
        for (Field f : fields)
        {
            try {
                constantsMap.put(f.getName(), f.getDouble(units));
            }
            catch ( IllegalAccessException x )
            {
                throw new RuntimeException(x);
            }
        }
    }
    
    private void setupPhysicalConstants()
    {
        PhysicalConstants physc = new PhysicalConstants();
        Class<PhysicalConstants> klass = PhysicalConstants.class;
        Field[] fields = klass.getFields();
        for (Field f : fields)
        {
            try {
                constantsMap.put(f.getName(), f.getDouble(physc));
            }
            catch ( IllegalAccessException x )
            {
                throw new RuntimeException(x);
            }
        }
    }    
}