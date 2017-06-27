package org.lcsim.conditions;

/**
 * @version $Id: ConditionsConverter.java,v 1.1.1.1 2010/01/25 22:23:07 jeremy
 *          Exp $
 * @author tonyj
 */
public interface ConditionsConverter<T> {
    Class<T> getType();

    T getData(ConditionsManager manager, String name);
}