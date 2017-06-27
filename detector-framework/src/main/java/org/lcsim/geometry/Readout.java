/*
 * Readout.java
 * 
 * Created on July 18, 2005, 3:54 AM
 */

package org.lcsim.geometry;

import org.lcsim.geometry.util.IDDescriptor;

/**
 * 
 * @author jeremym
 */
public interface Readout
{
    public String getName();

    public IDDecoder getIDDecoder();

    public IDDescriptor getIDDescriptor();
}