package org.lcsim.recon.tracking.trflayer;
import java.util.*;

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.RandomGenerator;
import org.lcsim.recon.tracking.trfutil.RandomGeneratorTest;

public class LayerSimulatorTest extends LayerSimulator
{
    
    private RandomGenerator _gen;
    
    
    public LayerSimulatorTest(Layer lyr)
    {
        super(lyr);
        _gen = new RandomGeneratorTest(1.,2.);
    }
    
    
    public  List generators()
    {
        List tmp = new ArrayList();
        tmp.add(_gen);
        return tmp;
    }
    
    
    public void addClusters( VTrack trv, int mcid)
    { }
    
    public String toString()
    {
        return "Dummy layer simulator.";
        
    }
}
