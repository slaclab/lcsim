/*
 * Calibrate.java
 *
 * Created on May 19, 2008, 11:50 AM
 *
 * $Id: Calibrate.java,v 1.8 2008/05/21 20:30:56 ngraf Exp $
 */

package org.lcsim.cal.calib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;
import org.lcsim.util.loop.LCIOEventSource;
import org.lcsim.util.loop.LCSimLoop;

/**
 *
 * @author Norman Graf
 */
public class Calibrate
{
    
    /** Creates a new instance of Calibrate */
    public Calibrate()
    {
    }
    
    public static void main(String[] args) throws Exception
    {
        // remind users of correct calling sequence
        if(args.length<2)
        {
            usage();
            return ;
        }
        
        // which Driver should we run?
        String driverName = args[0];
        Driver driver = Calibrate.createDriver(driverName);
        if(driver == null)
        {
            return;
        }
        
        // which files to process?
        String listOfFiles = args[1];
        List<File> filesToProcess = null;
        LCIOEventSource src = null;
        try
        {
            filesToProcess = filesToProcess(listOfFiles);
        }
        catch (FileNotFoundException x)
        {
            System.out.println("File "+filesToProcess+ " not found!");
            return;
        }
        try
        {
            src = new LCIOEventSource("Calibrate", filesToProcess);
            
            int numToProcess=-1;
            if(args.length>2) numToProcess=Integer.parseInt(args[2]);
            
            System.out.println("Processing "+(numToProcess<0 ? "all": numToProcess) +" events from "+listOfFiles+" using "+ driverName );
            LCSimLoop loop = new LCSimLoop();
            loop.setLCIORecordSource(src);
            System.out.println("adding the driver");
            loop.add(driver);
            System.out.println("looping");
            try
            {
                loop.loop(numToProcess);
            }
            catch(Exception e)
            {
                System.out.println("Error during event processing loop");
                e.printStackTrace();
                return;
            }
            System.out.println("done looping");
            loop.dispose();
            
            //remove suffix from list of filenames
            int truncate = listOfFiles.lastIndexOf(".");
            String listOfFilesName = listOfFiles.substring(0,truncate);

            String defaultAidaFileName = listOfFilesName+"_"+driver.getClass().getSimpleName()+"_"+date()+".aida";
            System.out.println("aida file written to "+defaultAidaFileName);
            AIDA.defaultInstance().saveAs(defaultAidaFileName);
        }
        catch (IOException x)
        {
            System.out.println("Experienced an IOException during");
            x.printStackTrace();
            return;
        }
    }
    
    public static void usage()
    {
        System.out.println("This is Calibrate");
        System.out.println("usage:");
        System.out.println("java Calibrate fullyQualifiedDriverName listOfInputFiles [number of events to process]");
    }
    
    public static Driver createDriver(String name)
    {
        // We're given the (string) name of a driver class.
        // First, make a Class object for that class.
        // If the named class doesn't exist, this will throw
        // a ClassNotFoundException:
        Class newClassObject = null;
        try
        {
            newClassObject = Class.forName(name);
        }
        catch (java.lang.ClassNotFoundException x)
        {
            System.out.println("\nYour Driver -- "+name+" -- is not recognized as a valid class \n");
            System.out.println("Is it fully qualified? Please check the full package name.");
            System.out.println("Is it in your classpath?\n");
            return null;
            // throw new AssertionError(x);
        }
        
        // Next, create an instance of the class:
        // This can throw InstantiationException, IllegalAccessException
        Object newObject = null;
        try
        {
            newObject = newClassObject.newInstance();
        }
        catch (java.lang.InstantiationException x)
        {
            throw new AssertionError(x);
        }
        catch (java.lang.IllegalAccessException x )
        {
            throw new AssertionError(x);
        }
        
        // This better be a driver. Cast it:
        Driver newDriver = (Driver) newObject;
        
        // OK. Now return it.
        return newDriver;
    }
    
    
    public static List<File> filesToProcess(String listOfFiles) throws Exception
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
    private static String date()
    {
        Calendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        DecimalFormat formatter = new DecimalFormat("00");
        String day = formatter.format(cal.get(Calendar.DAY_OF_MONTH));
        String month =  formatter.format(cal.get(Calendar.MONTH)+1);
        return cal.get(Calendar.YEAR)+month+day;
    }
    
}
