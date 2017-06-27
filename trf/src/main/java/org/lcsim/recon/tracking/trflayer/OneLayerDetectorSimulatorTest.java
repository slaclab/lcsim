package org.lcsim.recon.tracking.trflayer;
// Independent detector simulators (single inheritance).
import org.lcsim.recon.tracking.trfutil.Assert;
//**********************************************************************

// Simulator composed of one layer simulator.

public class OneLayerDetectorSimulatorTest extends DetectorSimulator
{
    
    // constructor
    public OneLayerDetectorSimulatorTest(  Detector det,
            String name, LayerSimulator lsim)
    {
        super(det);
        DetSimReturnStatus stat = addLayerSimulator(name,lsim);
        Assert.assertTrue( stat == DetSimReturnStatus.OK );
    }
    
}

