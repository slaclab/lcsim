package org.lcsim.geometry;

import java.io.IOException;
import java.io.InputStream;

import org.jdom.JDOMException;
import org.lcsim.detector.converter.compact.DetectorConverter;
import org.lcsim.geometry.compact.CompactElementFactory;
import org.lcsim.geometry.compact.CompactReader;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.Segmentation;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;
import org.lcsim.detector.DetectorStore;

/**
 * The GeometryReader extends the CompactReader and creates specific types based on the type attribute of an XML
 * element. The type string must match a class within the registered package for that base class. Currently supported
 * base classes with specific types are Subdetector, Segmentation, and Field.
 * <p>
 * For example, this compact XML will instantiate a Subdetector with specific type of 'CylindricalBarrelCalorimeter'.<br/> 
 * <pre>
 * <detector type="CylindricalBarrelCalorimeter" ... /> Support added for detailed geometry representation.
 * </pre>
 * 
 * @author Tony Johnson, SLAC
 * @author Jeremy McCormick, SLAC
 */
public class GeometryReader extends CompactReader {

    boolean buildDetailed = true;

    public GeometryReader() {
        super(new GeometryFactory());
    }

    public Detector read(InputStream in) throws IOException, JDOMException, ElementCreationException {
        
        // Build a compact description with support for specific types.
        Detector detector = (Detector) super.read(in);

        // Clear existing detector store.
        DetectorStore.getInstance().clear();

        // Build the detailed detector description.
        if (buildDetailed) {
            DetectorConverter cnv = new DetectorConverter();
            cnv.registerSubdetectorConverters();
            cnv.convert(detector, getDocument());
        }

        // Clear the cached document.
        resetDocument();

        return detector;
    }

    public boolean buildDetailed() {
        return buildDetailed;
    }

    public void setBuildDetailed(boolean buildDetailed) {
        this.buildDetailed = buildDetailed;
    }

    static class GeometryFactory extends CompactElementFactory {
        GeometryFactory() {
            super();
            register(Detector.class);
            register(Subdetector.class, "org.lcsim.geometry.subdetector");
            register(Segmentation.class, "org.lcsim.geometry.segmentation");
            register(Field.class, "org.lcsim.geometry.field");
        }
    }
}
