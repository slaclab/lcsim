/*
 * DefaultStrategy.java
 *
 * Created on March 29, 2006, 3:45 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.seedtracker.SeedLayer.SeedType;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * Define the default strategy to be used by the SeedTracker tracking algorithm.  The default strategy may be
 * over-ridden by calling the method SeedTracker.putStrategyList.
 * @author Richard Partridge
 * @version 1.0
 */
public class DefaultStrategy {
    private List<SeedStrategy> _strategylist;
    
    /** Creates a new instance of DefaultStrategy */
    public DefaultStrategy() {
        _strategylist = new ArrayList();
        // Barrel only strategies
//        List<SeedLayer> tv012layers = new ArrayList();
//        tv012layers.add(new SeedLayer("VertexBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Seed));
//        tv012layers.add(new SeedLayer("VertexBarrel", 1, BarrelEndcapFlag.BARREL, SeedType.Seed));
//        tv012layers.add(new SeedLayer("VertexBarrel", 2, BarrelEndcapFlag.BARREL, SeedType.Seed));
//        tv012layers.add(new SeedLayer("VertexBarrel", 3, BarrelEndcapFlag.BARREL, SeedType.Confirm));
//        tv012layers.add(new SeedLayer("VertexBarrel", 4, BarrelEndcapFlag.BARREL, SeedType.Confirm));
//        tv012layers.add(new SeedLayer("TrackerBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Extend));
//        tv012layers.add(new SeedLayer("TrackerBarrel", 1, BarrelEndcapFlag.BARREL, SeedType.Extend));
//        tv012layers.add(new SeedLayer("TrackerBarrel", 2, BarrelEndcapFlag.BARREL, SeedType.Extend));
//        tv012layers.add(new SeedLayer("TrackerBarrel", 3, BarrelEndcapFlag.BARREL, SeedType.Extend));
//        tv012layers.add(new SeedLayer("TrackerBarrel", 4, BarrelEndcapFlag.BARREL, SeedType.Extend));
        
//        _strategylist.add(new SeedStrategy("TV012",tv012layers));

        List<SeedLayer> tb345layers = new ArrayList();
        tb345layers.add(new SeedLayer("VertexBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("VertexBarrel", 1, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("VertexBarrel", 2, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("VertexBarrel", 3, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("VertexBarrel", 4, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("TrackerBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Extend));
        tb345layers.add(new SeedLayer("TrackerBarrel", 1, BarrelEndcapFlag.BARREL, SeedType.Confirm));
        tb345layers.add(new SeedLayer("TrackerBarrel", 2, BarrelEndcapFlag.BARREL, SeedType.Seed));
        tb345layers.add(new SeedLayer("TrackerBarrel", 3, BarrelEndcapFlag.BARREL, SeedType.Seed));
        tb345layers.add(new SeedLayer("TrackerBarrel", 4, BarrelEndcapFlag.BARREL, SeedType.Seed));
        
        _strategylist.add(new SeedStrategy("TB345",tb345layers));
        
        List<BarrelEndcapFlag> belist = new ArrayList();
        belist.add(BarrelEndcapFlag.ENDCAP_NORTH);
        belist.add(BarrelEndcapFlag.ENDCAP_SOUTH);
        
        for (BarrelEndcapFlag beflag : belist) {
            List<SeedLayer> td012layers = new ArrayList();
            td012layers.add(new SeedLayer("TrackerEndcap", 0, beflag, SeedType.Seed));
            td012layers.add(new SeedLayer("TrackerEndcap", 2, beflag, SeedType.Seed));
            td012layers.add(new SeedLayer("TrackerEndcap", 4, beflag, SeedType.Seed));
//            _strategylist.add(new SeedStrategy("TD012"+beflag.toString(),td012layers));
            
            List<SeedLayer> tca = new ArrayList();
            tca.add(new SeedLayer("TrackerBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Seed));
            tca.add(new SeedLayer("TrackerBarrel", 1, BarrelEndcapFlag.BARREL, SeedType.Seed));
            tca.add(new SeedLayer("TrackerEndcap", 4, beflag, SeedType.Seed));
//            _strategylist.add(new SeedStrategy("TCA"+beflag.toString(),tca));
            
            List<SeedLayer> tcb = new ArrayList();
            tcb.add(new SeedLayer("TrackerBarrel", 0, BarrelEndcapFlag.BARREL, SeedType.Seed));
            tcb.add(new SeedLayer("TrackerEndcap", 2, beflag, SeedType.Seed));
            tcb.add(new SeedLayer("TrackerEndcap", 4, beflag, SeedType.Seed));
//            _strategylist.add(new SeedStrategy("TCB"+beflag.toString(),tcb));
        }        
    }
    
    /**
     * Returns the default list of track-finding strategies
     * @return List of strategies to be used by SeedTracker
     */
    public List<SeedStrategy> getStrategyList() {
        return _strategylist;
    }
}
