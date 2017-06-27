/*
 * HelixParameterCalculator.java
 *
 * Created on July 7th, 2008, 11:09 AM
 *
 * 
 */
package org.lcsim.fit.helicaltrack;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.constants.Constants;
import org.lcsim.event.MCParticle;
import org.lcsim.event.EventHeader;

/**
 * Class used for calculating MC particle track paramters
 * @author Pelham Keahey
 * 
 */
public class HelixParamCalculator  {

    private double R, p, pt, theta, arclength;
    //Some varibles are usesd in other calculations, thus they are global
    /*
    xc, yc --coordinates
    mcdca -- MC distance of closest approach 
    mcphi0 --azimuthal angle
    tanL -- Slope SZ plane ds/dz
    x0,y0 are the position of the particle at the dca
    */
    private double mcdca,mcphi0,tanL,z0;
    private double x0,y0;
    /**
     * Constructor that is fed a magnetic field and MCPARTICLE
     * @param mcpc
     * @param cBField
     */
    public HelixParamCalculator(MCParticle mcp, double BField)
    {
        
        //Calculate theta, the of the helix projected into an SZ plane, from the z axis
        double px = mcp.getPX();
        double py = mcp.getPY();
        double pz = mcp.getPZ();
        pt = Math.sqrt(px*px + py*py);
        p = Math.sqrt(pt*pt + pz*pz);
        double cth = pz / p;
        theta = Math.acos(cth);
       
        //Calculate Radius of the Helix
        R = mcp.getCharge() * pt / (Constants.fieldConversion * BField);
        
        //Slope in the Dz/Ds sense, tanL Calculation
        tanL = pz / pt;

        //  Azimuthal direction at origin
        double mcphi = Math.atan2(py, px);

        //Distance of closest approach Calculation
        double xc   = mcp.getOriginX() + R * Math.sin(mcphi);
        double yc   = mcp.getOriginY() - R * Math.cos(mcphi);

        double Rc = Math.sqrt(xc*xc + yc*yc);

            if(mcp.getCharge()>0)
            {
            mcdca = R - Rc;
            }
            else
            {
            mcdca = R + Rc;
            }
        
        
        //azimuthal calculation of the momentum at the DCA, phi0, Calculation
        mcphi0 = Math.atan2(xc/(R-mcdca), -yc/(R-mcdca));
            if(mcphi0<0)
            {
                mcphi0 += 2*Math.PI;
            }
        //z0 Calculation, z position of the particle at dca
        x0 = -mcdca*Math.sin(mcphi0);
        y0 = mcdca*Math.cos(mcphi0);
        arclength  = (((mcp.getOriginX()-x0)*Math.cos(mcphi0))+((mcp.getOriginY()-y0)*Math.sin(mcphi0)));
        z0 = mcp.getOriginZ() - arclength * tanL;
    
    }
    
    
    /*
    *  added 9/3/2012 by mgraham
    *   calculate the helix parameters given 3-momentum and a point (+ charge and field)  
   */            
    public HelixParamCalculator(Hep3Vector momentum, Hep3Vector origin, int charge,double BField)
    {
        
        //Calculate theta, the of the helix projected into an SZ plane, from the z axis
        double px = momentum.x();
        double py = momentum.y();
        double pz = momentum.z();
        pt = Math.sqrt(px*px + py*py);
        p = Math.sqrt(pt*pt + pz*pz);
        double cth = pz / p;
        theta = Math.acos(cth);
//        System.out.println("pt = "+pt+"; costh = "+cth);
       
        //Calculate Radius of the Helix
        R = charge* pt / (Constants.fieldConversion * BField);
        
        
        //Slope in the Dz/Ds sense, tanL Calculation
        tanL = pz / pt;

        //  Azimuthal direction at origin
        double mcphi = Math.atan2(py, px);

        //Distance of closest approach Calculation
        double xc   = origin.x() + R * Math.sin(mcphi);
        double yc   = origin.y() - R * Math.cos(mcphi);
//        System.out.println("xc = "+xc+"; yc = "+yc);
        double Rc = Math.sqrt(xc*xc + yc*yc);

            if(charge>0)
            {
            mcdca = R - Rc;
            }
            else
            {
            mcdca = R + Rc;
            }
        
        
        //azimuthal calculation of the momentum at the DCA, phi0, Calculation
        mcphi0 = Math.atan2(xc/(R-mcdca), -yc/(R-mcdca));
            if(mcphi0<0)
            {
                mcphi0 += 2*Math.PI;
            }
        //z0 Calculation, z position of the particle at dca
        x0 = -mcdca*Math.sin(mcphi0);
        y0 = mcdca*Math.cos(mcphi0);
        arclength  = (((origin.x()-x0)*Math.cos(mcphi0))+((origin.y()-y0)*Math.sin(mcphi0)));
        z0 =origin.z() - arclength * tanL;
    
    }
    /**
     * Calculates the B-Field from event
     * @param mcpc
     * @param eventc
     */
    public HelixParamCalculator(MCParticle mcpc,EventHeader eventc)
    {
        this(mcpc,eventc.getDetector().getFieldMap().getField(new BasicHep3Vector(0.,0.,0.)).z());
    }
    /**
     * Return the radius of the Helix track
     * @return double R
     */
    public double getRadius()
    {
        return R;
    }
    /**
     * Return the theta angle for the projection of the helix in the SZ plane 
     * from the  z axis
     * @return double theta
     */
    public double getTheta()
    {
        return theta;
    }
    /**
     * Return the particle's momentum
     * @return double mcp momentum
     */
    public double getMCMomentum()
    {
        return p;
    }
    /**
     * Return the curvature (omega)
     * @return double omega
     */
    public double getMCOmega()
    {     
        return 1. / R;
    }
    /**
     * Return the transvers momentum of the MC particle, Pt
     * @return double Pt
     */
    public double getMCTransverseMomentum()
    {
        return pt;
    }
    /**
     * Return the slope of the helix in the SZ plane, tan(lambda)
     * @return double tanL
     */
    public double getSlopeSZPlane()
    {
        return tanL;
    }
    /**
     * Return the distance of closest approach
     * @return double mcdca
     */
    public double getDCA()
    {
      return mcdca;
    }
    /**
     * Return the azimuthal angle of the momentum when at the position of closest approach
     * @return double mcphi0
     */
    public double getPhi0()
    {
      return mcphi0;
    }
    /**
     * Return the z position at the distance of closest approach
     * @return double z0 position
     */
    public double getZ0()
    {
        return z0;
    }
    /**
     * Return the arclength of the helix from the ORIGIN TO THE DCA
     * @return double arclength
     */
    public double getArcLength()
    {
        return arclength;
    }
    /**
     * Return the x position of the particle when at the dca
     * @return double arclength
     */
    public double getX0()
    {
        return x0;
    }
    /**
     * Return the y position of the particle at the dca
     * @return double arclength
     */
    public double getY0()
    {
        return y0;
    }
    
}
