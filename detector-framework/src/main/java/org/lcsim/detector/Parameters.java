package org.lcsim.detector;

import java.util.HashMap;
import java.util.Set;

/**
 * Implementation of {@link IParameters}.
 * 
 * @author Jeremy McCormick
 * @version $Id: Parameters.java,v 1.2 2007/05/22 20:02:03 jeremy Exp $
 */
public class Parameters implements IParameters 
{    
    String name;
    
    public Parameters(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public Set<String> getBooleanArrayParameterNames() 
    {
        return boolArrayParams.keySet();
    }

    public Set<String> getBooleanParameterNames() 
    {
        return boolParams.keySet();
    }

    public Set<String> getDoubleParameterNames() 
    {
        return doubleParams.keySet();
    }

    public Set<String> getIntegerParameterNames() 
    {
        return intParams.keySet();
    }

    public Set<String> getStringParameterNames() 
    {
        return stringParams.keySet();
    }

    public Set<String> getDoubleArrayParameterNames() 
    {
        return doubleArrayParams.keySet();
    }

    public Set<String> getIntegerArrayParameterNames() 
    {
        return intArrayParams.keySet();
    }

    public Set<String> getStringArrayParameterNames() 
    {
        return stringArrayParams.keySet();
    }
    
    public void addStringParameter(String name, String value) 
    {
        stringParams.put(name, value);
    }

    public void addIntegerParameter(String name, int value) 
    {
        intParams.put(name, value);
    }

    public void addDoubleParameter(String name, double value) 
    {
        doubleParams.put(name, value);
    }

    public void addBooleanParameter(String name, boolean value) 
    {
        boolParams.put(name,value);
    }

    public void addStringArrayParameter(String name, String[] values) 
    {
        if ( values == null )
        {
            throw new IllegalArgumentException("parameter " + name + " is null");
        }        
        stringArrayParams.put(name, values);
    }

    public void addIntegerArrayParameter(String name, int[] values) 
    {
        if ( values == null )
        {
            throw new IllegalArgumentException("parameter " + name + " is null");
        }        
       intArrayParams.put(name, values);
    }

    public void addDoubleArrayParameter(String name, double[] values) 
    {
        if ( values == null )
        {
            throw new IllegalArgumentException("parameter " + name + " is null");
        }        
        doubleArrayParams.put(name,values);
    }

    public void addBooleanArrayParameter(String name, boolean[] values) 
    {
        if ( values == null )
        {
            throw new IllegalArgumentException("parameter " + name + " is null");
        }        
        boolArrayParams.put(name,values);
    }

    public boolean getBooleanParameter(String name) 
    {
        return boolParams.get(name);
    }

    public boolean[] getBooleanArrayParameter(String name) 
    {
        return boolArrayParams.get(name);
    }

    public double getDoubleParameter(String name) 
    {
        return doubleParams.get(name);
    }

    public double[] getDoubleArrayParameter(String name) 
    {
        return doubleArrayParams.get(name);
    }

    public int getIntegerParameter(String name) 
    {
        return intParams.get(name);
    }

    public int[] getIntegerArrayParameter(String name) 
    {
        return intArrayParams.get(name);
    }

    public String getStringParameter(String name) 
    {
        return stringParams.get(name);
    }

    public String[] getStringArrayParameter(String name) 
    {
        return stringArrayParams.get(name);
    }

    StringParameters stringParams = new StringParameters();
    StringArrayParameters stringArrayParams = new StringArrayParameters();
    DoubleParameters doubleParams = new DoubleParameters();
    DoubleArrayParameters doubleArrayParams = new DoubleArrayParameters();
    IntegerParameters intParams = new IntegerParameters();
    IntegerArrayParameters intArrayParams = new IntegerArrayParameters();
    BooleanParameters boolParams = new BooleanParameters();
    BooleanArrayParameters boolArrayParams = new BooleanArrayParameters();
   
    class ParameterMap <T>
    extends HashMap<String,T>
    {}
    
    class StringParameters 
    extends ParameterMap<String>
    {}
    
    class StringArrayParameters
    extends ParameterMap<String[]>
    {}
    
    class DoubleParameters
    extends ParameterMap<Double>
    {}
    
    class DoubleArrayParameters
    extends ParameterMap<double[]>
    {}
    
    class IntegerParameters
    extends ParameterMap<Integer>
    {}
    
    class IntegerArrayParameters
    extends ParameterMap<int[]>
    {}
    
    class BooleanParameters
    extends ParameterMap<Boolean>
    {}
    
    class BooleanArrayParameters
    extends ParameterMap<boolean[]>
    {}
}