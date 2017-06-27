package org.lcsim.detector;

import java.util.Set;

/**
 * Interface to named parameters.  Available types are
 * integer, boolean, double, and string, and arrays of
 * these types.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: IParameters.java,v 1.2 2007/05/22 20:02:03 jeremy Exp $
 */
public interface IParameters 
{
    public String getName();
    
	public Set<String> getStringParameterNames();
    public Set<String> getIntegerParameterNames();
    public Set<String> getDoubleParameterNames();
    public Set<String> getBooleanParameterNames();
    
    public Set<String> getStringArrayParameterNames();
    public Set<String> getIntegerArrayParameterNames();
    public Set<String> getDoubleArrayParameterNames();
    public Set<String> getBooleanArrayParameterNames();
	
	public String getStringParameter(String name);
	public int getIntegerParameter(String name);
	public double getDoubleParameter(String name);
	public boolean getBooleanParameter(String name);
	
	public String[] getStringArrayParameter(String name);
	public int[] getIntegerArrayParameter(String name);
	public double[] getDoubleArrayParameter(String name);
	public boolean[] getBooleanArrayParameter(String name);
	
	public void addStringParameter(String name, String value);
	public void addIntegerParameter(String name, int value);
	public void addDoubleParameter(String name, double value);
	public void addBooleanParameter(String name, boolean value);
	
	public void addStringArrayParameter(String name, String[] values);
	public void addIntegerArrayParameter(String name, int[] values);
	public void addDoubleArrayParameter(String name, double[] values);
	public void addBooleanArrayParameter(String name, boolean[] value);
}