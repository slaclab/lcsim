package org.lcsim.recon.tracking.trfbase;

/**
 * Class to describe the result of a propagation.
 *<p>
 * Class is constructed in a state indicating failure.
 *<p>
 * Call set_same() or set_path_distance(s) if propagation is
 * successful.
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class PropStat
{
    
    static final double DEFAULT_PATH_DISTANCE = 1.e30;
    
    private boolean _success;
    private double _s;
    
    //
    
    /**
     *Default Constructor leaves object invalid.
     * Default state indicates failure.
     * Call one of the set methods to validate.
     *
     */
    public PropStat()
    {
        _success = false;
        _s = 0.0;
    }
    
    //
    
    /**
     * Copy constructor.
     *
     * @param   pstat  PropStat to copy.
     */
    public PropStat( PropStat pstat)
    {
        _success = pstat._success;
        _s = pstat._s;
    }
    
    //
    
    /**
     * Set the distance of propagation.
     *
     * @param   s  distance to propagate.
     */
    public void setPathDistance(double s)
    {
        _success = true;
        _s = s;
    }
    
    //
    
    /**
     * Set successful propagation to same point.
     *
     */
    public void setSame()
    {
        _success = true;
        _s = 0.0;
    }
    
    //
    
    /**
     * Was propagation successful?
     *
     * @return true if propagation was successful.
     */
    public boolean success()
    {
        return _success;
    }
    
    //
    
    /**
     * Did track move forward?
     *
     * @return true if track propagated in forward direction.
     */
    public boolean forward()
    {
        return _success && _s>0.0;
    }
    
    //
    
    /**
     * Did track move backward?
     *
     * @return  true if track propagated in backward direction.
     */
    public boolean backward()
    {
        return _success && _s<0.0;
    }
    
    //
    
    /**
     * Did track propagate succesfully to the same position?
     *
     * @return true if track propagated to current position.
     */
    public boolean same()
    {
        return _success && _s==0.0;
    }
    
    //
    
    /**
     * Return the propagation path distance.
     *
     * @return path distance s.
     */
    public double pathDistance()
    {
        //should check on _success here assert( _success );
        return _s;
    }
    
    
    /**
     * String representation of PropStat.
     *
     * @return String representation of PropStat.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+"\n");
        if ( forward() ) sb.append("successful forward propagation");
        if ( backward() ) sb.append("successful backward propagation");
        if ( same() ) sb.append("successful propagation with no movement");
        if ( ! success() ) sb.append("propagation failed");
        if ( success() ) sb.append(" " + pathDistance());
        return sb.toString();
    }
    
    // deprecated, but used in tests...
    
    //
    
    /**
     * Set successful propagation forward.
     * Should only be used in tests.
     */
    public void setForward()
    {
        _success = true;
        _s = DEFAULT_PATH_DISTANCE;
    }
    
    //**********************************************************************
    
    //
    
    /**
     * Set successful propagation backward.
     * Should only be used in tests.
     */
    public void setBackward()
    {
        _success = true;
        _s = -DEFAULT_PATH_DISTANCE;
    }
    
}
