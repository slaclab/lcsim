package org.lcsim.detector.identifier;

/**
 * A manager for {@link IIdentifierDictionary} objects.
 *
 * @author Jeremy McCormick
 * @version $Id: IIdentifierDictionaryManager.java,v 1.3 2007/05/11 00:21:09 jeremy Exp $
 */

public interface IIdentifierDictionaryManager
{
    /**
     * Get an {@link IIdentifierDictionary} by name.
     * @param name The name of the IdentifierDictionary.
     * @return The IdentifierDictionary with matching name.
     */
    public IIdentifierDictionary getIdentifierDictionary(String name);
    
    /**
     * Add an {@link IIdentifierDictionary}.
     * @param dict The IdentifierDictionary to be added.
     */
    public void addIdentifierDictionary(IIdentifierDictionary dict);
    
    /**
     * Clear the list of {@link IIdentifierDictionary}s.
     */
    // FIXME: This should probably not be part of the public interface,
    //        but it is required to be available for DetectorStore.clear().
    public void clear();
}
