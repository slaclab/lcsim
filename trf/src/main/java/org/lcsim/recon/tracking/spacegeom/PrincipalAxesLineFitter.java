package org.lcsim.recon.tracking.spacegeom;
/**
 * Class for determining straight line parameters for 3D spacepoints
 * by solving for the principal axes of the moment of inertia tensor.
 * This version uses unweighted cells.
 * Array centroid is a point on the line.
 * Array dircos contains the direction cosines for the line.
 */
public class PrincipalAxesLineFitter
{
    //  private boolean _fit = false;
    private Matrix _tensor = new Matrix(3,3);
    private double[] _centroid = new double[3];
    private double[] _dircos = new double[3];
    private Matrix _dircov = new Matrix(3,3);
    private double _phi=0.;
    private double _theta=0.;
    
    /**
     * Default Constructor
     *
     */
    public PrincipalAxesLineFitter()
    {
    }
    
    /**
     * derive the line parameters given the moment of inertia tensor.
     * @param points the moment of inertia tensor
     */
    public void fit(double[][] points)
    {
        int npoints = points[0].length;
        // Calculate the centroid of the N points
        
        for(int i=0; i<3; ++i)
        {
            for(int j=0; j<npoints; ++j)
            {
                _centroid[i] += points[i][j];
            }
            _centroid[i]/=(double)npoints;
        }
        
        // Accumulate the tensor
        
        double dx=0.;
        double dy=0.;
        double dz=0.;
        double sumXX=0.;
        double sumXY=0.;
        double sumXZ=0.;
        double sumYY=0.;
        double sumYZ=0.;
        double sumZZ=0.;
        for(int i = 0; i<npoints; ++i)
        {
            dx = points[0][i] - _centroid[0];
            dy = points[1][i] - _centroid[1];
            dz = points[2][i] - _centroid[2];
            
            sumXX += dx*dx;
            sumXY += dx*dy;
            sumXZ += dx*dz;
            sumYY += dy*dy;
            sumYZ += dy*dz;
            sumZZ += dz*dz;
        }
        _tensor.set(0,0,   + sumYY+sumZZ);
        _tensor.set(1,0,   - sumXY);
        _tensor.set(2,0,   - sumXZ);
        _tensor.set(0,1,   - sumXY);
        _tensor.set(1,1,   + sumXX+sumZZ);
        _tensor.set(2,1,   - sumYZ);
        _tensor.set(0,2,   - sumXZ);
        _tensor.set(1,2,   - sumYZ);
        _tensor.set(2,2,   + sumXX+sumYY);
        
        // Calculate eigenvalues and eigenvectors
        Eigensystem es = _tensor.eigensystem();
        es.eigensort();
        
        //eigenvalues...
        Matrix eigenvalues = es.eigenvalues();
        //              System.out.println("Sorted (descending) eigenvalues of K:");
        //              eigenvalues.print(2);
        
        // eigenvectors...
        Matrix EVEC = es.eigenvectors();
        //              System.out.println("Eigenvectors of K sorted by descending eigenvalues:");
        //              EVEC.print(6);
        
        _phi = Math.atan2(-EVEC.at(1,2),-EVEC.at(0,2));
        _theta = Math.acos(-EVEC.at(2,2));
        
        _dircos[0] = -EVEC.at(0,2);
        _dircos[1] = -EVEC.at(1,2);
        _dircos[2] = -EVEC.at(2,2);
        // calculate the errors
        // Strandlie, Fruehwirth, NIM A 480 (2002) 734-740
        // Mardia, et al. Multivariate Analysis, Academic press, 6th printing, 1997
        //
        // Covariance matrix for normalized eigenvector is:
        //
        // V_i = (lambda_i/N) Sum(j!=i)[(lambda_j/(lambda_j-lambda_i)**2)gamma_j*gamma_j^T]
        //
        //                      Matrix cov = new Matrix(3,3);
        for(int i = 0; i<2; ++i)
        {
            Matrix tmp = EVEC.column(i).times(EVEC.column(i).transposed());
            tmp.times(eigenvalues.at(i,0)/(eigenvalues.at(i,0)-eigenvalues.at(2,0)));
            _dircov.plusEqual(tmp);
        }
        _dircov.times(eigenvalues.at(2,0)/npoints);
    }
    
    /**
     * Returns the (x, y, z) centroid of the set of points.
     * @return The centroid of the points
     */
    public double[] centroid()
    {
        double[] res = new double[3];
        System.arraycopy(_centroid, 0, res, 0, 3);
        return res;
    }
    
    /**
     * Returns the direction cosines of the set of points.
     * @return the direction cosines
     */
    public double[] dircos()
    {
        double[] res = new double[3];
        System.arraycopy(_dircos, 0, res, 0, 3);
        return res;
    }
    
    /**
     * Returns the uncertainty on the direction cosines.
     * This has not been tested
     * @return the uncertainty on the direction cosines
     */
    public Matrix dirCovariance()
    {
        return _dircov;
    }
    
    /**
     * The phi angle of the centroid
     * @return the phi angle of the centroid
     */
    public double phi()
    {
        return _phi;
    }
    
    /**
    * The theta angle of the centroid
     * @return the theta angle of the centroid
     */
    public double theta()
    {
        return _theta;
    }
}


