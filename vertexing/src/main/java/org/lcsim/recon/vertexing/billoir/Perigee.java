package org.lcsim.recon.vertexing.billoir;
/**
 * @version $Id: Perigee.java,v 1.2 2006/03/28 23:50:27 jstrube Exp $
 */
public class Perigee
{
    //attributes
    
    private double[] _par;
    //                          0 : epsilon (impact par. in xy projection, with sign)
    //                          1 : z coordinate
    //                          2 : theta angle
    //                          3 : phi angle
    //                          4 : 1/r (r = radius of curvature, with sign)
    private double[] _cov;           // cov(0:15) :  covariance matrix of par (in lower diagonal form)
    private double[] _wgt;           // wgt(0:15) :  weight matrix of par (inverse of cov)
    
    public Perigee( double[] par, double[] cov, double[] wgt)
    {
        _par = new double[5];
        System.arraycopy(par,0,_par,0,5);
        _cov = new double[15];
        System.arraycopy(cov,0,_cov,0,15);
        _wgt = new double[15];
        System.arraycopy(wgt,0,_wgt,0,15);
    }
    
    public double[] par()
    {
        return _par;
    }
    
    public double[] cov()
    {
        return _cov;
    }
    
    public double[] wgt()
    {
        return _wgt;
    }
}