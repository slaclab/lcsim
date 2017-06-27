package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfutil.Assert;

//**********************************************************************

// Detector composed of two subdetectors.

public class TwoSubDetectorTest extends Detector
{
    
    
    
    // constructor from one subdetector
    public TwoSubDetectorTest( Detector det1,
            Detector det2)
    {
        int stat1 = addSubdetector(det1);
        Assert.assertTrue(  stat1==0 );
        int stat2 = addSubdetector(det2);
        Assert.assertTrue( stat2==0 );
        Assert.assertTrue( isOk() );
    };
    
    
}