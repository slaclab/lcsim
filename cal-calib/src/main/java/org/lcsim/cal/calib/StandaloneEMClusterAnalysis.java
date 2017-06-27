package org.lcsim.cal.calib;
import java.io.BufferedReader;
import org.lcsim.recon.emid.hmatrix.HMatrixTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.util.loop.LCIOEventSource;
import org.lcsim.util.loop.LCSimLoop;

/**
 * A simple standalone EMClusterID example
 */
public class StandaloneEMClusterAnalysis
{
    public static void main(String[] args) throws Exception
    {
        if(args.length<2)
        {
            usage();
            return ;
        }
        
        String listOfFiles = args[0];
        List<File> filesToProcess = filesToProcess(listOfFiles);
        LCIOEventSource src = new LCIOEventSource("EMClusterAnalysis", filesToProcess);
        
        String task = args[1];
        if(!(task.equals("analyze") || task.equals("build")))
        {
            System.out.println("'"+args[1] +"' not a recognized task. Please specify 'analyze' or 'build'");
            return;
        }
        int numToProcess=-1;
        if(args.length>2) numToProcess=Integer.parseInt(args[2]);
        
        System.out.println("Processing "+numToProcess+" events from "+listOfFiles);
        for(File f : filesToProcess)
        {
            String HMatrixName = "";
            String[] parts = f.getName().split("_");
            for(String s : parts)
            {
                if(s.startsWith("Theta")) HMatrixName+=s+"_";
                if(s.contains("GeV")) HMatrixName+=s;
            }
//            HMatrixName+=".hmx";
        
            LCSimLoop loop = new LCSimLoop();
            loop.setLCIORecordSource(f);
            HMatrixTask taskType = HMatrixTask.ANALYZE;
            if(task.equals("build")) taskType = HMatrixTask.BUILD;
            EMClusterID emClusID = new EMClusterID(taskType); 
            emClusID.setHMatrixFileLocation(HMatrixName);
            loop.add(emClusID);
            loop.loop(numToProcess);
            loop.dispose();
        }
    }
    
    public static void usage()
    {
        System.out.println("This is StandaloneEMClusterAnalysis");
        System.out.println("usage:");
        System.out.println("java StandaloneEMClusterAnalysis listOfInputFiles build/analyze [number of events to process]");
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
}
