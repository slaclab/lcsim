package org.lcsim.geometry.compact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.material.XMLMaterialManager;
import org.lcsim.units.clhep.Constants;
import org.lcsim.util.xml.ClasspathEntityResolver;
import org.lcsim.util.xml.ElementFactory;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;
import org.lcsim.util.xml.JDOMExpressionFactory;

import Jama.Matrix;

/**
 * A tool for reading xml files containing compact detector descriptions.
 * 
 * This class does not create subclass objects. For example, CylindricalBarrelCalorimeter is inserted into
 * Detector as a generic Subdetector. To get subclasses, use the org.lcsim.geometry.GeometryReader class,
 * which extends this.
 * 
 * @author tonyj
 * @version $Id: CompactReader.java,v 1.49 2013/04/24 02:00:35 jeremy Exp $
 * 
 */
public class CompactReader {
    private ElementFactory factory;
    private JDOMExpressionFactory expr;
    private Document doc;
    private XMLMaterialManager xmat;

    /**
     * Create a CompactReader using a DefaultElementFactory.
     */
    public CompactReader() {
        this(new CompactElementFactory());
    }

    /**
     * Create a CompactReader using the specified ElementFactory.
     * 
     * @param factory The ElementFactory to be used for creating elements as the file is parsed.
     */
    public CompactReader(ElementFactory factory) {
        this.factory = factory;
    }

    /**
     * Read a compact geometry XML file.
     * 
     * @param in The input stream to read.
     * @throws java.io.IOException If an IO error occurs while reading the stream.
     * @throws org.jdom.JDOMException If invalid XML is found while reading the file.
     * @throws org.lcsim.geometry.compact.ElementFactory.ElementCreationException If the ElementFactory throws
     *             an ElementCreationException.
     * @return The parsed detector description.
     */
    public Detector read(InputStream in) throws IOException, JDOMException, ElementCreationException {
        expr = new JDOMExpressionFactory();

        // Setup CLHEP units in the expression evaluator.
        registerCLHEPConstants(expr);

        SAXBuilder builder = new SAXBuilder();
        builder.setFactory(expr);

        // Enable schema validation
        builder.setValidation(true);
        builder.setFeature("http://apache.org/xml/features/validation/schema", true);

        // Use a classpath entity resolver to get the schemas from the jar file.
        builder.setEntityResolver(new ClasspathEntityResolver());

        doc = builder.build(in);

        Element compact = doc.getRootElement();
        Detector det = factory.createElement(Detector.class, compact, null);

        readHeader(compact, det);
        readConstants(compact, det);
        readMatrices(compact, det);
        readRegions(compact, det);
        readLimits(compact, det);
        readMaterials(compact, det);
        Map<String, Readout> readoutMap = readReadouts(compact, det);
        readVisAttributes(compact, det);
        readSubdetectors(compact, det, readoutMap);
        readFields(compact, det);
        readIncludes(compact, det);

        return det;
    }

    private void readConstants(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Element define = lccdd.getChild("define");
        for (Iterator i = define.getChildren("constant").iterator(); i.hasNext();) {
            Element constant = (Element) i.next();
            Constant c = factory.createElement(Constant.class, constant, null);
            expr.addConstant(c.getName(), c.getValue());
            det.addConstant(c);
        }
    }
    
    private void readMatrices(Element compact, Detector det) {
        Element define = compact.getChild("define");
        for (Iterator iterator = define.getChildren("matrix").iterator(); iterator.hasNext();) {
            Element element = (Element)iterator.next();
            String name = element.getAttributeValue("name");
            int coldim = Integer.parseInt(element.getAttributeValue("coldim"));
            String rawValues = element.getAttributeValue("values");
            Scanner scanner = new Scanner(rawValues);
            List<double[]> rows = new ArrayList<double[]>();
            while (scanner.hasNextDouble()) {
                double[] row = new double[coldim];
                for (int i=0; i<coldim; i++) {
                    double value = scanner.nextDouble();
                    row[i] = value;
                }
                rows.add(row);
            }
            double[][] array = new double[rows.size()][coldim];
            for (int i=0, n=rows.size(); i<n; i++) {
                array[i] = rows.get(i);
            }
            Matrix matrix = new Matrix(array);
            det.addMatrix(name, matrix);            
        }
    }

    private void readHeader(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Element info = lccdd.getChild("info");
        det.setHeader(factory.createElement(Header.class, info, null));
    }

    private void readRegions(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Element regions = lccdd.getChild("regions");
        if (regions != null) {
            for (Iterator i = regions.getChildren("region").iterator(); i.hasNext();) {
                Element region = (Element) i.next();
                Region r = factory.createElement(Region.class, region, null);
                det.addRegion(r);
            }
        }
    }

    private void readLimits(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Element limits = lccdd.getChild("limits");
        if (limits != null) {
            for (Iterator i = limits.getChildren("limitset").iterator(); i.hasNext();) {
                Element limitset = (Element) i.next();
                LimitSet ls = factory.createElement(LimitSet.class, limitset, null);
                det.addLimitSet(ls);
            }
        }
    }

    private Map<String, Readout> readReadouts(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Map<String, Readout> readoutMap = new HashMap<String, Readout>();
        Element readouts = lccdd.getChild("readouts");
        for (Iterator i = readouts.getChildren("readout").iterator(); i.hasNext();) {
            Element readout = (Element) i.next();

            Readout r = null;
            try {
                r = createReadout(readout);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            readoutMap.put(r.getName(), r);
            det.addReadout(r);
        }
        return readoutMap;
    }

    private Readout createReadout(Element readoutElement) throws Exception {
        Readout readout = factory.createElement(Readout.class, readoutElement, null);
        Element segmentation = readoutElement.getChild("segmentation");

        // Setup an IDDecoder with segmentation for a calorimeter.
        if (segmentation != null) {
            String type = segmentation.getAttributeValue("type");
            Segmentation seg = factory.createElement(Segmentation.class, segmentation, type);
            readout.setSegmentation(seg);
        }
        // Use a generic IDDecoder.
        else {
            readout.setIDDecoder(new BaseIDDecoder());
        }

        return readout;
    }

    private void readSubdetectors(Element lccdd, Detector det, Map<String, Readout> readoutMap) throws JDOMException, ElementCreationException {
        Element detectors = lccdd.getChild("detectors");
        for (Iterator i = detectors.getChildren("detector").iterator(); i.hasNext();) {
            Element detector = (Element) i.next();
            String type = detector.getAttributeValue("type");

            Subdetector sub = factory.createElement(Subdetector.class, detector, type);
            String readout = detector.getAttributeValue("readout");
            if (readout != null) {
                Readout r = readoutMap.get(readout);
                if (r == null)
                    throw new JDOMException("Unknown readout " + readout);
                sub.setReadout(r);
            }

            String visref = detector.getAttributeValue("vis");
            if (visref != null) {
                VisAttributes vis = det.getVisAttributes().get(visref);
                if (vis == null)
                    throw new JDOMException("Unknown vis " + visref + " for subdetector " + sub.getName() + " in compact description.");
                sub.setVisAttributes(vis);
            }

            det.addSubdetector(sub);
        }
    }

    private void readFields(Element lccdd, Detector det) throws JDOMException, ElementCreationException {
        Element fields = lccdd.getChild("fields");
        if (fields != null) {
            for (Iterator i = fields.getChildren("field").iterator(); i.hasNext();) {
                Element f = (Element) i.next();
                String type = f.getAttributeValue("type");

                Field field = factory.createElement(Field.class, f, type);
                det.addField(field);
            }
        }
    }

    private void readMaterials(org.jdom.Element compact, Detector det) {
        // Setup XMLMatMgr's default data. This needs to be called before
        // the compact materials are loaded in order to resolve references.
        XMLMaterialManager.setup();

        // Create XMLMatMgr for this detector's materials.
        if (compact.getChild("materials") != null) {
            xmat = new XMLMaterialManager(compact.getChild("materials"));

            // FIXME Need to call this here???
            // matmgr.addReferencesFromCompact(lccdd);
        }

        // Set the detector's XMLMaterialManager, so it is accessible to
        // clients such as the LCDD converter.
        det.setXMLMaterialManager(xmat);
    }

    /**
     * Create the VisAttributes objects from vis elements in the display block.
     * 
     * @param lccdd
     * @param det
     * @throws JDOMException
     * @throws ElementCreationException
     */
    private void readVisAttributes(Element lccdd, Detector det) throws JDOMException, ElementCreationException {

        Element display = lccdd.getChild("display");
        if (display != null) {
            for (Iterator i = display.getChildren("vis").iterator(); i.hasNext();) {
                Element vis = (Element) i.next();
                assert (vis != null);
                VisAttributes v = factory.createElement(VisAttributes.class, vis, null);
                det.addVisAttributes(v);
            }

            // Add an invisible vis settings that shows daughters.
            VisAttributes invisible = new VisAttributes("InvisibleWithDaughters");
            invisible.setVisible(false);
            invisible.setShowDaughters(true);
            det.addVisAttributes(invisible);

            // Add an invisible vis settings that shows daughters.
            VisAttributes invisibleNoDau = new VisAttributes("InvisibleNoDaughters");
            invisibleNoDau.setVisible(false);
            invisibleNoDau.setShowDaughters(false);
            det.addVisAttributes(invisibleNoDau);
        }
    }

    // TODO: Should be protected or private but need to fix some external code first.
    public static void registerCLHEPConstants(JDOMExpressionFactory f) {
        Constants constants = Constants.getInstance();
        for (Entry<String, Double> unit : constants.entrySet()) {
            // System.out.println("adding constant " + unit.getKey() + "=" +unit.getValue());
            f.addConstant(unit.getKey(), unit.getValue());
        }
    }

    protected void resetDocument() {
        doc = null;
    }

    public Document getDocument() {
        return doc;
    }

    void readIncludes(Element lccdd, Detector det) {
        Element includes = lccdd.getChild("includes");

        if (includes == null)
            return;

        for (Object o : includes.getChildren("gdmlFile")) {
            Element gdmlFile = (Element) o;

            if (gdmlFile.getAttribute("ref") != null) {

                String ref = gdmlFile.getAttributeValue("ref");

                // System.out.println("merging in " + ref);

                try {
                    det.addGDMLReference(new URL(ref));
                } catch (Exception x) {
                    throw new RuntimeException(x);
                }
            } else if (gdmlFile.getAttribute("file") != null) {
                File file = new File(gdmlFile.getAttributeValue("file"));
                try {
                    URL url = new URL(file.toURI().toURL().toString());
                    det.addGDMLReference(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Missing ref or file attribute on gdmlFile element.");
            }
        }
    }
}
