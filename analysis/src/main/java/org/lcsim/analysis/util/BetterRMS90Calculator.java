package org.lcsim.analysis.util;

import hep.aida.ICloud1D;
import hep.aida.IEvaluator;
import hep.aida.ITuple;
import java.util.Arrays;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author tonyj
 */
public class BetterRMS90Calculator {

    public Result calculateRMS90(ICloud1D cloud) {
        return calculate(cloud2array(cloud));
    }

    public Result calculateRMS90(ITuple tuple, String expression) {
        AIDA aida = AIDA.defaultInstance();
        IEvaluator eval = aida.analysisFactory().createTupleFactory(aida.tree()).createEvaluator(expression);
        return calculate(tuple2array(tuple, eval));
    }

    public Result calculateRMS90(ITuple tuple, IEvaluator evaluator) {
        return calculate(tuple2array(tuple, evaluator));
    }

    private double[] tuple2array(ITuple tuple, IEvaluator evaluator) {
        evaluator.initialize(tuple);
        int entries = tuple.rows();
        double[] elist = new double[entries];
        tuple.start();
        for (int i = 0; i < entries; i++) {
            tuple.next();
            elist[i] = evaluator.evaluateDouble();
        }
        return elist;
    }

    private double[] cloud2array(ICloud1D cloud) {
        int entries = cloud.entries();
        double[] elist = new double[entries];
        for (int i = 0; i < entries; i++) {
            elist[i] = cloud.value(i);
        }
        return elist;
    }

    Result calculate(double[] elist) {
        int entries = elist.length;
        double rms90 = Double.NaN;
        double mean90 = Double.NaN;
        int ntail = (int) (.1 * entries);
        int ncore = entries - ntail;
        Arrays.sort(elist);

        // Calculate invariants once
        double svi = 0.;
        double sv2i = 0.;
        for (int i = ntail; i < ncore; i++) {
            svi += elist[i];
            sv2i += elist[i] * elist[i];
        }

        for (int k = 0; k <= ntail; k++) {
            double sv = svi;
            double sv2 = sv2i;
            for (int i = k; i < ntail; i++) {
                sv += elist[i];
                sv2 += elist[i] * elist[i];
            }
            for (int i = ncore; i < ncore+k; i++) {
                sv += elist[i];
                sv2 += elist[i] * elist[i];
            }
            double mean = sv / ncore;
            double rms = Math.sqrt(sv2 / ncore - mean * mean);
            if (Double.isNaN(rms90) || rms < rms90) {
                rms90 = rms;
                mean90 = mean;
            }
        }
        return new Result(rms90, mean90);
    }


}
