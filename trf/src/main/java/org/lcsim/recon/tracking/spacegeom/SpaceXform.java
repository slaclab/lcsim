package org.lcsim.recon.tracking.spacegeom;
/** Abstract interface for a class that transforms a SpacePoint
 * or SpacePointVector.
 *
 * Transformation may be invoked with xf.apply(spt) or xf(spt).
 * Inverse may be applied with xf.inverse()(spt) or xf.invert(spt).
 * In all cases the original value is unchanged and the transformed
 * value is returned.
 *
 *@author Norman A. Graf
 *@version 1.0
 */

public abstract class SpaceXform
{
    
    // methods to be implemented in subclass
    
    /** Return the inverse of this transformation.
     * The subclass manages its inverse--the inverse transformation
     * should be destroyed when and only when the original is destroyed.
     * @return the inverse of this transformation
     */
    public abstract SpaceXform inverse();
    
    /** Transform a space point.
     * @param spt SpacePoint on which to apply this transform
     * @return    SpacePoint on which this transform has been applied
     */
    public abstract SpacePoint apply( SpacePoint spt );
    
    /** Transform a space point vector.
     * @param svec SpacePointVector on which to apply this transform
     * @return SpacePointVector on which this transform has been applied
     */
    public abstract SpacePointVector apply( SpacePointVector svec );
    
    // methods implemented here
    
    /** Transform a space point.
     * @param spt SpacePoint to transform
     * @return transformed SpacePoint
     */
    public SpacePoint transform( SpacePoint spt )
    {
        return apply(spt);
    }
    
    /** Transform a space point vector.
     * @param svec SpacePointVector to transform
     * @return ransformed SpacePointVector
     */
    public SpacePointVector transform( SpacePointVector svec )
    {
        return apply(svec);
    }
    
    /** Transform a space point with inverse transformation.
     */
    SpacePoint invert( SpacePoint spt )
    {
        return inverse().apply(spt);
    }
    
    /** Transform a space point vector with inverse transformation.
     */
    SpacePoint invert( SpacePointVector svec)
    {
        return inverse().apply(svec);
    }
    
}
