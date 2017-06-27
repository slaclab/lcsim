package org.lcsim.job;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsReader;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.util.loop.LCSimConditionsManagerImplementation;

/**
 * Default implementation of {@link ConditionsSetup}.
 * 
 * @author Jeremy McCormick, SLAC
 */
public class DefaultConditionsSetup implements ConditionsSetup {

    private Integer run = null;
    private String detectorName = null;
    protected List<ConditionsListener> listeners = new ArrayList<ConditionsListener>();
    
    @Override
    public void addConditionsListener(ConditionsListener listener) {
        this.listeners.add(listener);
    }
    
    /**
     * Sub-classes should completely override this method and not call the super-class version,
     * especially if using a different conditions manager than the default.
     */
    @Override
    public void configure() {
        LCSimConditionsManagerImplementation.register();

        // Add extra listeners to manager.
        for (ConditionsListener listener : listeners) {
            ConditionsManager.defaultInstance().addConditionsListener(listener);
        }
    }

    @Override
    public void setup() throws ConditionsNotFoundException {               
        if (detectorName != null && run != null) {
            ConditionsManager.defaultInstance().setDetector(detectorName, run);
        }
    }

    @Override
    public void postInitialize() {
    }
    
    @Override
    public void setRun(Integer run) {
        this.run = run;
    }
    
    @Override
    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }
    
    @Override
    public void cleanup() {
    }
    
    @Override
    public void addAlias(String name, String target) {
        ConditionsReader.addAlias(name, target);
    }
}
