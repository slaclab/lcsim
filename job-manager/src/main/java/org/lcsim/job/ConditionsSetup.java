package org.lcsim.job;

import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;

/**
 * Simple helper class for setup of the conditions system by the job manager.
 * <p>
 * Methods are called from the {@link org.lcsim.job.JobControlManager} in the following order.
 * <nl>
 * <li>{@link #configure()} - configure conditions system before it is initialized
 * <li>{@link org.lcsim.job.ConditionsSetup#setup()} - possibly initialize conditions system with detector and run
 * <li>{@link #postInitialize()} - perform post-initialization actions
 * <li>{@link #cleanup()} - cleanup conditions system at end of job
 * </nl>
 * 
 * @author Jeremy McCormick, SLAC
 */
public interface ConditionsSetup {
    
    /**
     * Add a conditions listener to the manager.
     * @param listener the conditions listener
     */
    void addConditionsListener(ConditionsListener listener);
    
    /**
     * Configure the conditions system by setting state before initialization.
     */
    void configure();
    
    /**
     * Setup the conditions system by initializing it.
     * <p>
     * This method will call <code>setDetector</code> on the conditions manager if
     * the run number and detector name are set.     
     * @throws ConditionsNotFoundException if there is a conditions error
     */
    void setup() throws ConditionsNotFoundException;
    
    /**
     * Post initialization action after conditions system is initialized.
     */
    void postInitialize();
    
    /**
     * Perform post-job cleanup of the conditions system.
     */
    void cleanup();
    
    /**
     * Set the run number to be used for initializing conditions.
     * @param run the run number
     */
    void setRun(Integer run);
    
    /**
     * Set the detector name for initializing conditions.
     * @param name the detector name
     */
    void setDetectorName(String name);
    
    /**
     * Add a detector alias.
     * @param name the name of the detector
     * @param alias the alias target
     */
    void addAlias(String name, String target);
}
