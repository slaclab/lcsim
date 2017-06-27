package org.lcsim.recon.tracking.trflayer;

import java.util.*;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Miss;

/**
 * This class maintains a list of LayerStat objects, a pointer to
 * the current such object and a pointer to the cluster status.
 * The cluster status is the one holding the relevant cluster list
 * and miss.
 *<p>
 * These chains are constructed and modified by class LTrack.
 *<p>
 * Class LTrack is a friend so it can manipulate the chain and
 * iterators though private accessor methods.
 *<p>
 * The only public methods are the destructor and methods to return
 * the cluster list and miss.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class LayerStatChain
{
    
    // package (not private) attributes so LTrack can access them
    // chain
    LinkedList _stats; // List of LayerStat objects
    
    // current stats
    ListIterator _istat_current;
    
    // cluster status
    ListIterator _istat_cluster;
    
    // package (private) methods
    
    private boolean debug;
    
    // constructor from a layer status
    // current status is set, cluster status is left unset
    LayerStatChain( LayerStat lstat)
    {
        _stats = new LinkedList();
        _stats.add(lstat);
        _istat_current = _stats.listIterator(); //should be first iterator
        _istat_cluster = _stats.listIterator(_stats.size()); //should be last iterator, needs work
        if(debug) System.out.println("_istat_current.hasNext()= "+_istat_current.hasNext());
        if(debug) System.out.println("_istat_current= "+_istat_current);
        if(debug) System.out.println("_istat_cluster.hasNext()= "+_istat_cluster.hasNext());
        if(debug) System.out.println("_istat_cluster= "+_istat_cluster);
        
    }
    
    // return if the current status is set
    boolean currentStatusIsValid()
    {
        return _istat_current.hasNext();
    }
    
    // return the cluster status
    LayerStat clusterStatus()
    {
        Assert.assertTrue( clusterStatusIsValid() );
        return (LayerStat) _stats.get(_istat_cluster.nextIndex());
    }
    
    // methods
    
    //
    
    /**
     *default constructor
     *
     */
    public LayerStatChain()
    {
        _stats = new LinkedList();
        _istat_current = _stats.listIterator();              // at beginning
        _istat_cluster = _stats.listIterator(_stats.size()); // at end
    }
    
    //
    
    /**
     *copy constructor
     *
     * @param   lsc LayerStatChain to replicate
     */
    public LayerStatChain( LayerStatChain lsc)
    {
        
        // copy the iterators
                /*
                  _istat_current = _stats.end();
                  _istat_cluster = _stats.end();
                  StatList::const_iterator istat_rhs = rhs._stats.begin();
                  StatList::iterator istat_lhs = _stats.begin();
                  while ( istat_rhs != rhs._stats.end() ) {
                    if ( istat_rhs == rhs._istat_current ) _istat_current = istat_lhs;
                    if ( istat_rhs == rhs._istat_cluster ) _istat_cluster = istat_lhs;
                    ++istat_rhs;
                    ++istat_lhs;
                  }
                 
                 */
        
        // copy the list
        
        _stats =  new LinkedList();
        for(Iterator it = lsc._stats.iterator(); it.hasNext(); )
        {
            _stats.add( new LayerStat((LayerStat) it.next()) );
        }
        _istat_current = _stats.listIterator(_stats.size());
        _istat_cluster = _stats.listIterator(_stats.size());
        if(debug) System.out.println("_stats.size()= "+_stats.size());
        for(int i = 0; i< _stats.size(); ++i)
        {
            ListIterator cur = lsc._stats.listIterator(i);
            if(debug) System.out.println("cur= "+cur);
            ListIterator clu = lsc._stats.listIterator(i);
            if(debug) System.out.println("clu= "+clu);
            if (cur.equals(lsc._istat_current) ) _istat_current = _stats.listIterator(i);
            {
                _istat_current = _stats.listIterator(i);
                if(debug) System.out.println("found current match, i= "+i);
            }
            if (clu == lsc._istat_cluster) _istat_cluster = _stats.listIterator(i);
            {
                _istat_cluster = _stats.listIterator(i);
                if(debug) System.out.println("found cluster match, i= "+i);
            }
            
        }
        
        if(debug) System.out.println("copy _istat_current.hasNext()= "+_istat_current.hasNext());
        if(debug) System.out.println("copy _istat_cluster.hasNext()= "+_istat_cluster.hasNext());
    }
    
    // Is the cluster status set?
    boolean clusterStatusIsValid()
    {
        return _istat_current.hasNext();
    }
    
    // Is the cluster status at the first element of the list?
    boolean clusterStatusAtFirst()
    {
        return _istat_cluster == _stats.listIterator(0);
    }
    
    // Is the cluster status at the last element of the list?
    // (Last real entry -- not stl end().)
    boolean clusterStatusAtLast()
    {
        if ( ! currentStatusIsValid() ) return false;
        return _istat_cluster == _stats.listIterator(_stats.size()-1);
    }
    
    // return the current status
    LayerStat currentStatus()
    {
        Assert.assertTrue( currentStatusIsValid() );
        if(debug) System.out.println("get_current_status= "+_istat_current.hasNext());
        if(debug) System.out.println("_istat_current nextIndex= "+ _istat_current.nextIndex());
        return (LayerStat) _stats.get(_istat_current.nextIndex());
    }
    
    // Are there clusters associated with this chain?
    boolean hasClusters()
    {
        if ( ! clusterStatusIsValid() ) return false;
        return _istat_cluster.hasNext(); //need to check
    }
    
    // return the list of clusters
    List clusters()
    {
        if ( ! clusterStatusIsValid() ) return new LinkedList();
        return ( (LayerStat) _stats.get(_istat_cluster.nextIndex())).clusters();
        
    }
    
    // return the list of clusters near a track
    List clusters(ETrack tre)
    {
        if ( ! clusterStatusIsValid() ) return new LinkedList();
        return ( (LayerStat) _stats.get(_istat_cluster.nextIndex())).clusters(tre);
    }
    
    // Is there a miss associated with this chain?
    boolean hasMiss()
    {
        //return (get_miss() != null); //need to improve this
        return (clusterStatusIsValid() ); //need to check
    }
    
    // Return the miss.
    // Use get_miss()->new_copy() to get a mutable copy of the miss.
    Miss miss()
    {
        if ( ! clusterStatusIsValid() ) throw new IllegalArgumentException("LayerStatChain: check status before calling get_miss()!");
        else return ( (LayerStat) _stats.get(_istat_cluster.nextIndex())).miss();
    }
    
    
    /**
     * output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        int size = _stats.size();
        StringBuffer sb = new StringBuffer(getClass().getName()+" Layer status chain has " + size + " entr");
        
        if ( size == 0 )
        {
            sb.append( "ies.");
            return sb.toString();
        }
        if ( size == 1 ) sb.append( "y:");
        if ( size > 1 ) sb.append( "ies:");
        for ( Iterator istat=_stats.iterator(); istat.hasNext(); )
        {
            sb.append( "\n"+ istat.next() );
            if ( istat == _istat_current ) sb.append( " (current layer)");
            if ( istat == _istat_cluster ) sb.append( " (cluster layer)");
        }
        return sb.toString();
    }
    
}
