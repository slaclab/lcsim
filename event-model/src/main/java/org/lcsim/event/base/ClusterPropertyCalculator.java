package org.lcsim.event.base;

import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;

/**
 * ClusterPropertyCalculator Interface
 * 
 * @author cassell
 */
public interface ClusterPropertyCalculator {

    /**
     * Calculate properties from a CalorimeterHit list.
     */
    public void calculateProperties(List<CalorimeterHit> hits);
    
    /**
     * Calculate properties from a cluster.
     */
    public void calculateProperties(Cluster cluster);

    /**
     * Return position
     */
    public double[] getPosition();

    /**
     * Return position error
     */
    public double[] getPositionError();

    /**
     * Return phi direction
     */
    public double getIPhi();

    /**
     * Return theta direction
     */
    public double getITheta();

    /**
     * Return direction error
     */
    public double[] getDirectionError();

    /**
     * Return shape parameters
     */
    public double[] getShapeParameters();
}
