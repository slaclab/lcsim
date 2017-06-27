package org.lcsim.analysis.util;

public class Result {

        private double rms;
        private double mean;

        public Result(double rms, double mean) {
            this.rms = rms;
            this.mean = mean;
        }

        public double getMean() {
            return mean;
        }

        public double getRms() {
            return rms;
        }

        @Override
        public String toString() {
            return "RMS90.Result{" + "rms=" + rms + " mean=" + mean + '}';
        }
    }