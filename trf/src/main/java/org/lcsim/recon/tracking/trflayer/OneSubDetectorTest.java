package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfutil.Assert;

//**********************************************************************

// Detector composed of one subdetector.

public class OneSubDetectorTest extends Detector
{
    
    
    
    // constructor from one subdetector
    public OneSubDetectorTest( Detector det)
    {
        int stat = addSubdetector(det);
        Assert.assertTrue( stat==0);
        Assert.assertTrue( isOk() );
    };
    
};

