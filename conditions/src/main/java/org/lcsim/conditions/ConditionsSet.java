package org.lcsim.conditions;

import java.util.Set;

/**
 * 
 * @author Tony Johnson $Id: ConditionsSet.java,v 1.1.1.1 2010/01/25 22:23:07
 *         jeremy Exp $
 */
public interface ConditionsSet extends Conditions {
    double getDouble(String key) throws IllegalArgumentException;

    double getDouble(String key, double defValue);

    double[] getDoubleArray(String key) throws IllegalArgumentException;

    int getInt(String key) throws IllegalArgumentException;

    int getInt(String key, int defValue);

    String getString(String key) throws IllegalArgumentException;

    String getString(String key, String defValue);

    boolean getBoolean(String key) throws IllegalArgumentException;

    boolean getBoolean(String key, boolean defValue);

    int size();

    Set keySet();

    boolean containsKey(String key);

    Class getType(String key);

}
