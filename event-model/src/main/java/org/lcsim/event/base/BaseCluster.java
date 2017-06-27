package org.lcsim.event.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;

/**
 * <p>
 * This is a concrete implementation of the {@link org.lcsim.event.Cluster} LCIO interface.
 * <p>
 * This version is an overhaul of the previous base class, with the following changes:
 * <ul>
 * <li>Added several constructors with argument lists, including one that is fully qualified and another that takes a list of hits only.</li>
 * <li>Removed the prior implementation of subdetector energies in favor of a simple set method. (This part of the API is basically unused anyways.)</li>
 * <li>Added set methods for all class variables, where they were missing.</li>
 * <li>Added access to particle ID based on the official API.</li>
 * <li>Added a few utility methods for adding lists of hits and clusters.</li>
 * <li>Added a method for setting a hit contribution that is different from the corrected energy.</li>
 * <li>Simplified the {@link #calculateProperties()} method so that it doesn't do a bunch of array copies.</li>
 * <li>Added a copy constructor and implementation of {@link #clone()} method.</li>
 * </ul>
 *
 * @see org.lcsim.event.Cluster
 * @see org.lcsim.event.CalorimeterHit
 * @see org.lcsim.event.base.ClusterPropertyCalculator
 * @see org.lcsim.event.base.TensorClusterPropertyCalculator
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @author Norman Graf <ngraf@slac.stanford.edu>
 */
public class BaseCluster implements Cluster {

    protected ClusterPropertyCalculator calc = new TensorClusterPropertyCalculator();
    protected List<Cluster> clusters = new ArrayList<Cluster>();
    protected double[] directionError = new double[6];

    protected double energy;
    protected double energyError;

    protected List<Double> hitContributions = new ArrayList<Double>();
    protected List<CalorimeterHit> hits = new ArrayList<CalorimeterHit>();

    protected double iphi;
    protected double itheta;

    protected boolean needsPropertyCalculation = true;
    protected int pid;

    protected double[] position = new double[3];

    protected double[] positionError = new double[6];
    protected double[] shapeParameters;

    protected double[] subdetectorEnergies = new double[0];
    protected int type;

    /**
     * The no argument constructor.
     */
    public BaseCluster() {
    }

    /**
     * Copy constructor, which will create new arrays and lists in this object so the copied cluster's data is not incorrectly referenced.
     * <p>
     * The hits in the <code>CalorimeterHit</code> list are not themselves copied.
     *
     * @param cluster the <code>BaseCluster</code> to copy
     */
    public BaseCluster(final Cluster cluster) {

        // Copy the hit list.
        if (cluster.getCalorimeterHits() != null) {
            for (final CalorimeterHit hit : cluster.getCalorimeterHits()) {
                this.hits.add(hit);
            }
        }

        // Copy hit contributions.
        if (cluster.getHitContributions() != null) {
            this.hitContributions = new ArrayList<Double>();
            for (final Double contribution : cluster.getHitContributions()) {
                this.hitContributions.add(contribution);
            }
        }

        // Set energy and energy error.
        this.energy = cluster.getEnergy();
        this.energyError = cluster.getEnergyError();

        // Copy position into new array.
        if (cluster.getPosition() != null) {
            this.position = new double[cluster.getPosition().length];
            System.arraycopy(cluster.getPosition(), 0, this.position, 0, cluster.getPosition().length);
        }

        // Copy position error into new array.
        if (cluster.getPositionError() != null) {
            this.positionError = new double[cluster.getPositionError().length];
            System.arraycopy(cluster.getPositionError(), 0, this.positionError, 0, cluster.getPositionError().length);
        }

        // Set iphi and itheta.
        this.iphi = cluster.getIPhi();
        this.itheta = cluster.getITheta();

        // Copy direction error into new array.
        if (cluster.getDirectionError() != null) {
            this.directionError = new double[cluster.getDirectionError().length];
            System.arraycopy(cluster.getDirectionError(), 0, this.directionError, 0, cluster.getDirectionError().length);
        }

        // Copy shape parameters into new array.
        if (cluster.getShape() != null) {
            this.shapeParameters = new double[cluster.getShape().length];
            System.arraycopy(cluster.getShape(), 0, this.shapeParameters, 0, cluster.getShape().length);
        }

        // Copy type and PID.
        this.type = cluster.getType();
        this.pid = cluster.getParticleId();
    }

    /**
     * Basic constructor that takes a list of hits. It will apply the default energy calculation.
     *
     * @param hits the list of CalorimeterHits
     */
    public BaseCluster(final List<CalorimeterHit> hits) {
        this.addHits(hits);
    }

    /**
     * Almost fully qualified constructor, if the cluster's properties are already calculated. The energy given here will override the value
     * calculated from the hits. If this is not desired, then another constructor should be used instead. This constructor does not allow setting hit
     * contributions that are different from the hit energies.
     *
     * @param hits the list of hits
     * @param energy the total energy
     * @param energyError the energy error
     * @param position the position
     * @param positionError the position error
     * @param iphi the intrinsic phi
     * @param itheta the intrinsic theta
     * @param directionError the direction error
     * @param shapeParameters the shape parameters
     */
    public BaseCluster(final List<CalorimeterHit> hits, final double energy, final double energyError, final double[] position,
            final double[] positionError, final double iphi, final double itheta, final double[] directionError, final double[] shapeParameters,
            final int type, final int pid) {

        this.addHits(hits);

        // This will override the energy calculated from the hits, by design!
        this.energy = energy;
        this.energyError = energyError;

        this.position = position;
        this.positionError = positionError;

        this.iphi = iphi;
        this.itheta = itheta;

        this.directionError = directionError;
        this.shapeParameters = shapeParameters;

        this.type = type;
        this.pid = pid;

        this.needsPropertyCalculation = false;
    }

    /**
     * Add a sub-cluster to the cluster.
     *
     * @param cluster the cluster to add
     */
    public void addCluster(final Cluster cluster) {
        clusters.add(cluster);
        final List<CalorimeterHit> clusterHits = cluster.getCalorimeterHits();
        for (int i = 0; i < clusterHits.size(); i++) {
            hits.add(clusterHits.get(i));
            hitContributions.add(clusterHits.get(i).getCorrectedEnergy());
        }
        energy += cluster.getEnergy();
        needsPropertyCalculation = true;
    }

    /**
     ***************************************************** Implementation of get methods from the interface. *
     */

    /**
     * Add a list of sub-clusters to the cluster.
     *
     * @param the list of clusters to add
     */
    public void addClusters(final List<Cluster> clusters) {
        for (final Cluster cluster : clusters) {
            this.addCluster(cluster);
        }
    }

    /**
     * Add a hit to the cluster with default energy contribution.
     *
     * @param hit the hit to add
     */
    public void addHit(final CalorimeterHit hit) {
        this.addHit(hit, hit.getCorrectedEnergy());
        needsPropertyCalculation = true;
    }

    /**
     * Add a hit to the cluster with specified energy contribution.
     *
     * @param hit the hit to add
     * @param contribution the energy contribution of the hit [GeV]
     */
    public void addHit(final CalorimeterHit hit, final double contribution) {
        hits.add(hit);
        hitContributions.add(contribution);
        energy += contribution;
        needsPropertyCalculation = true;
    }

    /**
     * Add a list of hits to the cluster.
     *
     * @param the list of hits to add
     */
    public void addHits(final List<CalorimeterHit> hits) {
        for (final CalorimeterHit hit : hits) {
            this.addHit(hit);
        }
    }

    /**
     * Calculate the properties of this cluster using the current <code>ClusterPropertyCalculator</code>. The calculated properties will be set on the
     * following class variables:<br/>
     * {@link #position}, {@link #positionError}, {@link #iphi}, {@link #itheta}, {@link #directionError}, and {@link #shapeParameters}. Then
     * {@link #needsPropertyCalculation} will be set to <code>false</code> until the cluster's state changes.
     */
    public void calculateProperties() {
        if (!this.hasPropertyCalculator()) {
            throw new RuntimeException("No ClusterPropertyCalculator is set on this object.");
        }
        calc.calculateProperties(this);
        this.setPosition(calc.getPosition());
        this.setPositionError(calc.getPositionError());
        this.setIPhi(calc.getIPhi());
        this.setITheta(calc.getITheta());
        this.setDirectionError(calc.getDirectionError());
        this.setShapeParameters(calc.getShapeParameters());
        this.setNeedsPropertyCalculation(false);
    }

    /**
     * Calculate properties if needs property calculation.
     */
    void checkCalculateProperties() {
        if (this.needsPropertyCalculation()) {
            this.calculateProperties();
        }
    }

    /**
     * Clone to a new object using the copy constructor.
     *
     * @return the new object
     */
    @Override
    public Object clone() {
        return new BaseCluster(this);
    }

    /**
     * Get the list of CalorimeterHits of this cluster. The hits are not necessarily unique in the list.
     *
     * @return the hits comprising the cluster
     */
    @Override
    public List<CalorimeterHit> getCalorimeterHits() {
        return hits;
    }

    /**
     * Get the clusters that are part of this cluster.
     *
     * @return the clusters comprising the cluster
     */
    @Override
    public List<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Get the direction error of the cluster as a double array of size 6.
     *
     * @return the direction error of the cluster
     */
    @Override
    public double[] getDirectionError() {
        this.checkCalculateProperties();
        return directionError;
    }

    /**
     * Get the energy of the cluster, which by default will be the sum of the CalorimeterHit corrected energy values.
     *
     * @return the energy of the cluster
     */
    @Override
    public double getEnergy() {
        return energy;
    }

    /**
     * Get the energy error.
     *
     * @return the energy error
     */
    @Override
    public double getEnergyError() {
        return energyError;
    }

    /**
     * Get the individual hit contribution energies. This should be an array of the same size as the hit list. By default this array contains the
     * hit's corrected energies, but the contributions may be set to different values on a per hit basis using the
     * {@link #addHit(CalorimeterHit, double)} method.
     *
     * @return the individual hit contribution energies
     */
    @Override
    public double[] getHitContributions() {
        final double[] arrayCopy = new double[hitContributions.size()];
        for (int i = 0; i < hitContributions.size(); i++) {
            arrayCopy[i] = hitContributions.get(i);
        }
        return arrayCopy;
    }

    /**
     * Get the intrinsic phi direction of the cluster.
     *
     * @return the intrinsic phi direction of the cluster
     */
    @Override
    public double getIPhi() {
        this.checkCalculateProperties();
        return iphi;
    }

    /**
     * Get the intrinsic theta direction of the cluster.
     *
     * @return the intrinsic theta direction of the cluster
     */
    @Override
    public double getITheta() {
        this.checkCalculateProperties();
        return itheta;
    }

    /**
     ********************************** Implementation of set methods. *
     */

    /**
     * Get the PDG ID of the particle hypothesis.
     *
     * @return the PID
     */
    @Override
    public int getParticleId() {
        return pid;
    }

    /**
     * Get the position of the cluster as a double array of size 3.
     *
     * @return the position of the cluster
     */
    @Override
    public double[] getPosition() {
        this.checkCalculateProperties();
        return position;
    }

    /**
     * Get the position error of the cluster as a double array of size 6.
     *
     * @return the position error of the cluster
     */
    @Override
    public double[] getPositionError() {
        this.checkCalculateProperties();
        return positionError;
    }

    /**
     * Get the shape parameters of the cluster as a double array of unspecified size.
     *
     * @return the shape parameters of the cluster
     */
    @Override
    public double[] getShape() {
        this.checkCalculateProperties();
        return shapeParameters;
    }

    /**
     * Get the number of hits in the cluster, including hits in sub-clusters. Hits belonging to more than one cluster are counted once.
     *
     * @return the size of the cluster
     */
    @Override
    public int getSize() {
        final Set<CalorimeterHit> hitSet = new HashSet<CalorimeterHit>(this.hits);
        for (final Cluster cluster : clusters) {
            hitSet.addAll(cluster.getCalorimeterHits());
        }
        final int size = hitSet.size();
        hitSet.clear();
        return size;
    }

    /**
     * Get the list of subdetector energy contributions. The ordering and meaning of this array is unspecified by this class.
     *
     * @return the list of subdetector energy contributions
     */
    @Override
    public double[] getSubdetectorEnergies() {
        return subdetectorEnergies;
    }

    /**
     * Get a value defining the type of this cluster.
     *
     * @return the type of this cluster
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Return <code>true</code> if property calculator is set.
     *
     * @return <code>true</code> if property calculator is set
     */
    public boolean hasPropertyCalculator() {
        return calc != null;
    }

    /**
     * Return <code>true</code> if cluster is flagged as needed a property calculation.
     *
     * @return <code>true</code> if cluster needs property calculation
     */
    public boolean needsPropertyCalculation() {
        return needsPropertyCalculation;
    }

    /**
     * Remove a hit from the cluster.
     *
     * @param hit the hit to remove
     */
    public void removeHit(final CalorimeterHit hit) {
        final int index = hits.indexOf(hit);
        hits.remove(hit);
        final double hitEnergy = hit.getCorrectedEnergy();
        energy -= hitEnergy;
        hitContributions.remove(index);
        needsPropertyCalculation = true;
    }

    /**
     * Set the direction error of the cluster.
     *
     * @param directionError the direction error of the cluster
     * @throws IllegalArgumentException if array is wrong size
     */
    public void setDirectionError(final double[] directionError) {
        if (directionError.length != 6) {
            throw new IllegalArgumentException("The directionError array argument has the wrong length: " + position.length);
        }
        this.directionError = directionError;
    }

    /**
     **************************************************** Convenience methods for adding hits and clusters *
     */

    /**
     * Set a total energy of this cluster, overriding any energy value that may have been automatically calculated from hit energies.
     *
     * @param energy the total energy of this cluster
     */
    public void setEnergy(final double energy) {
        this.energy = energy;
    }

    /**
     * Set the error on the energy measurement.
     *
     * @param energyError the error on the energ measurement
     */
    public void setEnergyError(final double energyError) {
        this.energyError = energyError;
    }

    /**
     * Set the intrinsic phi of the cluster.
     *
     * @param iphi the intrinsic phi of the cluster
     */
    public void setIPhi(final double iphi) {
        this.iphi = iphi;
    }

    /**
     * Set the intrinsic theta of the cluster.
     *
     * @param iphi The intrinsic theta of the cluster.
     */
    public void setITheta(final double itheta) {
        this.itheta = itheta;
    }

    /**
     * Manually set whether the cluster needs property calculation.
     *
     * @param needsPropertyCalculation <code>true</code> if cluster needs property calculation
     */
    public void setNeedsPropertyCalculation(final boolean needsPropertyCalculation) {
        this.needsPropertyCalculation = needsPropertyCalculation;
    }

    /**
     * Get the PDG ID of the particle hypothesis.
     *
     * @return the PID
     */
    public void setParticleId(final int pid) {
        this.pid = pid;
    }

    /**
     ************************************** ClusterPropertyCalculator methods. *
     */

    /**
     * Set the position of the cluster.
     *
     * @param position the position of the cluster
     * @throws IllegalArgumentException if array is wrong size
     */
    public void setPosition(final double[] position) {
        if (position.length != 3) {
            throw new IllegalArgumentException("The position array argument has the wrong length: " + position.length);
        }
        this.position = position;
    }

    /**
     * Set the position error of the cluster.
     *
     * @param positionError the position error of the cluster
     * @throws IllegalArgumentException if array is wrong size
     */
    public void setPositionError(final double[] positionError) {
        if (positionError.length != 6) {
            throw new IllegalArgumentException("The positionError array argument has the wrong length: " + position.length);
        }
        this.positionError = positionError;
    }

    /**
     * Set a property calculator for computing position, etc.
     *
     * @param calc the property calculator
     */
    public void setPropertyCalculator(final ClusterPropertyCalculator calc) {
        this.calc = calc;
    }

    /**
     * Set the shape parameters of the cluster.
     *
     * @param shapeParameters the shape parameters
     */
    public void setShapeParameters(final double[] shapeParameters) {
        this.shapeParameters = shapeParameters;
    }

    /**
     * Set the subdetector energies.
     *
     * @param subdetectorEnergies the subdetector energies
     */
    public void setSubdetectorEnergies(final double[] subdetectorEnergies) {
        this.subdetectorEnergies = subdetectorEnergies;
    }

    /**
     * Set the type of the cluster.
     *
     * @param type the type of the cluster
     */
    public void setType(final int type) {
        this.type = type;
    }
}
