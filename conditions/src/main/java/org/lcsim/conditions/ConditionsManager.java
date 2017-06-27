package org.lcsim.conditions;

/**
 * The conditions manager is the main interface to the conditions system. The
 * conditions manager allows conditions to be stored in a hierarchical
 * structure, but places no restrictions on the type of data provided. The
 * conditions manager makes no assumptions about how the data is stored or
 * retrieved.
 */
public abstract class ConditionsManager {
    private static ConditionsManager theConditionsManager;

    /**
     * Get the default (shared) condition manager implementation.
     * @return The default conditions manager.
     */
    public static ConditionsManager defaultInstance() {
        if (theConditionsManager == null)
            theConditionsManager = new ConditionsManagerImplementation();
        return theConditionsManager;
    }

    public static void setDefaultConditionsManager(ConditionsManager manager) {
        //System.out.println("ConditionsManager.setDefaultConditionsManager from...");
        //new RuntimeException().printStackTrace();
        theConditionsManager = manager;
    }
    
    /**
     * Return true if there is a conditions manager installed.
     * @return true if conditions managed is setup
     */
    public static boolean isSetup() {
        return theConditionsManager != null;
    }

    /**
     * Normally called automatically by the framework to set the detector name
     * and run number,
     * @param name The current detector name.
     * @param run The current run number.
     * @throws org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException
     *             If the conditions associated with this detector/run number
     *             can not be found.
     */
    public abstract void setDetector(String name, int run) throws ConditionsNotFoundException;

    /**
     * Set the current run number. Normally called automatically by the
     * framework.
     * @param run The current run number
     * @throws org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException
     *             If the conditions associated with the specified run number
     *             can not be found.
     */
    public abstract void setRun(int run) throws ConditionsNotFoundException;

    /**
     * Get the current detector name
     * @return The detector name.
     */
    public abstract String getDetector();

    /**
     * Get the current run number.
     * @return The run number.
     */
    public abstract int getRun();

    /**
     * Get the conditions associated with the given name.
     * @param name The name of the conditions to search for.
     * @throws org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException
     *             If the named conditions can not be found.
     * @return The requested conditions.
     */
    public abstract ConditionsSet getConditions(String name) throws ConditionsSetNotFoundException;

    /**
     * Access conditions converted to a java object using a conditions
     * converter. The conditions are cached so that they do not need to be
     * re-read each time the same object is requested.
     * @param type The type of conditions requested (used to select an
     *            appropriate conditions converter).
     * @param name The name of the conditions requested.
     * @throws org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException
     *             If the specified conditions can not be found.
     * @return The converted conditions.
     */
    public abstract <T> CachedConditions<T> getCachedConditions(Class<T> type, String name) throws ConditionsSetNotFoundException;

    /**
     * Get an input stream to directly read raw conditions from the database.
     * The database makes no assumptions about the format of the data when this
     * method is used.
     * @param name The name of the conditions requested.
     * @throws org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException
     *             If the requested conditions can not be found.
     * @return The input stream from which the data can be read. The called
     *         should close thsi input stream when they have finished using it.
     */
    public abstract RawConditions getRawConditions(String name) throws ConditionsSetNotFoundException;

    /**
     * Adds a conditions converter. A conditions converter can be used to
     * convert data requested by the user into a specific Java object.
     * @param conv The converter to add.
     */
    public abstract void registerConditionsConverter(ConditionsConverter conv);

    /**
     * Removes a conditions converter.
     * @param conv The converter to remove.
     */
    public abstract void removeConditionsConverter(ConditionsConverter conv);

    /**
     * Add a listener to be notified about changes to ANY conditions.
     * @param listener The listener to add.
     */
    public abstract void addConditionsListener(ConditionsListener listener);

    /**
     * Remove a global change listener.
     * @param listener The listener to remove.
     */
    public abstract void removeConditionsListener(ConditionsListener listener);

    /**
     * Thrown if conditions associated with a given detector can not be found.
     */
    public static class ConditionsNotFoundException extends Exception {
        public ConditionsNotFoundException(String name, int run) {
            super("Conditions not found for detector " + name);
        }

        public ConditionsNotFoundException(String name, int run, Throwable t) {
            super("Conditions not found for detector " + name, t);
        }
    }

    /**
     * Thrown if specific set of conditions can not be found.
     */
    public static class ConditionsSetNotFoundException extends RuntimeException {
        public ConditionsSetNotFoundException(String message) {
            super(message);
        }

        public ConditionsSetNotFoundException(String message, Throwable t) {
            super(message, t);
        }
    }
}
