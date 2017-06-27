package org.lcsim.recon.cluster.fixedcone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.util.CalorimeterHitEsort;
import org.lcsim.event.base.BaseCluster;
import org.lcsim.recon.cluster.util.Clusterer;
import org.lcsim.recon.cluster.util.ClusterESort;
import org.lcsim.recon.cluster.util.FixedConeClusterPropertyCalculator;

import org.lcsim.util.fourvec.Lorentz4Vector;
import org.lcsim.util.fourvec.Momentum4Vector;

import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.CartesianPoint;

import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
 * FixedConeClusterer implements a
 * <font face="symbol">q-f </font> cone clustering algorithm
 * that assigns all neighboring hits to the same cluster if they fall
 * within a radius R of the cluster axis. The axis is originally defined
 * by a seed cell, and is iteratively updated as cells are added.
 * This version of the ClusterBuilder splits overlapping clusters
 * by assigning cells in the overlap region to the nearest cluster axis.
 *
 * @author Norman A. Graf, updated by Qingmin Zhang in 19 Dec,2007.
---------------------------------------------------------------* Updates:---------------------------------------------------
 * 1. Using it you can set the predicting function for cone size based on seed energy (The function format: A*log(Eseed)+B )
 * 2. Using it you can  apply  the cluster energy cut(it varies frome cluster to cluster according to the seed energy ) 
 *      before the found cluster being added to the cluster List (function Format :  A*Eseed+B*Eseed*Eseed)
 * 3. not like last version, the cone sizes of found clusters are different, so admended the rosolve() function for the overlap hits 
 *       using the cone size information 
 *Note: if Construction Fucntion without radius parameter is used, it means you will use predicting function. 
 *      Meanwhile if you don't set the function, default values will be set. 
 *      Otherwise, it's same to last version.
------------------------------------------------------------------------------------------------------------------------------------
 */
public class FixedConeClusterer implements Clusterer {

    private double _radius;
    private double _seedEnergy;
    private double _minEnergy;
    private int _numLayers;
    private double _samplingFraction;
    private double[] _layerEnergy;
    private boolean _radiusSetFlag;//it determines which one is used, preset(ture) or calculated with seed energy(false)
    private double[] _coneFunc;//(The function format: A*log(Eseed)+B )
    private double[] _cutFunc;//(function Format :  A*Eseed+B*Eseed*Eseed)
    private static final double PI = Math.PI;
    private static final double TWOPI = 2. * PI;
    public FixedConeClusterPropertyCalculator _clusterPropertyCalculator;
    private FixedConeDistanceMetric _dm;

    public enum FixedConeDistanceMetric {

        DOTPRODUCT, DPHIDCOSTHETA, DPHIDTHETA
    }

    /**
     * Constructor
     *
     * @param   radius The cone radius in <font face="symbol">q-f </font> space
     * @param   seed   The minimum energy for a cone seed cell (in GeV)
     * @param   minE   The minimum energy for a cluster (in GeV)
     * @param   distMetric The distance metric:
     *                     0 for dot-product method,
     *                     1 for R<sup>2</sup> = (d phi)<sup>2 + (d(cos theta))<sup>2</sup>
     *                       (simplified version of dot-product method)
     *                     2 for R<sup>2</sup> = (d phi)<sup>2 + (d theta)<sup>2</sup>
     *                       (backwards compatibility mode with old FixedConeClusterer implementation
     *                     Anything else will be assumed to be 0.
     */
    public FixedConeClusterer(double seed, double minE, double[] coneFunc, double[] cutFunc, FixedConeDistanceMetric distMetric) {
        // overwrite later with sampling fraction correction
        _seedEnergy = seed;
        _minEnergy = minE;
        _dm = distMetric;//distMetric;

        _coneFunc = coneFunc;
        _cutFunc = cutFunc;
        _radiusSetFlag = false;

    }

    public FixedConeClusterer(double seed, double minE, FixedConeDistanceMetric distMetric) {
        // overwrite later with sampling fraction correction
        _seedEnergy = seed;
        _minEnergy = minE;
        _dm = distMetric;//distMetric;
        //if the values aren't set, default value will be set as follows

        _coneFunc = new double[2];
        _coneFunc[0] = 0.011347;
        _coneFunc[1] = 0.043117;
        _cutFunc = new double[2];
        _cutFunc[0] = 3.2;
        _cutFunc[1] = 2.0;
        _radiusSetFlag = false;

    }

    public FixedConeClusterer(double radius, double seed, double minE, FixedConeDistanceMetric distMetric) {
        _radius = radius;
        // overwrite later with sampling fraction correction
        _seedEnergy = seed;
        _minEnergy = minE;
        _dm = distMetric;//distMetric;

        _radiusSetFlag = true;
    }

    /**
     * Constructor with default distance metric (dot-product method)
     *
     * @param   radius The cone radius in <font face="symbol">q-f </font> space
     * @param   seed   The minimum energy for a cone seed cell (in GeV)
     * @param   minE   The minimum energy for a cluster (in GeV)
     */
    public FixedConeClusterer(double radius, double seed, double minE) {
        this(radius, seed, minE, FixedConeDistanceMetric.DOTPRODUCT);
    }

    public void setPredictFunc(double[] coneFunc, double[] cutFunc) {
        _coneFunc = coneFunc;
        _cutFunc = cutFunc;
        _radiusSetFlag = false;
    }

    public void setRadius(double radius) {
        _radius = radius;
        _radiusSetFlag = true;
    }

    public void setSeed(double seed) {
        _seedEnergy = seed;
    }

    public void setMinE(double minE) {
        _minEnergy = minE;
    }

    /**
     * Make clusters from the input map
     */
    public List<Cluster> createClusters(Map<Long, CalorimeterHit> in) {
        List<CalorimeterHit> list = new ArrayList(in.values());
        return createClusters(list);
    }

    /**
     * Make clusters from the input list
     */
    public List<Cluster> createClusters(List<CalorimeterHit> in) {
//        IDDecoder decoder;
        List<Cluster> out = new ArrayList<Cluster>();
        List<Double> radius = new ArrayList<Double>();
        _clusterPropertyCalculator = new FixedConeClusterPropertyCalculator();

        // sort the vector in descending energy for efficiency
        // this starts with the highest energy seeds.
        Collections.sort(in, new CalorimeterHitEsort());

        int nclus = 0;
        int size = in.size();
        boolean[] used = new boolean[size];

        //   outer loop finds a seed
        for (int i = 0; i < size; ++i) {
            if (!used[i]) {
                CalorimeterHit p = in.get(i);
                double Eseed = p.getCorrectedEnergy();
                double clusterCut = 0.0;
                if (!_radiusSetFlag) {
                    clusterCut = _cutFunc[0] * Eseed + _cutFunc[1] * Eseed * Eseed;
                }
                if (p.getCorrectedEnergy() > _seedEnergy) // hit p as the seed of a new cluster
                {
                    if (!_radiusSetFlag) {
                        _radius = _coneFunc[0] * Math.log(p.getCorrectedEnergy()) + _coneFunc[1];
                    //if predicted radius is less than zero, it won't be thought as a seed
                    }
                    if (_radius < 0.0) {
                        System.out.println("Engergy = " + p.getCorrectedEnergy() + ",Radius = " + _radius);
                        break;
                    }
                    double rsquared = _radius * _radius;
                    double[] pos = p.getPosition();
                    SpacePoint sp = new CartesianPoint(pos);
                    double phi1 = sp.phi();
                    double sphi1 = sin(phi1);
                    double cphi1 = cos(phi1);
                    double theta1 = sp.theta();
                    double stheta1 = sin(theta1);
                    double ctheta1 = cos(theta1);
                    
//                    decoder = p.getIDDecoder();
//                    decoder.setID(p.getCellID());
                    double cellE = p.getCorrectedEnergy();
//                    double px = cellE * Math.cos(decoder.getPhi()) * Math.sin(decoder.getTheta());
//                    double py = cellE * Math.sin(decoder.getPhi()) * Math.sin(decoder.getTheta());
//                    double pz = cellE * Math.cos(decoder.getTheta());
                    double px = cellE * cphi1 * stheta1;
                    double py = cellE * sphi1 * stheta1;
                    double pz = cellE * ctheta1;
                    
                    Lorentz4Vector sum = new Momentum4Vector(px, py, pz, cellE);
                    double phiseed = sum.phi();
                    double thetaseed = sum.theta();

                    // constituent cells
                    List<CalorimeterHit> members = new ArrayList<CalorimeterHit>();
                    members.add(p);
                    // inner loop adds neighboring cells to seed

                    for (int j = i + 1; j < size; ++j) {
                        if (!used[j]) {
                            CalorimeterHit p2 = in.get(j);
                            SpacePoint sp2 = new CartesianPoint(p2.getPosition());
                            double phi = sp2.phi();
                            double theta = sp2.theta();
                            double dphi = phi - phiseed;

                            if (dphi < -PI) {
                                dphi += TWOPI;
                            }
                            if (dphi > PI) {
                                dphi -= TWOPI;
                            }
                            double dtheta = theta - thetaseed;
                            double R2 = 0;
                            boolean cond = false;

                            switch (_dm) {
                                case DPHIDCOSTHETA: // R^2 = dphi^2 + d(cos theta)^2

                                    double dcostheta = cos(theta) - cos(thetaseed);
                                    R2 = dphi * dphi + dcostheta * dcostheta;
                                    cond = (R2 < rsquared);
                                    break;
                                case DPHIDTHETA: // R^2 = dphi^2 + dtheta^2

                                    R2 = dphi * dphi + dtheta * dtheta;
                                    cond = (R2 < rsquared);
                                    break;
                                case DOTPRODUCT:
                                default: // dot product method

                                    double dotp = Math.sin(theta) * Math.sin(thetaseed) *
                                            (sin(phi) * sin(phiseed) +
                                            cos(phi) * cos(phiseed)) +
                                            cos(theta) * cos(thetaseed);
                                    cond = (dotp > cos(_radius));
                                    break;
                            }

                            if (cond) {
                                //  particle within cone
                                cellE = p2.getCorrectedEnergy();
                                px = cellE * cos(phi) * sin(theta);
                                py = cellE * sin(phi) * sin(theta);
                                pz = cellE * cos(theta);
                                sum.plusEquals(new Momentum4Vector(px, py, pz, cellE));
                                members.add(p2);
                                // tag this element so we don't reuse it
                                used[j] = true;

                                //   recalculate cone center
                                phiseed = sum.phi();
                                thetaseed = sum.theta();
                            }
                        }
                    }// end of inner loop


                    // if energy of cluster is large enough add it to the list
                    if (sum.E() > clusterCut) //_minEnergy)
                    {
                        BaseCluster clus = new BaseCluster();
                        clus.setPropertyCalculator(_clusterPropertyCalculator);
                        for (CalorimeterHit hit : members) {
                            clus.addHit(hit);
                        }
                        out.add(clus);
                        radius.add(_radius);
                        nclus++;
                    }

                }
            }// end of outer loop

        }

        if (nclus > 1) {
            // sort the clusters in descending energy
            Collections.sort(out, new ClusterESort());
            // loop over the found clusters and look for overlaps
            // i.e distance between clusters is less than 2*R
            for (int i = 0; i < out.size(); ++i) {
                for (int j = i + 1; j < out.size(); ++j) {
                    double dTheta = dTheta(out.get(i), out.get(j));
                    if (dTheta < radius.get(i) + radius.get(j)) {
                        resolve((BaseCluster) (out.get(i)), radius.get(i), (BaseCluster) (out.get(j)), radius.get(j));
                    }
                }
            }
        }

//        System.out.println("found "+out.size()+ " clusters");
        return out;
    }

    /**
     * Calculate the angle between two Clusters
     *
     * @param   c1  First Cluster
     * @param   c2  Second Cluster
     * @return     The angle between the two clusters
     */
    public double dTheta(Cluster c1, Cluster c2) {
        _clusterPropertyCalculator.calculateProperties(c1.getCalorimeterHits());
        Lorentz4Vector v1 = _clusterPropertyCalculator.vector();
        _clusterPropertyCalculator.calculateProperties(c2.getCalorimeterHits());
        Lorentz4Vector v2 = _clusterPropertyCalculator.vector();
        double costheta = (v1.vec3dot(v2)) / (v1.p() * v2.p());
        return Math.acos(costheta);
    }

    /**
     * Given two overlapping clusters, assign cells to nearest axis.
     * The cluster axes are <em> not </em> iterated.
     * Cluster quantities are recalculated after the split.
     *
     * @param   c1 First Cluster
     * @param   c2 Second Cluster
     */
    public void resolve(BaseCluster c1, double coneR1, BaseCluster c2, double coneR2) {
        // do not recalculate cluster axis until all reshuffling is done
        // do not want the cones to shift
        // this behavior may change in the future

        _clusterPropertyCalculator.calculateProperties(c1.getCalorimeterHits());
        Lorentz4Vector v1 = _clusterPropertyCalculator.vector();
        double phi1 = v1.phi();
        double theta1 = v1.theta();
        _clusterPropertyCalculator.calculateProperties(c2.getCalorimeterHits());
        Lorentz4Vector v2 = _clusterPropertyCalculator.vector();
        double phi2 = v2.phi();
        double theta2 = v2.theta();
        List<CalorimeterHit> cells1 = c1.getCalorimeterHits();
        List<CalorimeterHit> cells2 = c2.getCalorimeterHits();
        int size2 = cells2.size();
        List<CalorimeterHit> swap = new ArrayList<CalorimeterHit>();

        // loop over cells in first cluster...
        for (int i = 0; i < cells1.size(); ++i) {
            CalorimeterHit p2 = (CalorimeterHit) cells1.get(i);
            SpacePoint sp2 = new CartesianPoint(p2.getPosition());
//            IDDecoder decoder = p2.getIDDecoder();
//            decoder.setID(p2.getCellID());
            double phi = sp2.phi();
            double theta = sp2.theta();
            //distance to cluster 1
            double dphi1 = phi - phi1;
            if (dphi1 < -PI) {
                dphi1 += TWOPI;
            }
            if (dphi1 > PI) {
                dphi1 -= TWOPI;
            }
            double dtheta1 = theta - theta1;
            double R1 = dphi1 * dphi1 + (dtheta1) * (dtheta1);
            // distance to cluster 2
            double dphi2 = phi - phi2;
            if (dphi2 < -PI) {
                dphi2 += TWOPI;
            }
            if (dphi2 > PI) {
                dphi2 -= TWOPI;
            }
            double dtheta2 = theta - theta2;
            double R2 = dphi2 * dphi2 + (dtheta2) * (dtheta2);

            if (R2 / R1 < coneR2 / coneR1) {
                swap.add(p2);
            }
        }
        for (CalorimeterHit h : swap) {
            c1.removeHit(h);
            c2.addHit(h);
        }
        swap = new ArrayList<CalorimeterHit>();
        // repeat for cluster 2
        // only loop over cells in original cluster
        for (int i = 0; i < size2; ++i) {
            CalorimeterHit p2 = (CalorimeterHit) cells2.get(i);
//            IDDecoder decoder = p2.getIDDecoder();
//            decoder.setID(p2.getCellID());
            SpacePoint sp2 = new CartesianPoint(p2.getPosition());
            double phi = sp2.phi();
            double theta = sp2.theta();
            //distance to cluster 1
            double dphi1 = phi - phi1;
            if (dphi1 < -PI) {
                dphi1 += TWOPI;
            }
            if (dphi1 > PI) {
                dphi1 -= TWOPI;
            }
            double dtheta1 = theta - theta1;
            double R1 = dphi1 * dphi1 + (dtheta1) * (dtheta1);
            // distance to cluster 2
            double dphi2 = phi - phi2;
            if (dphi2 < -PI) {
                dphi2 += TWOPI;
            }
            if (dphi2 > PI) {
                dphi2 -= TWOPI;
            }
            double dtheta2 = theta - theta2;
            double R2 = dphi2 * dphi2 + (dtheta2) * (dtheta2);

            if (R1 / R2 < coneR1 / coneR2) {
                swap.add(p2);
            }
        }
        for (CalorimeterHit h : swap) {
            c2.removeHit(h);
            c1.addHit(h);
        }

        c1.calculateProperties();
        c2.calculateProperties();
    }

    public String toString() {
        return "FixedConeClusterer with radius " + _radius + " seed Energy " + _seedEnergy + " minimum energy " + _minEnergy + "distance metric " + _dm;
    }
}

