package org.lcsim.analysis.dbd.evtgen;

import hep.aida.IAnalysisFactory;
import hep.aida.IHistogram1D;
import hep.aida.IHistogramFactory;
import hep.aida.IPlotter;
import hep.aida.ITree;
import java.util.Random;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: CalculateZSmearing.java,v 1.3 2012/08/24 20:34:18 ngraf Exp $
 */
public class CalculateZSmearing
{

    public static void main(String[] args) throws Exception
    {
        Random ran = new Random(12345);
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = af.createTreeFactory().create();
        IHistogramFactory hf = af.createHistogramFactory(tree);

        IHistogram1D h1 = hf.createHistogram1D("Physics Z", 100, -10, 10);
        IHistogram1D h2 = hf.createHistogram1D("Physics Z - 1 bkgnd Z", 100, -10, 10);
        IHistogram1D h3 = hf.createHistogram1D("Physics Z - 2 bkgnd Z", 100, -10, 10);
        IHistogram1D h4 = hf.createHistogram1D("Physics Z - 3 bkgnd Z", 100, -10, 10);

        IPlotter plotter = af.createPlotterFactory().create("Z vertex smearing plot");
        plotter.createRegions(2, 2);

        plotter.region(0).plot(h1);
        plotter.region(1).plot(h2);
        plotter.region(2).plot(h3);
        plotter.region(3).plot(h4);

        Random r = new Random();

        for (int i = 0; i < 100000; i++)
        {
            double p = r.nextGaussian();
            h1.fill(p);
            for (int j = 0; j < 1; ++j) h2.fill(p - r.nextGaussian());          
            for (int k = 0; k < 2; ++k) h3.fill(p - r.nextGaussian());           
            for (int l = 0; l < 3; ++l) h4.fill(p-r.nextGaussian());
        } 
        plotter.show();
        plotter.writeToFile("ZVertexDistribution.png", "png");
    }
}