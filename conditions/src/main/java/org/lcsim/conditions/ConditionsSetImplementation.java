package org.lcsim.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 
 * @author Tony Johnson $Id: ConditionsSetImplementation.java,v 1.1.1.1
 *         2010/01/25 22:23:07 jeremy Exp $
 */
class ConditionsSetImplementation extends ConditionsImplementation implements ConditionsSet {
    ConditionsSetImplementation(ConditionsManagerImplementation manager, String name) throws IOException {
        super(manager, name);
        // We assume this points to a properties file
        InputStream in = manager.open(name, "properties");
        try {
            props = new Properties();
            props.load(in);
        } finally {
            in.close();
        }
    }

    public int getInt(String name) {
        String value = props.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + name);
        return parseInt(value, name);
    }

    public int getInt(String name, int defaultValue) {
        String value = props.getProperty(name);
        if (value == null)
            return defaultValue;
        return parseInt(value, name);
    }

    public double getDouble(String name) {
        String value = props.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + name);
        return parseDouble(value, name);
    }

    public double getDouble(String name, double defaultValue) {
        String value = props.getProperty(name);
        if (value == null)
            return defaultValue;
        return parseDouble(value, name);
    }

    public double[] getDoubleArray(String name) {
        String value = props.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + name);
        return parseDoubleArray(value, name);
    }

    private int parseInt(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error reading conditions: Illegal value " + value + " for " + name);
        }
    }

    private double parseDouble(String value, String name) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error reading conditions: Illegal value " + value + " for " + name);
        }
    }

    private double[] parseDoubleArray(String value, String name) {
        List<Double> doubles = new ArrayList<Double>();
        StringTokenizer st = new StringTokenizer(value, ",");
        while (st.hasMoreTokens()) {
            String doubleValue = st.nextToken();
            try {
                doubles.add(Double.parseDouble(doubleValue));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Error reading conditions: Illegal value " + value + " for " + name);
            }
        }
        // should be an easier way to do this...
        double[] returnDoubles = new double[doubles.size()];
        int i = 0;
        for (Double d : doubles) {
            returnDoubles[i++] = d;
        }
        return returnDoubles;
    }

    public String getString(String name) {
        String value = props.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + name);
        return value;
    }

    public String getString(String name, String defValue) {
        return props.getProperty(name, defValue);
    }

    public boolean getBoolean(String name) throws IllegalArgumentException {
        String value = props.getProperty(name);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + name);
        return parseBoolean(value, name);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String value = props.getProperty(name);
        if (value == null)
            return defaultValue;
        return parseBoolean(value, name);
    }

    private boolean parseBoolean(String value, String name) {
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
            return true;
        if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value))
            return false;
        throw new RuntimeException("Error reading conditions: Illegal value " + value + " for " + name);
    }

    public Set keySet() {
        return props.keySet();
    }

    public Class getType(String key) {
        String value = props.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("Missing value for " + key);
        try {
            Integer.parseInt(value);
            return Integer.TYPE;
        } catch (NumberFormatException x) {
            try {
                Double.parseDouble(value);
                return Double.TYPE;
            } catch (NumberFormatException xx) {
                return String.class;
            }
        }
    }

    public int size() {
        return props.size();
    }

    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    private Properties props;
}
