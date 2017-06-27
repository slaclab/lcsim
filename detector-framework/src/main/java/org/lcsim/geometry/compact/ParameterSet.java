package org.lcsim.geometry.compact;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.jdom.Element;

/**
 * 
 * ParameterSet is used to store generic subdetector parameters within the
 * compact Subdetector object. Values are converted from <parameter> elements
 * within the <detector> element. The parameters can be of type Double, Integer,
 * or String.
 * 
 * @author jeremym
 * @version $Id: ParameterSet.java,v 1.5 2006/02/01 23:40:43 jeremy Exp $
 */
public class ParameterSet
{
	/* Map for storing and looking up parameters by name. */
	Map<String, Object> _parameters = new HashMap<String, Object>();

	/** Constructor taking the <detector> node for a subdetector. */
	protected ParameterSet(Element node)
	{
		convertFromXML(node);
	}

	/** Constructor without Element argument. */
	protected ParameterSet()
	{}
	
	/** Convert <parameter> elements within <detector>. */
	private void convertFromXML(Element node)
	{
		assert (node != null);
		assert (node.getName().compareTo("detector") == 0);

		// Iterate over <parameter> elements
		for (Object object : node.getChildren("parameter"))
		{
			Element element = (Element) object;

			// Get name
			String name = element.getAttributeValue("name");
			assert (name != null);

			// Get parameter type
			String type = element.getAttributeValue("type");

			// Get parameter value (can be null)
			String value = element.getAttributeValue("value");

			// Get value from text data within the tag if no value attribute
			// exists
			if ( value == null )
			{
				value = element.getTextNormalize();
			}

			Object convert = null;

			// Explicit type conversion
			if ( type != null )
			{
				// Convert to Double
				if ( type.compareToIgnoreCase("double") == 0 ||
						type.compareToIgnoreCase("dbl") == 0 )
				{
					convert = Double.parseDouble(value);
				}
				// Convert to Integer
				else if ( type.compareToIgnoreCase("integer") == 0
						|| type.compareToIgnoreCase("int") == 0 )
				{
					convert = Integer.parseInt(value);
				}
				// Convert to String
				else if ( type.compareToIgnoreCase("string") == 0 || 
						type.compareToIgnoreCase("str") == 0)
				{
					convert = value;
				}
				// Type is invalid
				else
				{
					throw new RuntimeException("Unknown type " + type + " for parameter " + name);
				}
			} 
			// Implicit type conversion
			else
			{
				try
				{
					// First try parsing as Double
					convert = Double.parseDouble(value);
				} 
				catch (Exception e)
				{

					try
					{
						// Next try Int
						convert = Integer.parseInt(value);
					} 
					catch (Exception ee)
					{
						// Lastly, default to String
						convert = value;
					}
				}
			}

			// add the parameter
			//System.out.println("adding parameter " + name + "=" + value + " of type " + type);
			addParameter(name, convert);
		}
	}

	/** Add a parameter to the ParameterSet, checking that the type is valid. */
	public void addParameter(String name, Object object)
	{
		if ( object instanceof Double || object instanceof Integer
				|| object instanceof String )
		{
			if ( _parameters.get(name) == null )
			{
				_parameters.put(name, object);
			} else
			{
				throw new RuntimeException("Parameter " + name
						+ " already exists in this ParameterSet.");
			}
		} else
		{
			throw new RuntimeException("Parameter " + name
					+ " is not of type Double, Integer or String.");
		}
	}

	/** Return a named parameter as a generic java Object. */
	public Object getParameter(String name, Class klass)
	{
		Object object = _parameters.get(name);
		if ( object.getClass() == klass )
		{
			return object;
		} else
		{
			System.err.println("Found object but type does not match.");
			return null;
		}
	}

	/** Checks if the parameter exists. */
	public boolean exists(String name)
	{
		return _parameters.containsKey(name);
	}

	/** Return the list of parameter names. */
	public Set<String> getParameterNames()
	{
		return _parameters.keySet();
	}

	/** Return a named parameter of type Double. */
	public Double getDouble(String name)
	{
		return (Double) getParameter(name, Double.class);
	}

	/** Return a named parameter of type Integer. */
	public Integer getInteger(String name)
	{
		return (Integer) getParameter(name, Integer.class);
	}

	/** Return a named parameter of type String. */
	public String getString(String name)
	{
		return (String) getParameter(name, String.class);
	}
}
