package org.lcsim.geometry.subdetector;

import org.lcsim.geometry.Tracker;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractTracker.java,v 1.4 2010/12/03 01:21:39 jeremy Exp $
 */
abstract class AbstractTracker extends AbstractLayeredSubdetector implements Tracker
{
    public AbstractTracker( Element node ) throws JDOMException
    {
       super( node );
    }

    public boolean isTracker()
    {
        return true;
    }
}