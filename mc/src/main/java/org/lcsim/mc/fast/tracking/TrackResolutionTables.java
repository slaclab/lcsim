package org.lcsim.mc.fast.tracking;

import org.lcsim.conditions.ConditionsSet;

import java.io.*;

public class TrackResolutionTables {
    private ResolutionTable barrel;
    private ResolutionTable endcap;
    private double PolarInner;
    private double PolarOuter;
    private double PtMin;

    public TrackResolutionTables(ConditionsSet set, boolean beamSpotConstraint) throws IOException {
        PtMin = set.getDouble("PtMin");
        PolarInner = set.getDouble("PolarInner");
        PolarOuter = set.getDouble("PolarOuter");

        String postfix = beamSpotConstraint ? "-bc" : "-nbc";
        String barrelFile = set.getString("BarrelTableFile" + postfix);
        String endcapFile = set.getString("EndcapTableFile" + postfix);

        barrel = new ResolutionTable(set.getRawSubConditions(barrelFile).getReader());
        endcap = new ResolutionTable(set.getRawSubConditions(endcapFile).getReader());
    }

    public ResolutionTable getBarrelTable() {
        return barrel;
    }

    public ResolutionTable getEndcapTable() {
        return endcap;
    }

    public double getPolarInner() {
        return PolarInner;
    }

    public double getPolarOuter() {
        return PolarOuter;
    }

    public double getPtMin() {
        return PtMin;
    }
}
