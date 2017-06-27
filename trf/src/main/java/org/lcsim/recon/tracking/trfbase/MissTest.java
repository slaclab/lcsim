package org.lcsim.recon.tracking.trfbase;
// Concrete Miss class for testing Miss and providing an example.

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Miss;
import org.lcsim.recon.tracking.trfbase.Surface;

// Dummy miss class.

public class MissTest extends Miss
{
    
    // attributes
    
    // static pointer specifying the type
    static String _mytype = "MissTest";
    
    // surface
    private SurfTest _srf;
    
    // fixed likelihood -- in a real miss this would be calculated
    // from the track and surface
    private double _like;
    
    // constructor from surface and likelihood
    public MissTest(double par,double like)
    {
        _srf = new SurfTest(par);
        _like = like;
    }
    
    // return the type identifier
    public String type()
    { return _mytype; }
    
    //
    /** clone
     * @return new copy of this Miss
     */
    public Miss newCopy()
    {
        return new MissTest(_srf.parameter(0),_like);
    }
    
    // update the likelihood -- again should use track and surface;
    // here we simply decrease the old value by 10%
    /** update the likelihood
     * @param tre ETrack to update the likelihood
     */
    public void update( ETrack tre)
    { _like *= 0.9; }
    
    // return the surface
    public  Surface surface()
    {
        return new SurfTest(_srf);
    }
    
    // return the likelihood
    public double likelihood()
    { return _like; }
    
    public String toString()
    {
        return  "Miss Test layer with surface " + _srf
                + "\n and fixed likelihood = " + _like;
        
    }
    
    public boolean equals( MissTest miss)
    {
        if( ! _srf.equals(miss._srf) ) return false;
        if( _like != miss._like ) return false;
        return true;
    }
    
}