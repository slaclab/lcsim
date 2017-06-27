/*
 * SiliconRawHit.java
 *
 * Created on February 21, 2006, 5:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.event.base;

/**
 *
 * @author ngraf
 */
public class SiliconRawHit
{
    private int _cellID0;
    private int _cellID1;
    private int _timeStamp;
    private int _adcCounts;
    
    /** Creates a new instance of SiliconRawHit */
    public SiliconRawHit(int cellID0, int cellID1, int timeStamp, int adcCounts)
    {
        _cellID0 = cellID0;
        _cellID1 = cellID1;
        _timeStamp = timeStamp;
        _adcCounts = adcCounts;
    }

    /** The id0 of the cell that recorded the hit.
     */
    public int getCellID0()
    {
        return _cellID0;
    }

    /** The id1 of the cell that recorded the hit.
     */
    public int getCellID1()
    {
        return _cellID1;
    }

    /** The detector specific time stamp of the hit.
     */
    public int getTimeStamp()
    {
        return _timeStamp;
    }

    /** The ADC counts of the hit.
     */
    public int getADCCounts()
    {
        return _adcCounts;
    }
    
    /** Adds counts adcCounts at time timeStamp.
     * If timeStamp is prior to current time, time will be overwritten. 
     */
    public void addHit(int timeStamp, int adcCounts)
    {
        _adcCounts+=adcCounts;
        if(timeStamp<_timeStamp) _timeStamp=timeStamp;
    }
    
}
