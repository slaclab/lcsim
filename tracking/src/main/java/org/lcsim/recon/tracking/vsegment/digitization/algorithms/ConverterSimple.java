package org.lcsim.recon.tracking.vsegment.digitization.algorithms;

import java.util.*;

import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.recon.cat.util.NoSuchParameterException;

import org.lcsim.recon.tracking.vsegment.digitization.SimToDigiConverter;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.base.DigiTrackerHitElemental;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * Very simple-minded converter - for illustration purposes only.
 * Creates one <tt>DigiTrackerHit</tt> per input <tt>SimTrackerHit</tt>.
 * No smearing.
 * 
 * @author D.Onoprienko
 * @version $Id: ConverterSimple.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class ConverterSimple extends SimToDigiConverter {
  
// -- Constructors :  ----------------------------------------------------------
  
  public ConverterSimple() {
    _signalMode = SignalMode.ENERGY;
  }

// -- Setters :  ---------------------------------------------------------------
  
  /**
   * Set parameters. 
   * The following parameters can be set with this method:
   * <dl>
   * <dt>"SIGNAL"</dt> <dd>Tells converter how to calculate signal value assigned to 
   *            created <tt>DigiTrackerHits</tt>. Possible values:
   *           <tt>"UNIT"</tt> - each <tt>DigiTrackerHit</tt> is assigned signal value 1.;
   *           <tt>"COUNT_SIM_HITS"</tt> - each <tt>DigiTrackerHit</tt> is assigned signal value
   *                    equal to the number of <tt>SimTrackerHits</tt> that contributed to it;
   *           <tt>"ENERGY"</tt> - each <tt>DigiTrackerHit</tt> is assigned signal value
   *                    equal to combined energy depositions of <tt>SimTrackerHits</tt> 
   *                    that contributed to it.
   *           <br>Default: "ENERGY".</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, Object value) {
    try {
      if (name.equalsIgnoreCase("SIGNAL")) {
        String mode = (String)value;
        if (mode.equalsIgnoreCase("UNIT")) {
          _signalMode = SignalMode.UNIT;
        } else if (mode.equalsIgnoreCase("COUNT_SIM_HITS")) {
          _signalMode = SignalMode.COUNT;
        } else if (mode.equalsIgnoreCase("ENERGY")) {
          _signalMode = SignalMode.ENERGY;
        } else {
          throw new IllegalArgumentException("Illegal value "+mode+" for parameter "+name);
        }
      } else {
        throw new NoSuchParameterException(name, this.getClass());
      }
    } catch (ClassCastException x) {
      throw new IllegalArgumentException("Value of incompatible type", x);
    }
 }
  
// -- Digitization :  ----------------------------------------------------------
  
  /**
   * Convert a list of <tt>SimTrackerHit</tt>s that were produced in a single
   * track-sensor crossing into a list of <tt>DigiTrackerHit</tt>s.
   */
  public List<DigiTrackerHit> convert(List<SimTrackerHit> hits) {
    
    if (hits.isEmpty()) return new ArrayList<DigiTrackerHit>(1);

    MCParticle mcParticle = hits.get(0).getMCParticle();
    
    // create DigiTrackerHit for each SimTrackerHit :
    
    LinkedList<DigiTrackerHit> digiListTemp = new LinkedList<DigiTrackerHit>();
    for (SimTrackerHit hit : hits) {
      Sensor sensor = _segMan.getSensor(hit);
      if (sensor == null) continue;
      int channel = _segMan.getChannelID(sensor, hit);
      if (channel < 1) continue;
      double time = hit.getTime();
      double signal;
      if (_signalMode == SignalMode.ENERGY) {
        double path = hit.getPathLength();
        signal = hit.getdEdx() * ( (path > 0.) ? path : 1. );
      } else {
        signal = 1.;
      }
      if (signal > 0.) digiListTemp.add(new DigiTrackerHitElemental(signal, time, sensor, channel, mcParticle));
    }

    // combine DigiTrackerHits with the same channel ID :
    
    ArrayList<DigiTrackerHit> digiListFinal = new ArrayList<DigiTrackerHit>();
    while (!digiListTemp.isEmpty()) {
      ListIterator<DigiTrackerHit> it = digiListTemp.listIterator();
      int channel = -1;
      Sensor sensor = null;
      double time = 0.;
      double signal = 0.;
      while (it.hasNext()) {
        DigiTrackerHit dHit = it.next();
        if (channel == -1) {
          channel = dHit.getChannel();
          sensor = dHit.getSensor();
        }
        if (channel == dHit.getChannel() && sensor == dHit.getSensor()) {
          time += dHit.getTime()*dHit.getSignal();
          signal += dHit.getSignal();
          it.remove();
        }
      }
      if (signal > 0.) time /= signal;
      if (_signalMode == SignalMode.COUNT) signal = 1.;
      digiListFinal.add(new DigiTrackerHitElemental(signal, time, sensor, channel, mcParticle));
    }
    digiListFinal.trimToSize();

    return digiListFinal;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private enum SignalMode {UNIT, COUNT, ENERGY};
  private SignalMode _signalMode;

}
