package org.lcsim.recon.tracking.seedtracker;

/**
 * Classes implementing this interface can define additional requirements on seeds and tracks.
 * To make a particular {@link SeedTracker} driver use these requirements, call its
 * <tt>setTrackCheck(TrackCheck)</tt> method.
 * <p>
 * The <tt>checkSeed(SeedCandidate)</tt> method will be called for each 3-hit seed candidate
 * after the initial helix fit, before trying to attach any additional hits in confirmation layers.
 * If the method returns <tt>false</tt>, the seed candidate will be discarded.
 * <p>
 * The <tt>checkTrack(SeedTrack)</tt> method will be called for each found track before saving
 * it into the event record. If the method returns <tt>false</tt>, the track will be discarded.
 *
 * @author D. Onoprienko
 * @version $Id: TrackCheck.java,v 1.1 2009/08/07 23:33:17 onoprien Exp $
 */
public interface TrackCheck {

  boolean checkSeed(SeedCandidate candidate);

  boolean checkTrack(SeedTrack track);

}
