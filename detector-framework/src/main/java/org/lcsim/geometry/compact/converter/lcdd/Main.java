package org.lcsim.geometry.compact.converter.lcdd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.geometry.compact.CompactReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.converter.Converter;
import org.lcsim.util.xml.ClasspathEntityResolver;
import org.lcsim.util.xml.ElementFactory;

/**
 * This class is the front end for generating LCDD output from the compact
 * description.
 * 
 * @author tonyj
 * @author jeremym
 */
public class Main implements Converter {

    private boolean validate = true;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2)
            usage();
        InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
        OutputStream out = args.length == 1 ? System.out : new BufferedOutputStream(new FileOutputStream(args[1]));
        new Main().convert(args[0], in, out);
    }

    public Main() throws Exception {
        setValidationFromSystemProperty();
    }

    private void setValidationFromSystemProperty() {
        boolean validateProp = Boolean.parseBoolean(System.getProperty("org.lcsim.geometry.compact.converter.lcdd.validate", "true"));
        this.validate = validateProp;
        if (validateProp == false) {
            Logger.getLogger(Main.class.getName()).warning("XML output validation is turned OFF.");
        }
    }

    public void convert(String inputFileName, InputStream in, OutputStream out) throws Exception {
        ElementFactory factory = new LCDDElementFactory();
        CompactReader reader = new CompactReader(factory);
        Detector det = reader.read(in);
        Document doc = ((LCDDDetector) det).writeLCDD(inputFileName);

        XMLOutputter outputter = new XMLOutputter();
                
        if (validate) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            outputter.output(doc, stream);
            stream.close();

            SAXBuilder builder = new SAXBuilder();
            builder.setEntityResolver(new ClasspathEntityResolver());
            builder.setValidation(true);
            builder.setFeature("http://apache.org/xml/features/validation/schema", true);

            builder.build(new ByteArrayInputStream(stream.toByteArray()));
        }

        if (out != null) {
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, out);            
            out.close();
        }
    }

    private static void usage() {
        System.out.println("java " + Main.class.getName() + " <compact> [<lcdd>]");
        System.exit(0);
    }

    public String getOutputFormat() {
        return "lcdd";
    }

    public FileFilter getFileFilter() {
        return new LCDDFileFilter();
    }

    private static class LCDDFileFilter extends FileFilter {

        public boolean accept(java.io.File file) {
            return file.isDirectory() || file.getName().endsWith(".lcdd");
        }

        public String getDescription() {
            return "LCDD file (*.lcdd)";
        }
    }
}