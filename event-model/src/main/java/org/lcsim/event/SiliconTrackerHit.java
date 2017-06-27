package org.lcsim.event;

/**
 * An interface to be implemented by SiliconTrackerHits
 * @author tonyj
 * @version $Id: SiliconTrackerHit.java,v 1.2 2006/06/28 04:48:30 jstrube Exp $
 */
public interface SiliconTrackerHit
{
   long getCellID();
   int getTimestamp();
   int getADCCounts();
}
