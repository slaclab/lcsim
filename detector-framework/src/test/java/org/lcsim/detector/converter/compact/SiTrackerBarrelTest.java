package org.lcsim.detector.converter.compact;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTreeID;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepWriter;

import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * 
 * Writes an SiTrackerBarrel to HepRep to be looked at in Wired4.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class SiTrackerBarrelTest
extends TestCase
{
    private Detector detector;

    public SiTrackerBarrelTest(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(SiTrackerBarrelTest.class);
    }

    private static final String resource = 
        "/org/lcsim/detector/converter/compact/SiTrackerBarrelTest.xml";

    public void setUp()
    {
        InputStream in = 
            this.getClass().
            getResourceAsStream(resource);

        GeometryReader reader = new GeometryReader();

        try {
            detector = reader.read(in);
        }
        catch ( Throwable x )
        {
            throw new RuntimeException(x);
        }
    }
    
    public void testHepRep()
    {
        try {
            writeHepRep(new TestOutputFile("SiTrackerBarrelTest.heprep").getAbsolutePath());
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }
    }
    
    
    public final static String HITS_LAYER = "Hits";
    public final static String PARTICLES_LAYER = "Particles";

    private void writeHepRep(String filepath) throws Exception
    {
        HepRepFactory factory = HepRepFactory.create();
        HepRep root = factory.createHepRep();        

        // detector
        HepRepTreeID treeID = factory.createHepRepTreeID("DetectorType", "1.0");
        HepRepTypeTree typeTree = factory.createHepRepTypeTree(treeID);
        root.addTypeTree(typeTree);

        HepRepInstanceTree instanceTree = factory.createHepRepInstanceTree("Detector", "1.0", typeTree);
        root.addInstanceTree(instanceTree);

        String detectorLayer = "Detector";
        root.addLayer(detectorLayer);

        HepRepType barrel = factory.createHepRepType(typeTree, "Barrel");
        barrel.addAttValue("layer", detectorLayer);
        HepRepType endcap = factory.createHepRepType(typeTree, "Endcap");
        endcap.addAttValue("layer", detectorLayer);

        //DetectorElementToHepRepConverter cnv = 
        //    new DetectorElementToHepRepConverter();

        for (IDetectorElement de : DetectorElementStore.getInstance())
        {
            //System.out.println("converting : " + de.getName());
            DetectorElementToHepRepConverter.convert(de, factory, root, -1, false, null);
        }
        // end detector
        
        HepRepWriter writer = 
            HepRepFactory.create().createHepRepWriter(new FileOutputStream(filepath),false,false);
        writer.write(root,"test");
        writer.close();
    }        
}
