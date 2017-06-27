package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfutil.Assert;

//**********************************************************************

// Detector composed of two layers.

public class TwoLayerDetectorTest extends Detector
{
    
    
    
    // constructor from two layers
    public TwoLayerDetectorTest(String name1, Layer lyr1,
            String name2, Layer lyr2)
    {
        addLayer(name1,lyr1);
        addLayer(name2,lyr2);
        Assert.assertTrue( isOk() );
    }
    
};

