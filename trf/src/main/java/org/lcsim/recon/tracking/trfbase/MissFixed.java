package org.lcsim.recon.tracking.trfbase;
import org.lcsim.recon.tracking.trfutil.Assert;

/** Defines a miss with fixed surface and likelihood.
 *
 * 
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class MissFixed extends Miss
{
    
    // attributes
    
    // Type identifier.
    private String _mytype = "MissTest";
    
    // Surface.
    Surface _srf;
    
    // Likelihood.
    double _like;
    
    // methods
    
    //
    
    /**
     *Constructor from surface and likelihood.
     *
     * @param   srf Surface for this Miss
     * @param   like likelihood for this Miss
     */
    public MissFixed( Surface srf, double like)
    {
        _srf = srf;
        _like = like;
        Assert.assertTrue( _srf != null );
        Assert.assertTrue( _like >= 0.0 );
        Assert.assertTrue( _like <= 1.0 );
    }
    
    //
    /**
     *Return the type identifier
     *
     * @return String type of this class
     *Included only for completeness with C++ version
     */
    public String type()
    { return _mytype; }
    
    //
    
    /**
     * clone
     *
     * @return new copy of this Miss
     */
    public Miss newCopy()
    {
        return new MissFixed( _srf, _like);
    }
    
    //
    
    /**
     *update the likelihood with a new track
     *
     * @param   tre ETrack to update the Miss likelihood
     */
    public void update( ETrack tre)
    {
    }
    
    //
    
    /**
     *return the surface
     *
     * @return Surface for this Miss
     */
    public Surface surface()
    {
        return _srf;
    }
    
    //
    
    /**
     *return the likelihood
     *
     * @return likelihood for this Miss
     */
    public double likelihood()
    {
        return _like;
    }
    
    
    /**
     *output stream
     *
     * @return String representation for this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" Fixed miss at " +_srf + " with likelihood " + _like;
    }
    
}
