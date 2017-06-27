/*
 * DetectorLocator.java
 *
 * Created on July 17, 2005, 6:53 PM
 *
 */

package org.lcsim.geometry.util;

import org.lcsim.conditions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.util.cache.FileCache;

/**
 *
 * @author jeremym
 *
 */
abstract public class DetectorLocator
{    
    static final public String MASTER_TAGLIST_URL = "http://lcsim.org/detectors/taglist.txt";    
    
    public static Detector getCurrentDetector()
    {
        return findDetector(ConditionsManager.defaultInstance().getDetector());
    }
    
    /**
     * 
     * Static utility function to lookup a Detector by name and run number.
     *  
     * @param detName name of detector to lookup
     * @param runNum run number
     * @param mgr ConditionsManager, which is allowed to be null
     * SIDE EFFECT: Sets the state of the default ConditionsManager if mgr param is null. 
     */
    public static Detector findDetector(String detName, int runNum, ConditionsManager mgr)
    {
        Detector detector = null;
        try {

            /* Set reference to default condMgr if null. */
            if ( mgr == null ) mgr = ConditionsManager.defaultInstance();
            
            /* Set the detector in the conditions manager. */
            mgr.setDetector(detName,0);
        
            /* Get detector object by using GeometryReader. */
            RawConditions conditions = mgr.getRawConditions("compact.xml");
            InputStream in = conditions.getInputStream();
            GeometryReader reader = new GeometryReader();
            detector = (Detector) reader.read(in);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Problem while locating detector " + detName, e);
        }
            
        return detector;
    }

    /* Version of above with default run number and conditions manager. */
    public static Detector findDetector(String detName)
    {
        return findDetector(detName, 0, ConditionsManager.defaultInstance() );
    }
    
    public static List<String> getDetectorNameList()
    {
        File file;
        FileCache cache;
        BufferedReader reader;
        ArrayList<String> detList = new ArrayList<String>();
        try {
            cache = new FileCache ();
            file = cache.getCachedFile (new URL (MASTER_TAGLIST_URL) );
            reader = new BufferedReader (new FileReader (file));
            
            for (;;)
            {
                String line = reader.readLine();
                if (line == null) break;
                detList.add (line.trim ());
            }
        
            reader.close ();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error fetching master detector list.", e);
        }                
        
        return detList;
    }
  
    /** 
     * SIDEEFFECT: ConditionsManager will have conditions for last detector found.
     */
    private static void cacheDetectors(List<String> names)
    {
        for ( String n : names )
        {
            Detector det = findDetector(n);            
        }
    }
    
    public static void cacheDetectors()
    {
        cacheDetectors( getDetectorNameList() );
    }
}
