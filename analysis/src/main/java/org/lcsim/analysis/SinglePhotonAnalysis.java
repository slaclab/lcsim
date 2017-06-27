package org.lcsim.analysis;

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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import static java.lang.Math.sqrt;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: SinglePhotonAnalysis.java,v 1.8 2010/06/17 22:22:48 ngraf Exp $
 */
public class SinglePhotonAnalysis extends Driver
{

    private double _expectedResolution;
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree = aida.tree();
    private boolean _showPlots = true;
    private String _fileType = "png";
    private boolean _writeOutAidaFile = false;
    private String _defaultAidaFileName = "test";
    private String _detectorName;
    private String _particleType;

    public SinglePhotonAnalysis()
    {
    }

    protected void process(EventHeader event)
    {
        _detectorName = event.getDetectorName();
        _tree.mkdirs(_detectorName);
        _tree.cd(_detectorName);
        // organize the histogram tree by species and energy
        List<MCParticle> mcparts = event.getMCParticles();
        MCParticle mcpart = mcparts.get(mcparts.size() - 1);
        _particleType = mcpart.getType().getName();
        double mcEnergy = mcpart.getEnergy();
        long mcIntegerEnergy = Math.round(mcEnergy);
        boolean meV = false;
        if (mcEnergy < .99)
        {
            mcIntegerEnergy = Math.round(mcEnergy * 1000);
            meV = true;
        }

        _tree.mkdirs(_particleType);
        _tree.cd(_particleType);
        _tree.mkdirs(mcIntegerEnergy + (meV ? "_MeV" : "_GeV"));
        _tree.cd(mcIntegerEnergy + (meV ? "_MeV" : "_GeV"));

        // this analysis is intended for single particle calorimeter response.
        // let's make sure that the primary particle did not interact in the
        // tracker...
        if (mcpart.getSimulatorStatus().isDecayedInCalorimeter())
        {
            List<ReconstructedParticle> rpCollection = event.get(ReconstructedParticle.class, "PandoraPFOCollection");
            // System.out.println("Found "+rpCollection.size()+" ReconstructedParticles");
            aida.cloud1D("Number of ReconstructedParticles found").fill(rpCollection.size());
            for (ReconstructedParticle p : rpCollection)
            {
                aida.cloud1D("Cluster Energy", 99999).fill(p.getEnergy()); //put off conversion to a binned histogram
                int id = p.getType();
                aida.cloud1D("Cluster Energy pid= " + id).fill(p.getEnergy());
            }
        }// end of check on whether particle interacted in tracker
        //reset our histogram tree
        _tree.cd("/");
    }

    @Override
    protected void endOfData()
    {
        String AidaFileName = _defaultAidaFileName + "_" + _detectorName + "_" + this._particleType+"_"+this.getClass().getSimpleName() + "_" + date() + ".aida";

        if (_writeOutAidaFile)
        {
            System.out.println("writing aida file to " + AidaFileName);


            try
            {
                AIDA.defaultInstance().saveAs(AidaFileName);


            } catch (IOException x)
            {
                System.out.println("Experienced an IOException during");
                x.printStackTrace();
                return;
            }
        }
        fitPlots();
    }

    private void fitPlots()
    {
        boolean doit = true;
        if (doit)
        {
            _tree.cd(_detectorName);
            String[] pars =
            {
                "amplitude", "mean", "sigma"
            };

            IAnalysisFactory af = IAnalysisFactory.create();
            String[] dirs = _tree.listObjectNames(".");
            for (int ii = 0; ii < dirs.length; ++ii)
            {
//                System.out.println("dirs[" + ii + "]= " + dirs[ii]);
                String[] parts = dirs[ii].split("/");
//                for (int k = 0; k < parts.length; ++k)
//                {
//                    System.out.println("parts[" + k + "]= " + parts[k]);
//                }
                _tree.cd(dirs[ii]);
                String[] objects = _tree.listObjectNames(".");

//                for (int j = 0; j < objects.length; ++j)
//                {
//                    System.out.println("obj[" + j + "]= " + objects[j]);
//                }

                sortDirectoriesByEnergy(objects);

                int numberOfPoints = objects.length;
                double[] energies = new double[objects.length];
                for (int j = 0; j < objects.length; ++j)
                {
//                System.out.println(objects[j]);

                    String subDir = parts[1];
                    String[] st = objects[j].split("/")[1].split("_");
                    String e = st[0];
                    String unit = st[1];
////            System.out.println(e+" "+unit);
                    energies[j] = Double.parseDouble(e);
                    if (unit.contains("MeV"))
                        energies[j] /= 1000.;
//                System.out.println("energy: "+energies[j]);
                }
                IFunctionFactory functionFactory = af.createFunctionFactory(_tree);
                IFitFactory fitFactory = af.createFitFactory();
                IFitter jminuit = fitFactory.createFitter("Chi2", "jminuit");
                IFunction gauss = functionFactory.createFunctionByName("gauss", "G");
                IFunction line = functionFactory.createFunctionByName("line", "P1");
                IDataPointSetFactory dpsf = af.createDataPointSetFactory(_tree);

                IPlotter plotter = af.createPlotterFactory().create("sampling fraction plot");
                plotter.createRegions(3, 4, 0);
                IPlotterStyle style2 = plotter.region(7).style();
                style2.legendBoxStyle().setVisible(false);
                style2.statisticsBoxStyle().setVisible(false);


                IPlotterStyle style;

                double[] fitMeans = new double[numberOfPoints];
                double[] fitSigmas = new double[numberOfPoints];
                IDataPointSet energyMeans = dpsf.create("energy means vs E", 2);
                IDataPointSet energySigmas = dpsf.create("sigma \\/ E vs E", 2);
                IDataPointSet resolutionFit = dpsf.create("sigma \\/  E vs 1 \\/ \u221a E", 2);
                IDataPointSet energyResiduals = dpsf.create("energy residuals (%) vs E", 2);
                double eMax = 0;
                for (int i = 0; i < numberOfPoints; ++i)
                {
                    if (energies[i] > .1) // do not analyze 100MeV and below...
                    {
                        System.out.println("Energy " + energies[i]);
                        // +/- 5 sigma
                        int nSigma = 5;
                        double expectedSigma = _expectedResolution * sqrt(energies[i]);
                        double lowE = energies[i] - (expectedSigma * nSigma);
                        double hiE = energies[i] + (expectedSigma * nSigma);

                        ICloud1D e = (ICloud1D) _tree.find(objects[i] + "Cluster Energy");
                        if (!e.isConverted())
                        {
                            int nbins = 100;
//                            System.out.println(energies[i] + " - " + lowE + " + " + hiE);
                            e.convert(nbins, lowE, hiE);
                        }
                        IHistogram1D eHist = e.histogram();
                        gauss.setParameter("amplitude", eHist.maxBinHeight());
//                        gauss.setParameter("mean", eHist.mean());
//                        gauss.setParameter("sigma", eHist.rms());
                        // robustify...
                        gauss.setParameter("mean", energies[i]);
                        gauss.setParameter("sigma", expectedSigma);
                        style = plotter.region(i).style();
                        style.legendBoxStyle().setVisible(false);
                        style.statisticsBoxStyle().setVisible(false);
                        //
                        style.xAxisStyle().setLabel(_particleType+" "+energies[i]+" GeV");
                        style.titleStyle().setVisible(false);
                        //
                        double loElimit = lowE; //energies[i] - .6 * sqrt(energies[i]); // expect ~20% resolution, and go out 3 sigma
                        double hiElimit = hiE; //energies[i] + .6 * sqrt(energies[i]);
                        plotter.region(i).setXLimits(loElimit, hiElimit);
                        plotter.region(i).plot(eHist);
                        IFitResult jminuitResult = jminuit.fit(eHist, gauss);
                        double[] fitErrors = jminuitResult.errors();
                        IFunction fit = jminuitResult.fittedFunction();
                        for (int j = 0; j < pars.length; ++j)
                        {
                            System.out.println("   " + pars[j] + ": " + fit.parameter(pars[j]) + " +/- " + fitErrors[j]);
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
                        point1.coordinate(1).setValue(fitSigmas[i] / energies[i]);
                        point1.coordinate(1).setErrorPlus(fitErrors[2] / energies[i]);
                        point1.coordinate(1).setErrorMinus(fitErrors[2] / energies[i]);

                        // sigma/E vs 1/sqrt(E)

                        IDataPoint point3 = resolutionFit.addPoint();
                        point3.coordinate(0).setValue(1. / sqrt(energies[i]));
                        point3.coordinate(1).setValue(fitSigmas[i] / energies[i]);
                        point3.coordinate(1).setErrorPlus(fitErrors[2] / energies[i]);
                        point3.coordinate(1).setErrorMinus(fitErrors[2] / energies[i]);

                        // residuals
                        IDataPoint point2 = energyResiduals.addPoint();
                        point2.coordinate(0).setValue(energies[i]);
                        point2.coordinate(1).setValue(100. * (fitMeans[i] - energies[i]) / energies[i]);

                        // axis bookeeping...
                        if (energies[i] > eMax)
                            eMax = energies[i];
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
                System.out.println("Linearity fit:");
                printLineFitResult(fitLine);
                double eMaxBin = eMax + 10.;
                results.region(0).setXLimits(0., eMaxBin);
//                results.region(0).setYLimits(0., eMaxBin);
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
//                resolution.region(0).setYLimits(0., .2);
                resolution.region(0).plot(energySigmas);


                IPlotter resolution2 = af.createPlotterFactory().create("sigma/E vs 1/E");
                style = resolution2.region(0).style();
                style.xAxisStyle().setLabel("1/ \u221a Energy [1/GeV]");
                style.yAxisStyle().setLabel("sigma/E");
                style.statisticsBoxStyle().setVisibileStatistics("011");
                style.legendBoxStyle().setVisible(false);
                IFitResult resFitLine = jminuit.fit(resolutionFit, line);
                System.out.println("Resolution fit:");
                printLineFitResult(resFitLine);

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

                if (_showPlots)
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
                        plotter.writeToFile("energyPlots." + _fileType, _fileType);
                        results.writeToFile("linearity." + _fileType, _fileType);
                        resolution.writeToFile("resolution." + _fileType, _fileType);
                        resolution2.writeToFile("resolutionLinear." + _fileType, _fileType);
                        residuals.writeToFile("residuals." + _fileType, _fileType);
                    } catch (IOException e)
                    {
                        System.out.println("problem writing out hardcopy in " + _fileType + " format");
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
        for (int j = 0; j < s.length; ++j)
        {
//           System.out.println(s[j]);
            String subDir = s[j].split("/")[1]; // first token should be "."
//            System.out.println(subDir);
            String[] st = subDir.split("_");
            String e = st[0];
            String unit = st[1];
//            System.out.println(e+" "+unit);
            energies[j] = Double.parseDouble(e);
            if (unit.contains("MeV"))
                energies[j] /= 1000.;
            map.put(energies[j], s[j]);
//            System.out.println("energy: "+energies[j]);
        }
        Arrays.sort(energies);
        for (int j = 0; j < s.length; ++j)
        {
            s[j] = map.get(energies[j]);
        }
//        for(int j=0; j<s.length; ++j)
//        {
//            System.out.println(s[j]);
//        }
    }

    public void setExpectedResolution(double resolution)
    {
        System.out.println("setting expected resolution to " + resolution);
        _expectedResolution = resolution;
    }

    public void setShowPlots(boolean showPlots)
    {
        _showPlots = showPlots;
    }

    public void setFileType(String fileType)
    {
        _fileType = fileType;
    }

    public void setWriteAidaFile(boolean writeAidaFile)
    {
        _writeOutAidaFile = writeAidaFile;
    }

    public void setAidaFileName(String aidaFileName)
    {
        _defaultAidaFileName = aidaFileName;
    }

    private void printLineFitResult(IFitResult fit)
    {
        System.out.println(" fit status: " + fit.fitStatus());
        //String[] parNames = fit.fittedParameterNames();
        String[] parNames =
        {
            "intercept", "slope   "
        };
        double[] parVals = fit.fittedParameters();
        double[] parErrs = fit.errors();
        for (int i = 0; i < parNames.length; ++i)
        {
            System.out.println(parNames[i] + " : " + parVals[i] + " +/- " + parErrs[i]);
        }
    }

    private static String date()
    {
        Calendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        DecimalFormat formatter = new DecimalFormat("00");
        String day = formatter.format(cal.get(Calendar.DAY_OF_MONTH));
        String month = formatter.format(cal.get(Calendar.MONTH) + 1);
        return cal.get(Calendar.YEAR) + month + day;
    }
}

