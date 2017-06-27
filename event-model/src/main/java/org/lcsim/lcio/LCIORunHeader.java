package org.lcsim.lcio;

import java.util.Map;

/**
 * Not yet made public pending some thought about the right way to deal
 * with run headers.
 * @author tonyj
 */
interface LCIORunHeader
{
    int getRunNumber();
    
    /** Returns the name of the detector setup used in the simulation.
     */
    String getDetectorName();
    
    /** Description of the simulation, physics channels etc.
     */		 
    String getDescription();
    
    /** Returns the names of the active subdetectors
     *  used in the simulation.
     */ 
    String[] getActiveSubdetectors();

    /** Parameters defined for this run.
     */
    Map<String,int[]> getIntegerParameters();
    Map<String,float[]> getFloatParameters();
    Map<String,String[]> getStringParameters();
}
