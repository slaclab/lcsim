package org.lcsim.recon.tracking.trflayer;
// Independent detector simulators (single inheritance).
import org.lcsim.recon.tracking.trfutil.Assert;
//**********************************************************************

// Simulator composed of two layer simulators.

public class TwoLayerDetectorSimulatorTest extends DetectorSimulator
{
    
    
    
    // constructor
    public TwoLayerDetectorSimulatorTest(  Detector det,
            String name1, LayerSimulator lsim1,
            String name2, LayerSimulator lsim2)
    {
        super(det);
        DetSimReturnStatus stat1 = addLayerSimulator(name1,lsim1);
        Assert.assertTrue( stat1 == DetSimReturnStatus.OK );
        DetSimReturnStatus stat2 = addLayerSimulator(name2,lsim2);
        Assert.assertTrue( stat2 == DetSimReturnStatus.OK );
    }
    
}

