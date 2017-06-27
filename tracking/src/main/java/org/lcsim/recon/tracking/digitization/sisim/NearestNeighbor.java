/*
 * NearestNeighborClusteringAlgorithm.java
 */
package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.event.RawTrackerHit;

/**
 * This class uses a nearest neighbor algorithm to find clusters of hits on a
 * set of silicon sensor electrodes (i.e., an instance of SiStrips or SiPixels).
 *
 * The algorithm first finds possible cluster seeds that are above the seed
 * threshold.  Starting from a seed, the neighbor strips/pixels are searched
 * to see if we have hits above the neighbor threshold.  Neighbor channels
 * are added until we have a cluster where every neighboring channel for the
 * cluster has charge lying below the neighbor threshold.
 *
 * Thresholds are specified in units of electrons.
 *
 * HashMaps are used to speed up the algorithm when there are large numbers
 * of hits, and the algorithm is expected to scale linearly with the number
 * of RawTrackerHits on the electrodes.
 *
 * @author Richard Partridge
 */
public class NearestNeighbor implements ClusteringAlgorithm {

    private static String _NAME = "NearestNeighbor";
    private double _seed_threshold;
    private double _neighbor_threshold;

    /**
     * Instantiate NearestNeighborClusteringAlgorithm with specified thresholds.
     * Seed threshold is the minimum charge to initiate a cluster.  Neighbor
     * threshold is the minimum charge to add a neighboring cell to a cluster.
     * All thresholds are in units of electrons.
     *
     * @param seed_threshold seed threhold
     * @param neighbor_threshold neighbor threshold
     */
    public NearestNeighbor(double seed_threshold, double neighbor_threshold) {

        _seed_threshold = seed_threshold;
        _neighbor_threshold = neighbor_threshold;
    }

    /**
     * Instantiate NearestNeighborClusteringAlgorithm with default thresholds:
     *
     * seed_threshold = 4000 electrons
     * neighbor_threhold = 2000 electrons
     */
    public NearestNeighbor() {
        this(4000., 2000.);
    }

    /**
     * Set the seed threshold.  Units are electrons.
     *
     * @param seed_threshold seed threshold
     */
    public void setSeedThreshold(double seed_threshold) {
        _seed_threshold = seed_threshold;
    }

    /**
     * Set the neighbor threshold.  Units are electrons.
     *
     * @param neighbor_threshold neighbor threshold
     */
    public void setNeighborThreshold(double neighbor_threshold) {
        _neighbor_threshold = neighbor_threshold;
    }

    /**
     * Return the seed threshold.  Units are electrons.
     *
     * @return seed threshold
     */
    public double getSeedThreshold() {
        return _seed_threshold;
    }

    /**
     * Return the neighbor threshold.  Units are electrons.
     *
     * @return neighbor threshold
     */
    public double getNeighborThreshold() {
        return _neighbor_threshold;
    }

    /**
     * Return the name of the clustering algorithm.
     * 
     * @return _name clusting algorithm name
     */
    public String getName() {
        return _NAME;
    }

    /**
     * Find clusters using the nearest neighbor algorithm.
     *
     * @param electrodes electrodes we are clustering
     * @param readout_chip readout chip for these electrodes
     * @param raw_hits List of RawTrackerHits to be clustered
     * @return list of clusters, with a cluster being a list of RawTrackerHits
     */
    public List<List<RawTrackerHit>> findClusters(SiSensorElectrodes electrodes,
            ReadoutChip readout_chip, List<RawTrackerHit> raw_hits) {

        //  Check that the seed threshold is at least as large as  the neighbor threshold
        if (_seed_threshold < _neighbor_threshold) {
            throw new RuntimeException("Tracker hit clustering error: seed threshold below neighbor threshold");
        }

        //  Create maps that show the channel status and relate the channel number to the raw hit and vice versa
        int mapsize = 2 * raw_hits.size();
        Map<Integer, Boolean> clusterable = new HashMap<Integer, Boolean>(mapsize);
        Map<RawTrackerHit, Integer> hit_to_channel = new HashMap<RawTrackerHit, Integer>(mapsize);
        Map<Integer, RawTrackerHit> channel_to_hit = new HashMap<Integer, RawTrackerHit>(mapsize);

        //  Create list of channel numbers to be used as cluster seeds
        List<Integer> cluster_seeds = new ArrayList<Integer>();

        //  Loop over the raw hits and construct the maps used to relate cells and hits, initialize the
        //  clustering status map, and create a list of possible cluster seeds
        for (RawTrackerHit raw_hit : raw_hits) {

            // get the channel number for this hit
            SiTrackerIdentifierHelper sid_helper = (SiTrackerIdentifierHelper) raw_hit.getIdentifierHelper();
            IIdentifier id = raw_hit.getIdentifier();
            int channel_number = sid_helper.getElectrodeValue(id);

            //  Check for duplicate RawTrackerHit
            if (hit_to_channel.containsKey(raw_hit)) {
                throw new RuntimeException("Duplicate hit: "+id.toString());
            }

            //  Check for duplicate RawTrackerHits or channel numberss
            if (channel_to_hit.containsKey(channel_number)) {
                throw new RuntimeException("Duplicate channel number: "+channel_number);
            }

            //  Add this hit to the maps that relate channels and hits
            hit_to_channel.put(raw_hit, channel_number);
            channel_to_hit.put(channel_number, raw_hit);

            //  Get the signal from the readout chip (units are electrons)
            double signal = readout_chip.decodeCharge(raw_hit);

            //  Mark this hit as available for clustering if it is above the neighbor threshold
            clusterable.put(channel_number, signal >= _neighbor_threshold);

            //  Add this hit to the list of seeds if it is above the seed threshold
            if (signal >= _seed_threshold) {
                cluster_seeds.add(channel_number);
            }
        }

        //  Create a list of clusters
        List<List<RawTrackerHit>> cluster_list = new ArrayList<List<RawTrackerHit>>();

        //  Now loop over the cluster seeds to form clusters
        for (int seed_channel : cluster_seeds) {

            //  First check if this hit is still available for clustering
            if (!clusterable.get(seed_channel)) continue;

            //  Create a new cluster
            List<RawTrackerHit> cluster = new ArrayList<RawTrackerHit>();

            //  Create a queue to hold channels whose neighbors need to be checked for inclusion
            LinkedList<Integer> unchecked = new LinkedList<Integer>();

            //  Add the seed channel to the unchecked list and mark it as unavailable for clustering
            unchecked.addLast(seed_channel);
            clusterable.put(seed_channel, false);

            //  Check the neighbors of channels added to the cluster
            while (unchecked.size() > 0) {

                //  Pull the next channel off the queue and add it's hit to the cluster
                int clustered_cell = unchecked.removeFirst();
                cluster.add(channel_to_hit.get(clustered_cell));

                //  Get the neigbor channels
                Set<Integer> neighbor_channels = electrodes.getNearestNeighborCells(clustered_cell);

                //   Now loop over the neighbors and see if we can add them to the cluster
                for (int channel : neighbor_channels) {

                    //  Get the status of this channel
                    Boolean addhit = clusterable.get(channel);

                    //  If the map entry is null, there is no raw hit for this channel
                    if (addhit == null) continue;

                    //  Check if this neighbor channel is still available for clustering
                    if (!addhit) continue;

                    //  Add channel to the list of unchecked clustered channels
                    //  and mark it unavailable for clustering
                    unchecked.addLast(channel);
                    clusterable.put(channel, false);

                }  // end of loop over neighbor cells
            }  // end of loop over unchecked cells

            //  Finished with this cluster - add it to the list of clusters
            if (cluster.size() > 0) {
                cluster_list.add(cluster);
            }

        }  //  End of loop over seeds

        //  Finished finding clusters
        return cluster_list;
    }
}
