package org.lcsim.event.base;

import hep.physics.event.BaseEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.event.EventHeader;
import org.lcsim.event.Hit;
import org.lcsim.event.MCParticle;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;

/**
 * A base implementation for EventHeader
 *
 * @author Tony Johnson
 */
public class BaseLCSimEvent extends BaseEvent implements EventHeader {

    private class MetaData implements LCMetaData {

        private transient IDDecoder dec;
        private int flags;
        private Map<String, float[]> floatMap;
        private Map<String, int[]> intMap;
        private final String name;
        private Map<String, String[]> stringMap;
        private final Class type;

        MetaData(final String name, final Class type, final int flags, final Map intMap, final Map floatMap,
                final Map stringMap) {
            this.name = name;
            this.type = type;
            this.flags = flags;
            this.intMap = intMap;
            this.floatMap = floatMap;
            this.stringMap = stringMap;
        }

        MetaData(final String name, final Class type, final int flags, final String readoutName) {
            this.name = name;
            this.type = type;
            this.flags = flags;
            if (readoutName != null) {
                this.getStringParameters().put(READOUT_NAME, new String[] {readoutName});
            }
        }

        /**
         * Make an IDDecoder for this MetaData using the CellIDEncoding parameter.
         *
         * @return An IDDecoder made built from the CellIDEncoding.
         */
        private IDDecoder createIDDecoderFromCellIDEncoding() {
            final String[] cellIdEncoding = this.getStringParameters().get("CellIDEncoding");
            IDDecoder result = null;
            if (cellIdEncoding != null) {
                result = new BaseIDDecoder();
                try {
                    final IDDescriptor desc = new IDDescriptor(cellIdEncoding[0]);
                    result.setIDDescription(desc);
                } catch (final IDDescriptor.IDException x) {
                    throw new RuntimeException(x);
                }
            }
            return result;
        }

        public org.lcsim.geometry.IDDecoder findIDDecoder() {
            // If the IDDecoder name is explicitly set then use it, otherwise
            // use the name of the collection itself.
            String readoutName = name;
            if (stringMap != null) {
                final String[] names = stringMap.get(READOUT_NAME);
                if (names != null && names.length >= 1) {
                    readoutName = names[0];
                }
            }

            // Find the IDDecoder using the Detector.
            org.lcsim.geometry.IDDecoder result = null;
            try {
                result = BaseLCSimEvent.this.getDetector().getDecoder(readoutName);
            } catch (final RuntimeException x) {
            }

            // Detector lookup failed. Attempt to use the CellIDEncoding collection parameter.
            if (result == null) {
                result = this.createIDDecoderFromCellIDEncoding();
            }

            // If both methods failed, then there is a problem.
            // if (result == null)
            // throw new RuntimeException("Could not find or create an IDDecoder for the collection: " + name +
            // ", readout: " + readoutName);

            return result;
        }

        @Override
        public EventHeader getEvent() {
            return BaseLCSimEvent.this;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public Map<String, float[]> getFloatParameters() {
            if (floatMap == null) {
                floatMap = new HashMap<String, float[]>();
            }
            return floatMap;
        }

        @Override
        public org.lcsim.geometry.IDDecoder getIDDecoder() {
            if (dec == null) {
                dec = this.findIDDecoder();
            }
            return dec;
        }

        @Override
        public Map<String, int[]> getIntegerParameters() {
            if (intMap == null) {
                intMap = new HashMap<String, int[]>();
            }
            return intMap;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, String[]> getStringParameters() {
            if (stringMap == null) {
                stringMap = new HashMap<String, String[]>();
            }
            return stringMap;
        }

        @Override
        public Class getType() {
            return type;
        }

        @Override
        public boolean isSubset() {
            return LCIOUtil.bitTest(flags, LCIOConstants.BITSubset);
        }

        @Override
        public boolean isTransient() {
            return LCIOUtil.bitTest(flags, LCIOConstants.BITTransient);
        }

        @Override
        public void setSubset(final boolean isSubset) {
            flags = LCIOUtil.bitSet(flags, LCIOConstants.BITSubset, isSubset);
        }

        @Override
        public void setTransient(final boolean isTransient) {
            flags = LCIOUtil.bitSet(flags, LCIOConstants.BITTransient, isTransient);
        }
    }

    private static final int NANO_SECONDS = 1000000;
    public static final String READOUT_NAME = "ReadoutName";
    private final ConditionsManager conditionsManager = ConditionsManager.defaultInstance();

    private final String detectorName;
    private final Map<String, float[]> floatParameters = new HashMap<String, float[]>();
    private final Map<String, int[]> intParameters = new HashMap<String, int[]>();    
    private final Map<String, String[]> stringParameters = new HashMap<String, String[]>();

    private final Map<List, LCMetaData> metaDataMap = new IdentityHashMap<List, LCMetaData>();
    
    /** Creates a new instance of BaseLCSimEvent */
    public BaseLCSimEvent(final int run, final int event, final String detectorName) {
        this(run, event, detectorName, System.currentTimeMillis() * NANO_SECONDS);
    }

    public BaseLCSimEvent(final int run, final int event, final String detectorName, final long timeStamp) {
        super(run, event, timeStamp);
        this.detectorName = detectorName;
        try {
            conditionsManager.setDetector(detectorName, run);
        } catch (final ConditionsNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    public BaseLCSimEvent(final int run, final int event, final String detectorName, final long timeStamp,
            final boolean triggerConditionsUpdate) {
        super(run, event, timeStamp);
        this.detectorName = detectorName;
        if (triggerConditionsUpdate) {
            try {
                conditionsManager.setDetector(detectorName, run);
            } catch (final ConditionsNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
    }

    @Override
    public <T> List<List<T>> get(final Class<T> type) {
        final List<List<T>> result = new ArrayList<List<T>>();
        for (final Map.Entry<List, LCMetaData> entry : metaDataMap.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getType())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public <T> List<T> get(final Class<T> type, final String name) {
        return (List<T>) this.get(name);
    }

    @Override
    public Detector getDetector() {
        return conditionsManager.getCachedConditions(Detector.class, "compact.xml").getCachedData();
    }

    @Override
    public String getDetectorName() {
        return detectorName;
    }

    @Override
    public Map<String, float[]> getFloatParameters() {
        return floatParameters;
    }

    @Override
    public Map<String, int[]> getIntegerParameters() {
        return intParameters;

    }

    @Override
    public Set<List> getLists() {
        return metaDataMap.keySet();
    }

    @Override
    public List<MCParticle> getMCParticles() {
        return this.get(MCParticle.class, MC_PARTICLES);
    }

    @Override
    public Collection<LCMetaData> getMetaData() {
        return metaDataMap.values();
    }

    @Override
    public LCMetaData getMetaData(final List x) {
        return metaDataMap.get(x);
    }

    @Override
    public Map<String, String[]> getStringParameters() {
        return stringParameters;
    }

    @Override
    public float getWeight() {
        return 1.0f;
    }

    @Override
    public boolean hasCollection(final Class type) {
        for (final LCMetaData meta : metaDataMap.values()) {
            if (type.isAssignableFrom(meta.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCollection(final Class type, final String name) {
        if (!this.hasItem(name)) {
            return false;
        }
        final Object collection = this.get(name);
        if (!(collection instanceof List)) {
            return false;
        }
        return type.isAssignableFrom(metaDataMap.get(collection).getType());
    }

    @Override
    public boolean hasItem(final String name) {
        return super.keys().contains(name);
    }

    @Override
    public void put(final String name, final List collection, final Class type, final int flags) {
        this.put(name, collection, type, flags, null);
    }

    @Override
    public void put(final String name, final List collection, final Class type, final int flags, final Map intMap,
            final Map floatMap, final Map stringMap) {
        super.put(name, collection);
        final LCMetaData meta = new MetaData(name, type, flags, intMap, floatMap, stringMap);
        metaDataMap.put(collection, meta);
    }

    @Override
    public void put(final String name, final List collection, final Class type, final int flags,
            final String readoutName) {
        super.put(name, collection);

        final LCMetaData meta = new MetaData(name, type, flags, readoutName);
        metaDataMap.put(collection, meta);

        this.setCollectionMetaData(collection, type, meta);
    }

    @Override
    public void put(final String name, final Object component) {
        // Check if collection exists already which is an error.
        if (this.hasItem(name)) {
            throw new IllegalArgumentException("An item called " + name + " already exists in the event.");
        }
        super.put(name, component);
        if (component instanceof List) {
            final List list = (List) component;
            Class type = list.isEmpty() ? Object.class : list.get(0).getClass();
            for (final Object o : list) {
                if (!type.isAssignableFrom(o.getClass())) {
                    type = Object.class;
                }
            }
            metaDataMap.put(list, new MetaData(name, type, 0, null));
        }
    }

    /**
     * Removes a collection from the event.
     */
    @Override
    public void remove(final String name) {
        final Object collection = this.get(name);
        if (collection instanceof List) {
            metaDataMap.remove(collection);
        }
        super.keys().remove(name);
    }

    private void setCollectionMetaData(final List collection, final Class type, final LCMetaData meta) {
        // Set MetaData on collection objects if necessary.
        if (Hit.class.isAssignableFrom(type)) {
            for (final Object o : collection) {
                final Hit hit = (Hit) o;
                if (hit.getMetaData() == null) {
                    ((Hit) o).setMetaData(meta);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Run " + this.getRunNumber() + " Event " + this.getEventNumber() + " ("
                + new Date(this.getTimeStamp() / NANO_SECONDS) + ") Detector: " + detectorName;
    }
}
