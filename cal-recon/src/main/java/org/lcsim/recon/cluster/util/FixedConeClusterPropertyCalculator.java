package org.lcsim.recon.cluster.util;

import java.util.List;
import org.lcsim.spacegeom.PrincipalAxesLineFitter;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.util.fourvec.Lorentz4Vector;
import org.lcsim.util.fourvec.Momentum4Vector;
import org.lcsim.event.Cluster;
import org.lcsim.event.base.ClusterPropertyCalculator;

/**
 * A class encapsulating the behavior of a calorimeter cluster.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class FixedConeClusterPropertyCalculator implements ClusterPropertyCalculator
{
    private IDDecoder _decoder;
    private Lorentz4Vector _vec;
    // second moment of the cluster
    private double _width;
    private double[] _layerEnergy;
    private double _clusterEnergy;
    private double[] _layerWidth;
    private double[] _centroid;
    private double[] _directionCosines;
    private double _samplingFraction;
    private CalorimeterHit _hottestCell;
    private double _highestCellEnergy;
    private boolean _isEndCap;
    private boolean _isNorth;
    private double _chisq = 99999.;
    private int layers;
    private double _itheta;
    private double _iphi;
    
    /**
     * Fully qualified constructor
     */
    public FixedConeClusterPropertyCalculator()
    {
        layers = 100;
    }

    public void calculateProperties(Cluster cluster) {
        calculateProperties(cluster.getCalorimeterHits());
    }

    public void calculateProperties(List<CalorimeterHit> hits)
    {
        _vec = calculateVec(hits);
        _layerEnergy = new double[layers];
        _layerWidth = new double[layers];
        // the array of cell (x,y,z) coordinates
        double[][] points = new double[3][hits.size()];
        int npoints=0;
        _highestCellEnergy = 0.;
        
        for(CalorimeterHit h : hits )
        {
            _decoder = h.getIDDecoder();
            _decoder.setID(h.getCellID());
            double e = h.getCorrectedEnergy();
            if (e>_highestCellEnergy)
            {
                _highestCellEnergy = e;
                _hottestCell = h;
                // for now let highest energy cells determine location
//                _isEndCap = cell.isEndcap();
//                _isNorth = cell.isNorth();
            }
            // calculate the energy-weighted cluster width (second moment)
            double dtheta=_vec.theta() - _decoder.getTheta();
            double dphi=_vec.phi() - _decoder.getPhi();
            // phi-wrap at 0:2pi?
            if (dphi>Math.PI)
            {
                dphi-=2.*Math.PI;
            }
            if (dphi<-Math.PI)
            {
                dphi+=2.*Math.PI;
            }
            double dRw=(dtheta*dtheta + dphi*dphi)*e;
            _width+=dRw;
            // increment the energy deposited in this layer
            _layerEnergy[_decoder.getLayer()]+=e;
            _clusterEnergy+=e;
            // increment the width (second moment of energy) in this layer
            _layerWidth[_decoder.getLayer()]+=dRw;
            //store the hit (x,y,z) coordinates
            points[0][npoints] = _decoder.getX();
            points[1][npoints] = _decoder.getY();
            points[2][npoints] = _decoder.getZ();
            npoints++;
        }
        // normalize the second moments
        _width /= _clusterEnergy;
        for(int i=0; i<layers; ++i)
        {
            _layerWidth[i]/=_clusterEnergy;
        }
        
        // fit a straight line through the cells and store the results
        PrincipalAxesLineFitter lf = new PrincipalAxesLineFitter();
        lf.fit(points);
        _centroid = lf.centroid();
        _directionCosines = lf.dircos();
        double dr = Math.sqrt( (_centroid[0]+_directionCosines[0])*(_centroid[0]+_directionCosines[0]) +
                (_centroid[1]+_directionCosines[1])*(_centroid[1]+_directionCosines[1]) +
                (_centroid[2]+_directionCosines[2])*(_centroid[2]+_directionCosines[2]) ) -
                Math.sqrt(	(_centroid[0])*(_centroid[0]) +
                (_centroid[1])*(_centroid[1]) +
                (_centroid[2])*(_centroid[2]) ) ;
        double sign = 1.;
        if(dr < 0.)sign = -1.;
        _itheta = Math.acos(_directionCosines[2]);
        _iphi = Math.atan2(_directionCosines[1],_directionCosines[0]);
        
        // finish up the cluster (base class method)
//        calculateDerivedQuantities();
    }
    
    /**
     * Calculate the cluster four-momentum.
     * The Lorentz four-vector is derived from the cluster cells.
     *
     */
    public Lorentz4Vector calculateVec(List<CalorimeterHit> hits)
    {
        Lorentz4Vector sum = new Momentum4Vector();
        double[] sums = {0.,0.,0.};
        double wtsum = 0.;
        for(int i=0;i<hits.size();i++)
        {
            CalorimeterHit hit = hits.get(i);
            double[] pos = new double[3];
            _decoder = hit.getIDDecoder();
            _decoder.setID(hit.getCellID());
            pos[0] = _decoder.getX();
            pos[1] = _decoder.getY();
            pos[2] = _decoder.getZ();
            double wt = hit.getCorrectedEnergy();
            wtsum += wt;
            for(int j=0;j<3;j++)
            {
                sums[j] += wt*pos[j];
            }
        }
        sum.plusEquals(new Momentum4Vector(sums[0], sums[1], sums[2], wtsum));
        return sum;
    }
    
    /**
     * The cluster width (energy second moment).
     *
     * @return The cluster width
     */
    public double width()
    {
        return _width;
    }
    
    /**
     * The cluster four-momentum
     *
     * @return The Lorentz four-vector
     */
    public Lorentz4Vector vector()
    {
        return _vec;
    }
    
    /**
     * The constituent cells
     *
     * @return Vector of the CalorimeterHits constituting the cluster.
     */
    /**
     * The cluster energy deposited in a specific layer
     *
     * @return  The cluster energy in layer <b>layer</b>
     */
    public double layerEnergy(int layer)
    {
        return _layerEnergy[layer];
    }
    
    /**
     * The cluster layer energies
     *
     * @return  The array of cluster energies in each layer.
     */
    public double[] layerEnergies()
    {
        return _layerEnergy;
    }
    
    
    /**
     * The cluster energy corrected for sampling fractions
     *
     * @return  The cluster energy
     */
    public double clusterEnergy()
    {
        return _clusterEnergy;
    }
    
    /**
     * The energy of the highest energy cell in this cluster
     *
     * @return The energy of the highest energy cell in this cluster corrected by the sampling fraction.
     */
    public double highestCellEnergy()
    {
        return _highestCellEnergy;
    }
    
    /**
     * The CalorimeterHit in this cluster with the highest energy
     *
     * @return  The CalorimeterHit in this cluster with the highest energy
     */
    public CalorimeterHit hottestCell()
    {
        return _hottestCell;
    }
    
    /**
     * The cluster width (energy second moment) deposited in a specific layer
     *
     * @return  The cluster width in layer <b>layer</b>
     */
    public double layerWidth(int layer)
    {
        return _layerWidth[layer];
    }
    
    /**
     * The unweighted spatial centroid (x,y,z) of the cluster line fit
     *
     * @return The unweighted spatial centroid (x,y,z) of the cluster
     */
    public double[] centroid()
    {
        return _centroid;
    }
    
    /**
     * The direction cosines of the cluster line fit
     *
     * @return The direction cosines of the cluster
     */
    public double[] directionCosines()
    {
        return _directionCosines;
    }
    
    
    /**
     * Returns topological position of cluster.
     *
     * @return true if in EndCap
     */
    public boolean isEndCap()
    {
        return _isEndCap;
    }
    
    
    /**
     * Returns topological position of cluster.
     *
     * @return  true if in "North" EndCap
     */
    public boolean isNorth()
    {
        return _isNorth;
    }
    
    public void setChisq(double chisq)
    {
        _chisq = chisq;
    }
    
    public double chisq()
    {
        return _chisq;
    }
    public double[] getPosition()
    {
        return _centroid;
    }
    public double[] getPositionError()
    {
        double[] positionError = {0.,0.,0.,0.,0.,0.};
        return positionError;
    }
    public double getIPhi()
    {
        return _iphi;
    }
    public double getITheta()
    {
        return _itheta;
    }
    public double[] getDirectionError()
    {
        double[] directionError = {0.,0.,0.,0.,0.,0.};
        return directionError;
    }
    public double[] getShapeParameters()
    {
        double[] shapeParameters = {0.,0.,0.,0.,0.,0.};
        shapeParameters[0] = _width;
        return shapeParameters;
    }
    
    
}

