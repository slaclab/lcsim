package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.TrackError;

// A file to test the abstract Interactor class.

public class InteractorTest extends Interactor
{
    
    // static methods
    
    // Return the type name.
    public static String get_type_name()
    {
        return "InteractorTest";
    }
    
    // Return the type.
    public static String get_static_type()
    {
        return get_type_name();
    }
    
    // attributes
    
    private double _errfac;
    
    // methods
    
    // constructor
    public InteractorTest(double errfac)
    {
        _errfac = errfac;
    }
    
    // copy Constructor
    public InteractorTest(InteractorTest it)
    {
        _errfac = it._errfac;
    }
    
    // Return the type.
    public String get_type()
    {
        return get_static_type();
    }
    
    // method for adding the interaction:
    public void interact(ETrack tre)
    {
        TrackError err = tre.error();
        err.set(0,0, err.get(0,0)*_errfac);
        tre.setError(err);
    }
    
    // return the error factor
    public double get_errfac()
    {
        return _errfac;
    }
    
    // make a clone
    public Interactor newCopy()
    {
        return new InteractorTest(this);
    }
    
    public String toString()
    {
        return "Test Interactor";
    }
    
}


