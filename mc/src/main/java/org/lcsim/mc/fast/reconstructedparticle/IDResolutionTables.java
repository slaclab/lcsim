/*
 * IDResolutionTables.java
 *
 * Created on July 6, 2005, 7:13 PM
 *
 * @version "$Id: IDResolutionTables.java,v 1.2 2006/09/08 03:19:45 ngraf Exp $"
 */

package org.lcsim.mc.fast.reconstructedparticle;

import org.lcsim.conditions.ConditionsSet;

/**
 *
 * @author Daniel
 */
public class IDResolutionTables {

    private double ElectronEff;
    private double MuonEff;
    private double ProtonEff;
    private double KaonEff;
    private double NeutronEff;
    private double WtChgTrkCal;

    /** Creates a new instance of IDResolutionTables */
    IDResolutionTables(ConditionsSet set) {
        ElectronEff = set.getDouble("Electron");
        MuonEff = set.getDouble("Muon");
        ProtonEff = set.getDouble("Proton");
        KaonEff = set.getDouble("Kaon");
        NeutronEff = set.getDouble("Neutron");
        WtChgTrkCal = set.getDouble("wt_charged_track_calorimeter_energy");
    }

    public double getElectronEff() {
        return ElectronEff;
    }

    public double getMuonEff() {
        return MuonEff;
    }

    public double getProtonEff() {
        return ProtonEff;
    }

    public double getKaonEff() {
        return KaonEff;
    }

    public double getNeutronEff() {
        return NeutronEff;
    }

    public double getWtChgTrkCal() {
        return WtChgTrkCal;
    }
}
