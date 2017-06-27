package org.lcsim.detector.converter.compact;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierField;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.IdentifierDictionary;
import org.lcsim.detector.identifier.IdentifierDictionaryManager;
import org.lcsim.detector.identifier.IdentifierField;
import org.lcsim.detector.identifier.IdentifierHelper;
import org.lcsim.geometry.Readout;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.util.IDDescriptor;

/**
 * An abstract implementation of {@link ISubdetectorConverter} that provides some utilities 
 * and default method implementations for Subdetector conversion.
 * @author Jeremy McCormick
 * @version $Id: AbstractSubdetectorConverter.java,v 1.9 2011/02/25 03:09:38 jeremy Exp $
 */
public abstract class AbstractSubdetectorConverter implements ISubdetectorConverter {

    public IDetectorElement makeSubdetectorDetectorElement(Detector detector, Subdetector subdetector) {
        IDetectorElement subdetectorDE = new SubdetectorDetectorElement(subdetector.getName(), detector.getDetectorElement());
        subdetector.setDetectorElement(subdetectorDE);
        return subdetectorDE;
    }

    /**
     * Sub-classes must implement this.
     */
    public abstract void convert(Subdetector subdet, Detector detector);

    /**
     * Sub-classes must implement this.
     */
    public abstract Class<? extends Subdetector> getSubdetectorType();

    /**
     * Creates a concrete {@link org.lcsim.detector.identifier.IIdentifierHelper} for this Subdetector. 
     * Concrete converter types should override this if they want to use a different type of helper.
     * 
     * @return An IIdentifierHelper for this Subdetector.
     */
    public IIdentifierHelper makeIdentifierHelper(Subdetector subdetector, SystemMap systemMap) {
        // Do not create helper if there is no Readout.
        if (subdetector.getReadout() == null) {
            return null;
        }

        // Make the IdentifierDictionary.
        IIdentifierDictionary iddict = makeIdentifierDictionary(subdetector);

        IIdentifierHelper helper = null;

        if (iddict.hasField("system") && iddict.hasField("barrel")) {
            helper = new DetectorIdentifierHelper(subdetector.getDetectorElement(), iddict, systemMap);
        } else {
            // Make a generic IdentifierHelper if standard fields are not present.
            helper = new IdentifierHelper(iddict);
        }

        return helper;
    }

    /**
     * Utility method for creating an {@link org.lcsim.detector.identifier.IIdentifierDictionary} 
     * for a {@link org.lcsim.geometry.Subdetector} by using its {@link org.lcsim.geometry.IDDecoder}.
     * 
     * @param subdet The Subdetector.
     * @return A IdentifierDictionary.
     */
    static IIdentifierDictionary makeIdentifierDictionary(Subdetector subdet) {
        Readout ro = subdet.getReadout();
        IIdentifierDictionary iddict = null;
        if (ro != null) {
            IDDescriptor desc = ro.getIDDescriptor();
            List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
            for (int i = 0; i < desc.fieldCount(); i++) {
                int nbits = Math.abs(desc.fieldLength(i));
                int start = desc.fieldStart(i);
                boolean signed = desc.isSigned(i);
                String name = desc.fieldName(i);
                IIdentifierField field = new IdentifierField(name, nbits, start, signed);
                fields.add(field);
            }

            iddict = new IdentifierDictionary(ro.getName(), fields);
            IdentifierDictionaryManager.getInstance().addIdentifierDictionary(iddict);
        }
        return iddict;
    }

    public void makeIdentifiers(Subdetector subdet) {

        // FIXME: Should this throw an Exception?
        if (subdet.getDetectorElement() == null)
            return;

        // FIXME: Should this throw an Exception?
        if (subdet.getDetectorElement().getIdentifierHelper() == null)
            return;

        IIdentifierHelper helper = subdet.getDetectorElement().getIdentifierHelper();
        IIdentifierDictionary iddict = helper.getIdentifierDictionary();

        // Create a new ID.
        IExpandedIdentifier expId = new ExpandedIdentifier(helper.getIdentifierDictionary().getNumberOfFields());

        // Set the system ID.
        int system = subdet.getSystemID();
        if (iddict.hasField("system")) {
            int systemIndex = iddict.getFieldIndex("system");
            expId.setValue(systemIndex, system);
        }

        // Set the ID on the primary subdetector container.
        IIdentifier id = helper.pack(expId);
        subdet.getDetectorElement().setIdentifier(id);

        // Set the endcap IDs if applicable.
        if (subdet.isEndcap()) {
            if (iddict.hasField("barrel")) {
                int barrelIndex = iddict.getFieldIndex("barrel");
                // Create IDs for the positive and negative endcaps.
                for (IDetectorElement endcap : subdet.getDetectorElement().getChildren()) {
                    // This is an endcap in the positive Z direction.
                    if (endcap.getName().contains("positive")) {
                        expId.setValue(barrelIndex, 1);
                    }
                    // This is an endcap in the negative Z direction.
                    else if (endcap.getName().contains("negative")) {
                        expId.setValue(barrelIndex, 2);
                    }
                    IIdentifier endcapId = helper.pack(expId);
                    endcap.setIdentifier(endcapId);
                }
            }
        }
    }
}