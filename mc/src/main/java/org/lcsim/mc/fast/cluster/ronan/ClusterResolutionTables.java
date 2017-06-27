package org.lcsim.mc.fast.cluster.ronan;

import org.lcsim.conditions.ConditionsSet;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

public class ClusterResolutionTables {
    private boolean JETParameterization;
    private double JETResolution;
    private double JETHadDegradeFraction;
    private double JETEMEnergyFraction;
    private double JETHadEnergyFraction;
    private double Lambda_j;

    private double EMAlignmentError;
    private double EMConstantTerm;
    private double EMPositionError;
    private double EMResolution;
    private double EMOnset;
    private double EMSharpness;

    private double HADAlignmentError;
    private double HADConstantTerm;
    private double HADPositionError;
    private double HADResolution;
    private double HADOnset;
    private double HADSharpness;

    private double PolarEMInner;
    private double PolarEMOuter;
    private double PolarHADInner;
    private double PolarHADOuter;

    ClusterResolutionTables(ConditionsSet set) {
        JETParameterization = Boolean.parseBoolean(set.getString("JETParameterization"));
        JETResolution = set.getDouble("JETResolution");
        JETHadDegradeFraction = set.getDouble("JETHadDegradeFraction");
        JETEMEnergyFraction = set.getDouble("JETEMEnergyFraction");
        JETHadEnergyFraction = set.getDouble("JETHadEnergyFraction");

        EMOnset = set.getDouble("EMOnset");
        EMSharpness = set.getDouble("EMSharpness");
        PolarEMInner = set.getDouble("PolarEMInner");
        PolarEMOuter = set.getDouble("PolarEMOuter");

        EMResolution = set.getDouble("EMResolution");
        EMConstantTerm = set.getDouble("EMConstantTerm");
        EMPositionError = set.getDouble("EMPositionError");
        EMAlignmentError = set.getDouble("EMAlignmentError");

        HADOnset = set.getDouble("HADOnset");
        HADSharpness = set.getDouble("HADSharpness");
        PolarHADInner = set.getDouble("PolarHADInner");
        PolarHADOuter = set.getDouble("PolarHADOuter");

        HADResolution = set.getDouble("HADResolution");
        HADConstantTerm = set.getDouble("HADConstantTerm");
        HADPositionError = set.getDouble("HADPositionError");
        HADAlignmentError = set.getDouble("HADAlignmentError");
        if (JETParameterization) {
            Lambda_j = (pow(JETResolution, 2) - JETEMEnergyFraction * pow(EMResolution, 2) - JETHadEnergyFraction * pow(HADResolution, 2)) / ((1. - JETHadDegradeFraction) * JETEMEnergyFraction * pow(EMResolution, 2) + JETHadDegradeFraction * JETHadEnergyFraction * pow(HADResolution, 2));
            EMResolution *= sqrt(1. + Lambda_j * (1. - JETHadDegradeFraction));
            HADResolution *= sqrt(1. + Lambda_j * JETHadDegradeFraction);
            System.out.println(" JETParameterization settings    Lamda_j= " + Lambda_j + " EMResolution= " + EMResolution + " HADResolution= " + HADResolution);
        }
    }

    public double getEMAlignmentError() {
        return EMAlignmentError;
    }

    public double getEMConstantTerm() {
        return EMConstantTerm;
    }

    public double getEMPositionError() {
        return EMPositionError;
    }

    public double getEMResolution() {
        return EMResolution;
    }

    public double getEMOnset() {
        return EMOnset;
    }

    public double getEMSharpness() {
        return EMSharpness;
    }

    public double getHADAlignmentError() {
        return HADAlignmentError;
    }

    public double getHADConstantTerm() {
        return HADConstantTerm;
    }

    public double getHADPositionError() {
        return HADPositionError;
    }

    public double getHADResolution() {
        return HADResolution;
    }

    public double getHADOnset() {
        return HADOnset;
    }

    public double getHADSharpness() {
        return HADSharpness;
    }

    public double getPolarEMInner() {
        return PolarEMInner;
    }

    public double getPolarEMOuter() {
        return PolarEMOuter;
    }

    public double getPolarHADInner() {
        return PolarHADInner;
    }

    public double getPolarHADOuter() {
        return PolarHADOuter;
    }
}
