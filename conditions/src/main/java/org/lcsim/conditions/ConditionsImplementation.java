package org.lcsim.conditions;

/**
 * 
 * @author Tony Johnson
 */
abstract class ConditionsImplementation implements Conditions {
    private ConditionsManagerImplementation manager;
    private String name;

    /** Creates a new instance of ConditionsImplementation */
    ConditionsImplementation(ConditionsManagerImplementation manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    public void addConditionsListener(ConditionsListener listener) {
        manager.addConditionsListener(listener);
    }

    public void removeConditionsListener(ConditionsListener listener) {
        manager.removeConditionsListener(listener);
    }

    public RawConditions getRawSubConditions(String name) {
        return manager.getRawConditions(this.name + "/" + name);
    }

    public ConditionsSet getSubConditions(String name) {
        return manager.getConditions(this.name + "/" + name);
    }

    public <T> CachedConditions<T> getCachedSubConditions(Class<T> type, String name) {
        return manager.getCachedConditions(type, this.name + "/" + name);
    }

    String getName() {
        return name;
    }

    ConditionsManagerImplementation getManager() {
        return manager;
    }
}