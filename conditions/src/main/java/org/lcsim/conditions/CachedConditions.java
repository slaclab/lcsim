package org.lcsim.conditions;

/**
 * @version $Id: CachedConditions.java,v 1.1.1.1 2010/01/25 22:23:07 jeremy Exp
 *          $
 * @author tonyj
 */
public interface CachedConditions<T> extends Conditions {
    T getCachedData();
}