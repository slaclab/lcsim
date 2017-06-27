package org.lcsim.recon.tracking.vsegment.mctruth;

import java.util.*;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * If this driver is added to the processing chain, an {@link MCTruth} object will
 * be created and put into the event. This object can later be retrieved by other
 * drivers through a call to <tt>event.get("MCTruth")</tt>, and used to access Monte
 * Carlo truth information. Some drivers, like <tt>DigitizationDriver</tt>, will 
 * automatically check for the presence of an <tt>MCTruth</tt> object in the event,
 * and if present, they will use it to store relations between objects they create
 * and objects created by the simulator, like <tt>MCParticles</tt> and <tt>SimTrackerHits</tt>.
 *
 * @author D. Onoprienko
 * @version $Id: MCTruthDriver.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class MCTruthDriver extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  public MCTruthDriver() {
  }
// -- Event processing :  ------------------------------------------------------
  
  public void process(EventHeader event) {
    
    // Process children if any
    
    super.process(event);
    
    // Create MCTruth object and attach it to the event
    
    MCTruth mcTruth = new MCTruth(this, event);
    event.put("MCTruth", mcTruth);
    
  }
  
// -- Private parts :  ---------------------------------------------------------
  
}
