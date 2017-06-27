package org.lcsim.recon.tracking.trfbase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfutil.Assert;

// Hit.
public class McHitTest extends Hit
{
    
    double[] pdata = { 1.0, 2.0, 3.0 };
    
    // Return the type name.
    public static String typeName()
    { return "McHitTest";
    }
        /*
        // Return the type.
        public  String get_static_type()
        { return get_type_name();
        }
         */
    public String toString()
    {
        return"McTest hit.";
    }
    protected boolean equal(Hit hp)
    {
        Assert.assertTrue( hp.type().equals(type()) );
        return true;
    }
    
    public McHitTest()
    {
    }
    public McHitTest( McHitTest ht)
    {
    }
    
    public String type()
    { return staticType();
    }
    
    public int size()
    { return 1;
    }
    public HitVector measuredVector()
    { return new HitVector(1,pdata);
    }
    public HitError measuredError()
    { return new HitError(1,pdata);
    }
    public HitVector predictedVector()
    { return new HitVector(1,pdata);
    }
    public HitError predictedError()
    { return new HitError(1,pdata);
    }
    public HitDerivative dHitdTrack()
    { return new HitDerivative(1,pdata);
    }
    public HitVector differenceVector()
    { return new HitVector(1,pdata);
    }
    public void update(ETrack tre)
    {
    }
}

