package org.lcsim.recon.cat;

import java.util.*;

import org.lcsim.event.*;
import org.lcsim.util.Driver;

import org.lcsim.recon.cat.util.NoSuchParameterException;

/**
 * Driver for removing tracker hits associated with previously found tracks.
 *
 * @author D. Onoprienko
 * @version $Id: AssociatedHitRemover.java,v 1.1 2007/04/06 21:48:14 onoprien Exp $
 */
public class AssociatedHitRemover extends Driver {
  
  // --  Constructors :  -------------------------------------------------------
  
  public AssociatedHitRemover() {}
  
  // -- Setters :  -------------------------------------------------------------
  
  /**
   * Set any <tt>String</tt> parameter.
   * The following parameters can be set with this method:<br>
   * <tt>"Track_List"</tt> - name of input track list. Hits associated with tracks from this list will be excluded.
   * <tt>"Input_Tracker_Hit_List"</tt> - name of input hit list.
   * <tt>"Output_Tracker_Hit_List"</tt> - name of output hit list.
   * @param name   Name of parameter to be set
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, Object value) throws NoSuchParameterException {
    if (name.equalsIgnoreCase("Track_List")) {
      _trackListName = (String)value;
    } else if (name.equalsIgnoreCase("Input_Tracker_Hit_List")) {
      _inputTrackerHitListName = (String)value;
    } else if (name.equalsIgnoreCase("Output_Tracker_Hit_List")) {
      _outputTrackerHitListName = (String)value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
  
  // -- Processing event :  ----------------------------------------------------
  
  /**
   * Process event.
   */
  public void process(EventHeader event) {
    
    _event = event;
    
    // Fetch input lists :
    
    _inputTrackerHitList = _event.get(TrackerHit.class, _inputTrackerHitListName);
    _trackList = _event.get(Track.class, _trackListName);
    
    // Compile list of tracker hits associated with tracks :
    
    ArrayList<TrackerHit> associatedTrackerHitList = new ArrayList<TrackerHit>();
    for (Track track : _trackList) {
      associatedTrackerHitList.addAll(track.getTrackerHits());
    }
    
    // Compile a list of unassociated hits :
    
    ArrayList _outputTrackerHitList = new ArrayList<TrackerHit>();
    for (TrackerHit hit : _inputTrackerHitList) {
      for (TrackerHit assHit : associatedTrackerHitList) {
        hit = match(hit, assHit);
        if (hit == null) break;
      }
      if (hit != null) _outputTrackerHitList.add(hit);
    }
    _outputTrackerHitList.trimToSize();
    
    // Put the output list back into the event :
    
    _event.put(_outputTrackerHitListName, _outputTrackerHitList, TrackerHit.class, 0);
    
    return;
    
  }

  /**
   * Returns the remaining portion of hit after subtracting refHit from it.
   * Used for matching hits from input collection with hits associated with
   * previously found tracks. Simple implementation here returns the original hit
   * if both <tt>hit</tt> and <tt>refHit</tt> refer to the same object. Subclasses 
   * may override this method with more elaborate algorithms.
   */
  protected TrackerHit match(TrackerHit hit, TrackerHit refHit) {
    return (hit == refHit) ? null : hit ;
  }
  
  // -- Private helper methods :  ----------------------------------------------
  
  
  // -- Private data :  --------------------------------------------------------
  
  protected EventHeader _event;
  
  protected String _inputTrackerHitListName;
  protected String _trackListName;
  protected String _outputTrackerHitListName;
  
  protected List<TrackerHit> _inputTrackerHitList;
  protected List<Track> _trackList;
  protected ArrayList<TrackerHit> _outputTrackerHitList;
  
}
