package org.lcsim.geometry;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

/**
 * 
 * @author tonyj
 */
public interface HepRepProvider
{
    public void appendHepRep( HepRepFactory factory, HepRep heprep );
}
