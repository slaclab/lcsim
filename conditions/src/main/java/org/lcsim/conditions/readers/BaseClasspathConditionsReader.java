package org.lcsim.conditions.readers;

import java.io.IOException;
import java.io.InputStream;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsReader;

/**
 * This ConditionsReader finds conditions from embedded jar resources
 * based on a resource path.
 */
public class BaseClasspathConditionsReader extends ConditionsReader {

    private String _resourcePath;    
    String propFileName = "detector.properties";
    
    int _runNumber = -1;
    String _detectorName;

    public BaseClasspathConditionsReader() {
    }
    
    public BaseClasspathConditionsReader(String resourcePath) throws IOException {
        _resourcePath = resourcePath;
        
        // This shouldn't be here but some convoluted logic in ConditionsReader depends on it for now.
        if (getClass().getResourceAsStream("/" + _resourcePath + "/" + propFileName) == null) {
            throw new IOException("Unable to find " + _resourcePath + "/" + propFileName + " on the classpath.");
        }
    }

    /**
     * Open an InputStream to conditions data by name and type.
     * 
     * To be found, the conditions data must exist as a resource on the classpath like: 
     * 
     * /[resourcePath]/[name].[type]
     * 
     * It will throw an <code>IOException</code> if the conditions do not exist.
     * 
     * @return An InputStream to the conditions data or null if does not exist.
     */
    public InputStream open(String name, String type) throws IOException {
        InputStream in = getClass().getResourceAsStream("/" + _resourcePath + "/" + name + "." + type);
        if (in == null) {
        	throw new IOException("The conditions " + name + " of type " + type + " do not exist at path " + _resourcePath);
        }
        return in;
    }
    
    /**
     * Set the base path for this reader to search for classpath resources.
     * @param resourcePath the resource path
     */
    public void setResourcePath(String resourcePath) {
        _resourcePath = resourcePath;
    }
    
    /**
     * Update this reader for possibly new detector or run number.
     * This reader caches the detector name and run number, and this method will
     * return new if either is different from the cached values.
     * @return true if new detector or run number
     */
    public boolean update(ConditionsManager manager, String detectorName, int run) throws IOException {
        
        boolean update = false;
        
        // Check detector name.
        if (_detectorName == null || _detectorName != detectorName) {
            _detectorName = detectorName;
            update = true;
        }
        
        // Check run number.
        if (run != _runNumber) {
            update = true;
        } 
                
        return update;
    }
    
    /**
     * Close the reader.  
     * @throws IOException never
     */
    public void close() throws IOException {
    }
}