package org.lcsim.recon.tracking.trffit;
// concrete implementation to test FullFitter

public class FullFitterTest extends FullFitter
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "FullFitterTest"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // non-static data
    
    // test parameter
    private int _param;
    
    // non-static methods
    
    // Constructor.
    public FullFitterTest(int param)
    { _param=param; }
    
    // Return the type.
    public String get_type()
    { return staticType(); }
    
    // Fit the specified track.
    public int fit(HTrack trh)
    {
        return _param;
    }
    
    public String toString()
    {
        return "Test full fitter.";
    }
    
}

