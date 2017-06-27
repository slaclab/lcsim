package org.lcsim.detector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.detector.identifier.IIdentifier;

public class DetectorElementIdentifierHash {

    private Map<Long, IDetectorElementContainer> idhash = new HashMap<Long, IDetectorElementContainer>();

    public void put(IDetectorElement de) {
        
        IIdentifier id = de.getIdentifier();

        // Ignore null id.
        if (id == null) {
            return;
        }

        // Ignore invalid id.
        if (!id.isValid()) {
            //System.out.println("ignoring invalid ID " + de.getIdentifier().getValue() + " from " + de.getName());
            return;
        }

        Long rawid = id.getValue();

        // Create list if doesn't exist.
        if (idhash.get(rawid) == null) {
            idhash.put(rawid, new DetectorElementContainer());
        } /*else {
            //System.out.println("WARNING: already have container for ID " + rawid);
        }*/

        // Add a hash from id to DetectorElement.
        idhash.get(rawid).add(de);
    }

    public IDetectorElementContainer get(IIdentifier id) {
        return get(id.getValue());
    }

    public IDetectorElementContainer get(Long rawid) {
        IDetectorElementContainer container = idhash.get(rawid);
        if (container == null) {
            container = new DetectorElementContainer();
        }
        return container;
    }

    public IDetectorElementContainer get(List<IIdentifier> ids) {
        IDetectorElementContainer ret = new DetectorElementContainer();
        for (IIdentifier id : ids) {
            IDetectorElementContainer src = get(id);
            ret.addAll(src);
        }
        return ret;
    }

    public void clear() {
        idhash.clear();
    }
}