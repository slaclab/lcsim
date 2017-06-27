package org.lcsim.geometry.compact;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of system ID to Subdetectors, used by
 * org.lcsim.geometry.compact.Detector.
 * 
 * @author jeremym
 */
class SystemIDMap
{
    private Map<Integer, Subdetector> _subdetectors = new HashMap<Integer, Subdetector>();

    /**
     * Add an entry mapping system id to a Subdetector.
     * 
     * @param sysid
     *            System ID which must be > 0
     * @param subdetector
     *            Subdetector with this sysid
     */
    protected void add(int sysid, Subdetector subdetector)
    {
        if (sysid > 0)
        {
            if (_subdetectors.containsKey(sysid))
            {
                throw new RuntimeException("The System ID " + sysid + " of " + subdetector.getName()
                        + " is already used by " + _subdetectors.get(sysid).getName());
            }
            else
            {
                _subdetectors.put(sysid, subdetector);                
            }
        }
//        else
//        {
//            System.err.println("WARNING: The system id <" + sysid + "> of Subdetector <" + subdetector.getName()
//                    + "> was ignored, because it is not greater than zero.");
//        }
    }

    /** Retrieve a Subdetector by system ID. */
    protected Subdetector get(int sysid)
    {
        return _subdetectors.get(sysid);
    }
}
