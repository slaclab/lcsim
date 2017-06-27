/*
 * InterpolatedHMatrixProcessor.java
 *
 * Created on June 5, 2008, 9:46 AM
 *
 * $Id: InterpolatedHMatrixProcessor.java,v 1.4 2008/06/06 00:36:15 ngraf Exp $
 */

package org.lcsim.cal.calib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.lcsim.recon.emid.hmatrix.HMatrix;

/**
 * This is a convenience class to process HMatrix files gerenerated at a discrete set of
 * angles and energies and produce a properties file which can be used by the
 * InterpolatedHMatrix class.
 * @author Norman Graf
 */
public class InterpolatedHMatrixProcessor
{
    public static void main(String[] args) throws Exception
    {
        if(args.length<2)
        {
            usage();
            return ;
        }
        
        String listOfFiles = args[0];
        String detectorName = args[1];
        
        List<File> filesToProcess = filesToProcess(listOfFiles);
        int numToProcess = filesToProcess.size();
        double energy = 0.;
        double theta = 0.;
        
        // Sets to hold the angles and energies
        SortedSet<Double> angles = new TreeSet<Double>();
        SortedSet<Double> energies = new TreeSet<Double>();
        
        // Map to hold all the arrays of numbers
        Map<String, double[]> avMap = new TreeMap<String, double[]>();
        Map<String, double[]> covMap = new HashMap<String, double[]>();
        
        int hMatrixDimensionality = 0;
        for(File f : filesToProcess)
        {
            String[] parts = f.getName().split("_");
            for(String s : parts)
            {
                if(s.startsWith("Theta"))
                {
                    theta = Double.parseDouble(s.substring(5));
                }
                if(s.contains("GeV"))
                {
                    energy = Double.parseDouble(s.substring(0,s.length()-3));
                }
            }
            angles.add(theta);
            energies.add(energy);
            String key = "Theta_"+theta+"_Energy_"+energy;
            
            HMatrix h = HMatrix.read(f.getName());
            hMatrixDimensionality = h.averageVector().length;
            avMap.put(key, h.averageVector());
            covMap.put(key, h.packedInverseCovarianceMatrix());
            
        } // end of loop over files...
        
        // let's write this stuff out to a properties file
        FileOutputStream fos = new FileOutputStream(detectorName+"_HMatrices.properties");
        Writer w =  new BufferedWriter(new OutputStreamWriter(fos));
        
        // add some provenance to the file...
        w.write("# "+detectorName+" "+comment()+"\n");
        
        w.write("Dimensionality = "+hMatrixDimensionality+"\n");
        // the energies and angles...
        w.write("ThetaVals  = "+stripBrackets(angles.toString())+"\n");
        w.write("EnergyVals = "+stripBrackets(energies.toString())+"\n");
        
        // array of averages...
        Set<String> keys = avMap.keySet();
        for(String s : keys)
        {
            double[] vals = avMap.get(s);
            w.write(s+"_vals = "+stripBrackets(Arrays.toString(vals))+"\n");
        }
        // packed array of inverse covariance matrix
        keys = covMap.keySet();
        for(String s : keys)
        {
            double[] covs = covMap.get(s);
            w.write(s+"_covs = "+stripBrackets(Arrays.toString(covs))+"\n");
        }
        w.flush();
        w.close();
    }
    
    static void usage()
    {
        System.out.println("This is InterpolatedHMatrixProcessor");
        System.out.println("usage:");
        System.out.println("java InterpolatedHMatrixProcessor listOfInputHMatrixFiles detectorname");
        System.out.println("  files in the list should have the form: ");
        System.out.println("   detectorName_ThetaX_YGeV_Z.hmx");
    }
    
    static List<File> filesToProcess(String listOfFiles) throws Exception
    {
        List<File> filesToProcess = new ArrayList<File>();
        FileInputStream fin =  new FileInputStream(listOfFiles);
        BufferedReader br =  new BufferedReader(new InputStreamReader(fin));
        String line;
        
        while ( (line = br.readLine()) != null)
        {
            File f = new File(line.trim());
            if(!f.exists()) throw new RuntimeException("Input file "+f+ " does not exist!");
            filesToProcess.add(f);
        }
        
        return filesToProcess;
    }
    
    static String stripBrackets(String s)
    {
        String bad = "[]";
        String result = "";
        for ( int i = 0; i < s.length(); i++ )
        {
            if (  bad.indexOf(s.charAt(i)) < 0  )
                result += s.charAt(i);
        }
        return result;
    }
    
    static String comment()
    {
        Calendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        DecimalFormat formatter = new DecimalFormat("00");
        String day = formatter.format(cal.get(Calendar.DAY_OF_MONTH));
        String month =  formatter.format(cal.get(Calendar.MONTH)+1);
        String myDate =cal.get(Calendar.YEAR)+month+day;
        return myDate+" "+System.getProperty("user.name");
    }
    
}