package org.lcsim.cal.calib;
import java.util.List;
import java.util.ArrayList;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.subdetector.CylindricalCalorimeter;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;
import hep.aida.ITree;

import static java.lang.Math.sqrt;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.Cluster;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.recon.emid.hmatrix.HMatrix;
import org.lcsim.recon.emid.hmatrix.HMatrixTask;
import org.lcsim.recon.emid.hmatrix.HMatrixBuilder;
import org.lcsim.recon.emid.hmatrix.HMatrixConditionsConverter;
import org.lcsim.math.chisq.ChisqProb;
import org.lcsim.math.moments.CentralMomentsCalculator;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer.FixedConeDistanceMetric;

/**
 * Reconstruction: EM Clusters
 */
public class EMClusterID extends Driver
{
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree;
    private boolean _initialized;
    private ConditionsSet _cond;
    private String _detName;
    private HMatrixTask _task;
    private int _nLayers;
    
    private HMatrixBuilder _hmb;
    private HMatrix _hmx;
    
    // the number of variables in the measurement vector
    private int _nmeas;
    
    // the vector of measured values
    double[] _vals;
    
    // the mapping of physical layers to measurement space
    double[] _layerMapping;
    
    private DecimalFormat _myFormatter = new DecimalFormat("#.###");
    
    private boolean _debug = true;
    
    // where to write the HMatrix if in accumulate mode
    private String _fileLocation = "default.hmx";
    
    public EMClusterID()
    {
        this(HMatrixTask.ANALYZE);
//        this(HMatrixTask.BUILD);
    }
    
    public EMClusterID(HMatrixTask task)
    {
        _tree = aida.tree();
        _task = task;
        
        if(task==HMatrixTask.ANALYZE)
        {
            // The HMatrix could possibly change, so be sensitive to this.
            getConditionsManager().registerConditionsConverter(new HMatrixConditionsConverter());
        }
        
    }
    
    protected void process(EventHeader event)
    {
        super.process(event);
        
        //FIXME: need to get the EM calorimeter names from a conditions file
        // FIXME should get calorimeterhit collection names from a conditions file
        
        String[] det = {"EMBarrel","EMEndcap"};
        String[] hitsToGet = {"EcalBarrHits","EcalEndcapHits"};
        
        if(!_initialized)
        {
            ConditionsManager mgr = ConditionsManager.defaultInstance();
            try
            {
                _cond = mgr.getConditions("HMatrix");
            }
            catch(ConditionsSetNotFoundException e)
            {
                System.out.println("ConditionSet HMatrix not found for detector "+mgr.getDetector());
                System.out.println("Please check that this properties file exists for this detector ");
            }
            // sanity check
            String detectorNameFromFile = _cond.getString("detectorName");
            if(!detectorNameFromFile.equals(event.getDetectorName()))
            {
                System.out.println("detector name from HMatrix.properties: "+detectorNameFromFile +" detector name from event "+event.getDetectorName());
                throw new RuntimeException("detector name mismatch in HMatrix!");
            }
            // the vector of measurements starts as the longitudinal layers
            _nmeas = _cond.getInt("measurementDimension");
            // would add any additional measurements (e.g. width) here
            _vals = new double[_nmeas];
            
            // add the detector name and measurement dimensionality to the output file
            _fileLocation = event.getDetectorName()+"_"+_fileLocation+"_"+_nmeas+".hmx";
            
            
            _layerMapping = _cond.getDoubleArray(_nmeas+"layerMapping");
//            for (int i=0; i<_layerMapping.length; ++i)
//            {
//                System.out.println("_layerMapping[ "+i+" ]= "+ _layerMapping[i]);
//            }
            // sanity check
            
            CylindricalCalorimeter calsub = (CylindricalCalorimeter)event.getDetector().getSubdetectors().get(det[0]);
            _nLayers = calsub.getLayering().getLayerCount();
            if(_nLayers != _layerMapping.length)
            {
                System.out.println("found "+_nLayers+" layers in the "+det[0]);
                throw new RuntimeException("layer number mismatch in EMCalorimeter!");
            }
            
            //FIXME key needs to be better defined
            int key = 0;
            if(_task==HMatrixTask.ANALYZE)
            {
                //FIXME need to fetch name of HMAtrix file to use from a conditions file
                _hmx = getConditionsManager().getCachedConditions(HMatrix.class, "LongitudinalHMatrix"+_nmeas+".hmx").getCachedData();
            }
            else if(_task==HMatrixTask.BUILD)
            {
                _hmb = new HMatrixBuilder(_nmeas,key);
            }
            _detName = event.getDetectorName();
            
            
            _initialized = true;
        }
        
        List<Cluster> photons = new ArrayList<Cluster>();
        for(int j=0; j<det.length; ++j)
        {
            List<CalorimeterHit> collection = event.get(CalorimeterHit.class,hitsToGet[j]);
            
            
            double radius = 0.5;
            double seed = 0.;
            double minE = 0.05;
            
            // create the clusterer
            FixedConeClusterer fcc = new FixedConeClusterer(radius, seed, minE, FixedConeDistanceMetric.DPHIDTHETA);
            //cluster
            List<Cluster> clusters = fcc.createClusters(collection);
            
            int minHitsInCluster = 20;
            
            // add this list of clusters to the event (for event display)
            event.put(hitsToGet[j]+"Clusters ",clusters);
            // Loop over all the clusters
            int nGoodClusters = 0;
            for (Cluster cluster : clusters)
            {
                int ncells = cluster.getCalorimeterHits().size();
                double energy = cluster.getEnergy();
                //FIXME should cut on cluster corrected energy
                if(ncells > minHitsInCluster)
                {
                    nGoodClusters++;
                    
                    aida.cloud1D("Number of cells in cluster").fill(ncells);
                    aida.cloud2D("Number of cells in cluster vs cluster energy").fill(energy, ncells);
                    
                    aida.cloud2D("Hottest cell in cluster vs cluster energy").fill(energy,hottestCellEnergy(cluster));
//                    aida.cloud1D("Cluster raw energy").fill(bc.getRawEnergy() );
                    aida.cloud1D("ClusterCorrected energy").fill(energy);
                    // should be able to fetch this from the cluster...
                    double[] layerE = layerFractionalEnergies(cluster);
                    // accumulate the longitudinal energy fractions into the measurement vector...
                    for(int i=0; i<layerE.length; ++i)
                    {
                        if(_layerMapping[i] != -1)
                        {
                            _vals[(int)_layerMapping[i]] += layerE[i];
//                            System.out.println("LayerE[ "+i+" ]= "+layerE[i]+" _vals[ "+(int)_layerMapping[i]+" ] "+_vals[(int)_layerMapping[i]]);
                            aida.cloud2D("Fractional Energy vs physical Layer").fill(i,layerE[i]);
                            aida.cloud2D("Fractional Energy vs mapped Layer").fill(_layerMapping[i],layerE[i]);
                            
                        }
                    }
                    
                    for(int i=0; i<_vals.length; ++i)
                    {
                        aida.cloud1D("Fractional Energy for mapped Layer "+i).fill(_vals[i]);
                        for(int k=i+1; k<_vals.length; ++k)
                        {
                            aida.cloud2D("Fractional Energy "+i+" vs "+k).fill(_vals[i], _vals[k]);
                        }
                    }
                    
                    // have now filled the vector of measurements. need to either accumulate the HMatrix or apply it
                    if (_task==HMatrixTask.BUILD)
                    {
                        _hmb.accumulate(_vals);
                    }
                    if (_task==HMatrixTask.ANALYZE)
                    {
                        double chisq = _hmx.chisquared(_vals);
                        aida.cloud1D("Chisq").fill(chisq);
                        aida.cloud2D("Chisq vs energy").fill(energy,chisq);
                        if(chisq<500)
                        {
                            aida.cloud1D("Chisq(<500)").fill(chisq);
                            aida.cloud2D("Chisq (<500) vs energy").fill(energy,chisq);
                        }
                        aida.cloud1D("Chisq Probability").fill(ChisqProb.gammq(_nmeas,chisq));
                        
                        double chisqD = _hmx.chisquaredDiagonal(_vals);
                        aida.cloud1D("ChisqD").fill(chisqD);
                        aida.cloud2D("ChisqD vs energy").fill(energy,chisqD);
                        
                        double chisqDProb = ChisqProb.gammq(_nmeas,chisqD);
                        if(chisqDProb<.0000000001) chisqDProb = 0.0000000001;
                        aida.cloud1D("ChisqD Probability").fill(chisqDProb);
                        aida.cloud1D("log10 ChisqDProb").fill(Math.log10(chisqDProb));
                    }
                    double[] pos = cluster.getPosition();
                    aida.cloud2D("centroid x vs y").fill(pos[0], pos[1]);
                    aida.cloud1D("centroid radius").fill( sqrt(pos[0]*pos[0] + pos[1]*pos[1]) );
                    //
                    // reset measurement vector...
                    //
                    for(int i=0; i<_vals.length; ++i)
                    {
                        _vals[i]=0.;
                    }
                }// end over loop over clusters with greater tna minHits
            }// end of loop over clusters
            aida.cloud1D(det[j]+ "number of found clusters (above hits threshold)").fill(nGoodClusters);
        }// end of loop over collections
        event.put("photons",photons);
    }
    
    private double[] layerFractionalEnergies(Cluster clus)
    {
        //FIXME could reuse this array
        double[] layerEnergies = new double[_nLayers];
        double clusterEnergy = 0.;
        List<CalorimeterHit> hits = clus.getCalorimeterHits();
        for(CalorimeterHit hit : hits)
        {
            IDDecoder decoder = hit.getIDDecoder();
            decoder.setID(hit.getCellID());
            double e = hit.getCorrectedEnergy();
            int layer = decoder.getLayer();
//            System.out.println("layer "+layer+" energy "+e);
            clusterEnergy+=e;
            layerEnergies[layer]+=e;
        }
        for(int i=0; i<_nLayers; ++i)
        {
            layerEnergies[i]/=clusterEnergy;
//            System.out.println("i= "+i+" layerEnergies= "+layerEnergies[i]);
        }
//        System.out.println(clusterEnergy+" "+clus.getEnergy());
        return layerEnergies;
    }
    
    private double hottestCellEnergy(Cluster clus)
    {
        double hottestCellEnergy = 0.;
        List<CalorimeterHit> hits = clus.getCalorimeterHits();
//        System.out.println("New cluster with "+hits.size()+ " hits and energy "+clus.getEnergy());
        for(CalorimeterHit hit : hits)
        {
            double e = hit.getCorrectedEnergy();
            if(e>hottestCellEnergy) hottestCellEnergy=e;
        }
        
        return hottestCellEnergy;
    }
    
    protected void endOfData()
    {
        if (_task==HMatrixTask.BUILD)
        {
            _hmb.validate();
            _hmb.write(_fileLocation,commentForHMatrix());
        }
    }
    
    public void setHMatrixFileLocation(String filename)
    {
        _fileLocation = filename;
    }
    
    private String commentForHMatrix()
    {
        Calendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        DecimalFormat formatter = new DecimalFormat("00");
        String day = formatter.format(cal.get(Calendar.DAY_OF_MONTH));
        String month =  formatter.format(cal.get(Calendar.MONTH)+1);
        String myDate =cal.get(Calendar.YEAR)+month+day;
        return _detName+" "+myDate+" "+System.getProperty("user.name");
    }
}
