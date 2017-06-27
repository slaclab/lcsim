package org.lcsim.conditions;

import java.util.EventObject;

/**
 * @version $Id: ConditionsEvent.java,v 1.2 2013/10/18 21:42:47 jeremy Exp $
 * @author Tony Johnson
 */
public class ConditionsEvent extends EventObject {
    ConditionsEvent(ConditionsManager source) {
        super(source);
    }

    public ConditionsManager getConditionsManager() {
        return (ConditionsManager) getSource();
    }
}
