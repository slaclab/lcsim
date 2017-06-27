package org.lcsim.detector.identifier;

/**
 * A mixin interface for objects that have an associated {@link IIdentifier},
 * {@link IExpandedIdentifier}, and an {@link IIdentifierHelper}
 * for performing converting between these.
 *  
 * @author Jeremy McCormick
 * @version $Id: Identifiable.java,v 1.1 2008/05/15 22:06:12 jeremy Exp $
 */
public interface Identifiable 
{
    /**
     * Get the {@link IIdentifier} associated with this object.
     * @return The Identifier.
     */
    public IIdentifier getIdentifier();
    
    /**
     * Get the {@link IExpandedIdentifier} associated with this object.
     * Should use {@link IIdentifierHelper} to unpack the 
     * {@link IIdentifier}.
     * 
     * @return The ExpandedIdentifier.
     */
    public IExpandedIdentifier getExpandedIdentifier();   
    
    /**
     * Get the {@link IIdentifierHelper} associated with this object. 
     * @return The IdentifierHelper.
     */
    public IIdentifierHelper getIdentifierHelper();
}
