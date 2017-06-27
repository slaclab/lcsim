package org.lcsim.conditions;

import java.util.EventListener;

/**
 * A conditions listener can be registered by objects wishing to be notified
 * when a specific set of conditions changes.
 * @author Tony Johnson
 */
public interface ConditionsListener extends EventListener {
    /**
     * Called when the conditions associated with this listener change.
     * @param event The event associated with the change.
     */
    void conditionsChanged(ConditionsEvent event);
}
