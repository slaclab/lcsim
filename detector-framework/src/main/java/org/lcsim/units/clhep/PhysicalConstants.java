//$Id: PhysicalConstants.java,v 1.4 2007/02/23 02:48:48 jeremy Exp $
package org.lcsim.units.clhep;

import static org.lcsim.units.clhep.SystemOfUnits.MeV;
import static org.lcsim.units.clhep.SystemOfUnits.atmosphere;
import static org.lcsim.units.clhep.SystemOfUnits.cm3;
import static org.lcsim.units.clhep.SystemOfUnits.eplus;
import static org.lcsim.units.clhep.SystemOfUnits.henry;
import static org.lcsim.units.clhep.SystemOfUnits.joule;
import static org.lcsim.units.clhep.SystemOfUnits.kelvin;
import static org.lcsim.units.clhep.SystemOfUnits.m;
import static org.lcsim.units.clhep.SystemOfUnits.mg;
import static org.lcsim.units.clhep.SystemOfUnits.mole;
import static org.lcsim.units.clhep.SystemOfUnits.s;

/**
 * Port of CLHEP's PhysicalConstants.h C++ header file,
 * containing common physical constants from the PDG guide.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PhysicalConstants.java,v 1.4 2007/02/23 02:48:48 jeremy Exp $
 */

public final class PhysicalConstants 
{
	public static final double     pi  = 3.14159265358979323846;
	public static final double  twopi  = 2*pi;
	public static final double halfpi  = pi/2;
	public static final double     pi2 = pi*pi;

	//
	//
	//
	public static final double Avogadro = 6.0221367e+23/mole;

	//
	//	 c   = 299.792458 mm/ns
	//	c^2 = 898.7404 (mm/ns)^2
	//
	public static final double c_light   = 2.99792458e+8 * m/s;
	public static final double c_squared = c_light * c_light;

	//
	//	h     = 4.13566e-12 MeV*ns
	//	hbar  = 6.58212e-13 MeV*ns
	//	hbarc = 197.32705e-12 MeV*mm
	//
	public static final double h_Planck      = 6.6260755e-34 * joule*s;
	public static final double hbar_Planck   = h_Planck/twopi;
	public static final double hbarc         = hbar_Planck * c_light;
	public static final double hbarc_squared = hbarc * hbarc;

	//
	//
	//
	public static final double electron_charge = - eplus; // see SystemOfUnits.h
	public static final double e_squared = eplus * eplus;

	//
	//	 amu_c2 - atomic equivalent mass unit
	//	 amu    - atomic mass unit
	//
	public static final double electron_mass_c2 = 0.51099906 * MeV;
	public static final double   proton_mass_c2 = 938.27231 * MeV;
	public static final double  neutron_mass_c2 = 939.56563 * MeV;
	public static final double           amu_c2 = 931.49432 * MeV;
	public static final double              amu = amu_c2/c_squared;

	//
	//	 permeability of free space mu0    = 2.01334e-16 Mev*(ns*eplus)^2/mm
	//	 permittivity of free space epsil0 = 5.52636e+10 eplus^2/(MeV*mm)
	//
	public static final double mu0      = 4*pi*1.e-7 * henry/m;
	public static final double epsilon0 = 1./(c_squared*mu0);

	//
	//	 electromagnetic coupling = 1.43996e-12 MeV*mm/(eplus^2)
	//
	public static final double elm_coupling           = e_squared/(4*pi*epsilon0);
	public static final double fine_structure_const   = elm_coupling/hbarc;
	public static final double classic_electr_radius  = elm_coupling/electron_mass_c2;
	public static final double electron_Compton_length = hbarc/electron_mass_c2;
	public static final double Bohr_radius = electron_Compton_length/fine_structure_const;

	public static final double alpha_rcl2 = fine_structure_const
	*classic_electr_radius
	*classic_electr_radius;

	public static final double twopi_mc2_rcl2 = twopi*electron_mass_c2
	*classic_electr_radius
	*classic_electr_radius;
	//
	//
	//
	public static final double k_Boltzmann = 8.617385e-11 * MeV/kelvin;

	//
	//
	//
	public static final double STP_Temperature = 273.15*kelvin;
	public static final double STP_Pressure    = 1.*atmosphere;
	public static final double kGasThreshold   = 10.*mg/cm3;
}
