package org.lcsim.recon.tracking.trflayer;

import java.util.*;

import org.lcsim.recon.tracking.trfbase.Miss;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfutil.Assert;

// Dummy layer class for LayerStat test.
// Needed to access private methods of LayerStat.
// Original C++ code used friends to get at private methods....
//

public class TLayer extends Layer
{
    
    private boolean _same;
    private  boolean _exit;
    private  Miss _miss;
    private  ClusterFinder _finder;
    private int _state;
    
    private boolean debug;
    
    public TLayer()
    {
    }
    
    public TLayer(boolean same, boolean exit, Miss miss,
            ClusterFinder finder, int state)
    {
        _same = same;
        _exit = exit;
        _miss = miss;
        _finder = finder;
        _state = state;
    }
    
    public LayerStat propagate()
    {
        LayerStat lstat = new LayerStat(this);
        propagate(lstat);
        return lstat;
    }
    
    public void propagate(LayerStat lstat)
    {
        if ( _exit ) lstat.setAtExit();
        if ( _miss!=null ) lstat.setMiss(_miss);
        if ( _finder!=null ) lstat.setFinder(_finder);
        lstat.setState(_state);
    }
    
    public void check(LayerStat lstat)
    {
        Assert.assertTrue( _exit == lstat.atExit() );
        if(debug) System.out.println("Miss = "+_miss);
        if(debug) System.out.println("Miss likelihood= "+_miss.likelihood());
        if(debug) System.out.println("LStat likelihood= "+lstat.miss().likelihood());
        
        Assert.assertTrue( _miss == null || _miss.likelihood()
        == lstat.miss().likelihood() );
        Assert.assertTrue( _finder == lstat.finder() );
        Assert.assertTrue( _state == lstat.state() );
    }
    
    //not used
    protected List _propagate(LTrack  trl,  Propagator  prop)
    {
        return new ArrayList();
    }
}

