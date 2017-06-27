package org.lcsim.recon.tracking.trfbase;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/** The class McCluster provides a simple implementation of the list
 * of MC track ID's for a cluster.  The list is provided in the
 * constructor and is held directly.
 *
 * This class is abstract due to pure virtual methods in its base.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */


public abstract class McCluster extends Cluster
{
    
    // data
    
    // List of associated Monte Carlo track ID's.
    private int[] _mcids;
    
    // methods
    
    //
    
    /**
     *Default constructor.
     *
     */
    public McCluster()
    {
        _mcids = new int[0];
    }
    
    //
    
    /**
     *Constructor from a single mc id
     *
     * @param   mcid  MC track ID to associate with this Cluster
     */
    public McCluster( int mcid )
    {
        _mcids = new int[1];
        _mcids[0] = mcid;
    }
    
    //
    
    /**
     *Constructor from an array of mc ids
     *
     * @param   mcids  array of MC tracks ID's  to associate with this Cluster
     */
    public McCluster( int[] mcids)
    {
        _mcids = new int[mcids.length];
        System.arraycopy(mcids,0, _mcids, 0, mcids.length);
    }
    
    //
    
    /**
     *Constructor from a List of mc ids
     *
     * @param   mcids List of MC tracks ID's  to associate with this Cluster
     */
    public McCluster( List mcids)
    {
        _mcids = new int[mcids.size()];
        int i = 0;
        for(Iterator it = mcids.iterator(); it.hasNext(); )
        {
            _mcids[i++] = ((Integer) it.next()).intValue();
        }
    }
    
    //
    
    /**
     *Copy constructor.
     *
     * @param   mcclus McCluster to replicate
     */
    public McCluster( McCluster mcclus)
    {
        _mcids = mcclus._mcids;
    }
    
    //
    
    /**
     *Return the ID's of MC tracks contributing to this cluster.
     *
     * @return   List of MC track ID's for this cluster
     */
    public List mcIds()
    {
        List list = new ArrayList();
        for(int i = 0; i<_mcids.length; ++i)
        {
            list.add(new Integer(_mcids[i]));
        }
        return list;
    }
    
    
    /**
     *Return the ID's of MC tracks contributing to this cluster.
     *
     * @return  array of MC track ID's for this cluster
     */
    public int[] mcIdArray()
    {
        int[] tmp = new int[_mcids.length];
        System.arraycopy(_mcids, 0, tmp, 0, _mcids.length);
        return tmp;
        
    }
    
}

