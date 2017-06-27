package org.lcsim.recon.tracking.trfbase;
// SimInteractorTest
// A file to test the abstract SimInteractor class.
import org.lcsim.recon.tracking.trfbase.SimInteractor;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;

public class SimInteractorTest extends SimInteractor
{
    
    private double _radLength;
    private double _mult;
    
    // constructor
    public SimInteractorTest( double radLength, double mult )
    {
        _radLength = radLength;
        _mult = mult;
    }
    
    // method for changing the track:
    public void interact( VTrack vtrk)
    {
        // the real test is statistical... so here, just change the
        // track vector by a prescribed amount, and let a large data
        // set determine whether the track modifications are in line
        // with the covariance matrix modifications.
        TrackVector trv = ( vtrk.vector() );
        trv.set(0, trv.get(0) * _mult);
        trv.set(1, trv.get(1) * _mult);
        vtrk.setVectorAndKeepDirection( trv );
    }
    
    //return the number of radiation lengths:
    public double get_rad_length()
    {
        return _radLength;
    }
    
    //make a clone
    public SimInteractor newCopy()
    {
        return new SimInteractorTest(this._radLength, this._mult);
    }
    
    public String toString()
    {
        return "SimInteractorTest radlength= "+_radLength+" mult= "+_mult+"\n";
    }
    
}







