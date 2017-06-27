package org.lcsim.detector.tracker.silicon;
/*
 * DopedSilicon.java
 *
 * Created on July 26, 2005, 3:31 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

//import static org.lcsim.units.clhep.SystemOfUnits.*;
//import static org.lcsim.units.clhep.PhysicalConstants.*;
import java.util.*;

/**
 *
 * @author tknelson
 */
public class DopedSilicon
{

    // Fields
    //=======
    
    // Static
    static public double K_BOLTZMANN = 8.617385E-5; // eV/degK
    static public double ENERGY_EHPAIR = 3.62E-9; // 3.62E-9 GeV/e-h pair
    
    // Member
    private double _temperature = 293.0; // * kelvin;
    private double _doping_concentration = 6.0E+11; // / cm3; 
    private EnumMap<ChargeCarrier, Double> _carrier_concentration = new EnumMap<ChargeCarrier,Double>(ChargeCarrier.class);    
    
    // Constructors
    //=============
    public DopedSilicon()
    {
        setElectronConcentration(1.0E+14);
        setHoleConcentration(1.0E+14);
    }

    // Setters/Accessors
    //==================
    public void setTemperature(double temperature){_temperature = temperature;}
    public void setDopingConcentration(double doping_concentration) 
    {
        _doping_concentration = doping_concentration;
    }
    public void setElectronConcentration(double electron_concentration)
    {
        _carrier_concentration.put(ChargeCarrier.ELECTRON,electron_concentration);
    }
    public void setHoleConcentration(double hole_concentration)
    {
        _carrier_concentration.put(ChargeCarrier.HOLE,hole_concentration);
    }
    
    public double getTemperature(){return _temperature;}
    public double getCarrierConcentration(ChargeCarrier charge_carrier)
    {
        return _carrier_concentration.get(charge_carrier);
    }
    
    // Methods
    //========
    
    // Lorentz angle calculation for silicon sensors
    public double tanLorentzAngle(double b_field, ChargeCarrier charge_carrier)
    {
        return b_field * mobility(charge_carrier) * 1.0E-4; // (must clean up units)
    }
    
    // Mobility calculation with correction for irradiated sensors
    public double mobility(ChargeCarrier charge_carrier)
    {
        return charge_carrier.muMin(_temperature) + charge_carrier.mu0(_temperature) / 
                (1.0 + Math.pow(_carrier_concentration.get(charge_carrier)/charge_carrier.nRef(_temperature), 
                                charge_carrier.alpha(_temperature)));
    }

    // Silicon type
    public boolean isNtype(){return _doping_concentration > 0.0;}
    
    
}
