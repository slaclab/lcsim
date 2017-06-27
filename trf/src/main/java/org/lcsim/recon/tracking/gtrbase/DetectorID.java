package org.lcsim.recon.tracking.gtrbase;

/**
 *
 *     This class defines methods that can construct a unique ID for any
 *     detector element. In order for two different subdetectors to have 
 *     unique ids but allow each subdetector to implement its own numbering 
 *     scheme , the ID ( 32 bits long integer ) reserves the last 8 bits. 
 *     It uses them to identify each particular subdetector. The rest 
 *     ( 24 bits ) are free for use by subdetectors. Each subdetector should 
 *     implement its own numbering scheme . The ID is constructed using that 
 *     subdetector ID . This way, the interperetation of subdetector ID is 
 *     left to the discretion of each subdetector.
 *
 *     It is in no way guaranteed that convention remains the same. The
 *     reserved 8 bits can be upper or lower or any other. It is required
 *     that subdetector ID should be no longer than 24 bits though.
 *     If subdetector follows that rule its subdetector id will be
 *     always packed and unpacked correctly.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class DetectorID
{
    protected int _detid;

    /**
     *Construct an instance for detector type dettyp and subdetector id subdetid.
     *
     * @param   dettyp The detector type.
     * @param   subdetid The subdetector type.
     */
    public DetectorID( int dettyp , int subdetid )
    {
        if ( dettyp == 0 ) throw new IllegalArgumentException("dettyp 0 not allowed");
        int subid = subdetid;
        if ( (subid  <<  8 >> 8) != subdetid )throw new IllegalArgumentException("Only 8 bits allowed for detector type");
        //  Assert.assert ( subid.intValue()  <<  8 >> 8 == subdetid );
        int  type = dettyp;
        if ( (type << 24 >> 24) != dettyp )throw new IllegalArgumentException("Only 24 bits allowed for subdetector ID");
        
        //  Assert.assert ( (type.intValue() << 24 >> 24) != 0);
        _detid = ((type << 24) | subid) ;
    }
    
    /**
     *Construct a default instance.
     *
     */
    public DetectorID( )
    {
    }
    
    /**
     * Construct an instance from a detector ID.
     *
     * @param   detid The detector ID.
     */
    public DetectorID( int detid )
    {
        // basic sanity check
        if(detid<0) throw new IllegalArgumentException("detid must be an unsigned int");
        _detid = detid;
    }
    
    /**
     * Construct an instance replicating the DetectorID (copy constructor).
     *
     * @param   detid The DetectorID to replicate.
     */
    public DetectorID(   DetectorID detid )
    {
        _detid = detid._detid;
    }
       
    
    /**
     * Return the subdetector type.
     *
     * @return The subdetector type.
     */
    public int subdetectorType( )
    {
        return _detid >> 24 ;
    }
    
    /**
     *Return subdetector ID from detector id detid.
     *
     * @return The subdetector id.
     */
    public int subdetectorId( )
    {
        return _detid << 8 >> 8;
    }
    
    /**
     *Return the detector ID.
     *
     * @return The detector ID.
     */
    public int detectorId( )
    {
        return _detid;
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("DetectorID ");
        sb.append( _detid );
        return sb.toString();
    }
    
    
    /**
     * Test equality.
     *
     * @param   detid The DetectorID to test.
     * @return true if the DetectorIDs are equal.
     */
    public boolean equals(DetectorID detid)
    {
        return _detid==detid._detid;
    }
    
    
    /**
     * Test inequality.
     *
     * @param   detid The DetectorID to test.
     * @return true if the DetectorIDs are not equal.
     */
    public boolean notEquals(DetectorID detid)
    {
        return !equals( detid );
    }
    
    
    /**
     * Test ordering.
     *
     * @param   detid  The DetectorID to test.
     * @return true if detid is larger.
     */
    public boolean lessThan(DetectorID detid)
    {
        return _detid<detid._detid;
    }
    
}
