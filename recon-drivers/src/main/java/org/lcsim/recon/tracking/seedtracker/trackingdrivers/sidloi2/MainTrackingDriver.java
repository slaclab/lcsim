/*
 * Tracking driver for sidloi2 detector
 */
package org.lcsim.recon.tracking.seedtracker.trackingdrivers.sidloi2;

import java.util.List;

import org.lcsim.fit.helicaltrack.HelicalTrackHitDriver;
import org.lcsim.fit.helicaltrack.HelicalTrackHitDriver.HitType;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.recon.tracking.seedtracker.SeedTracker;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;
import org.lcsim.util.Driver;

/**
 *
 * @author Richard Partridge
 */
public class MainTrackingDriver extends Driver {

    private SeedTracker _st;

    public MainTrackingDriver() {

        //  Setup the sensor configuration
        add(new MakeSensorsDriver());

        //  Digitization and hit making driver for planar sensors
        TrackerHitDriver_sidloi2 thd = new TrackerHitDriver_sidloi2();
        add(thd);

        //  Driver to make HelicalTrackHits for tracking
        HelicalTrackHitDriver hitdriver = new HelicalTrackHitDriver();
        hitdriver.addCollection(thd.getStripHits1DName(), HitType.Digitized);
        hitdriver.addCollection(thd.getPixelHitsName(), HitType.Digitized);
        hitdriver.OutputCollection("HelicalTrackHits");
        add(hitdriver);

        //  Tracking code
        String sfile = StrategyXMLUtils.getDefaultStrategiesPrefix() + "autogen_ttbar_sidloi3.xml";
        List<SeedStrategy> slist = StrategyXMLUtils.getStrategyListFromResource(sfile);
        _st = new SeedTracker(slist);
        add(_st);
    }

    public SeedTracker getSeedTracker() {
        return _st;
    }
}
