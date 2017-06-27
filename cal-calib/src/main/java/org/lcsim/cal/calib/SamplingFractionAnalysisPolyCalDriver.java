/*
 * SamplingFractionAnalysisDriver.java
 *
 * Created on May 19, 2008, 11:54 AM
 *
 * $Id: SamplingFractionAnalysisPolyCalDriver.java,v 1.4 2012/02/10 15:26:26 grefe Exp $
 */
package org.lcsim.cal.calib;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import hep.aida.ITree;
import hep.physics.vec.Hep3Vector;

import java.util.List;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;
import org.lcsim.geometry.subdetector.PolyhedraEndcapCalorimeter2;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer.FixedConeDistanceMetric;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import Jama.Matrix;

/**
 *
 * @author Norman Graf
 */
public class SamplingFractionAnalysisPolyCalDriver extends Driver
{

    private ConditionsSet _cond;
    private CollectionManager _collectionmanager = CollectionManager.defaultInstance();
    // we will accumulate the raw energy values in three depths:
    // 1. Layers (0)1 through (20)21 of the EM calorimeter (note that if layer 0 is massless, SF==1.)
    // 2. last ten layers of the EM calorimeter
    // 3. the hadron calorimeter
    //
    private double[][] _acc = new double[3][3];
    private double[] _vec = new double[3];
    // let's use a clusterer to remove effects of calorimeter cells hit far, far away.
    // use the only cross-detector clusterer we have:
    private FixedConeClusterer _fcc;
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree;
    private boolean _initialized = false;
    private boolean _debug = false;
    // TODO fix this dependence on EM calorimeter geometry
    boolean skipFirstLayer = false;
//    int firstEmStartLayer = 0;
//    int secondEmStartLayer = 20;

    private double[] _ecalLayering;
    boolean _useFirstLayer;

//    double emCalInnerRadius = 0.;
    double emCalInnerZ = 0.;

    boolean _isHcalDigital = false;

    /**
     * Creates a new instance of SamplingFractionAnalysisDriver
     */
    public SamplingFractionAnalysisPolyCalDriver()
    {
        _tree = aida.tree();
    }

    protected void process(EventHeader event)
    {
        super.process(event);
        //System.out.println("processing SamplingFractionAnalysisDriver");
        // TODO make these values runtime definable        
        String[] det
                = {
                    "EcalBarrel", "EcalEndcap"
                };
//        String[] collNames = {"EcalBarrHits", "EcalEndcapHits", "HcalBarrHits", "HcalEndcapHits"};
//        double[] mipHistMaxBinVal = {.0005, .0005, .005, .005};
//        double timeCut = 100.; // cut on energy depositions coming more than 100 ns late
//        double ECalMipCut = .0001/3.; // determined from silicon using muons at normal incidence
//        double HCalMipCut = .0008/3.; // determined from scintillator using muons at normal incidence
//        double[] mipCut = {ECalMipCut, ECalMipCut, HCalMipCut, HCalMipCut};

        if (!_initialized) {
            ConditionsManager mgr = ConditionsManager.defaultInstance();
            try {
                _cond = mgr.getConditions("CalorimeterCalibration");
            } catch (ConditionsSetNotFoundException e) {
                System.out.println("ConditionSet CalorimeterCalibration not found for detector " + mgr.getDetector());
                System.out.println("Please check that this properties file exists for this detector ");
            }
            double radius = .5;
            double seed = 0.;//.1;
            double minE = .05; //.25;
            _fcc = new FixedConeClusterer(radius, seed, minE, FixedConeDistanceMetric.DPHIDTHETA);

            // detector geometries here...
            // barrel
            //CylindricalCalorimeter calsubBarrel = (CylindricalCalorimeter)event.getDetector().getSubdetectors().get(det[0]);
            System.out.println("looking up subdet: " + det[0]);
            Subdetector subdet = event.getDetector().getSubdetectors().get(det[0]);
            System.out.println("proc subdet: " + subdet.getName());

            _ecalLayering = _cond.getDoubleArray("ECalLayering");
            _useFirstLayer = _cond.getDouble("IsFirstEmLayerSampling") == 1.;
            skipFirstLayer = !_useFirstLayer;
            ConditionsSet hcalProperties = mgr.getConditions("SamplingFractions/HcalBarrel");

            _isHcalDigital = hcalProperties.getBoolean("digital");
            System.out.println("HCal is " + (_isHcalDigital == true ? "" : "not") + " read out digitally");

            AbstractPolyhedraCalorimeter calsubBarrel = (AbstractPolyhedraCalorimeter) event.getDetector().getSubdetectors().get(det[0]);
            // TODO remove this hardcoded dependence on the first layer
//            if (calsubBarrel.getLayering().getLayer(0).getSlices().get(0).isSensitive()) {
//                skipFirstLayer = true;
//                firstEmStartLayer += 1;
//                secondEmStartLayer += 1;
//            }
//            Layering layering = calsubBarrel.getLayering();
//            for(int i=0; i<layering.size(); ++i)
//            {
//                Layer l = layering.getLayer(i);
//                System.out.println("layering "+i);
//                List<LayerSlice> slices = l.getSlices();
//                for(int j=0; j<slices.size(); ++j)
//                {
//                    LayerSlice slice = slices.get(j);
//                    System.out.println("Layer "+i+" slice "+j+" is "+ slice.getMaterial().getName() +" and "+(slice.isSensitive() ? " is sensitive" : ""));
//                }
//            }
//            emCalInnerRadius = calsubBarrel.getInnerRadius();
            //endcap
//            AbstractPolyhedraCalorimeter calsubEndcap = (AbstractPolyhedraCalorimeter) event.getDetector().getSubdetectors().get(det[1]);
//            emCalInnerZ = abs(calsubEndcap.getZMin());
            if (skipFirstLayer) {
                System.out.println("processing " + event.getDetectorName() + " with an em calorimeter with a massless first gap");
            }
//            System.out.println("Calorimeter bounds: r= " + emCalInnerRadius + " z= " + emCalInnerZ);
            System.out.println("initialized...");

            _initialized = true;
        }

        // organize the histogram tree by species and energy
        List<MCParticle> mcparts = event.getMCParticles();
        //MCParticle mcpart = mcparts.get(mcparts.size() - 1); // this only works if particle was generated by stdhep, does not work with GEANT gps
        MCParticle mcpart = null;
        // Look for the most energetic final state particle
        for (MCParticle myMCP : mcparts) {
            if (myMCP.getGeneratorStatus() == MCParticle.FINAL_STATE) {
                if (mcpart == null) {
                    mcpart = myMCP;
                } else if (mcpart.getEnergy() < myMCP.getEnergy()) {
                    mcpart = myMCP;
                }
            }
        }

        if (mcpart != null) {
            String particleType = mcpart.getType().getName();
            double mcEnergy = mcpart.getEnergy();
            long mcIntegerEnergy = Math.round(mcEnergy);
            boolean meV = false;
            if (mcEnergy < .99) {
                mcIntegerEnergy = Math.round(mcEnergy * 1000);
                meV = true;
            }

            _tree.mkdirs(particleType);
            _tree.cd(particleType);
            _tree.mkdirs(mcIntegerEnergy + (meV ? "_MeV" : "_GeV"));
            _tree.cd(mcIntegerEnergy + (meV ? "_MeV" : "_GeV"));

            // this analysis is intended for single particle calorimeter response.
            // let's make sure that the primary particle did not interact in the
            // tracker...
//        Hep3Vector endpoint = mcpart.getEndPoint();
//        // this is just crap. Why not use SpacePoint?
//        double radius = sqrt(endpoint.x()*endpoint.x()+endpoint.y()*endpoint.y());
//        double z = endpoint.z();
////        System.out.println("Input MCParticle endpoint: r="+radius+" z= "+z);
//
//        boolean doit = true;
//        if(radius<emCalInnerRadius && abs(z) < emCalInnerZ) doit = false;
//        if(doit)
            if (mcpart.getSimulatorStatus().isDecayedInCalorimeter()) {
//            // now let's check the em calorimeters...
//            // get all of the calorimeter hits...
//            List<CalorimeterHit> allHits = new ArrayList<CalorimeterHit>();
//            // and the list after cuts.
//            List<CalorimeterHit> hitsToCluster = new ArrayList<CalorimeterHit>();
//            int i = 0;
//            for(String name : collNames)
//            {
////                System.out.println("fetching "+name+" from the event");
//                List<CalorimeterHit> hits = event.get(CalorimeterHit.class, name);
////                System.out.println(name+ " has "+hits.size()+" hits");
//                // let's look at the hits and see if we need to cut on energy or time...
//                for(CalorimeterHit hit: hits)
//                {
//                    aida.histogram1D(name+" raw calorimeter cell energy",100, 0., mipHistMaxBinVal[i]).fill(hit.getRawEnergy());
//                    aida.histogram1D(name+" raw calorimeter cell energy full range",100, 0., 0.2).fill(hit.getRawEnergy());
////                    aida.cloud1D(name+" raw calorimeter cell energies").fill(hit.getRawEnergy());
//                    aida.histogram1D(name+" calorimeter cell time",100,0., 200.).fill(hit.getTime());
//                    if(hit.getTime()<timeCut)
//                    {
//                        if(hit.getRawEnergy()>mipCut[i])
//                        {
//                            hitsToCluster.add(hit);
//                        }
//                    }
//                }
//                allHits.addAll(hits);
//                ++i;
//            }
//            System.out.println("ready to cluster "+hitsToCluster.size()+ " hits");
                String processedHitsName = _cond.getString("ProcessedHitsCollectionName");
                List<CalorimeterHit> hitsToCluster = _collectionmanager.getList(processedHitsName);//event.get(CalorimeterHit.class, processedHitsName);

                if (_debug) {
                    System.out.println("clustering " + hitsToCluster.size() + " hits");
                }
                // quick check
//            for(CalorimeterHit hit : hitsToCluster)
//            {
//                System.out.println("hit ");
//                System.out.println(hit.getLCMetaData().getName());
//            }
                // cluster the hits
                List<Cluster> clusters = _fcc.createClusters(hitsToCluster);
                if (_debug) {
                    System.out.println("found " + clusters.size() + " clusters");
                }
                aida.histogram1D("number of found clusters", 10, -0.5, 9.5).fill(clusters.size());
                for (Cluster c : clusters) {
//                System.out.println(c);
                    aida.cloud1D("cluster energy for all clusters").fill(c.getEnergy());
                }

                // proceed only if we found a single cluster above threshold
                // too restrictive! simply take the highest energy cluster
                if (clusters.size() > 0) {
                    int nHCalHits = 0;
                    Cluster c = clusters.get(0);

                    aida.cloud1D("Highest energy cluster energy").fill(c.getEnergy());
                    aida.cloud1D("Highest energy cluster number of cells").fill(c.getCalorimeterHits().size());

                    double clusterEnergy = c.getEnergy();
                    double mcMass = mcpart.getType().getMass();
                    // subtract the mass to get kinetic energy...
                    double expectedEnergy = mcEnergy;
                    // In case of protons and neutrons we expect only the kinetic energy in the shower
                    if (mcpart.getPDGID() == 2212 || mcpart.getPDGID() == 2112) {
                        expectedEnergy = mcEnergy - mcMass;
                    }
//                System.out.println(mcpart.getType().getName()+" "+expectedEnergy);
                    aida.cloud1D("measured - predicted energy").fill(clusterEnergy - expectedEnergy);

                    // let's now break down the cluster by component.
                    // this analysis uses:
                    // 1.) first 20 EM layers
                    // 2.) next 10 EM layers
                    // 3.) Had layers
                    List<CalorimeterHit> hits = c.getCalorimeterHits();
                    double[] vals = new double[4];
                    int nHcalHits = 0;
                    double clusterRawEnergy = 0.;
                    for (CalorimeterHit hit : hits) {
                        long id = hit.getCellID();
                        IDDecoder decoder = hit.getIDDecoder();
                        decoder.setID(id);
                        int layer = decoder.getLayer();
                        String detectorName = decoder.getSubdetector().getName();
                        int type = 0;
                        int caltype = 0;
                        // FIXME Hard-coded name.
                        if (detectorName.toUpperCase().startsWith("ECAL")) {
//                            if (layer >= firstEmStartLayer && layer < secondEmStartLayer) {
//                                type = 0;
//                            } else {
//                                type = 1;
//                            }
                            for (int i = 1; i < _ecalLayering.length + 1; ++i) {
                                if (layer >= _ecalLayering[i - 1]) {
                                    caltype = i - 1;
                                }
                            }
                        }
                        // FIXME Hard-coded name.
                        if (detectorName.toUpperCase().startsWith("HCAL")) {
                            type = 2;
                            caltype = 3;
                            nHCalHits += 1;
                        }
                        if(_debug)
                        {
                            System.out.println(detectorName+" layer: "+layer+" type: "+type+" caltype: "+caltype+" raw");
                        }
                        clusterRawEnergy += hit.getRawEnergy();
                        vals[caltype] += hit.getRawEnergy();
//                        if (_isHcalDigital == true) {
//                                nHcalHits += nHCalHits;
//                            }
                    } // end of loop over hits in cluster
                    if (_isHcalDigital == true) {
                                vals[3] = nHCalHits;
                            }
                    // set up linear least squares:
                    // expectedEnergy = a*E1 + b*E2 +c*E3
                    for (int j = 0; j < 3; ++j) {
                        if(_debug)
                        {
                            System.out.println("clusterRawEnergy= "+clusterRawEnergy+" vals["+j+"]= "+vals[j]);
                        }
                        _vec[j] += expectedEnergy*vals[j+1];
                        for (int k = 0; k < 3; ++k) {
                            _acc[j][k] += vals[j+1] * vals[k+1] ;
                        }
                    }
                } // end of single cluster cut

//            event.put("All Calorimeter Hits",allHits);
//            event.put("Hits To Cluster",hitsToCluster);
                event.put("Found Clusters", clusters);
            }// end of check on decays outside tracker volume
        } else {
            System.out.println("null MCPointer at event " + event.getEventNumber());
        }
        _tree.cd("/");
    }

    protected void endOfData()
    {
        System.out.println("done! endOfData.");
        // calculate the sampling fractions...
        Matrix A = new Matrix(_acc, 3, 3);
        A.print(6, 4);
        Matrix b = new Matrix(3, 1);
        for (int i = 0; i < 3; ++i) {
            b.set(i, 0, _vec[i]);
        }
        b.print(6, 4);
        try {
            Matrix x = A.solve(b);
            x.print(6, 4);
            System.out.println("SamplingFractions: " + (skipFirstLayer ? "1., " : "") + 1. / x.get(0, 0) + ", " + 1. / x.get(1, 0) + ", " + 1. / x.get(2, 0));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("try reducing dimensionality...");
            Matrix Ap = new Matrix(_acc, 2, 2);
            Ap.print(6, 4);
            Matrix bp = new Matrix(2, 1);
            for (int i = 0; i < 2; ++i) {
                bp.set(i, 0, _vec[i]);
            }
            bp.print(6, 4);
            try {
                Matrix x = Ap.solve(bp);
                x.print(6, 4);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public void setDebug(boolean debug)
    {
        _debug = debug;
    }
}
