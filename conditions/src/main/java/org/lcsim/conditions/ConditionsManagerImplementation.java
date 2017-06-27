package org.lcsim.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tony Johnson
 */
public class ConditionsManagerImplementation extends ConditionsManager {
    
    protected Map<Class, ConditionsConverter> converters = new HashMap<Class, ConditionsConverter>();
    protected Map<String, CachedConditions> cache = new HashMap<String, CachedConditions>();
    protected ConditionsReader reader;
    protected String detectorName;
    protected int run;
    protected List<ConditionsListener> listenerList = new ArrayList<ConditionsListener>();
    protected static final Logger logger = Logger.getLogger(ConditionsManagerImplementation.class.getName());

    /**
     * The default implementation of ConditionsManager. This implementation does
     * not currently handle run-dependent constants.
     */
    protected ConditionsManagerImplementation() {
        // logger.setLevel(Level.ALL);
        // ConsoleHandler handler = new ConsoleHandler();
        // handler.setLevel(Level.ALL);
        // logger.addHandler(handler);
    }

    public void setDetector(String detectorName, int run) throws ConditionsNotFoundException {
        if ((this.run != run) || !detectorName.equals(this.detectorName)) {
            ConditionsReader newReader = null;
            if (reader == null) {
                newReader = ConditionsReader.create(this, detectorName, run);
            } else {
                try {
                    if (reader.update(this, detectorName, run))
                        newReader = reader;
                } catch (IllegalArgumentException x) {
                    newReader = ConditionsReader.create(this, detectorName, run);
                } catch (IOException x) {
                    throw new ConditionsSetNotFoundException("Failed to update conditions reader, detector: " + detectorName + ", run: " + run, x);
                }
            }
            this.run = run;
            if (newReader != null)
                setConditionsReader(newReader, detectorName);
        }
    }

    public void setConditionsReader(ConditionsReader newReader, String name) {
        detectorName = name;
        if (newReader != reader) {
            ConditionsReader oldReader = reader;
            reader = newReader;
            if (oldReader != null) {
                try {
                    oldReader.close();
                } catch (IOException x) {
                }
            }
        }
        fireConditionsChanged();
        logger.log(Level.FINE, "Detector changed: {0} {1}", new Object[] { detectorName, run });
    }

    public void removeConditionsConverter(ConditionsConverter conv) {
        converters.remove(conv.getType());
    }

    public void registerConditionsConverter(ConditionsConverter conv) {
        converters.put(conv.getType(), conv);
    }

    public <T> CachedConditions<T> getCachedConditions(Class<T> type, String name) throws ConditionsSetNotFoundException {
        if (name == null)
            throw new IllegalArgumentException("Name argument points to null.");
        if (name.equals(""))
            throw new IllegalArgumentException("Name argument has length zero.");
        CachedConditions cond = cache.get(name);
        if (cond != null)
            return cond;
        logger.log(Level.FINE, "Getting cached conditions {0}", name);
        ConditionsConverter converter = converters.get(type);
        if (converter == null)
            throw new ConditionsSetNotFoundException("No converter registered for type: " + type.getName());
        cond = new CachedConditionsImplementation(this, name, converter);
        cache.put(name, cond);
        return cond;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public int getRun() {
        return run;
    }

    public String getDetector() {
        return detectorName;
    }

    public void addConditionsListener(ConditionsListener l) {
        listenerList.add(l);
    }

    public void removeConditionsListener(ConditionsListener l) {
        listenerList.remove(l);
    }

    protected void fireConditionsChanged() {
        ConditionsEvent event = new ConditionsEvent(this);
        ArrayList<ConditionsListener> listeners = new ArrayList<ConditionsListener>(listenerList);
        for (ConditionsListener cl : listeners)
            cl.conditionsChanged(event);
    }

    protected InputStream open(String name, String type) throws IOException {
        if (reader == null)
            throw new IOException("Detector description has not been set");
        return reader.open(name, type);
    }

    public ConditionsSet getConditions(String name) throws ConditionsSetNotFoundException {
        try {
            logger.log(Level.FINE, "Reading raw conditions {0}", name);
            return new ConditionsSetImplementation(this, name);
        } catch (IOException x) {
            throw new ConditionsSetNotFoundException("Conditions set not found: " + name, x);
        }
    }

    public RawConditions getRawConditions(String name) throws ConditionsSetNotFoundException {
        logger.log(Level.FINE, "Reading raw conditions {0}", name);
        return new RawConditionsImplementation(this, name);
    }
    
    protected ConditionsReader getConditionsReader() {
        return reader;
    }
    
    protected void clearCache() {
        this.cache.clear();
    }
}