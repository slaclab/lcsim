/*
 * MCReconstructedParticle.java
 *
 * Created on February 6, 2007, 1:09 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.lcsim.event.base;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.event.MCParticle;
import org.lcsim.event.ParticleID;

/**
 *
 * @author cassell
 */
public class MCReconstructedParticle extends BaseReconstructedParticle
{
    MCParticle pp;
    /** Creates a new instance of MCReconstructedParticle */
    public MCReconstructedParticle(MCParticle p)
    {
        super(p.getEnergy(),p.getMomentum());
        super.setMass(p.getMass());
        super.setCharge(p.getCharge());
        super.setReferencePoint(p.getOrigin());
        super.setGoodnessOfPid(1.);
        ParticleID pid = new CheatParticleID(p.getPDGID());
        super.setParticleIdUsed(pid);
        super.addParticleID(pid);
        pp = p;
    }
    public void useMassAndP(double mass)
    {
        super.setMass(mass);
        Hep3Vector mom = super.getMomentum();
        double E = Math.sqrt(mass*mass+mom.x()*mom.x()+mom.y()*mom.y()+mom.z()*mom.z());
        super.set4Vector(new BasicHepLorentzVector(E,mom));
    }
    public void useMassAndE(double mass)
    {
        super.setMass(mass);
        Hep3Vector mom = super.getMomentum();
        double pmag = Math.sqrt(mom.x()*mom.x()+mom.y()*mom.y()+mom.z()*mom.z());
        double norm = 0.;
        if(super.getEnergy() > mass)
        {
            norm = Math.sqrt(super.getEnergy()*super.getEnergy()-mass*mass)/pmag;
        }
        Hep3Vector newmom = new BasicHep3Vector(mom.x()*norm, mom.y()*norm, mom.z()*norm);
        super.set4Vector(new BasicHepLorentzVector(super.getEnergy(),newmom));
    }
    public MCParticle getMCParticle(){return pp;}
}
