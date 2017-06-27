package org.lcsim.recon.tracking.trffit;
// Hit class to test fitter.
// Prediction is the first track parameter.
// Each hit returns one prediction.
// Measurement and error are specified in hit constructor.
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfutil.Assert;

//**********************************************************************

public class HitTestFit1 extends Hit
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "HitTestFit1"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // data
    private double _msmt;               // measurement
    private double _emsmt;              // square of measured error
    private double _pred;               // prediction
    private double _epred;              // square of prediction error
    
    // methods
    
    // Output stream.
    public String toString()
    {
        return  "TestFit1 hit: \n"
                + "measure = " + measuredVector().get(0) + " +/- "
                + Math.sqrt(_emsmt) + "\n"
                + "predict = " + predictedVector().get(0) + " +/- "
                + Math.sqrt(_epred);
    }
    
    // Equality.
    protected boolean equal(  Hit hit)
    {
        Assert.assertTrue( hit.type() == type() );
        return cluster() == hit.cluster() &&
                _pred == ((  HitTestFit1 ) hit)._pred;
    };
    
    
    
    // Constructor.
    public HitTestFit1(double msmt, double emsmt,   ETrack tre)
    {_msmt = msmt;
     _emsmt = emsmt;
     update(tre);
    }
    
    // Return the type.
    public String type()
    { return staticType(); }
    
    // Return hit chractersistics.
    public int size()
    { return 1; }
    public HitVector measuredVector()
    { return new HitVector(_msmt); }
    public HitError measuredError()
    { return new HitError(_emsmt); }
    public HitVector predictedVector()
    { return new HitVector(_pred); }
    public HitError predictedError()
    { return new HitError(_epred); }
    public HitDerivative dHitdTrack()
    {
        HitDerivative hder = new HitDerivative(1);
        hder.set(0,0, 1.0);
        return hder;
    }
    public HitVector differenceVector()
    { return predictedVector().minus(measuredVector()); }
    
    // Update.
    public void update(   ETrack tre )
    {
        _pred = tre.vector().get(0);
        _epred = tre.error().get(0,0);
    }
    
}
