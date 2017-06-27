package org.lcsim.recon.tracking.trfxyp;
import java.util.Random;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.SimInteractor;
/**
 * Class for adding Multiple Scattering to track vectors defined at
 * XYPlanes.  Single point interaction is assumed.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ThinXYPlaneMsSim extends SimInteractor
{
    
    // static attributes
    private static final int IV = SurfXYPlane.IV;
    private static final int IZ = SurfXYPlane.IZ;
    private static final int IDVDU = SurfXYPlane.IDVDU;
    private static final int IDZDU = SurfXYPlane.IDZDU;
    private static final int IQP = SurfXYPlane.IQP;
    
    //attributes
    // radiation lengths in material
    private double _radLength;
    
    // random number generator
    private Random _r;
    
    /**
     * Construct an instance from the number of radiation
     * lengths of the thin xy plane material.
     * The Interactor is constructed with the
     * appropriate number of radiation lengths.
     *
     * @param   radLength The thickness of the material in radiation lengths.
     */
    public ThinXYPlaneMsSim( double radLength )
    {
        _radLength = radLength;
        _r = new Random();
    }
    
    /**
     *Interact the given track in this thin xy plane,
     *using the thin material approximation for multiple scattering.
     *Note that the track parameters are modified to simulate
     *the effects of multiple scattering in traversing the thin xy
     *plane of material.
     *
     * @param   vtrk  The Vrack to scatter.
     */
    public void interact( VTrack vtrk)
    {
        
        TrackVector trv = new TrackVector( vtrk.vector() );
        // first, how much should the track be scattered:
        // uses radlength from material and angle track - plane (layer)
        double trackMomentum = trv.get(IQP);
        
        double f = trv.get(IDVDU);
        double g = trv.get(IDZDU);
        
        // here theta does not correspond to the spherical theta as well as x/y/zhat
        // do not correspond to their track direction in x/y/z
        double theta = Math.atan(Math.sqrt(f*f + g*g));
        double phi = 0.;
        if (f != 0.) phi = Math.atan(Math.sqrt((g*g)/(f*f)));
        if (f == 0.0 && g < 0.0) phi = 3.*Math.PI/2.;
        if (f == 0.0 && g > 0.0) phi = Math.PI/2.;
        if (f == 0.0 && g == 0.0)
        {
            phi = 99.;// that we can go on further.....
            System.out.println(" DVDU and DZDU both 0");
        }
        
        if((f<0)&&(g>0))
            phi = Math.PI - phi;
        if((f<0)&&(g<0))
            phi = Math.PI + phi;
        if((f>0)&&(g<0))
            phi = 2*Math.PI - phi;
        
        double trueLength = _radLength/Math.cos(theta);
        
        double scatRMS = (0.0136)*trackMomentum*Math.sqrt(trueLength)*
                (1 + 0.038*Math.log(trueLength));
        
        double zhat = Math.sqrt(1-Math.sin(theta)*Math.sin(theta));
        double xhat = Math.sin(theta)*Math.cos(phi);
        double yhat = Math.sin(theta)*Math.sin(phi);
        
        double[] scatterVec = new double[3];
        double[] finalVec = new double[3];
        double[][] Rotation = new double[3][3];
        
        //set Rotation matrix as given in D0note ????
        Rotation[0][0] = -yhat;
        Rotation[0][1] = -zhat*xhat;
        Rotation[0][2] = xhat;
        Rotation[1][0] = xhat;
        Rotation[1][1] = -zhat*yhat;
        Rotation[1][2] = yhat;
        Rotation[2][0] = 0;
        Rotation[2][1] = xhat*xhat+yhat*yhat;
        Rotation[2][2] = zhat;
        
        //now set the Vector after scattering ( (0,0,1) ->( theta1, theta2, 1)*norm)
        scatterVec[0] = scatRMS*_r.nextGaussian();
        scatterVec[1] = scatRMS*_r.nextGaussian();
        scatterVec[2] = 1.0;
        
        double norm = Math.sqrt(scatterVec[0]*scatterVec[0] + scatterVec[1]*scatterVec[1]
                + scatterVec[2]*scatterVec[2]);
        
        if (norm!=0)
        {
            scatterVec[0] /= norm;
            scatterVec[1] /= norm;
            scatterVec[2] /= norm;
        };
        
        //now go back to the global coordinate system if necessary (not if f=g=0)
        double finalvu;
        double finalzu;
        if (phi != 99.)
        {
            for (int k = 0; k<3; k++)
            {
                finalVec[k] = 0.;
                for (int l = 0; l<3 ; l++)
                {
                    finalVec[k] += Rotation[k][l]*scatterVec[l];
                }
            }
            finalvu = finalVec[0]/finalVec[2];
            finalzu = finalVec[1]/finalVec[2];
        }
        else
        {
            finalvu = scatterVec[0];
            finalzu = scatterVec[1];
        };
        
        
        // calculate new qpt
        //qpt' = qpt * sin theta'/sin theta
        double zqprime = trackMomentum;
        if (trv.get(IDZDU)!=0.&&finalzu!=0.)
        {
            zqprime = trackMomentum*(Math.sin(Math.atan(1./finalzu)))/
                    Math.sin(Math.atan(1./trv.get(IDZDU)));
        }
        if (trv.get(IDZDU)==0.&&finalzu==0.)  zqprime = trackMomentum;
        if (trv.get(IDZDU)==0.&&finalzu!=0.)  zqprime = trackMomentum*(Math.sin(Math.atan(1./finalzu)));
        if (trv.get(IDZDU)!=0.&&finalzu==0.)  zqprime = trackMomentum/Math.sin(Math.atan(1./trv.get(IDZDU)));
        
        trv.set(IDVDU, finalvu);
        trv.set(IDZDU, finalzu);
        trv.set(IQP, zqprime);
        
        // assume that we don't encounter back-scattering... which is
        // assumed above anyway.
        vtrk.setVectorAndKeepDirection( trv );
    }
    
    /**
     * Make a clone of this object.
     * Note that new copy will have a different random number generator.
     *
     * @return A Clone of this instance.
     */
    public SimInteractor newCopy()
    {
        return new ThinXYPlaneMsSim(_radLength);
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public double radLength()
    {
        return _radLength;
    }
    
}


