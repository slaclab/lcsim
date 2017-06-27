package org.lcsim.geometry.subdetector;

import org.lcsim.geometry.Tracker;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: TestBeamTracker.java,v 1.4 2010/11/30 00:16:29 jeremy Exp $
 */
public class TestBeamTracker extends AbstractTestBeam implements Tracker {

    public TestBeamTracker(Element node) throws JDOMException {
        super(node);
    }

    public boolean isTracker() {
        return true;
    }
}