package org.lcsim.recon.tracking.trflayer;
// Independent detector simulators (single inheritance).
import org.lcsim.recon.tracking.trfutil.Assert;
//**********************************************************************

// Simulator composed of two detector simulators

public class TwoSubDetectorSimulatorTest extends DetectorSimulator
{
    
    // constructor
    public TwoSubDetectorSimulatorTest(  Detector det,
            DetectorSimulator dsim1,
            DetectorSimulator dsim2)
    {
        super(det);
        DetSimReturnStatus stat1 = addDetectorSimulator(dsim1);
        Assert.assertTrue( stat1 == DetSimReturnStatus.OK );
        DetSimReturnStatus stat2 = addDetectorSimulator(dsim2);
        Assert.assertTrue( stat2 == DetSimReturnStatus.OK );
    }
    
}