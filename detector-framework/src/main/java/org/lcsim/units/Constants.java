package org.lcsim.units;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
}
