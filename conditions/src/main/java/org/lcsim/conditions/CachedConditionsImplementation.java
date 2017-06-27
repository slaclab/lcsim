package org.lcsim.conditions;

/**
 * @version $Id: CachedConditionsImplementation.java,v 1.1.1.1 2010/01/25
 *          22:23:07 jeremy Exp $
 * @author tonyj
 */
class CachedConditionsImplementation<T> extends ConditionsImplementation implements CachedConditions<T>, ConditionsListener {
    private ConditionsConverter<T> conv;
    private T data;

    CachedConditionsImplementation(ConditionsManagerImplementation manager, String name, ConditionsConverter<T> conv) {
        super(manager, name);
        this.conv = conv;
        manager.addConditionsListener(this);
    }

    public T getCachedData() {
        if (data == null)
            data = conv.getData(getManager(), getName());
        return data;
    }

    public void conditionsChanged(ConditionsEvent conditionsEvent) {
        data = null;
    }
}
