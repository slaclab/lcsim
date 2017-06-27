/*
 * CentralMomentsCalculator.java
 *
 * Created on April 5, 2006, 11:25 AM
 *
 * $Id: CentralMomentsCalculator.java,v 1.1.1.1 2010/11/30 21:32:00 jeremy Exp $
 */

package org.lcsim.math.moments;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * calculates rotational and translational invariant moments of spatial distributions
 * @author Norman Graf
 */
public class CentralMomentsCalculator
{
    // 0
    private double m000;
    
    // 1
    private double m100, m010, m001;
    
    // 2
    private double m110, m101, m011;
    private double m200, m020, m002;
    
    // centroids
    private double xc, yc, zc;
    
    // invariants
    private double J1, J2, J3;
    
    
    
    private Matrix _tensor = new Matrix(3,3);
    
    // eigenvalues
    private double[] _eigenvalues = new double[3];
    
    // eigenvectors
    private Matrix _eigenvectors;
    
    // direction cosines for the cluster direction
    private double[] _dircos = new double[3];

    
    /**
     * Creates a new instance of CentralMomentsCalculator
     */
    public CentralMomentsCalculator()
    {
    }
    
    public void calculateMoments(double[] x, double[] y, double[] z, double[] w)
    {
        reset();
        //TODO make this more efficient.
        int n = x.length;
        // TODO check that all arrays are the same size.
        
        // calculate centroids
        for(int i=0; i<n; ++i)
        {
            m000 += w[i];
            m100 += x[i]*w[i];
            m010 += y[i]*w[i];
            m001 += z[i]*w[i];
        }
        
        xc = m100/m000;
        yc = m010/m000;
        zc = m001/m000;
        
        // on to the higher moments wrt centroid
        double xa, ya, za;
        for(int i=0; i<n; ++i)
        {
            xa = x[i]-xc;
            ya = y[i]-yc;
            za = z[i]-zc;
            
            m110 += xa*ya*w[i];
            m101 += xa*za*w[i];
            m011 += ya*za*w[i];
            
            m200 += xa*xa*w[i];
            m020 += ya*ya*w[i];
            m002 += za*za*w[i];
        }
        
        // normalize
        m110 /= m000;
        m101 /= m000;
        m011 /= m000;
        
        m200 /= m000;
        m020 /= m000;
        m002 /= m000;
        
        J1 = m200 + m020 + m002;
        J2 = m200*m020 + m200*m002 + m020*m002 - m110*m110 - m101*m101 - m011*m011;
        J3 = m200*m020*m002 + 2.*m110*m101*m011 - m002*m110*m110 - m020*m101*m101 - m200*m011*m011;
        
        // now for eigenvalues, eigenvectors
        _tensor.set(0, 0,    m020 + m002);
        _tensor.set(1,0,   - m110);
        _tensor.set(2,0,   - m101);
        _tensor.set(0,1,   - m110);
        _tensor.set(1,1,   + m200 + m002);
        _tensor.set(2,1,   - m011);
        _tensor.set(0,2,   - m101);
        _tensor.set(1,2,   - m011);
        _tensor.set(2,2,   + m200 + m020);
        
        EigenvalueDecomposition eig = _tensor.eig();
        _eigenvalues = eig.getRealEigenvalues();
//        System.out.println("eigenvalues: "+_eigenvalues[0]+" "+_eigenvalues[1]+" "+_eigenvalues[2]);
        _eigenvectors = eig.getV();
//        System.out.println("eigenvectors:");
//        _eigenvectors.print(4,4);
        
        // direction cosines are the eigenvector elements corresponding to the lowest eigenvalue
        // note that eigenvalues are sorted in ascending order by EigenvalueDecomposition
        _dircos[0] = -_eigenvectors.get(0,0);
        _dircos[1] = -_eigenvectors.get(1,0);
        _dircos[2] = -_eigenvectors.get(2,0);
//        System.out.println("dircos: "+_dircos[0]+" "+_dircos[1]+" "+_dircos[2]);
 
    }
    
    public double[] eigenvalues()
    {
        return _eigenvalues;
    }
    
    public double[] directionCosines()
    {
        return _dircos;
    }
    
    public double[] centroid()
    {
        return new double[] {xc, yc, zc};
    }
    
    public double[] centroidVariance()
    {
        return new double[] {m200, m020, m002, m110, m101, m011};
    }
    
    public double[] invariants()
    {
        
        return new double[] {J1, J2, J3};
    }
    
    void reset()
    {
        m000 = 0.;
        m100 = 0.;
        m010  = 0.;
        m001  = 0.;
        m110  = 0.;
        m101  = 0.;
        m011  = 0.;
        m200  = 0.;
        m020  = 0.;
        m002  = 0.;
    }
    
}
