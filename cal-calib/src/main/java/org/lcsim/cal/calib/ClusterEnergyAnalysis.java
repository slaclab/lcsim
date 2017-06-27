/*
 * ClusterEnergyAnalysis.java
 *
 * Created on July 14, 2008, 6:18 PM
 *
 * $Id: ClusterEnergyAnalysis.java,v 1.11 2008/07/23 16:16:37 ngraf Exp $
 */

package org.lcsim.cal.calib;

import hep.aida.IAnalysisFactory;
import hep.aida.ICloud1D;
import hep.aida.IDataPoint;
import hep.aida.IDataPointSet;
import hep.aida.IDataPointSetFactory;
import hep.aida.IFitFactory;
import hep.aida.IFitResult;
import hep.aida.IFitter;
import hep.aida.IFunction;
import hep.aida.IFunctionFactory;
import hep.aida.IHistogram1D;
import hep.aida.IPlotter;
import hep.aida.IPlotterStyle;
import hep.aida.ITree;
import hep.physics.vec.Hep3Vector;
import java.io.IOException;
import java.util.List;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer.FixedConeDistanceMetric;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import static java.lang.Math.sin;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.lcsim.event.MCParticle;

/**
 *
 * @author Norman Graf
 */
public class ClusterEnergyAnalysis extends Driver
{
    private ConditionsSet _cond;
    private FixedConeClusterer _fcc;
    private CollectionManager _collectionmanager = CollectionManager.defaultInstance();
    
    private double[] _ecalLayering;
    boolean _useFirstLayer;
    
    private double emCalInnerRadius = 0.;
    private double emCalInnerZ = 0.;
    
    private Map<String, Double> _fitParameters = new HashMap<String, Double>();
    
    
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree;
    
    private boolean _initialized;
    /** Creates a new instance of ClusterEnergyAnalysis */
    public ClusterEnergyAnalysis()
    {
        _tree = aida.tree();
    }
    
    protected void process(EventHeader event)
    {
        if(!_initialized)
        {
            ConditionsManager mgr = ConditionsManager.defaultInstance();
            try
            {
                _cond = mgr.getConditions("CalorimeterCalibration");
            }
            catch(ConditionsSetNotFoundException e)
            {
                System.out.println("ConditionSet CalorimeterCalibration not found for detector "+mgr.getDetector());
                System.out.println("Please check that this properties file exists for this detector ");
            }
            double radius = .5;
            double seed = 0.;//.1;
            double minE = .05; //.25;
            _fcc = new FixedConeClusterer(radius, seed, minE, FixedConeDistanceMetric.DPHIDTHETA);
            
            _ecalLayering = _cond.getDoubleArray("ECalLayering");
            _useFirstLayer = _cond.getDouble("IsFirstEmLayerSampling")==1.;
            
            // photons
            String photonFitParametersList = _cond.getString("PhotonFitParameters");
            String[]  photonFitParameters = photonFitParametersList.split(",\\s");
            for(int i=0; i<photonFitParameters.length; ++i)
            {
                _fitParameters.put(photonFitParameters[i], _cond.getDouble(photonFitParameters[i]));
            }
            // neutral hadrons
            String hadronFitParametersList = _cond.getString("NeutralHadronFitParameters");
            String[]  hadronFitParameters = hadronFitParametersList.split(",\\s");
            for(int i=0; i<hadronFitParameters.length; ++i)
            {
                _fitParameters.put(hadronFitParameters[i], _cond.getDouble(hadronFitParameters[i]));
            }
            
            _initialized = true;
        }
        
        if(event.getEventNumber()%1000==0) System.out.println("Event "+event.getEventNumber());
        
        //start processing the event.
        // organize the histogram tree by species and energy
        List<MCParticle> mcparts = event.getMCParticles();
        MCParticle mcpart = mcparts.get(mcparts.size()-1);
        String particleType = mcpart.getType().getName();
        double mcEnergy = mcpart.getEnergy();
        long mcIntegerEnergy = Math.round(mcEnergy);
        boolean meV = false;
        if(mcEnergy<.99)
        {
            mcIntegerEnergy = Math.round(mcEnergy*1000);
            meV = true;
        }
        
        // TODO: make this cluster type selection more robust
        String type = "gamma";
        if(!particleType.equals("gamma")) type = "neutralHadron";
        
        
//        _tree.mkdirs(particleType);
//        _tree.cd(particleType);
        _tree.mkdirs(type);
        _tree.cd(type);
        _tree.mkdirs(mcIntegerEnergy+(meV ? "_MeV": "_GeV"));
        _tree.cd(mcIntegerEnergy+(meV ? "_MeV": "_GeV"));
        
        
        // this analysis is intended for single particle calorimeter response.
        // let's make sure that the primary particle did not interact in the
        // tracker...
        Hep3Vector endpoint = mcpart.getEndPoint();
        // this is just crap. Why not use SpacePoint?
        double radius = sqrt(endpoint.x()*endpoint.x()+endpoint.y()*endpoint.y());
        double z = endpoint.z();
        
        boolean doit = true;
        if(radius<emCalInnerRadius && abs(z) < emCalInnerZ) doit = false;
        if(doit)
        {
            String processedHitsName = _cond.getString("ProcessedHitsCollectionName");
            List<CalorimeterHit> hitsToCluster = _collectionmanager.getList(processedHitsName);
            // cluster the hits
            List<Cluster> clusters = _fcc.createClusters(hitsToCluster);
            
            
            // simple sanity check to make sure things don't go awry
            for(Cluster c : clusters)
            {
                aida.cloud1D("uncorrected cluster energy for all clusters").fill(c.getEnergy());
                double e = correctClusterEnergy(c, type);
                aida.cloud1D("corrected cluster energy for all clusters").fill(e);
            }
            
            // for simplicity proceed only with the highest energy cluster...
            if(clusters.size()>0)
            {
                Cluster c = clusters.get(0);
                aida.cloud1D("uncorrected cluster energy for highest energy cluster").fill(c.getEnergy());
                double e = correctClusterEnergy(c, type);
                aida.cloud1D("corrected cluster energy for highest energy cluster").fill(e);
//                SpacePoint p = new CartesianPoint(c.getPosition());
//                double clusterTheta = p.theta();
//                aida.cloud2D("uncorrected cluster energy for all clusters vs theta").fill(clusterTheta, c.getEnergy());
//                aida.cloud2D("corrected cluster energy for all clusters vs theta").fill(clusterTheta, e);
//                aida.cloud2D("raw-corrected cluster energy for all clusters vs theta").fill(clusterTheta, c.getEnergy()-e);
            }
            
        }// end of check on decays outside tracker volume
        //reset our histogram tree
        _tree.cd("/");
        
    }
    
    
    
    
//    protected void endOfData()
//    {
//        boolean showPlots = false;
//        String fileType = "png";
//        String[] pars = {"amplitude", "mean","sigma"};
//        //      int[] intEnergy = {1, 2, 5, 10, 20, 50, 100,  500 };
////        String fileFullPath   = "C:/orglcsimAnalyses/SamplingFractionAnalysis_gamma_Theta90_acme0605.aida";
////        if(args.length>0) fileFullPath = args[0];
////        if(args.length>1)
////        {
////            fileType = args[1];
////            showPlots = false;
////        }
//        IAnalysisFactory af = IAnalysisFactory.create();
////        ITree tree = af.createTreeFactory().create(fileFullPath,"xml", true, false);
//        System.out.println("calling ls on tree");
//        // TODO find out if I can capture output of ls command to get a list
//        // of available directories...
//        _tree.ls("./gamma");
//        String[] onames = _tree.listObjectNames("./gamma");
//
//        sortDirectoriesByEnergy(onames);
//
//        int numberOfPoints = onames.length;
//        double[] energies = new double[onames.length];
//        for(int j=0; j<onames.length; ++j)
//        {
////            System.out.println(onames[j]);
//            String subDir = onames[j].substring(8); // length of "./gamma/" is 8, so remove leading directory
//            StringTokenizer st = new StringTokenizer(subDir,"_");
//            String e = st.nextToken();
//            String unit = st.nextToken();
////            System.out.println(e+" "+unit);
//            energies[j] = Double.parseDouble(e);
//            if(unit.contains("MeV")) energies[j]/=1000.;
////            System.out.println("energy: "+energies[j]);
//        }
//        IFunctionFactory  functionFactory = af.createFunctionFactory(_tree);
//        IFitFactory       fitFactory = af.createFitFactory();
//        IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");
//        IFunction gauss = functionFactory.createFunctionByName("gauss","G");
//        IFunction line = functionFactory.createFunctionByName("line","P1");
//        IDataPointSetFactory dpsf = af.createDataPointSetFactory(_tree);
//
//        IPlotter plotter = af.createPlotterFactory().create("sampling fraction plot");
//        plotter.createRegions(3, 4, 0);
//        IPlotterStyle style2 = plotter.region(7).style();
//        style2.legendBoxStyle().setVisible(false);
//        style2.statisticsBoxStyle().setVisible(false);
//
//
//        IPlotterStyle style;
//
//        double[] fitMeans = new double[numberOfPoints];
//        double[] fitSigmas = new double[numberOfPoints];
//        IDataPointSet energyMeans = dpsf.create("energy means vs E",2);
//        IDataPointSet energySigmas = dpsf.create("sigma \\/ E vs E",2);
//        IDataPointSet resolutionFit = dpsf.create("sigma \\/  E vs 1 \\/ \u221a E",2);
//        IDataPointSet energyResiduals = dpsf.create("energy residuals (%) vs E",2);
//        double eMax = 0;
//        for(int i=0; i< numberOfPoints; ++i)
//        {
//            if(energies[i] > .1) // do not analyze 100MeV and below...
//            {
//                System.out.println("Energy "+energies[i]);
//
//                ICloud1D e = (ICloud1D) _tree.find(onames[i]+"corrected cluster energy for highest energy cluster");
//                e.convertToHistogram();
//                IHistogram1D eHist = e.histogram();
//                gauss.setParameter("amplitude",eHist.maxBinHeight());
//                gauss.setParameter("mean",eHist.mean());
//                gauss.setParameter("sigma",eHist.rms());
//                style = plotter.region(i).style();
//                style.legendBoxStyle().setVisible(false);
//                style.statisticsBoxStyle().setVisible(false);
//                double loElimit = energies[i] - .6*sqrt(energies[i]); // expect ~20% resolution, and go out 3 sigma
//                double hiElimit = energies[i] + .6*sqrt(energies[i]);;
//                plotter.region(i).setXLimits(loElimit, hiElimit);
//                plotter.region(i).plot(eHist);
//                IFitResult jminuitResult = jminuit.fit(eHist,gauss);
//                double[] fitErrors = jminuitResult.errors();
//                IFunction fit = jminuitResult.fittedFunction();
//                for(int j=0; j<pars.length; ++j)
//                {
//                    System.out.println("   "+pars[j]+": "+ fit.parameter(pars[j])+" +/- "+fitErrors[j]);
//                }
//                fitMeans[i] = fit.parameter("mean");
//                fitSigmas[i] = fit.parameter("sigma");
//                plotter.region(i).plot(fit);
////            plotter.region(7).plot(eHist);
//
//                // the means
//                IDataPoint point = energyMeans.addPoint();
//                point.coordinate(0).setValue(energies[i]);
//                point.coordinate(1).setValue(fitMeans[i]);
//                point.coordinate(1).setErrorPlus(fitErrors[1]);
//                point.coordinate(1).setErrorMinus(fitErrors[1]);
//
//                // sigma
//                IDataPoint point1 = energySigmas.addPoint();
//                point1.coordinate(0).setValue(energies[i]);
//                point1.coordinate(1).setValue(fitSigmas[i]/energies[i]);
//                point1.coordinate(1).setErrorPlus(fitErrors[2]/energies[i]);
//                point1.coordinate(1).setErrorMinus(fitErrors[2]/energies[i]);
//
//                // sigma/E vs 1/sqrt(E)
//
//                IDataPoint point3 = resolutionFit.addPoint();
//                point3.coordinate(0).setValue(1./sqrt(energies[i]));
//                point3.coordinate(1).setValue(fitSigmas[i]/energies[i]);
//                point3.coordinate(1).setErrorPlus(fitErrors[2]/energies[i]);
//                point3.coordinate(1).setErrorMinus(fitErrors[2]/energies[i]);
//
//                // residuals
//                IDataPoint point2 = energyResiduals.addPoint();
//                point2.coordinate(0).setValue(energies[i]);
//                point2.coordinate(1).setValue(100.*(fitMeans[i]-energies[i])/energies[i]);
//
//                // axis bookeeping...
//                if(energies[i] > eMax) eMax = energies[i];
//            } // end of 100 MeV cut
//        }
//
//
//
//        IPlotter results = af.createPlotterFactory().create("linearity");
//        style = results.region(0).style();
//        style.xAxisStyle().setLabel("MC Energy [GeV]");
//        style.yAxisStyle().setLabel("Cluster Energy [GeV]");
//        style.titleStyle().setVisible(false);
//        style.statisticsBoxStyle().setVisibileStatistics("011");
//        style.legendBoxStyle().setVisible(false);
//        IFitResult fitLine = jminuit.fit(energyMeans, line);
//        System.out.println(" fit status: "+fitLine.fitStatus());
//        double eMaxBin = eMax+10.;
//        results.region(0).setXLimits(0., eMaxBin);
//        results.region(0).setYLimits(0., eMaxBin);
//        results.region(0).plot(energyMeans);
//        results.region(0).plot(fitLine.fittedFunction());
//
//
//        IPlotter resolution = af.createPlotterFactory().create("resolution");
//        style = resolution.region(0).style();
//        style.xAxisStyle().setLabel("Energy [GeV]");
//        style.yAxisStyle().setLabel("sigma/E");
//        style.titleStyle().setVisible(false);
//        style.statisticsBoxStyle().setVisible(false);
//        style.legendBoxStyle().setVisible(false);
//        resolution.region(0).setXLimits(0., eMaxBin);
//        resolution.region(0).setYLimits(0., .2);
//        resolution.region(0).plot(energySigmas);
//
//
//        IPlotter resolution2 = af.createPlotterFactory().create("sigma/E vs 1/E");
//        style = resolution2.region(0).style();
//        style.xAxisStyle().setLabel("1/ \u221a Energy [1/GeV]");
//        style.yAxisStyle().setLabel("sigma/E");
////        style.statisticsBoxStyle().setVisibileStatistics("011");
//        style.legendBoxStyle().setVisible(false);
//        IFitResult resFitLine = jminuit.fit(resolutionFit, line);
//        System.out.println(" fit status: "+resFitLine.fitStatus());
////        resolution2.region(0).setXLimits(0., 1.05);
////        resolution2.region(0).setYLimits(0., .2);
//        resolution2.region(0).plot(resolutionFit);
//        resolution2.region(0).plot(resFitLine.fittedFunction());
//
//        IPlotter residuals = af.createPlotterFactory().create("residuals (%)");
//        style = residuals.region(0).style();
//        style.xAxisStyle().setLabel("Energy [GeV]");
//        style.yAxisStyle().setLabel("Residuals [%]");
//        style.statisticsBoxStyle().setVisible(false);
//        style.titleStyle().setVisible(false);
//
//        residuals.region(0).setXLimits(0., eMaxBin);
//
//        residuals.region(0).plot(energyResiduals);
//
//        if(showPlots)
//        {
//            plotter.show();
//            results.show();
//            resolution.show();
//            resolution2.show();
//            residuals.show();
//        }
//        else
//        {
//            try
//            {
//                plotter.writeToFile("energyPlots."+fileType,fileType);
//                results.writeToFile("linearity."+fileType,fileType);
//                resolution.writeToFile("resolution."+fileType,fileType);
//                resolution2.writeToFile("resolutionLinear."+fileType,fileType);
//                residuals.writeToFile("residuals."+fileType,fileType);
//            }
//            catch(IOException e)
//            {
//                System.out.println("problem writing out hardcopy in "+fileType+" format");
//                e.printStackTrace();
//            }
//        }
//    }
    
    
    
    private double correctClusterEnergy(Cluster c, String type)
    {
        double e = 0.;
        // brute force for the time being
        // in the future we will either move this to the sampling fraction corrections
        // or use the cluster direction for calculating the angle effect.
        List<CalorimeterHit> hits = c.getCalorimeterHits();
        for (CalorimeterHit hit : hits)
        {
            Subdetector det = hit.getSubdetector();
            String detName = det.getName();
//            System.out.println(detName);
            boolean isEM = detName.startsWith("EM");
            boolean isEndcap = det.isEndcap();
            IDDecoder decoder = hit.getIDDecoder();
            decoder.setID(hit.getCellID());
            int layer = decoder.getLayer();
            int caltype = 0;
            if(isEM)
            {
                for(int i=1; i<_ecalLayering.length+1; ++i)
                {
                    if(layer >= _ecalLayering[i-1]) caltype = i-1;
                }
            }
            //TODO change this when had cal layering changes...
            else
            {
                caltype  = 3;
            }
//            System.out.println("layer= "+layer+" caltype= "+caltype);
            String name = type + "_" +(isEM ? "em"+caltype : "had") + (isEndcap ? "e" : "b");
//            System.out.println("fit parameter name "+name);
            SpacePoint p = new CartesianPoint(hit.getPosition());
            double hitTheta = p.theta();
            double normalTheta = hitTheta;
            // now calculate normal to the sampling plane
            if(isEndcap)
            {
                normalTheta -= PI/2.;
                normalTheta = abs(normalTheta);
            }
            else
            {
                normalTheta -= PI;
            }
            double a = 0.;
            double b = 0.;
            if(caltype==0 && !_useFirstLayer)
            {
                a = 0.;
                b = 1.;
            }
            else
            {
                a = _fitParameters.get(name+"_0");
                b = _fitParameters.get(name+"_1");
            }
            
            double correctionFactor = a + b*sin(normalTheta);
//            aida.cloud2D(name+" "+layer+"hit Theta vs correction factor ").fill(hitTheta, correctionFactor);
//            aida.cloud2D(name+" hit Theta vs correction factor ").fill(hitTheta, correctionFactor);
//            if(isEM) aida.cloud2D("EM layer vs caltype").fill(layer, caltype);
            
            // now apply the correction
            
            e += hit.getRawEnergy()/correctionFactor;
        }
        return e;
    }
    
//    private static void sortDirectoriesByEnergy(String[] s)
//    {
//        Map<Double, String> map = new HashMap<Double, String>();
//        double[] energies = new double[s.length];
//        for(int j=0; j<s.length; ++j)
//        {
////            System.out.println(onames[j]);
//            String subDir = s[j].substring(8); // length of "./gamma/" is 8, so remove leading directory
//            StringTokenizer st = new StringTokenizer(subDir,"_");
//            String e = st.nextToken();
//            String unit = st.nextToken();
////            System.out.println(e+" "+unit);
//            energies[j] = Double.parseDouble(e);
//            if(unit.contains("MeV")) energies[j]/=1000.;
//            map.put(energies[j], s[j]);
////            System.out.println("energy: "+energies[j]);
//        }
//        Arrays.sort(energies);
//        for(int j=0; j<s.length; ++j)
//        {
//            s[j] = map.get(energies[j]);
//        }
//        for(int j=0; j<s.length; ++j)
//        {
//            System.out.println(s[j]);
//        }
//
//
//
//    }
    
    protected void endOfData()
    {
        boolean showPlots = false;
        boolean doit = false;
        if(doit)
        {
            String fileType = "png";
            String[] pars = {"amplitude", "mean","sigma"};
            
            IAnalysisFactory af = IAnalysisFactory.create();
            String[] dirs = _tree.listObjectNames(".");
            for (int ii=0; ii<dirs.length; ++ii)
            {
//            System.out.println("dirs["+i+"]= "+dirs[i]);
                String[] parts = dirs[ii].split("/");
//            for(int k=0; k<parts.length; ++k)
//            {
//                System.out.println("parts["+k+"]= "+parts[k]);
//            }
                _tree.cd(dirs[ii]);
                String[] objects = _tree.listObjectNames(".");
                
//            for(int j=0; j<objects.length;++j)
//            {
//                System.out.println("obj["+j+"]= "+objects[i]);
//            }
                
                sortDirectoriesByEnergy(objects);
                
                int numberOfPoints = objects.length;
                double[] energies = new double[objects.length];
                for(int j=0; j<objects.length; ++j)
                {
//                System.out.println(objects[j]);
                    
                    String subDir =parts[1];
                    String[] st = objects[j].split("/")[1].split("_");
                    String e = st[0];
                    String unit = st[1];
////            System.out.println(e+" "+unit);
                    energies[j] = Double.parseDouble(e);
                    if(unit.contains("MeV")) energies[j]/=1000.;
//                System.out.println("energy: "+energies[j]);
                }
                IFunctionFactory  functionFactory = af.createFunctionFactory(_tree);
                IFitFactory       fitFactory = af.createFitFactory();
                IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");
                IFunction gauss = functionFactory.createFunctionByName("gauss","G");
                IFunction line = functionFactory.createFunctionByName("line","P1");
                IDataPointSetFactory dpsf = af.createDataPointSetFactory(_tree);
                
                IPlotter plotter = af.createPlotterFactory().create("sampling fraction plot");
                plotter.createRegions(3, 4, 0);
                IPlotterStyle style2 = plotter.region(7).style();
                style2.legendBoxStyle().setVisible(false);
                style2.statisticsBoxStyle().setVisible(false);
                
                
                IPlotterStyle style;
                
                double[] fitMeans = new double[numberOfPoints];
                double[] fitSigmas = new double[numberOfPoints];
                IDataPointSet energyMeans = dpsf.create("energy means vs E",2);
                IDataPointSet energySigmas = dpsf.create("sigma \\/ E vs E",2);
                IDataPointSet resolutionFit = dpsf.create("sigma \\/  E vs 1 \\/ \u221a E",2);
                IDataPointSet energyResiduals = dpsf.create("energy residuals (%) vs E",2);
                double eMax = 0;
                for(int i=0; i< numberOfPoints; ++i)
                {
                    if(energies[i] > .1) // do not analyze 100MeV and below...
                    {
                        System.out.println("Energy "+energies[i]);
                        
                        ICloud1D e = (ICloud1D) _tree.find(objects[i]+"corrected cluster energy for all clusters");
                        if(!e.isConverted()) e.convertToHistogram();
                        IHistogram1D eHist = e.histogram();
                        gauss.setParameter("amplitude",eHist.maxBinHeight());
                        gauss.setParameter("mean",eHist.mean());
                        gauss.setParameter("sigma",eHist.rms());
                        style = plotter.region(i).style();
                        style.legendBoxStyle().setVisible(false);
                        style.statisticsBoxStyle().setVisible(false);
                        double loElimit = energies[i] - .6*sqrt(energies[i]); // expect ~20% resolution, and go out 3 sigma
                        double hiElimit = energies[i] + .6*sqrt(energies[i]);;
                        plotter.region(i).setXLimits(loElimit, hiElimit);
                        plotter.region(i).plot(eHist);
                        IFitResult jminuitResult = jminuit.fit(eHist,gauss);
                        double[] fitErrors = jminuitResult.errors();
                        IFunction fit = jminuitResult.fittedFunction();
                        for(int j=0; j<pars.length; ++j)
                        {
                            System.out.println("   "+pars[j]+": "+ fit.parameter(pars[j])+" +/- "+fitErrors[j]);
                        }
                        fitMeans[i] = fit.parameter("mean");
                        fitSigmas[i] = fit.parameter("sigma");
                        plotter.region(i).plot(fit);
//            plotter.region(7).plot(eHist);
                        
                        // the means
                        IDataPoint point = energyMeans.addPoint();
                        point.coordinate(0).setValue(energies[i]);
                        point.coordinate(1).setValue(fitMeans[i]);
                        point.coordinate(1).setErrorPlus(fitErrors[1]);
                        point.coordinate(1).setErrorMinus(fitErrors[1]);
                        
                        // sigma
                        IDataPoint point1 = energySigmas.addPoint();
                        point1.coordinate(0).setValue(energies[i]);
                        point1.coordinate(1).setValue(fitSigmas[i]/energies[i]);
                        point1.coordinate(1).setErrorPlus(fitErrors[2]/energies[i]);
                        point1.coordinate(1).setErrorMinus(fitErrors[2]/energies[i]);
                        
                        // sigma/E vs 1/sqrt(E)
                        
                        IDataPoint point3 = resolutionFit.addPoint();
                        point3.coordinate(0).setValue(1./sqrt(energies[i]));
                        point3.coordinate(1).setValue(fitSigmas[i]/energies[i]);
                        point3.coordinate(1).setErrorPlus(fitErrors[2]/energies[i]);
                        point3.coordinate(1).setErrorMinus(fitErrors[2]/energies[i]);
                        
                        // residuals
                        IDataPoint point2 = energyResiduals.addPoint();
                        point2.coordinate(0).setValue(energies[i]);
                        point2.coordinate(1).setValue(100.*(fitMeans[i]-energies[i])/energies[i]);
                        
                        // axis bookeeping...
                        if(energies[i] > eMax) eMax = energies[i];
                    } // end of 100 MeV cut
                }
                
                IPlotter results = af.createPlotterFactory().create("linearity");
                style = results.region(0).style();
                style.xAxisStyle().setLabel("MC Energy [GeV]");
                style.yAxisStyle().setLabel("Cluster Energy [GeV]");
                style.titleStyle().setVisible(false);
                style.statisticsBoxStyle().setVisibileStatistics("011");
                style.legendBoxStyle().setVisible(true);
                IFitResult fitLine = jminuit.fit(energyMeans, line);
                System.out.println(" fit status: "+fitLine.fitStatus());
                double eMaxBin = eMax+10.;
                results.region(0).setXLimits(0., eMaxBin);
                results.region(0).setYLimits(0., eMaxBin);
                results.region(0).plot(energyMeans);
                results.region(0).plot(fitLine.fittedFunction());
                
                
                IPlotter resolution = af.createPlotterFactory().create("resolution");
                style = resolution.region(0).style();
                style.xAxisStyle().setLabel("Energy [GeV]");
                style.yAxisStyle().setLabel("sigma/E");
                style.titleStyle().setVisible(false);
                style.statisticsBoxStyle().setVisible(false);
                style.legendBoxStyle().setVisible(false);
                resolution.region(0).setXLimits(0., eMaxBin);
                resolution.region(0).setYLimits(0., .2);
                resolution.region(0).plot(energySigmas);
                
                
                IPlotter resolution2 = af.createPlotterFactory().create("sigma/E vs 1/E");
                style = resolution2.region(0).style();
                style.xAxisStyle().setLabel("1/ \u221a Energy [1/GeV]");
                style.yAxisStyle().setLabel("sigma/E");
//        style.statisticsBoxStyle().setVisibileStatistics("011");
                style.legendBoxStyle().setVisible(false);
                IFitResult resFitLine = jminuit.fit(resolutionFit, line);
                System.out.println(" fit status: "+resFitLine.fitStatus());
//        resolution2.region(0).setXLimits(0., 1.05);
//        resolution2.region(0).setYLimits(0., .2);
                resolution2.region(0).plot(resolutionFit);
                resolution2.region(0).plot(resFitLine.fittedFunction());
                
                IPlotter residuals = af.createPlotterFactory().create("residuals (%)");
                style = residuals.region(0).style();
                style.xAxisStyle().setLabel("Energy [GeV]");
                style.yAxisStyle().setLabel("Residuals [%]");
                style.statisticsBoxStyle().setVisible(false);
                style.titleStyle().setVisible(false);
                
                residuals.region(0).setXLimits(0., eMaxBin);
                
                residuals.region(0).plot(energyResiduals);
                
                if(showPlots)
                {
                    plotter.show();
                    results.show();
                    resolution.show();
                    resolution2.show();
                    residuals.show();
                }
                else
                {
                    try
                    {
                        // hardcopy
                        plotter.writeToFile("energyPlots."+fileType,fileType);
                        results.writeToFile("linearity."+fileType,fileType);
                        resolution.writeToFile("resolution."+fileType,fileType);
                        resolution2.writeToFile("resolutionLinear."+fileType,fileType);
                        residuals.writeToFile("residuals."+fileType,fileType);
                    }
                    catch(IOException e)
                    {
                        System.out.println("problem writing out hardcopy in "+fileType+" format");
                        e.printStackTrace();
                    }
                    
                }
            }// end of loop over directories
        }
    }
    
    private static void sortDirectoriesByEnergy(String[] s)
    {
        Map<Double, String> map = new HashMap<Double, String>();
        double[] energies = new double[s.length];
        for(int j=0; j<s.length; ++j)
        {
//           System.out.println(s[j]);
            String subDir = s[j].split("/")[1]; // first token should be "."
//            System.out.println(subDir);
            String[] st = subDir.split("_");
            String e = st[0];
            String unit = st[1];
//            System.out.println(e+" "+unit);
            energies[j] = Double.parseDouble(e);
            if(unit.contains("MeV")) energies[j]/=1000.;
            map.put(energies[j], s[j]);
//            System.out.println("energy: "+energies[j]);
        }
        Arrays.sort(energies);
        for(int j=0; j<s.length; ++j)
        {
            s[j] = map.get(energies[j]);
        }
//        for(int j=0; j<s.length; ++j)
//        {
//            System.out.println(s[j]);
//        }
    }
    
}
