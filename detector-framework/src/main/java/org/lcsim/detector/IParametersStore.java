package org.lcsim.detector;

/**
 * Access to all {@link IParameters} objects for the current detector.
 * 
 * @author Jeremy McCormick
 * @version $Id: IParametersStore.java,v 1.2 2007/05/25 20:16:26 jeremy Exp $
 */
public interface IParametersStore 
extends IObjectStore<IParameters>
{
    /**
     * Get a set of parameters by name.
     * @param name The name of the parameter set.
     * @return     The parameters.
     */
    public IParameters get(String name);
}