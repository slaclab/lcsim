package org.lcsim.detector.material;

import static java.lang.Math.PI;
import static java.lang.Math.log;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.lcsim.units.clhep.PhysicalConstants.Avogadro;
import static org.lcsim.units.clhep.PhysicalConstants.classic_electr_radius;
import static org.lcsim.units.clhep.PhysicalConstants.electron_mass_c2;
import hep.physics.particle.Particle;
import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.material.IMaterial.State;

/**
 *
 * A calculator for dEdX using the Bethe-Bloch formula
 * from the PDG guide.  
 *
 * <a href="http://pdg.lbl.gov/2006/reviews/passagerpp.pdf">Passage of Particles Through Matter</a>
 *
 * @author jeremym
 * @author caroline
 */
public class BetheBlochCalculator
{	
	/**
	 * 
	 * Calculate Bethe-Bloch using IMaterial and Particle.
	 * 
	 * @param material
	 * @param particle
	 * @param distance
	 * @return The energy loss in MeV. 
	 */
	public static double computeBetheBloch(
			IMaterial material,
			Particle particle,
			double distance)
	{
		return computeBetheBloch(
				material,
				particle.getMomentum(),
				particle.getMass(),
				particle.getCharge(), 
				distance);
	}
	
	/**
	 * Calculate Bethe-Bloch using an IMaterial.
	 * 
	 * @param material
	 * @param p
	 * @param mass
	 * @param charge
	 * @param distance
	 * @return The energy loss in MeV.
	 */
	public static double computeBetheBloch(
			IMaterial material,
			Hep3Vector p,
			double mass,
			double charge,
			double distance)
	{
		return computeBetheBloch(
				material.getZ(),
				material.getA(),
				material.getDensity(),
				material.getState(),
				material.getPressure(),
				material.getTemperature(),
				p,
				mass,
				charge,
				distance);
	}
	
	/**
	 * Calculate Bethe Bloch with numerical parameters.
	 * 
	 * The pressure and temperature arguments are only used if the
	 * state is a gas.
	 * 
	 * @param Z
	 * @param A
	 * @param density
	 * @param state
	 * @param pressure
	 * @param temperature
	 * @param p
	 * @param mass
	 * @param charge
	 * @param distance
	 * @return The energy loss in MeV.
	 */
    public static double computeBetheBloch(
    		double Z,
    		double A,
    		double density,
    		State state,
    		double pressure,
    		double temperature,
    		Hep3Vector p,
            double mass, 
            double charge, 
            double distance)
    {        
        double zeff = Z;
        double aeff = A;
        double ZoverA = zeff / aeff;
        
        // K matches PDG, pg. 238 --> 0.307075 in MeV g-1 cm2 */
        double K = ((4 * PI) * Avogadro * (classic_electr_radius * classic_electr_radius) * electron_mass_c2);
        double z2 = charge * charge;
        
        // Convert p(GeV) to p(MeV).       
        double[] pmev = new double[3];
        for (int i = 0; i < 3; i++)
        {
            pmev[i] = p.v()[i] * 1e+3;
        }
        
        double mag2 = pmev[0] * pmev[0] + pmev[1] * pmev[1] + pmev[2] * pmev[2];
        
        double beta2 = mag2 / (mass * mass + mag2);
        //double beta = sqrt(beta2);
        
        double coeff1 = K * z2 * (ZoverA) * (1 / beta2);        
        
        double gamma = 1 / (sqrt(1 - beta2));
        double gamma2 = gamma * gamma;
                
        // Compute T_max.
        double numer_T_max = 2 * electron_mass_c2 * beta2 * gamma2;
        double denom_T_max = 1 + (2 * gamma * electron_mass_c2 / mass) + pow((electron_mass_c2 / mass), 2);
        double T_max = numer_T_max / denom_T_max;                
        
        // Compute I using lelaps/CEPack/cematerial.cc .
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
        
        // Compute plasma E.
        double eta = 1.0;
        double rho_STP = density;
        double rho = density;
        
        if (state == IMaterial.Gas)
        {
            eta = pressure * (273.15 / temperature);
            rho_STP = rho / eta;
        }
        
        double plasmaE = 28.816 * sqrt(rho_STP * ZoverA);
        plasmaE *= 1e-6;
        
        // Cbar
        double Cbar = 2.0 * log( I / plasmaE ) + 1.0;
        
        // Xa
        double Xa = Cbar / 4.6052;
        
        double X1;
        if (state == IMaterial.Gas)
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
        if (state == IMaterial.Gas)
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
        
        //double ASP = 0.1536 * ZoverA;        
        //double BSP = log(511.0e9 / I2);
        
        if (state == IMaterial.Gas)
        {
            double eta_corr_1 = 0.5 * log10(eta);
            double eta_corr_2 = 4.6052 * eta_corr_1;
            
            Cbar -= eta_corr_2;
            X1   -= eta_corr_1;
            X0   -= eta_corr_1;
        }
        
        //double delta_estimate = log(plasmaE / I) + log((sqrt(beta2) * gamma)) - 0.5;
        
        double delta = 0;
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
               
        double coeff2 = 0.5 * (log((2 * electron_mass_c2 * beta2 * gamma2 * T_max) / I2));
        
        coeff2 -= beta2;             
        coeff2 -= delta;     
        
        double result = coeff1 * coeff2;
        
        result = result * density;
        result = result * distance;        
        
        return result;
    }   
}
