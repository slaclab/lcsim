package org.lcsim.material;

import static org.lcsim.units.clhep.PhysicalConstants.*;

import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static java.lang.Math.log;

import org.lcsim.material.MaterialState;

/**
 * 
 * Old class for material property calculations.
 * 
 * @deprecated Use functionality provided by
 *             {@link org.lcsim.detector.material.IMaterial} and 
 *             {@link org.lcsim.detector.material.BetheBlochCalculator}.
 *             
 * @author Jeremy McCormick
 */
//@Deprecated
abstract public class MaterialCalculator
{
    private static boolean _debug = false;
    
    /* Fine structure constant from PDG July 2004 */
    public static final double FINE_STRUCTURE_CONSTANT = 1 / 137.03599911;
    
    public static double computeRadiationLengthEstimate(double A, double Z)
    {
        return (180.0 * A / (Z * Z));
    }
    
    /**
     * Compute radiation length using Tsai formula
     *
     * Based on LELAPS CEPack/ceelement::setIntLength()
     *
     */
    public static double computeRadiationLengthTsai(double A, double Z)
    {
        double azsq = FINE_STRUCTURE_CONSTANT * Z;
        azsq *= azsq;
        double f = azsq
                * (1.0 / (1.0 + azsq) + 0.20206 - 0.0369 * azsq + 0.0083 * azsq
                * azsq - 0.002 * azsq * azsq * azsq);
        
        double Lrad, LradP;
        if (Z == 1)
        {
            Lrad = 5.31;
            LradP = 6.144;
        }
        else if (Z == 2)
        {
            Lrad = 4.79;
            LradP = 5.621;
        }
        else if (Z == 3)
        {
            Lrad = 4.74;
            LradP = 5.805;
        }
        else if (Z == 4)
        {
            Lrad = 4.71;
            LradP = 5.924;
        }
        else
        {
            Lrad = log(184.15 / pow(Z, 0.333333333));
            LradP = log(1194.0 / pow(Z, 0.666666667));
        }
        double rlen = 716.408 * A / ((Z * Z * (Lrad - f) + Z * LradP));
        return rlen;
    }
    
    /**
     * Compute NIL, using hard-coded values for Z <= 2 from PDG.
     *
     * Above helium, use a simple fit.
     *
     * Base on LELAPS CEPack/ceelement::setIntLength()
     *
     */
    public static double computeNuclearInteractionLength(double A, double Z)
    {
        double NIL = 0;
        if (Z == 1)
        {
            if (A < 1.5)
            {
                NIL = 50.8;
            }
            else
            {
                NIL = 54.7;
            }
        }
        else if (Z == 2)
        {
            NIL = 65.2;
        }
        else
        {
            NIL = 40.8 * pow(A, 0.289);
        }
        
        return NIL;
    }
    
    /* electron mass x c^2 in MeV */
    public static final double M_e = 0.510998918;
    
    /* Avogadro's number */
    public static final double N_A = 6.0221415e23;
    
    /* classical electron radius in cm */
    public static final double r_e = 2.81794032528e-13;
    
    // public static final double clight = 2.99792458e08; // m/s
    
   /*
    * @param material org.lcsim.material.Material
    * @param p particle momentum in GeV
    * @param mass particle mass (MeV/c)
    * @param charge particle charge in electron charge
    * @param distance path length in cm to scale the dEdx
    * @return computed dEdx (MeV/cm)
    */
    public static double computeBetheBloch(Material material, double[] p,
            double mass, double charge, double distance)
    {
        assert (p.length == 3);
        
        double zeff = material.getZeff();
        double aeff = material.getAeff();
        double ZoverA = zeff / aeff;
        
        /* K matches PDG, pg. 238 --> 0.307075 in MeV g-1 cm2 */
        double K = ((4 * PI) * N_A * (r_e * r_e) * M_e);
        double z2 = charge * charge;
        
        /*
         * Convert p(GeV) to p(MeV).
         */
        double[] pmev = new double[3];
        for (int i = 0; i < 3; i++)
        {
            pmev[i] = p[i] * 1e+3;
        }
        
        double mag2 = pmev[0] * pmev[0] + pmev[1] * pmev[1] + pmev[2] * pmev[2];
        
        double beta2 = mag2 / (mass * mass + mag2);
        double beta = sqrt(beta2);
        
        double coeff1 = K * z2 * (ZoverA) * (1 / beta2);
        // end first coefficient calc
        
        double gamma = 1 / (sqrt(1 - beta2));
        double gamma2 = gamma * gamma;
        
        
        /* compute T_max */
        double numer_T_max = 2 * M_e * beta2 * gamma2;
        double denom_T_max = 1 + (2 * gamma * M_e / mass) + pow((M_e / mass), 2);
        double T_max = numer_T_max / denom_T_max;
        
        /* end compute T_max */
        
        if ( _debug )
        {
            System.out.println("z2="+z2);
            System.out.println("K=" + K);
            System.out.println("coeff1 = " + coeff1);
            System.out.println("gamma2 = " + gamma2);
            System.out.println("numer_T_max = " + numer_T_max);
            System.out.println("denom_T_max = " + denom_T_max);
            System.out.println("T_max = " + T_max);
            System.out.println("beta=" + beta);
            System.out.println("beta2 = " + beta2);
        }
        
        /* compute I using lelaps/CEPack/cematerial.cc */
        double I = 0.0;
        if (zeff > 12)
        {
            I = zeff * (9.76 + 58.8 * pow(zeff, -1.19));
        }
        else
        {
            if (zeff == 1)
            {
                I = 18.7;
            }
            else
            {
                I = 13.0 * zeff;
            }
        }
        I *= 1e-6; // convert I to MeV
        double I2 = I * I;
        
        // plasma E
        double eta = 1.0;
        double rho_STP = material.getDensity();
        double rho = material.getDensity();
        
        org.lcsim.material.MaterialState state = material.getState();
        if (state == org.lcsim.material.MaterialState.GAS)
        {
            eta = material.getPressure() * (273.15 / material.getTemperature());
            rho_STP = rho / eta;
        }
        
        double plasmaE = 28.816 * sqrt(rho_STP * ZoverA);
        plasmaE *= 1e-6;
        //
        
        if ( _debug )
        {
            System.out.println("plasmaE=" + plasmaE);
            System.out.println("I=" + I);
            System.out.println("I2=" + I2);
            System.out.println("eta=" + eta);
            System.out.println("rho_STP=" + rho_STP);
            System.out.println("rho=" + rho);
            System.out.println("state="+state);
        }
        
        // Cbar
        double Cbar = 2.0 * log( I / plasmaE ) + 1.0;
        
        // Xa
        double Xa = Cbar / 4.6052;
        
        double X1;
        if (state == MaterialState.GAS)
        {
            if (Cbar < 12.25)
            {
                X1 = 4.0;
            }
            else
            {
                X1 = 5.0;
            }
        }
        else
        {
            if ( I < 100.0)
            {
                X1 = 2.0;
            }
            else
            {
                X1 = 3.0;
            }
        }
        
        double m = 3.0;
        
        double X0 = 0;
        if (state == org.lcsim.material.MaterialState.GAS)
        {
            if (Cbar < 10.0)        X0 = 1.6;
            else if (Cbar < 10.5)   X0 = 1.7;
            else if (Cbar < 11.0)   X0 = 1.8;
            else if (Cbar < 11.5)   X0 = 1.9;
            else if (Cbar < 13.804) X0 = 2.0;
            else                    X0 = 0.326 * Cbar - 2.5;
        }
        else
        {
            if (I < 100.0)
            {
                if (Cbar < 3.681) X0 = 0.2;
                else              X0 = 0.326 * Cbar - 1.0;
                X1 = 2.0;
            }
            else
            {
                if (Cbar < 5.215) X0 = 0.2;
                else              X0 = 0.326 * Cbar - 1.5;
                X1 = 3.0;
            }
        }
        
        double a = 4.6052 * (Xa - X0) / pow(X1 - X0, m);
        
        double ASP = 0.1536 * ZoverA;
        
        double BSP = log(511.0e9 / I2);
        
        if (state == MaterialState.GAS)
        {
            double eta_corr_1 = 0.5 * log10(eta);
            double eta_corr_2 = 4.6052 * eta_corr_1;
            
            Cbar -= eta_corr_2;
            X1   -= eta_corr_1;
            X0   -= eta_corr_1;
        }
        
        double delta_estimate = log(plasmaE / I) + log((sqrt(beta2) * gamma)) - 0.5;
        
        double delta = 0;
//        double X = beta * gamma;
        double X = log10(sqrt(mag2) / (mass));
        
        if ( X0 < X && X < X1 )
        {
            delta = 4.6052 * X - Cbar + a * (X1 - X);
        }
        else if ( X > X1 )
        {
            delta = 4.6052 * X - Cbar;
        }
        else if ( X < X0 )
        {
            delta = 0;
        }
        
        if ( _debug )
        {
            System.out.println("Cbar=" + Cbar);
            System.out.println("Xa=" + Xa);
            System.out.println("X1="+X1);
            System.out.println("X="+X);
            System.out.println("X0="+X0);
            System.out.println("delta="+delta);
        }
        
        double coeff2 = 0.5 * (log((2 * M_e * beta2 * gamma2 * T_max) / I2));
        
        if ( _debug )
        {
            System.out.println("delta_estimate=" + delta_estimate);
            System.out.println("coeff2="+coeff2);
        }
        
//        double _num = 2 * M_e * beta2 * gamma2 * T_max;
//        System.out.println("2me c2*beta2 ... = " + _num);
//        _num = _num / I2;
//        System.out.println("numerator / i2 = " + _num);
        
        coeff2 -= beta2;
        
        if ( _debug ) System.out.println("coeff2 - beta2 = " + coeff2);
        
        coeff2 -= delta;
        
        if ( _debug ) System.out.println("coeff2 - delta = " + coeff2);
        
        double result = coeff1 * coeff2;
        if ( _debug ) System.out.println("dEdx (MeV cm2/g) = " + result);
        
        result = result * material.getDensity();
        
        if ( _debug ) System.out.println("dEdx (MeV/cm) = " + result);
        
        result = result * distance;
        
        if ( _debug ) System.out.println("dEdx (MeV/cm) * distance = " + result);
        
        return result;
    }
    
    public double computeRadiationLength(Material m)
    {
    	double z = m.getZeff();
    	double a = m.getAeff();
    	double ionpot = 
    		(4 * fine_structure_const * Avogadro * z * (z + 1) * 
    		 classic_electr_radius * classic_electr_radius * 
    		 log(183.0 * pow(z,-.33333334))) / a; 
    	ionpot = 1.0/ionpot;
    	return ionpot;
    }
    
}
/*
 * beta2 = 0.5 coeff1 = 0.6141498919582697 gamma2 = 0.6666666666666669
 * numer_T_max = 0.3406659453333334 denom_T_max = 2.0955776330067057 T_max =
 * 0.16256422094205625 I = 1.8699999999999997E-5 I2 = 3.496899999999999E-10
 * plasmaE = 28.816 delta = -0.11689887976099544 coeff2 = 9.440219502206741
 * coeff2 (final) = 8.998668942087239 final results = 5.526531558551115 result =
 * 5.526531558551115
 */

/*
 *
 * excitation potential from lelaps/CEPack/ceelement.c
 *
 * if (e.Z > 12) { I = Z * (9.76 + 58.8 * pow(Z, -1.19)); } else { if (e.Z == 1)
 * I = 18.7; // ********should be 19.0 else I = 13.0 * e.Z; }
 */

/*
 * public static double computeCoulombCorrection(double Z) { if ( Z <= 0 ) {
 * throw new IllegalArgumentException("Z cannot be <= 0."); }
 *
 * double az2 = (FINE_STRUCTURE_CONSTANT * Z) * (FINE_STRUCTURE_CONSTANT * Z);
 * double az4 = az2 * az2;
 *
 * double coulomb = (COULOMB_CORRECTIONS[0] * az4 + COULOMB_CORRECTIONS[1] +
 * 1.0/(1.0 + az2)) * az2 - (COULOMB_CORRECTIONS[2] * az4 +
 * COULOMB_CORRECTIONS[3]) * az4;
 *
 * return coulomb; }
 */

/*
 * public static double computeTsaiFactor(double Z, double coulombCorrection) {
 * double logZ3 = log(Z) / 3.0; int iz = (int) (Z + 0.5) - 1;
 *
 * double lrad, lprad; if ( iz <= 3) { lrad = TSAI_LRAD_LIGHT[iz]; lprad =
 * TSAI_LPRAD_LIGHT[iz]; } else { lrad = log(184.15) - logZ3; lprad =
 * log(1194.0) - 2 * logZ3; }
 */

/**
 * FINE_STRUCTURE_CONSTANT is actually 'alpha_rcl2' in G4Element but this is
 * same as F.S.C. in CLHEP.
 */
// double tsai = 4 * FINE_STRUCTURE_CONSTANT * Z * ( Z * (lrad *
// coulombCorrection) + lprad);
// return tsai;
// }
// public static double computeTsaiFactor(double Z)
// {
// return computeTsaiFactor(Z, computeCoulombCorrection(Z) );
// }
// public static double computeNuclearInteractionLengthEstimate(double A)
// {
// return ( Material.LAMBDA0 * pow(A, 0.33333));
// }
/*
 * Coulomb correction factors from G4Element via Phys Rev. D50 3-1 (1994) page
 * 1254
 */
// public static final double[] COULOMB_CORRECTIONS = { 0.0083, 0.20206, 0.0020,
// 0.0369 };
/*
 * Tsai constants from G4Element::ComputeLradTsaiFactor() via Phys Rev. D50 3-1
 * (1994) page 1254
 */
// public static final double[] TSAI_LRAD_LIGHT = { 5.31, 4.79, 4.74, 4.71 };
// public static final double[] TSAI_LPRAD_LIGHT = { 6.144, 5.621, 5.805, 5.924
// };
