package org.lcsim.event;

import hep.physics.event.generator.MCEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lcsim.geometry.Detector;
import org.lcsim.geometry.IDDecoder;

/**
 * The event header from which information about the rest of the event
 * can be obtained.
 * @author Tony Johnson
 * @version $Id: EventHeader.java,v 1.23 2012/07/02 21:50:17 jeremy Exp $
 */
public interface EventHeader extends MCEvent
{
    /**
     * Get the list of MC particles associated with this event.
     */
    List<MCParticle> getMCParticles();

    /**
     * Get a collection of data from the event.
     * This method will throw <tt>IllegalArgumentException</tt> if the requested data is not found.
     * @param type The type of object requested.
     * @return The collection of data.
     */
    <T> List<List<T>> get(Class<T> type);
    /**
     * Obtain a collection of objects associated with this event, specified by type and name. 
     * This method will throw <tt>IllegalArgumentException</tt> if the requested data is not found.
     * @param type The class of items stored in the requested collection
     * @param name The name of the requested collection
     * @return The resulting collection.
     */
    <T> List<T> get(Class<T> type, String name);

    /**
     * Get the meta data associated with a list obtained from this event.
     */
    LCMetaData getMetaData(List x);

    /**
     * Get all the meta data associated with this event.
     * @return A <code>Collection</code> of <code>LCMetaData</code> objects associated with this event.
     */
    Collection<LCMetaData> getMetaData();

    /**
     * Get all the lists associated with this event.
     * @return A <code>Set</code> of <code>List</code> objects associated with this event.
     */
    public Set<List> getLists();

    /**
     * The name of the detector, used to obtain geometry and conditions.
     */
    String getDetectorName();

    /**
     * The creation time of this event (in nS since 1-Jan-1970 GMT).
     */
    long getTimeStamp();

    /**
     * Get the detector description read from the conditions database
     */
    Detector getDetector();

    /**
     * Add a collection to the event.
     * @param name The name used to stored this collection in the event.
     * @param collection The data collection
     * @param type The class of objects stored in the collection.
     * @param flags The LCIO flags associated with the collection.
     */
    void put(String name, List collection, Class type, int flags);

    /**
     * Add a collection to the event.
     * @param name The name used to stored this collection in the event.
     * @param collection The data collection
     * @param type The class of objects stored in the collection.
     * @param flags The LCIO flags associated with the collection.
     * @param readoutName The name of the readout to be used to decode hits in this collection
     */
    void put(String name, List collection, Class type, int flags, String readoutName);   
    
    /**
     * 
     * @param name
     * @param collection
     * @param type
     * @param flags
     * @param intMap
     * @param floatMap
     * @param stringMap
     */
    public void put(String name, List collection, Class type, int flags, Map intMap, Map floatMap, Map stringMap);

    /**
     * Removes an item from the event
     */
    void remove(String name);

    /**
     * Test if the event contains a collection of a given type and name. Can be used to avoid having
     * to catch an exception if the event does not contain an expected collection when calling get.
     */
    boolean hasCollection(Class type, String collection);

    /** 
     * Test if the event contains at least one collection of the given type.
     */
    boolean hasCollection(Class type);

    /**
     * Test if the event contains a given item.
     * @param name The name of the item to look for
     * @return <code>true</code> if the event contains an item with the given name.
     */
    boolean hasItem(String name);

    /**
     * Get the event weight
     * @return The weight
     */
    float getWeight();

    Map<String,int[]> getIntegerParameters();
    Map<String,float[]> getFloatParameters();
    Map<String,String[]> getStringParameters();

    /**
     * List of elements stored in the event may have meta-data associated with
     * them. This interface allows this meta-data to be extracted.
     * LCIO allows arbitrary maps of int, float or strings to be stored 
     * with each collection and these can also be accessed via this interface. 
     */
    public interface LCMetaData
    {
        /**
         * The name of the associated data collection
         */
        String getName();

        /**
         * The type of objects stored in the associated data collection.
         */
        Class getType();
        /**
         * The LCIO flags stored with the associated data collection.
         */
        int getFlags();
        /**
         * An IDDecoder that can be used to decode the ID's stored in this
         * data collection. Primarily used for calorimeter and tracker hits.
         */
        IDDecoder getIDDecoder();

        Map<String,int[]> getIntegerParameters();
        Map<String,float[]> getFloatParameters();
        Map<String,String[]> getStringParameters();

        /** Get the event with which this meta-data is associated. */
        EventHeader getEvent();
        /**
         * Flag whether the collection associated with this meta-data is a subset
         * of some other collection. When flagged in this way the collection will be
         * written to an LCIO file as a reference collection, ie as a set of pointers
         * to objects in the master collection.
         */
        void setSubset(boolean isSubset);
        boolean isSubset();
        /** 
         * Flag whether the collection associated with this meta-data should be 
         * treated as transient. Transient collections are never written out to
         * LCIO files.
         */
        void setTransient(boolean isTransient);
        boolean isTransient();
    }
}
