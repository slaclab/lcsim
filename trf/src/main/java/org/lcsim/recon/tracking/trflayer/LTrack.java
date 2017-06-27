package org.lcsim.recon.tracking.trflayer;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
/**
 * Class LTrack holds an ETrack and a chain of LayerStat objects
 * with a pointer to the current status.  This class manages both
 * the track and status chain.
 *<p>
 * The status chain (class LayerStatChain) has few public methods.
 * This class is a friend and directly manipulates it attributes.
 * This class provides the interface for maniputlaing the chain.
 *<p>
 * Non-default LTrack objects are created only in Layer method
 * propagate(tre,prop).  Other classes (including Layer subclasses)
 * may construct default objects and assign them or may copy with
 * the copy constructor.
 *<p>
 * The chain of layer status objects correspond exactly to the nest
 * of layers that carried out the propagation.  I.e., the first
 * status points to the top layer, the second to the first sublayer, etc.
 *<p>
 * 17sep99
 * LTrack now carries the result of the propagation in a PropStat object.
 * Default is failed propagation.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class LTrack
{
    
    // attributes
    
    // track
    private  ETrack _tre;
    
    // chain of LayerStat objects
    // The first is the top of the list.
    private  LayerStatChain _chain;
    
    // propagation status
    private  PropStat _pstat;
    
    //package (friend) protection
    
    private boolean debug;
    
    /**
     *  constructor from track and layer status
     * The status is used to start a chain.
     * This is only used by Layer::propagate(tre,prop).
     * Track is copied.
     * Layer is used to start a new chain.
     *
     * @param   tre ETrack
     * @param   lstat LayerStatus
     */
    public LTrack( ETrack tre, LayerStat lstat) //public for testing CNG 1/9/01
    {
        _tre = new ETrack(tre);
        _chain = new LayerStatChain(lstat);
        _pstat = new PropStat();
    }
    
    // methods
    
    //
    
    /**
     *Default constructor.
     * This is needed so layers can return lists of LTrack's.
     *
     */
    public LTrack()
    {
        _tre = new ETrack();
        _chain = new LayerStatChain();
        _pstat = new PropStat();
    }
    
    //
    
    /**
     *copy constructor
     * track and complete status chain are copied
     *
     * @param   trl LTrack to replicate
     */
    public LTrack(LTrack trl)
    {
        _tre = new ETrack(trl._tre);
        _chain = new LayerStatChain(trl._chain);
        _pstat = new PropStat(trl._pstat);
    }
    
    //
    
    /**
     *return if the current status is defined
     *
     * @return true if current status is defined
     */
    public boolean atValidStatus()
    {
        return _chain.currentStatusIsValid();
    }
    
    //
    
    /**
     *Return if the current status is the top-level status.
     * Returns true for an empty status list.
     *
     * @return true for an empty status list.
     */
    public boolean atTopStatus()
    {
        return _chain._istat_current.previousIndex()==0;
    }
    
    //
    
    /**
     *return the current status
     *
     * @return the current status
     */
    public LayerStat status()
    {
        Assert.assertTrue( atValidStatus() );
        return _chain.currentStatus();
    }
/*
  // return the current status const
  public  LayerStat get_status()
  {
        return new LayerStat();
  }
 */
    //
    
    /**
     *Step down the status chain.
     * Returns false when the last element in the chain is reached.
     *
     * @return false when the last element in the chain is reached.
     */
    public boolean setNextStatus()
    {
        if ( !_chain._istat_current.hasNext() ) return false;
        if(debug) System.out.println("in set_next_status");
        if(debug) System.out.println("_chain._istat_current "+_chain._istat_current);
        if(debug) System.out.println("_chain._stats.listIterator(_chain._stats.size()) "+_chain._stats.listIterator(_chain._stats.size()));
        if ( _chain._istat_current == _chain._stats.listIterator(_chain._stats.size()) ) return false;
        if(!_chain._istat_current.hasNext()) return false;
        _chain._istat_current = _chain._stats.listIterator(_chain._istat_current.nextIndex()+1);
        if ( !_chain._istat_current.hasNext() ) return false;
        if(debug) System.out.println("returning true");
        return true;
    }
    
    //
    
    /**
     *Step up the status chain.
     * Returns false when the first element in the chain is reached.
     *
     * @return false when the first element in the chain is reached.
     */
    public boolean setPreviousStatus()
    {
        if ( atTopStatus() ) return false;
        if(debug) System.out.println("in set_previous_status");
        if(debug) System.out.println("_istat_current nextIndex= "+_chain._istat_current.nextIndex());
        _chain._istat_current = _chain._stats.listIterator(_chain._istat_current.previousIndex());
        if(debug) System.out.println("_istat_current nextIndex= "+_chain._istat_current.nextIndex());
        
        return true;
    }
    
    //
    
    /**
     *Add a status to the end of the list.
     * Returns a reference to the added status.
     * Sets pointer to reference the new status.
     * Note that the argument may be a layer.
     *
     * @param   lstat LayerStat to add
     * @return reference to the added status
     */
    public LayerStat pushStatus(LayerStat lstat)
    {
        //Add lstat to the end of the chain
        _chain._stats.addLast(new LayerStat(lstat));
        //set the current iterator to the end of the list
        _chain._istat_current = _chain._stats.listIterator(_chain._stats.size()-1);
        return status();
    }
    
    //
    
    /**
     *drop the last status from the list
     * Set current status back only if it pointed at deleted element.
     * Return false if there was no status to drop.
     *
     * @return false if there was no status to drop
     */
    public  boolean popStatus()
    {
        if (  _chain._stats.size()==0 ) return false;
        // Drop the last element in the list.
        _chain._stats.removeLast();
        // Set the pointer to that last element
        _chain._istat_current = _chain._stats.listIterator(_chain._stats.size()-1);
        return true;
    }
    
    //
    
    /**
     *Set the cluster status to be the current status.
     *
     */
    public  void setClusterStatus()
    {
        _chain._istat_cluster = _chain._istat_current;
    }
    
    //
    /**
     *clear the cluster status.
     * by setting iterator to last element
     * need to check consistency here...
     *
     */
    public void clearClusterStatus()
    {
        _chain._istat_cluster = _chain._stats.listIterator(_chain._stats.size());
    }
    
    //
    
    /**
     *fetch the status chain
     *
     * @return LayerStatChain
     */
    public LayerStatChain statusChain()
    { return _chain; }
/*
  // fetch the status chain const
  public  LayerStatChain get_status_chain() { return new LayerStatChain(_chain); }
 */
    //
    
    /**
     *fetch reference to the track
     *
     * @return reference to the track
     */
    public ETrack track()
    { return _tre; };
/*
  // fetch const reference to the track
  public ETrack get_track() { return new ETrack(_tre); };
 */
    //
    
    /**
     *Fetch reference to propagation status.
     *
     * @return propagation status
     */
    public PropStat propStat()
    { return _pstat; }
/*
  // Fetch const reference to propagation status.
  public  PropStat get_prop_stat()   { return new PropStat(_pstat); }
 */
    
    
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        return getClass().getName()+" track: \n"+_tre+"\nLTrack chain: \n"+_chain;
    }
    
}