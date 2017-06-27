package org.lcsim.event.base;

import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;

/**
 * An abstract implementation of {@link ClusterPropertyCalculator}. 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public abstract class AbstractClusterPropertyCalculator implements ClusterPropertyCalculator {

    protected double[] position = new double[3];
    protected double[] positionError = new double[6];
    protected double iphi;
    protected double itheta;
    protected double[] directionError = new double[6];
    protected double[] shapeParameters = new double[6];
          
    @Override
    public void calculateProperties(List<CalorimeterHit> hits) {
    }
    
    @Override
    public void calculateProperties(Cluster cluster) {
        calculateProperties(cluster.getCalorimeterHits());
    }
    
    protected void reset() {
        position = new double[3];
        positionError = new double[6];
        iphi = 0;
        itheta = 0;
        directionError = new double[6];
        shapeParameters = new double[6];
    }

    @Override
    public double[] getPosition() {
        return position;
    }

    @Override
    public double[] getPositionError() {
        return positionError;
    }

    @Override
    public double getIPhi() {
        return iphi;
    }

    @Override
    public double getITheta() {
        return itheta;
    }

    @Override
    public double[] getDirectionError() { 
        return directionError;
    }

    @Override
    public double[] getShapeParameters() {
        return shapeParameters;
    }
}
