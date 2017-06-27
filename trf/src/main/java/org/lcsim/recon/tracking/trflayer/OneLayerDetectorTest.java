package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfutil.Assert;

//**********************************************************************

// Detector composed of one layer.

public class OneLayerDetectorTest extends Detector
{
    
    
    
    // constructor from one layer
    public OneLayerDetectorTest(String name, Layer lyr)
    {
        int stat = addLayer(name,lyr);
        Assert.assertTrue( stat==0 );
    }
    
};