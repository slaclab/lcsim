package org.lcsim.recon.tracking.ftf;
public class FtfGeneral
{
    //
    //    Some constants
    //
    static   float toDeg =57.29577951f ;
    
    static  double bFactor = 0.0029979 ;
    
    static boolean TRDEBUG = false;
    //
    //-->   Functions
    //
    
    static float  seta(float r, float z)
    {
        return (float)(3.0F * (z) / (Math.abs(z)+2.0F*(r)));
    }
    static float reta(float eta, float r)
    {
        return ((2.F*(r)*eta / ( 3 - Math.abs(eta)) )) ;
    }
    static float sgn( float a)
    {
        return (float)( ( (a) > 0   ) ? (1) :(-1) );
    }
    
}