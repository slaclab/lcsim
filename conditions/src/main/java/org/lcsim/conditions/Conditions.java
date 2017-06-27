package org.lcsim.conditions;

import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;

/**
 * A base class implemented by all types of conditions.
 * @version $Id: Conditions.java,v 1.2 2013/10/18 21:42:47 jeremy Exp $
 * @author Tony Johnson
 */
public interface Conditions {
    ConditionsSet getSubConditions(String name) throws ConditionsSetNotFoundException;

    RawConditions getRawSubConditions(String name) throws ConditionsSetNotFoundException;

    <T> CachedConditions<T> getCachedSubConditions(Class<T> type, String name) throws ConditionsSetNotFoundException;

    /**
     * Add a listener to be notified about changes to these conditions.
     * @param listener The listener to add.
     */
    void addConditionsListener(ConditionsListener listener);

    /**
     * Remove a change listener from these conditions.
     * @param listener The listener to remove.
     */
    void removeConditionsListener(ConditionsListener listener);
}
