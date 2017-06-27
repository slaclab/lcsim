package org.lcsim.recon.tracking.trflayer;
// Independent detector simulators (single inheritance).
import org.lcsim.recon.tracking.trfutil.Assert;
//**********************************************************************

// Simulator composed of one detector simulator

public class OneSubDetectorSimulatorTest extends DetectorSimulator
{
    
    
    
    // constructor
    public OneSubDetectorSimulatorTest(  Detector det,
            DetectorSimulator dsim)
    {
        super(det);
        DetSimReturnStatus stat = addDetectorSimulator(dsim);
        Assert.assertTrue( stat == DetSimReturnStatus.OK );
    }
    
}
