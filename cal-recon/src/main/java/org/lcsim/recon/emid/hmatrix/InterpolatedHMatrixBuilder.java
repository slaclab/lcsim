/*
 * InterpolatedHMatrixBuilder.java
 *
 * Created on June 5, 2008, 11:54 PM
 *
 * $Id: InterpolatedHMatrixBuilder.java,v 1.1 2008/06/06 07:27:32 ngraf Exp $
 */

package org.lcsim.recon.emid.hmatrix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.math.interpolation.BilinearInterpolator;

/**
 * This class uses data stored in a properties file to create an InterpolatedHMatrix
 * which is able to calculate a cluster "chi-squared" for arbitrary values of theta and 
 * energy
 *
 * @author Norman Graf
 */
public class InterpolatedHMatrixBuilder
{
    static final boolean debug = false;
    
    /**
     * Given a detectorname, this method will locate the appropriate
     * properties file and create an HMatrix which interpolates between 
     * the discrete points in theta and energy.
     *
     * @param detectorName the name of a supported detector
     * @return an InterpolatedHMatrix
     */
    public static InterpolatedHMatrix build(String detectorName)
    {
        String conditionsSetName = detectorName+"_HMatrices";
        ConditionsManager mgr = ConditionsManager.defaultInstance();
        try
        {
            mgr.setDetector(detectorName, 0);
        }
        catch( ConditionsNotFoundException e)
        {
            System.out.println("Conditions not found for detector "+mgr.getDetector());
            System.out.println("Please check that this properties file exists for this detector ");
            throw new RuntimeException("Conditions not found for detector "+mgr.getDetector());
        }
        ConditionsSet cs = null;
        try
        {
            cs = mgr.getConditions(conditionsSetName);
        }
        catch(ConditionsSetNotFoundException e)
        {
            System.out.println("ConditionSet "+conditionsSetName+" not found for detector "+mgr.getDetector());
            System.out.println("Please check that this properties file exists for this detector ");
            throw new RuntimeException("ConditionSet "+conditionsSetName+" not found for detector "+mgr.getDetector());
            
        }
        
        int dim = cs.getInt("Dimensionality");
        double[] energies = cs.getDoubleArray("EnergyVals");
        int numEnergies = energies.length;
        if(debug) System.out.println("energies: "+Arrays.toString(energies));
        double[] angles = cs.getDoubleArray("ThetaVals");
        int numAngles = angles.length;
        if(debug) System.out.println("angles: "+Arrays.toString(angles));
        
        // Maps to hold all the arrays of numbers keyed on theta and energy
        Map<String, double[]> avMap = new TreeMap<String, double[]>();
        Map<String, double[]> covMap = new HashMap<String, double[]>();
        
        for(double theta : angles)
        {
            for( double energy : energies)
            {
                String key = "Theta_"+theta+"_Energy_"+energy;
                avMap.put(key, cs.getDoubleArray(key+"_vals"));
                covMap.put(key, cs.getDoubleArray(key+"_covs"));
                if(debug) System.out.println(key);
            }
        }
        
        // for each theta and energy point, we have:
        // n           average values
        // n*(n+1)/2   covariance values (packed)
        //
        // will need to create a bilinearInterpolator for each of these (n^2+3n)/2 parameters
        
        // order is theta, energy
        double[][] tmp = new double[numAngles][numEnergies];
        BilinearInterpolator[] valInterpolators = new BilinearInterpolator[dim];
        
        // mean vector [dim]
        for (int iVal=0; iVal<dim; ++iVal)
        {
            for(int i=0; i<numAngles; ++i)
            {
                for(int j=0; j<numEnergies; ++j)
                {
                    String key = "Theta_"+angles[i]+"_Energy_"+energies[j];
                    tmp[i][j] = avMap.get(key)[iVal];
                    if(debug) System.out.println(key +"_"+iVal+" "+  tmp[i][j]);
                }
            }
            valInterpolators[iVal] = new BilinearInterpolator(angles, energies, tmp);
        }
        
        // cov matrix elements [(dim*(dim+1))/2]
        int covDim = (dim*(dim+1))/2;
        BilinearInterpolator[] covInterpolators = new BilinearInterpolator[(dim*(dim+1))/2];
        
        for (int iVal=0; iVal<covDim; ++iVal)
        {
            for(int i=0; i<numAngles; ++i)
            {
                for(int j=0; j<numEnergies; ++j)
                {
                    String key = "Theta_"+angles[i]+"_Energy_"+energies[j];
                    tmp[i][j] = covMap.get(key)[iVal];
                    if(debug) System.out.println(key +"_"+iVal+" "+  tmp[i][j]);
                }
            }
            covInterpolators[iVal] = new BilinearInterpolator(angles, energies, tmp);
        }
        
        return new InterpolatedHMatrix(dim, valInterpolators, covInterpolators);
    }
}