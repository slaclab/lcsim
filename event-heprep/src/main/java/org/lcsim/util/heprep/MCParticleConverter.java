package org.lcsim.util.heprep;

import static java.lang.Math.abs;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.particle.properties.UnknownParticleIDException;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.awt.Color;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.MCParticle;
import org.lcsim.geometry.Detector;
import org.lcsim.util.swim.HelixSwimmer;
/**
 *
 * -Changed to allow no detector.  By default, now uses a B field of (0,0,0).
 * -Removed unused trackingZMax variable.  FIXME: Should be using zmax?
 * -Removed unused flags variable.  FIXME: Should be using collection flags?
 *
 * @author tonyj
 * @version $Id: MCParticleConverter.java,v 1.13 2011/11/14 23:02:14 jeremy Exp $
 */
class MCParticleConverter implements HepRepCollectionConverter
{
	private static final double[] IP = { 0,0,0 };
	//private boolean _noDetector = false;

	public boolean canHandle(Class k)
	{
		return MCParticle.class.isAssignableFrom(k);
	}
	public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
	{
		LCMetaData meta = event.getMetaData(collection);
		String name = meta.getName();
		//int flags = meta.getFlags();
		Detector detector = null;
		/*  try
        {
		 */
		try {
			detector = event.getDetector();
		}
		catch (Exception x)
		{}
		
		double trackingRMax = 10000;
		//double trackingZMax = 20000;

		if (detector != null) {
			trackingRMax = detector.getConstants().get("tracking_region_radius").getValue();
			//trackingZMax = detector.getConstants().get("tracking_region_zmax").getValue();
		}

		double ptMinCut = 0.05;
		double rCut = 1.0;
		double[] field;
		if (detector != null)
			field = detector.getFieldMap().getField(IP);
		else
			field = new double[3];
		
		HelixSwimmer helix = new HelixSwimmer(field[2]);

		HepRepType typeX = factory.createHepRepType(typeTree, name);
		typeX.addAttValue("layer",LCSimHepRepConverter.PARTICLES_LAYER);
		typeX.addAttValue("drawAs","Line");

		typeX.addAttDef("momentum","Particle Momentum", "physics", "GeV");
		typeX.addAttDef("energy","Particle Energy","physcs","GeV");
		typeX.addAttDef("pT","Particle Transverse Energy","physics","GeV");
		typeX.addAttDef("time","Particle Production Time","physics","nanoseconds");
		typeX.addAttDef("type","Particle Type", "physics", "");


		HepRepType neutralType = factory.createHepRepType(typeX, "Neutral");
		neutralType.addAttValue("color",Color.ORANGE);

		HepRepType photonType = factory.createHepRepType(neutralType, "Photon");
		photonType.addAttValue("color",Color.YELLOW);

		HepRepType neutrinoType = factory.createHepRepType(neutralType, "Neutrino");
		neutrinoType.addAttValue("color",Color.ORANGE);

		HepRepType neutralHadronType = factory.createHepRepType(neutralType, "Neutral hadron");
		neutralHadronType.addAttValue("color",Color.GREEN);

		HepRepType chargedType = factory.createHepRepType(typeX, "Charged");
		chargedType.addAttValue("color",Color.BLUE);

		HepRepInstance charged = factory.createHepRepInstance(instanceTree, typeX);
		HepRepInstance neutral = factory.createHepRepInstance(instanceTree, typeX);

		for (MCParticle p : (List<MCParticle>) collection)
		{
			try
			{
				Hep3Vector start = p.getOrigin();
				Hep3Vector momentum = p.getMomentum();
				double charge = p.getCharge();
				helix.setTrack(momentum, start, (int) charge);
				Hep3Vector stop;

				try
				{
					stop = p.getEndPoint();
					// Workaround for simdet
					if (stop.x() == 0 && stop.y() == 0 && stop.z() == 0)
					{
						if(p.getGeneratorStatus()==MCParticle.FINAL_STATE) stop = helix.getPointAtDistance(trackingRMax);
					}
				}
				catch (RuntimeException x)
				{
					// Use the helix swimmer to swim to end of tracking region
					if(p.getGeneratorStatus()==MCParticle.FINAL_STATE)
					{
						stop = helix.getPointAtDistance(trackingRMax);
					}
					else
					{
						stop = new BasicHep3Vector();
					}
				}

				if (charge == 0 || field[2] == 0)
				{
					HepRepInstance instanceX = factory.createHepRepInstance(charged, chargedType);
					if(charge == 0) 
					{
						int pdgId = p.getPDGID();
						//TODO are there nuetral types other than photon, neutrino, and neutral hadron?
						HepRepType type = neutralHadronType;
						if(isNeutrino(pdgId)) type = neutrinoType;
						if(abs(pdgId)==22) type = photonType;
						instanceX = factory.createHepRepInstance(neutral, type);
					}
//					HepRepInstance instanceX = factory.createHepRepInstance(charge == 0 ? neutral : charged, charge == 0 ? neutralType : chargedType);
					setDefaultAttValues(instanceX,p);

					factory.createHepRepPoint(instanceX,start.x(),start.y(),start.z());
					factory.createHepRepPoint(instanceX,stop.x(),stop.y(),stop.z());

				}
				else
				{
					double pT = Math.sqrt(momentum.x()*momentum.x()+momentum.y()*momentum.y());
					// if particle starts at origin and has no apprecaible pT, don't draw
					double r = Math.sqrt(start.x()*start.x()+start.y()*start.y());
					if(pT>ptMinCut || (pT<ptMinCut && r>rCut))
					{
						double dAlpha = 10; // 1cm
						HepRepInstance instanceX = factory.createHepRepInstance(charged, chargedType);

						setDefaultAttValues(instanceX,p);


						factory.createHepRepPoint(instanceX,start.x(),start.y(),start.z());
						double absZ = Math.abs(stop.z());
						double rSquared = stop.x()*stop.x()+stop.y()*stop.y();
						Hep3Vector point = start;

						for (int k = 1;k<200;k++)
						{
							double d = VecOp.sub(point,stop).magnitudeSquared();

							if (d < 2)
							{
								factory.createHepRepPoint(instanceX,stop.x(),stop.y(),stop.z());
								break;
							}
							else if (Math.abs(point.z()) > absZ ||
									point.x()*point.x()+point.y()*point.y() > rSquared)
							{
								break;
							}
							else
							{
								point = helix.getPointAtDistance(k*dAlpha);
								factory.createHepRepPoint(instanceX,point.x(),point.y(),point.z());
							}
						}
					}
				}
			}
			catch (UnknownParticleIDException x)
			{
				// Just ignore it for now.
			}
		}
		/*
        }
        catch(Exception ex)
        {
            // Just ignore it for now.
            if(!_noDetector)
            {
                System.out.println(ex);
                System.out.println("Cannot display MCParticles without a detector!");
                _noDetector = true;
            }
        }*/
	}

	boolean isNeutrino(int pdgId)
	{
		if(abs(pdgId)==12) return true;
		if(abs(pdgId)==14) return true;
		if(abs(pdgId)==16) return true;

		return false;
	}


	private void setDefaultAttValues(HepRepInstance instanceX, MCParticle p)
	{
		double x = p.getMomentum().x();
		double y = p.getMomentum().y();
		double pT = Math.sqrt(x*x + y*y);

		instanceX.addAttValue("pT",pT);
		instanceX.addAttValue("particle",p.getType().getName());
		instanceX.addAttValue("energy",p.getEnergy());
		instanceX.addAttValue("momentum",p.getMomentum().magnitude());
		instanceX.addAttValue("time",p.getProductionTime());
	}

}
