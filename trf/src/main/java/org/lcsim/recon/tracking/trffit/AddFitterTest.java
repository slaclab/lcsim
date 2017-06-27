package org.lcsim.recon.tracking.trffit;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;

//concrete implementation of AddFitter for testing only
public class AddFitterTest extends AddFitter
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "AddFitterTest"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // non-static data
    
    // test parameter
    private int _param;
    
    // non-static methods.
    
    // Fit.
    public int addHitFit(ETrack tre, double chsq,  Hit  hit)
    {
        return 2*_param;
    }
    
    // Constructor.
    public AddFitterTest(int param)
    { _param = param;
    }
    
    // Return the type.
    public String get_type()
    { return staticType(); }
    
    
    public String toString()
    {return  "Test add fitter.";
     
    }
    
}
