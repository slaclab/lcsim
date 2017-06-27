package org.lcsim.spacegeom;

import java.io.PrintStream;
/**
 *
 *@version $Id: Eigensystem.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
class Eigensystem
{
    
    protected Matrix _eigenvalue;
    protected Matrix _eigenvector;
    protected int _index[];
    
    public Eigensystem()
    {
    }
    
    public void eigensort()
    {
        if(_eigenvalue == null)
        {
            System.err.println("Eigensystem::eigensort - No eigenvalues to sort.");
            System.exit(0);
        }
        if(_eigenvector == null)
        {
            System.err.println("Eigensystem::eigensort - No eigenvectors to sort.");
            System.exit(0);
        }
        int j1 = _eigenvalue.rows();
        _index = new int[j1];
        for(int i = 0; i < j1; i++)
            _index[i] = i;
        
        for(int j = 0; j < j1 - 1; j++)
        {
            int i1 = j;
            double d = _eigenvalue.at(i1, 0);
            for(int k = j + 1; k < j1; k++)
                if(_eigenvalue.at(k, 0) >= d)
                {
                i1 = k;
                d = _eigenvalue.at(i1, 0);
                }
            
            if(i1 != j)
            {
                int k1 = _index[i1];
                _index[i1] = _index[j];
                _index[j] = k1;
                _eigenvalue.set(i1, 0, _eigenvalue.at(j, 0));
                _eigenvalue.set(j, 0, d);
                for(int l = 0; l < j1; l++)
                {
                    double d1 = _eigenvector.at(l, j);
                    _eigenvector.set(l, j, _eigenvector.at(l, i1));
                    _eigenvector.set(l, i1, d1);
                }
                
            }
        }
        
    }
    
    public double eigenvalue(int i)
    {
        if(_eigenvalue == null)
        {
            System.err.println("Eigensystem::getEigenvalue - No vector of eigenvalues availible.");
            System.exit(0);
        }
        int j = _eigenvalue.rows();
        if(i > j - 1 || i < 0)
        {
            System.err.println("Eigensystem::getEigenvalue - Invalid index specified.");
            System.exit(0);
        }
        return _eigenvalue.at(i, 0);
    }
    
    public Matrix eigenvalues()
    {
        return _eigenvalue;
    }
    
    public Matrix eigenvector(int i)
    {
        if(_eigenvector == null)
        {
            System.err.println("Eigensystem::getEigenvector - No matrix of eigenvectors availible.");
            System.exit(0);
        }
        int j = _eigenvalue.rows();
        if(i > j - 1 || i < 0)
        {
            System.err.println("Eigensystem::getEigenvector - Invalid index specified.");
            System.exit(0);
        }
        Matrix matrix = _eigenvector.submatrix(j, 1, 0, i);
        return matrix;
    }
    
    public Matrix eigenvectors()
    {
        return _eigenvector;
    }
    
    public int[] index()
    {
        return _index;
    }
    
    public void setEigenvalues(Matrix matrix)
    {
        if(matrix.cols() != 1)
        {
            System.err.println("Eigensystem::setEigenvalues -> Invalid dimension.");
            System.exit(0);
        }
        _eigenvalue = matrix;
    }
    
    public void setEigenvectors(Matrix matrix)
    {
        _eigenvector = matrix;
    }
    
    public void setIndex(int ai[])
    {
        _index = ai;
    }
}
