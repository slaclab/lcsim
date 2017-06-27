package org.lcsim.cal.calib;
/*
 * ClusterEnergyAnalysisHistogramFitter.java
 *
 * Created on October 19, 2006, 12:46 PM
 *
 * $Id: ClusterEnergyAnalysisHistogramFitter.java,v 1.1 2008/07/30 22:10:10 ngraf Exp $
 */
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
import hep.aida.IPlotterFactory;
import hep.aida.IPlotterStyle;
import hep.aida.ITree;
import static java.lang.Math.sqrt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Norman Graf
 */
public class ClusterEnergyAnalysisHistogramFitter
{
    
    public static void main(String[] args) throws Exception
    {
        boolean showPlots = true;
        String fileType = "png";
        String[] pars = {"amplitude", "mean","sigma"};
        //      int[] intEnergy = {1, 2, 5, 10, 20, 50, 100,  500 };
//        String fileFullPath   = "C:/orglcsimAnalyses/SamplingFractionAnalysis_gamma_Theta90_acme0605.aida";
//        String fileFullPath   = "C:/lcsim/gammaTheta90_Runem_20080722.aida";
  
        String fileFullPath = null;
        if(args.length == 0)
        {
            System.out.println("Please provide name of aida file to be analyzed");
            return;
        }
        if(args.length>0) fileFullPath = args[0];
        if(args.length>1)
        {
            fileType = args[1];
            showPlots = false;
        }
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = af.createTreeFactory().create(fileFullPath,"xml", true, false);
        
        String[] dirs = tree.listObjectNames(".");
        for (int ii=0; ii<dirs.length; ++ii)
        {
//            System.out.println("dirs["+i+"]= "+dirs[i]);
            String[] parts = dirs[ii].split("/");
//            for(int k=0; k<parts.length; ++k)
//            {
//                System.out.println("parts["+k+"]= "+parts[k]);
//            }
            tree.cd(dirs[ii]);
            String[] objects = tree.listObjectNames(".");
            
//            for(int j=0; j<objects.length;++j)
//            {
//                System.out.println("obj["+j+"]= "+objects[i]);
//            }
            
            sortDirectoriesByEnergy(objects);
            
            // standard style for fitted functions
            IPlotterFactory pf = af.createPlotterFactory();
            IPlotterStyle fitStyle = pf.createPlotterStyle();
            fitStyle.dataStyle().outlineStyle().setVisible(true);
            fitStyle.dataStyle().outlineStyle().setColor("BLACK");
            fitStyle.dataStyle().outlineStyle().setThickness(5);
            
            // standard style for histogram data
            IPlotterStyle dataStyle = af.createPlotterFactory().createPlotterStyle();
            dataStyle.dataStyle().lineStyle().setVisible(true);
            dataStyle.dataStyle().lineStyle().setColor("BLACK");
            dataStyle.dataStyle().fillStyle().setColor("RED");
            dataStyle.dataStyle().fillStyle().setVisible(true);
            
            // standard style for point data
            IPlotterStyle pointStyle = af.createPlotterFactory().createPlotterStyle();
            pointStyle.dataStyle().markerStyle().setColor("RED");
            pointStyle.dataStyle().errorBarStyle().setColor("RED");
//            pointStyle.dataStyle().lineStyle().setVisible(true);
            pointStyle.dataStyle().outlineStyle().setColor("BLACK");
//            pointStyle.dataStyle().lineStyle().setColor("RED");
//            pointStyle.dataStyle().fillStyle().setVisible(false);
            
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
            IFunctionFactory  functionFactory = af.createFunctionFactory(tree);
            IFitFactory       fitFactory = af.createFitFactory();
            IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");
            IFunction gauss = functionFactory.createFunctionByName("gauss","G");
            IFunction line = functionFactory.createFunctionByName("line","P1");
            IDataPointSetFactory dpsf = af.createDataPointSetFactory(tree);
            
            IPlotter plotter = af.createPlotterFactory().create("sampling fraction plot");
            plotter.createRegions(3, 4, 0);
            IPlotterStyle style2 = plotter.region(7).style();
            style2.legendBoxStyle().setVisible(false);
            style2.statisticsBoxStyle().setVisible(false);
            
            style2.dataStyle().fillStyle().setColor("RED");
            
            IPlotterStyle style;
            
            double[] fitMeans = new double[numberOfPoints];
            double[] fitSigmas = new double[numberOfPoints];
            IDataPointSet energyMeans = dpsf.create("energy means vs E",2);
            IDataPointSet energySigmas = dpsf.create("sigma \\/ E vs E",2);
            IDataPointSet resolutionFit = dpsf.create("sigma \\/  E vs 1 \\/ \u221a E",2);
            IDataPointSet energyResiduals = dpsf.create("energy residuals vs E",2);
            IDataPointSet energyResidualsPercent = dpsf.create("energy residuals (%) vs E",2);
            double eMax = 0;
            double eSigma = 0.8;
            if(dirs[ii].contains("gamma")) eSigma = 0.2;
            for(int i=0; i< numberOfPoints; ++i)
            {
                if(energies[i] > .1) // do not analyze 100MeV and below...
                {
                    System.out.println("Energy "+energies[i]);
                    
                    ICloud1D e = (ICloud1D) tree.find(objects[i]+"corrected cluster energy for highest energy cluster");//"corrected cluster energy");
                    if(!e.isConverted()) e.convertToHistogram();
                    IHistogram1D eHist = e.histogram();
                    gauss.setParameter("amplitude",eHist.maxBinHeight());
                    gauss.setParameter("mean",eHist.mean());
                    gauss.setParameter("sigma",eHist.rms());
                    style = plotter.region(i).style();
                    style.legendBoxStyle().setVisible(false);
                    style.statisticsBoxStyle().setVisible(false);
                    style.dataStyle().fillStyle().setColor("RED");
                    double loElimit = energies[i] - 3*eSigma*sqrt(energies[i]); // expect ~20% resolution, and go out 3 sigma
                    double hiElimit = energies[i] + 3*eSigma*sqrt(energies[i]);;
                    plotter.region(i).setXLimits(loElimit, hiElimit);
                    plotter.region(i).plot(eHist, dataStyle);
                    IFitResult jminuitResult = jminuit.fit(eHist,gauss);
                    double[] fitErrors = jminuitResult.errors();
                    IFunction fit = jminuitResult.fittedFunction();
                    for(int j=0; j<pars.length; ++j)
                    {
                        System.out.println("   "+pars[j]+": "+ fit.parameter(pars[j])+" +/- "+fitErrors[j]);
                    }
                    fitMeans[i] = fit.parameter("mean");
                    fitSigmas[i] = fit.parameter("sigma");
                    plotter.region(i).plot(fit, fitStyle);
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
                    
                    // residualsPercent %
                    IDataPoint point2 = energyResidualsPercent.addPoint();
                    point2.coordinate(0).setValue(energies[i]);
                    point2.coordinate(1).setValue(100.*(fitMeans[i]-energies[i])/energies[i]);
                    
                    // residualsPercent [GeV]
                    IDataPoint point4 = energyResiduals.addPoint();
                    point4.coordinate(0).setValue(energies[i]);
                    point4.coordinate(1).setValue(fitMeans[i]-energies[i]);
                    
                    // axis bookeeping...
                    if(energies[i] > eMax) eMax = energies[i];
                } // end of 100 MeV cut
            }
            
            
            
            IPlotter results = af.createPlotterFactory().create("linearity");
            style = results.region(0).style();
            style.xAxisStyle().setLabel("MC Energy [GeV]");
            style.yAxisStyle().setLabel("Cluster Energy [GeV]");
            style.titleStyle().setVisible(true);
//            style.statisticsBoxStyle().setVisibileStatistics("011");
            style.statisticsBoxStyle().setVisible(true);
            style.legendBoxStyle().setVisible(true);
            IFitResult fitLine = jminuit.fit(energyMeans, line);
            System.out.println(" fit status: "+fitLine.fitStatus());
            double eMaxBin = eMax+10.;
            results.region(0).setXLimits(0., eMaxBin);
            results.region(0).setYLimits(0., eMaxBin);
            results.region(0).plot(energyMeans, pointStyle);
            results.region(0).plot(fitLine.fittedFunction(),fitStyle);
            
            
            IPlotter resolution = af.createPlotterFactory().create("resolution");
            style = resolution.region(0).style();
            style.xAxisStyle().setLabel("Energy [GeV]");
            style.yAxisStyle().setLabel("sigma/E");
            style.titleStyle().setVisible(false);
            style.statisticsBoxStyle().setVisible(false);
            style.legendBoxStyle().setVisible(false);
            resolution.region(0).setXLimits(0., eMaxBin);
            resolution.region(0).setYLimits(0., eSigma);
            resolution.region(0).plot(energySigmas, pointStyle);
            
            
            IPlotter resolution2 = af.createPlotterFactory().create("sigma/E vs 1/E");
            style = resolution2.region(0).style();
            style.xAxisStyle().setLabel("1/ \u221a Energy [1/GeV]");
            style.yAxisStyle().setLabel("sigma/E");
            style.statisticsBoxStyle().setVisibileStatistics("011");
            style.statisticsBoxStyle().setVisible(true);
            style.legendBoxStyle().setVisible(true);
            IFitResult resFitLine = jminuit.fit(resolutionFit, line);
            System.out.println(" fit status: "+resFitLine.fitStatus());
            double[] resFitLinePars = resFitLine.fittedParameters();
            double[] resFitLineParErrors = resFitLine.errors();
            String[] resFitLineParNames = resFitLine.fittedParameterNames();
            
            System.out.println(" Energy Resolution Fit: ");
            for (int i=0; i< resFitLinePars.length; ++i)
            {
                System.out.println(resFitLineParNames[i]+" : "+resFitLinePars[i]+" +/- "+resFitLineParErrors[i]);
            }
//        resolution2.region(0).setXLimits(0., 1.05);
//        resolution2.region(0).setYLimits(0., .2);
            resolution2.region(0).plot(resolutionFit, pointStyle);
            resolution2.region(0).plot(resFitLine.fittedFunction(),fitStyle);
            
            // residuals
            IPlotter residuals = af.createPlotterFactory().create("residuals");
            style = residuals.region(0).style();
            style.xAxisStyle().setLabel("Energy [GeV]");
            style.yAxisStyle().setLabel("Residuals [GeV]");
            style.statisticsBoxStyle().setVisible(false);
            style.titleStyle().setVisible(false);
            
            residuals.region(0).setXLimits(0., eMaxBin);
            
            residuals.region(0).plot(energyResiduals, pointStyle);
            
            
            // residuals %
            
            IPlotter residualsPercent = af.createPlotterFactory().create("residuals (%)");
            style = residualsPercent.region(0).style();
            style.xAxisStyle().setLabel("Energy [GeV]");
            style.yAxisStyle().setLabel("Residuals [%]");
            style.statisticsBoxStyle().setVisible(false);
            style.titleStyle().setVisible(false);
            
            residualsPercent.region(0).setXLimits(0., eMaxBin);
            
            
            residualsPercent.region(0).plot(energyResidualsPercent, pointStyle);
            
            if(showPlots)
            {
                plotter.show();
                results.show();
                resolution.show();
                resolution2.show();
                residuals.show();
                residualsPercent.style().dataStyle().outlineStyle().setColor("BLACK");
                residualsPercent.show();
            }
            else
            {
                // hardcopy
                plotter.writeToFile("energyPlots."+fileType,fileType);
                results.writeToFile("linearity."+fileType,fileType);
                resolution.writeToFile("resolution."+fileType,fileType);
                resolution2.writeToFile("resolutionLinear."+fileType,fileType);
                residuals.writeToFile("residuals."+fileType,fileType);
                residualsPercent.writeToFile("residualsPercent."+fileType,fileType);
            }
        }// end of loop over directories
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
