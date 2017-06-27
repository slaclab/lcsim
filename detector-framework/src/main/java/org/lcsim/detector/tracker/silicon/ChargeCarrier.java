package org.lcsim.detector.tracker.silicon;
/*
 * ChargeCarrier.java
 *
 * Created on October 13, 2005, 3:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author tknelson
 */

public enum ChargeCarrier
{
    ELECTRON(-1,1268.0,-2.33,92.0,-0.57,1.3E+17,2.4,0.91,-0.146),
    HOLE(1,406.9,-2.23,54.3,-0.57,2.35E+17,2.4,0.88,-0.146);
    
    private final int _charge;
    private final double _mu_0_factor;
    private final double _mu_0_exponent;
    private final double _mu_min_factor;
    private final double _mu_min_exponent;
    private final double _N_ref_factor;
    private final double _N_ref_exponent;
    private final double _alpha_factor;
    private final double _alpha_exponent;
    
    ChargeCarrier(int charge, double mu_0_factor, double mu_0_exponent, double mu_min_factor, double mu_min_exponent,
            double N_ref_factor, double N_ref_exponent, double alpha_factor, double alpha_exponent)
    {
        _charge = charge;
        _mu_0_factor = mu_0_factor;
        _mu_0_exponent = mu_0_exponent;
        _mu_min_factor = mu_min_factor;
        _mu_min_exponent = mu_min_exponent;
        _N_ref_factor = N_ref_factor;
        _N_ref_exponent = N_ref_exponent;
        _alpha_factor = alpha_factor;
        _alpha_exponent = alpha_exponent;
    }
    
    // Methods
    public int charge()
    {return _charge;}
    double mu0(double temperature)
    {return _mu_0_factor * Math.pow( (temperature/300.0), _mu_0_exponent);}
    double muMin(double temperature)
    {return _mu_min_factor * Math.pow( (temperature/300.0), _mu_min_exponent);}
    double nRef(double temperature)
    {return _N_ref_factor * Math.pow( (temperature/300.0), _N_ref_exponent);}
    double alpha(double temperature)
    {return _alpha_factor * Math.pow( (temperature/300.0), _alpha_exponent);}
    
    // Static
    public static ChargeCarrier getCarrier(int charge)
    {
        if (charge == -1) return ChargeCarrier.ELECTRON;
        if (charge == +1) return ChargeCarrier.HOLE;
        else throw new RuntimeException("No ChargeCarrier for charge: "+charge);
    }
    
}
