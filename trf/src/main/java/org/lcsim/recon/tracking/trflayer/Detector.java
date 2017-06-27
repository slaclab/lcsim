package org.lcsim.recon.tracking.trflayer;
import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 * A detector is a collection of named layers.  It provides a method
 *  for retrieving any of these layers by name.
 *<p>
 * This class is intended for use as a base class.  It is not abstract
 * but the constructor is protected.  Similarly, there are no public
 * methods for adding, modifying or removing layers or subdetectors.
 * A subclass is expected to call methods to add a single layer or
 * all the layers from another detector.  Typically these calls would
 * be made in the constructor.
 *<p>
 * The return status for each change is zero if the change was
 * successful.  After all changes, is_ok() may be called to verify
 * internal consistency.
 *<p>
 * The layer names must be unique.  New names are checked for uniqueness
 * each time a layer is added.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class Detector
{
    
    // enums
    private static final int UNCHECKED = 0;
    private static final int CHECKED_OK = 1;
    private static final int CHECKED_BAD = 2;
    // Flags for assertions.
    // If set false, assertion will fail.
    private static boolean CHECK_SIZE_MISMATCH = false;
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "Detector"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // List of layer names in original order.
    private List _names;
    
    // Map layer names to layers.
    private Map _layermap;
    
    // Check status.
    // Set by method check.
    private  int _check;
    
    // methods
    
    // Check internal status.
    // Note that if _check is set to CHECKED_BAD, it is not changed.
    // Return nonzero for error.
    private void check()
    {
        // This checks the detector for internal consistency and sets the
        // status accordingly.
        // Presently, the status is inherently good, so no check is needed.
        
        // Bad check stays bad.
        if ( _check == CHECKED_BAD ) return;
        
        // Check that the list of names and map have the same size.
        // There is a logic error if this fails.
        if ( _names.size() != _layermap.size() )
        {
            Assert.assertTrue( CHECK_SIZE_MISMATCH );
            _check = CHECKED_BAD;
        }
        
        // All tests passed.
        _check = CHECKED_OK;
    }
    
    // methods
    
    // Default constructor.
    protected Detector()
    {
        _check = UNCHECKED;
        _names = new ArrayList();
        _layermap = new HashMap();
    }
    
    // Return the map of layers indexed by name.
    protected Map layerMap()
    { return _layermap; }
    
    // Add a named layer.
    //
    // Return 0 for success.
    //
    // Return nonzero and do not add layer if the name duplicates
    // a known name.
    //
    protected  int addLayer(String name, Layer lyr)
    {
        // Set unchecked.
        if ( _check == CHECKED_OK ) _check = UNCHECKED;
        
        // Check name does not already appear in map.
        if ( isAssigned(name) ) return 11;
        
        // Add layer.
        _names.add(name);
        _layermap.put(name, lyr);
        
        return 0;
        
    }
    
    // Add a subdetector.
    //
    // Return 0 for success.
    //
    // Return nonzero and add no layers if any of the subdetector
    // layer names duplicate those here.
    //
    protected  int addSubdetector( Detector subdet)
    {
        // Set unchecked.
        if ( _check == CHECKED_OK ) _check = UNCHECKED;
        
        // Exit if the subdetector does not check ok.
        if ( ! subdet.isOk() ) return 21;
        
        // Fetch the subdetector names and layer map.
        List subnames = subdet.layerNames();
        Map sublayermap = subdet.layerMap();
        
        // Check that the names are unique.
        
        for ( Iterator iname=subnames.iterator(); iname.hasNext(); )
            if ( isAssigned( (String) iname.next()) ) return 22;
        
        // Add the layers from each subdetector.
        _names.addAll( subnames );
        _layermap.putAll( sublayermap );
        
        // The check status will reflect that of the added layers.
        return 0;
        
    }
    
    // methods
    
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     * Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *Return whether the check status is ok.
     * If the status has not been checked, this will dos so.
     *
     * @return true if check status is ok
     */
    public boolean isOk()
    {
        if ( _check == UNCHECKED ) check();
        return _check == CHECKED_OK;
    }
    
    //
    
    /**
     *Return the list of layer names.
     *
     * @return List of layer names
     */
    public List layerNames()
    { return _names; }
    
    //
    
    /**
     *Print the layers.
     *
     */
    public void printLayers()
    {
        System.out.println(toString());
    }
    
    
    /**
     *output stream
     *
     * @return String representation of class
     */
    public String toString()
    {
        
        // Fetch list of layer names (including subdetectors)
        List names = layerNames();
        
        // header
        int size = names.size();
        StringBuffer sb = new StringBuffer( getClass().getName()+" has " + size + " layer");
        if ( size != 1 ) sb.append("s");
        if ( size > 0 ) sb.append(":");
        else sb.append(".");
        
        // Loop over layers.
        
        for ( Iterator iname=names.iterator(); iname.hasNext(); )
        {
            String name = (String) iname.next();
            sb.append("\n"+name + ": " + layer(name));
        }
        return sb.toString();
    }
    
    //
    
    /**
     *Return whether a name has been assigned.
     *
     * @param   name String name of detector
     * @return true if name is assigned
     */
    public boolean isAssigned(String name)
    {
        return _layermap.containsKey(name);
    }
    
    //
    
    /**
     *Return a layer.
     * Throw exception or crash if name is unrecognized.
     * To avoid crash, first check with is_assigned().
     *
     * @param   name String name of detector
     * @return Layer with name name
     */
    public Layer layer(String name)
    {
        if ( _layermap.containsKey(name))
        {
            return (Layer)_layermap.get(name);
        }
        else
        {
            throw new IllegalArgumentException("Detector does not contain Layer "+name);
        }
        
    }
    
    
    //Don't care?
    // Register layers with ObjTable.
    // Returns the number of layers which could not be registered and were
    // not already registered with the same name and address.  Normally
    // this should be zero.
    //  public int register_with_objtable()
    //  {
    //  	return 137;
    //  }
    
}