package org.lcsim.util.loop;

import hep.io.stdhep.*;
import hep.physics.event.generator.GeneratorFactory;
import hep.physics.event.generator.MCEvent;
import hep.physics.particle.BasicParticle;
import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticlePropertyProvider;
import hep.physics.particle.properties.ParticleType;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that converts MCEvent<-->StdhepEvent.
 * This version uses the Ron Cassell algorithm for deciding on parent/child relationships.
 * @author Tony Johnson (tonyj@slac.stanford.edu)
 * @version $Id: StdhepConverter.java,v 1.7 2007/11/13 00:31:33 jeremy Exp $
 */
class StdhepConverter
{
	private ParticlePropertyProvider ppp;
	private GeneratorFactory factory;
	private boolean haveWarned;

	StdhepConverter()
	{
		this(ParticlePropertyManager.getParticlePropertyProvider());
	}
	   StdhepConverter(ParticlePropertyProvider ppp)
	   {
	      this(ppp, new GeneratorFactory());
	   }
	StdhepConverter(ParticlePropertyProvider ppp, GeneratorFactory factory)
	{
		this.ppp = ppp;
		this.factory = factory;
	}
	/**
	 * Convert from a StdhepEvent to an MCEvent.
	 * Useful when reading stdhep files.
	 */
	MCEvent convert(StdhepEvent hepevt)
	{
		MCEvent event = factory.createEvent(0,hepevt.getNEVHEP());

		int n = hepevt.getNHEP();
		BasicParticle[] particle = new BasicParticle[n];
		for (int i=0; i<n; i++)
		{
			Hep3Vector origin = new BasicHep3Vector(hepevt.getVHEP(i,0),hepevt.getVHEP(i,1),hepevt.getVHEP(i,2));
			Hep3Vector momentum = new BasicHep3Vector(hepevt.getPHEP(i,0),hepevt.getPHEP(i,1),hepevt.getPHEP(i,2));
			HepLorentzVector p = new BasicHepLorentzVector(hepevt.getPHEP(i,3),momentum);
			ParticleType type = ppp.get(hepevt.getIDHEP(i));
			particle[i] = factory.createParticle(origin,p,type,hepevt.getISTHEP(i), hepevt.getVHEP(i,3));
			particle[i].setMass(hepevt.getPHEP(i,4));
		}
		int[] vec = new int[n];
		List<Set<BasicParticle>> ancestors = new ArrayList<Set<BasicParticle>>(n);
		for (int i=0; i<n; i++) ancestors.add(new HashSet<BasicParticle>());
		// Deal with parents
		for (int i=0; i<n; i++)
		{
			int idx1 = hepevt.getJMOHEP(i,0) - 1;
			int idx2 = hepevt.getJMOHEP(i,1) - 1;
			int l = fillIndexVec(vec,idx1,idx2);
			//System.out.println("parent: "+i+" "+idx1+" "+idx2+" "+l);
			for (int j=0; j<l; j++)
			{
				checkAndAddDaughter(particle,ancestors,vec[j],i);
			}
		}
		// Deal with daughters
		for (int i=0; i<n; i++)
		{
			int idx1 = hepevt.getJDAHEP(i,0) % 10000 - 1;
			int idx2 = hepevt.getJDAHEP(i,1) % 10000 - 1;
			int l = fillIndexVec(vec,idx1,idx2);
			//System.out.println("child: "+i+" "+idx1+" "+idx2+" "+l);
			for (int j=0; j<l; j++)
			{
				checkAndAddDaughter(particle,ancestors,i,vec[j]);
			}
		}
		event.put(MCEvent.MC_PARTICLES,Arrays.asList(particle));
		// Add original stdhep event in case we want to write it out.
		event.put("StdhepEvent",hepevt);
		return event;
	}
	private void checkAndAddDaughter(BasicParticle[] particle, List<Set<BasicParticle>> ancestors, int parentID, int childID)
	{
		if (parentID == childID) return; // Can't be parent of self
		Set<BasicParticle> ancestor = ancestors.get(childID);
		boolean added = ancestor.add(particle[parentID]);
		if (added) particle[parentID].addDaughter(particle[childID]);
		//System.out.println("add "+parentID+" "+childID+" "+added);
	}
	private int fillIndexVec(int[] vec, int idx1, int idx2)
	{
		int l = 0;
		try
		{
			if ( idx1 >= 0 && idx2 >= 0 )
			{
				if ( idx1 < idx2 )
				{
					for ( int i = idx1; i < (idx2 + 1); i++ )
					{
						vec[l++] = i;
					}
				}
				else if ( idx1 > idx2 )
				{
					vec[l++] = idx1;
					vec[l++] = idx2;
				}
				// indices are equal
				else
				{
					vec[l++] = idx1;
				}
			}
			else if ( idx1 >= 0 )
			{
				vec[l++] = idx1;
			}
		}
		catch (ArrayIndexOutOfBoundsException x)
		{
			if (!haveWarned) System.err.println("Warning: Array index out of bounds exception caused by corrupt stdhep file ignored");
			haveWarned = true;
		}
		return l;
	}
}