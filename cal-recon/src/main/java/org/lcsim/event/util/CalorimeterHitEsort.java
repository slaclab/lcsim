package org.lcsim.event.util;

import java.util.Comparator;

import org.lcsim.event.CalorimeterHit;
/**
 * @version $Id: CalorimeterHitEsort.java,v 1.4 2006/06/28 04:48:33 jstrube Exp $
 */

public class CalorimeterHitEsort implements Comparator
{
    public int compare(Object obj1, Object obj2)
    {
        CalorimeterHit v1 = (CalorimeterHit) obj1;
        CalorimeterHit v2 = (CalorimeterHit) obj2;
        return (int) Math.signum(v2.getRawEnergy() - v1.getRawEnergy());
    }
}