/*
 * ClusteringAlgorithm.java
 *
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.List;

import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.event.RawTrackerHit;

/**
 * Interface for a clustering algorithm that clusters hits on an electrodes.
 *
 * @author Richard Partridge
 */
public interface ClusteringAlgorithm {

    /**
     * Finds the clusters given a list of RawTrackerHits on a particular
     * silicon sensor with electrodes given by SiSensorElectrodes.  A list
     * of clusters is returned, with each cluster being a list of RawTrackerHits
     * the form the cluster.
     *
     * @param electodes electrodes on this sensor to cluster
     * @param readout readout chip for these electrodes
     * @param hits raw hits
     * @return list of clusters, with each cluster being a list of RawTrackerHits
     */
    public List<List<RawTrackerHit>> findClusters(SiSensorElectrodes electrodes,
            ReadoutChip readout, List<RawTrackerHit> hits);

}