package org.lcsim.event.base;

import static java.lang.Math.abs;

/**
 * 
 * This class provides static utility methods for determining the type of a particle based on the PDGID.  It based 
 * on the primary document on PDGIDs issued by the Particle Data Group and also on some code from the ATLAS LXR.
 * 
 * <h2>References</h2>
 * <ul> 
 * <li><a href="http://pdg.lbl.gov/2007/mcdata/mc_particle_id_contents.html">PDG Numbering Scheme</a></li>
 * <li><a href="http://alxr.usatlas.bnl.gov/lxr/source/atlas/PhysicsAnalysis/MuonID/MuonIDValidation/MuonIDValidation/pdg.h">pdg.h</a></li>
 * <li><a href="http://alxr.usatlas.bnl.gov/lxr/source/atlas/PhysicsAnalysis/MuonID/MuonIDValidation/src/pdg.cxx">pdg.cxx</a></li>
 * </ul>
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: ParticleTypeClassifier.java,v 1.3 2011/08/24 18:51:17 jeremy Exp $
 *
 */
public final class ParticleTypeClassifier 
{
	/**
	 * Class is 100% static utility methods
	 * so it should not be instantiated.
	 */
	private ParticleTypeClassifier()
	{}

	/**
	 * True if <code>p</code> is a lepton.
	 * Checks that <code>p</code> is between 11 and 18.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a lepton.
	 */
	public static boolean isLepton(int p)
	{
		return p > 10 && p < 19;
	}
	
	/**
	 * True if <code>p</code> is a charged lepton.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a charged lepton.
	 */
	public static boolean isChargedLepton(int p)
	{
		int pp = abs(p);
		return pp == 11 || pp == 13 || pp == 15 || pp == 17;
	}
	
	/**
	 * True if <code>p</code> is a neutrino.
	 * Checks that <code>p</code> is odd. 
	 * @param p
	 * @return
	 */
	public static boolean isNeutrino(int p)
	{
		int pp = abs(p);
		return pp == 12 || pp == 14 || pp == 16 || pp == 18;
	}
	
	/**
	 * True if <code>p</code> is an electron.
	 * @param p
	 * @return
	 */
	public static boolean isElectron(int p)
	{
		return p == 11;
	}
			
	/**
	 * True if <code>p</code> is a positron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a postitron.
	 */
	public static boolean isPositron(int p)
	{
		return p == -11;
	}
	
	/**
	 * True if <code>p</code> is a muon.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a muon.
	 */
	public static boolean isMuon(int p)
	{
		return abs(p) == 13;
	}
	
	/**
	 * True if <code>p</code> is a tau.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a tau.
	 */
	public static boolean isTau(int p)
	{
		return abs(p) == 15;
	}
	
	/**
	 * True if <code>p</code> is a photon.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a photon.
	 */
	public static boolean isPhoton(int p)
	{
		return p == 22;
	}
	 		
	/**
	 * True if <code>p</code> is an electron or photon.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an electron or photon.
	 */
	public static boolean isEM(int p)
	{
		return isElectron(p) || isPhoton(p);
	}
	
	/**
	 * True if <code>p</code> is a boson.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a boson.
	 */
	public static boolean isBoson(int p)
	{
		int pp = abs(p);
		return pp > 20 && pp < 38;
	}
			
	/**
	 * True if <code>p</code> is a quark.
	 * @param p
	 * @return
	 */
	public static boolean isQuark(int p)
	{
		int pp = abs(p);
		return pp > 0 && pp < 9;
	}
	
	/**
	 * True if <code>p</code> is a hadron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a hadron.
	 * FIXME: This may be incorrect.  Needs to be checked.
	 */
	public static boolean isHadron(int p)
	{
		int pp = abs(p);
		return pp > 100 && (pp < 1000000 || pp > 9000000) && (pp % 10 != 0);
	}
	
	/**
	 * True if <code>p</code> is a proton.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a proton.
	 */
	public static boolean isProton(int p)
	{
		return p == 2212;		
	}

	/**
	 * True if <code>p</code> is an antiproton.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a antiproton.
	 */
	public static boolean isAntiProton(int p)
	{
		return p == -2212;
	}
	
	/**
	 * True if <code>p</code> is a neutron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a neutron.
	 */
	public static boolean isNeutron(int p)
	{
		return p == 2112;
	}
	
	/**
	 * True if <code>p</code> is an antineutron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an antineutron.
	 */
	public static boolean isAntiNeutron(int p)
	{
		return p == -2112;
	}

	/**
	 * True if <code>p</code> is a kaon.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an antineutron.
	 */
	public static boolean isKaon(int p)
	{
		return p == 130 || p == 310 || p == 311 || abs(p) == 321;
	}
	
	/**
	 * True if <code>p</code> is a charged kaon.
	 * @param p The PDGID.
	 * @return True if particle is a charged kaon.
	 */
	public static boolean isChargedKaon(int p)
	{
		return abs(p) == 321;
	}
			
	/**
	 * True if <code>p</code> is an UD hadron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an UD hadron.
	 */
	public static boolean isUDHadron(int p)
	{
		int flavor = flavor(p);
		return isHadron(p) && (flavor == 1 || flavor == 2);
	}
	
	/**
	 * True if <code>p</code> is an S hadron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an S hadron.
	 */
	public static boolean isSHadron(int p)
	{
		return isHadron(p) && flavor(p) == 3;
	}
	
	/**
	 * True if <code>p</code> is a C hadron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a C hadron.
	 */
	public static boolean isCHadron(int p)
	{
		return isHadron(p) && flavor(p) == 4;
	}
	
	/**
	 * True if <code>p</code> is a B hadron.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a B hadron.
	 */
	public static boolean isBHadron(int p)
	{
		return isHadron(p) && flavor(p) == 5;
	}
	
	/**
	 * True if <code>p</code> is a down quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a down quark.
	 */
	public static boolean isDownQuark(int p)
	{
		return abs(p) == 1;
	}
	
	/**
	 * True if <code>p</code> is an up quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a up quark.
	 */
	public static boolean isUpQuark(int p)
	{
		return abs(p) == 2;
	}
	
	/**
	 * True if <code>p</code> is a strange quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a strange quark.
	 */
	public static boolean isStrangeQuark(int p)
	{
		return abs(p) == 3;
	}
	
	/**
	 * True if <code>p</code> is a charm quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a charm quark.
	 */
	public static boolean isCharmQuark(int p)
	{
		return abs(p) == 4;
	}
	
	/**
	 * True if <code>p</code> is a bottom quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a bottom quark.
	 */
	public static boolean isBottomQuark(int p)
	{
		return abs(p) == 5;
	}
		
	/**
	 * True if <code>p</code> is a top quark.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a top quark.
	 */
	public static boolean isTopQuark(int p)
	{
		return abs(p) == 6;
	}		
	
	/**
	 * True if particle is a K0L or a K0S.
	 * @param p
	 * @return
	 */
	public static boolean isNeutralKaon(int p)
	{
		return p == 130 || p == 310;
	}
	
	/**
	 * True if <code>p</code> is an ion or nucleus.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is an ion or nucleus.
	 * FIXME: This relies on the PDGID of ions and nuclei being 0, which may 
	 *        not be completely accurate.  For instance, geantinos have a PDGID
	 *        of 0 also, so it may not be correct for all cases.
	 */
	public static boolean isIon(int p)
	{
		return p == 0;
	}		
	
	/**
	 * True if <code>p</code> is a Z boson.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a Z boson.
	 */
	public static boolean isZBoson(int p)
	{
		return p == 23;
	}
	
	/**
	 * True if <code>p</code> is a W boson.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a W boson.
	 */
	public static boolean isWBoson(int p)
	{
		return abs(p) == 24;
	}	
	
	/**
	 * True if <code>p</code> is any type of Higgs boson.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a Higgs boson.
	 */
	public static boolean isHiggs(int p)
	{
		int pp = abs(p);
		return pp == 25 || pp == 35 || pp == 36 || pp == 37; 
	}
	
	/**
	 * True if <code>p</code> is a SUSY particle.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a SUSY particle.
	 */
	public static boolean isSUSY(int p)
	{
		return p > 1000000 && p < 2000015;
	}
	
	/**
	 * True if <code>p</code> is a Technicolor particle.
	 * @param p The PDGID.
	 * @return True if <code>p</code> is a Technicolor particle.
	 */
	public static boolean isTechnicolor(int p)
	{
		int pp = abs(p);
		return pp > 3000110 && pp < 3000224;
	}
	
	/**
	 * Get the flavor of a hadron indicating its quark content.
	 * This method is only used internally.
	 * @param p The PDGID.
	 * @return The flavor of the particle.
	 */
	static int flavor(int p)
	{
		int rid = abs(p) % 10000;
		int flavor = 0;
		if (rid > 999)
		{
			flavor = rid / 1000;
		}
		else
		{
			int flavor1 = rid / 100;
			int flavor2 = (rid % 100) / 10;
			flavor = flavor1;
			if (flavor1 == flavor2 && flavor1 < 4)
			{
				flavor = 1;
			}
			if (p == 130) flavor = 3;
		}
		return flavor;
	}	
}
