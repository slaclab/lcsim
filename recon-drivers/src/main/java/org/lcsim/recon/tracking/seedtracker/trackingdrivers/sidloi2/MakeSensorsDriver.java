/*
 * Main driver for setting up the sensor configuration.
 * Currently, we just use default configurations.
 */
package org.lcsim.recon.tracking.seedtracker.trackingdrivers.sidloi2;

import org.lcsim.util.Driver;

/**
 *
 * @author Richard Partridge
 */
public class MakeSensorsDriver extends Driver {

    public MakeSensorsDriver() {
        add(new SiVertexBarrelSensorSetup("SiVertexBarrel"));
        add(new SiTrackerBarrelSensorSetup("SiTrackerBarrel"));
        add(new SiVertexEndcapSensorSetup("SiVertexEndcap"));
        add(new SiVertexEndcapSensorSetup("SiTrackerForward"));
        add(new SiTrackerEndcap2SensorSetup("SiTrackerEndcap"));
    }
}
