package org.lcsim.analysis;

import hep.aida.ITree;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.HepLorentzVector;
import hep.physics.vec.VecOp;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import static java.lang.Math.abs;

/**
 *
 * @author Norman A Graf
 * 
 *  @version $Id:
 */
public class HiggsRecoilMassAnalysis extends Driver
{
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree;
    
    // analysis-specific variables
    private String _reconstructedParticleListName = "PandoraPFOCollection";
    private double _minMuonMomentum = 20.;
    private double _cmsEnergy = 250.;
    /** Creates a new instance of HiggsRecoilAnalysis */
    public HiggsRecoilMassAnalysis()
    {
    }
    
    protected void process(EventHeader event)
    {
//       Fetch the list of ReconstructedParticles...
         List<ReconstructedParticle> rpList = event.get(ReconstructedParticle.class, _reconstructedParticleListName);      
//       Create a collection to hold muons found in this event
        List<ReconstructedParticle> muons = new ArrayList<ReconstructedParticle>();
        
        for (ReconstructedParticle rp : rpList)
        {
            if(abs(rp.getType())==13)
            {
                if(rp.getMomentum().magnitude()>_minMuonMomentum)
                {
                    muons.add(rp);
                    aida.cloud1D("Muon Momentum").fill(rp.getMomentum().magnitude());
                }
            }
        }
//     Calculate the invariant mass of the two muons
       double invMass = 0.;
//        
//     create the Z
       HepLorentzVector Z = new  BasicHepLorentzVector();
//        
        aida.cloud1D("number of muons passing cut").fill(muons.size());
//        
        if(muons.size()==2)
        {
            for(int i=0; i<2; ++i)
            {
                ReconstructedParticle mu = muons.get(i);
                Z = VecOp.add(Z, mu.asFourVector());
            }
            
            invMass = Z.magnitude();
            aida.cloud1D("Dimuon invariant mass").fill(invMass);
            
            // calculate the recoil mass if we have a Z
            double zMass = 91.;
            double zMassWindow = 5.;
            if(Math.abs(zMass-invMass)<zMassWindow)
            {
                // M_recoil = sqrt( s^2 - 2*sqrt(s)*E_ll + M^2_ll )                
                double s = _cmsEnergy;
                double recoilMass = Math.sqrt(s*s -(2.*s*Z.t()) + Z.magnitudeSquared());
                aida.cloud1D("Recoil mass").fill(recoilMass);
                aida.histogram1D("Recoil mass binned and weighted", 100, 100.,150.).fill(recoilMass,event.getWeight());
            }
        }
    }
    
    public void setReconstructedParticleListName(String rplname)
    {
        _reconstructedParticleListName = rplname;
    }
    
    public void setMinMuonMomentum( double minMomentum)
    {
        _minMuonMomentum = minMomentum;
    }
    public void setCMSEnergy(double cmsEnergy)
    {
        _cmsEnergy = cmsEnergy;
    }
}

