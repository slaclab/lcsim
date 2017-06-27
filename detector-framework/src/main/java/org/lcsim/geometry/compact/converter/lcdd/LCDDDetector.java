package org.lcsim.geometry.compact.converter.lcdd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.Readout;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Author;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;
import org.lcsim.geometry.compact.converter.lcdd.util.Constant;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.Detector;
import org.lcsim.geometry.compact.converter.lcdd.util.Generator;
import org.lcsim.geometry.compact.converter.lcdd.util.HitProcessor;
import org.lcsim.geometry.compact.converter.lcdd.util.HitsCollection;
import org.lcsim.geometry.compact.converter.lcdd.util.IDField;
import org.lcsim.geometry.compact.converter.lcdd.util.IDSpec;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDDMaterialHelper;
import org.lcsim.geometry.compact.converter.lcdd.util.Limit;
import org.lcsim.geometry.compact.converter.lcdd.util.LimitSet;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Region;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Tracker;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.UnsegmentedCalorimeter;
import org.lcsim.geometry.compact.converter.lcdd.util.VisAttributes;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.util.cache.FileCache;

import Jama.Matrix;

/**
 * 
 * @author tonyj
 */
class LCDDDetector extends org.lcsim.geometry.compact.Detector {
    
    // Reference to compact doc.
    private Element compact;

    // Checksum of LCDD file.
    private long checksum;

    LCDDDetector(Element node) {
        super(node);
        this.compact = node;
    }

    Document writeLCDD(String filename) throws IOException, JDOMException {
        
        LCDD lcdd = new LCDD();

        checksum = calculateChecksum(compact);

        Detector detector = lcdd.getHeader().getDetector();
        detector.setTitle(getHeader().getDetectorName());
        lcdd.getHeader().setComment(getHeader().getComment());

        Generator generator = lcdd.getHeader().getGenerator();
        generator.setTitle(getName());
        generator.setVersion(getVersion());
        generator.setFile(filename);
        generator.setChecksum(getChecksum());

        Author author = lcdd.getHeader().getAuthor();
        author.setAuthorName(getHeader().getAuthor());

        /* standard defines */
        Define define = lcdd.getDefine();
        Rotation rotation = new Rotation("identity_rot");
        define.addRotation(rotation);

        Rotation reflect = new Rotation("reflect_rot");
        reflect.setX(Math.PI);
        define.addRotation(reflect);

        Position position = new Position("identity_pos");
        define.addPosition(position);

        /* constants */
        for (org.lcsim.geometry.compact.Constant c : getConstants().values()) {
            // System.out.println("adding constant [" + c.getName() + "] = [" + c.getValue() + "] with value = [" + String.valueOf(c.getValue()) + "]");
            define.addConstant(new Constant(c.getName(), String.valueOf(c.getValue())));
        }
        
        /* Write out the matrices associated with this detector, e.g. for material properties. */
        writeMatrices(lcdd);

        /* Setup materials. */
        setupMaterials(lcdd);

        // Readouts.
        Map<Readout, IDSpec> idMap = writeReadouts(lcdd);

        // Regions.
        writeRegions(lcdd);

        // Limits.
        writeLimits(lcdd);

        // Vis attributes.
        writeVisAttribs(lcdd);

        // world volume
        writeWorldVolume(lcdd);
        
        // tracking volume
        writeTrackingVolume(lcdd);
        
        // Subdetectors.
        writeSubdetectors(lcdd, idMap);

        // Fields.
        writeFields(lcdd);

        // GDML references.
        writeGDMLRefs(lcdd);

        lcdd.cleanUp();

        // Make LCDD document.
        Document doc = new Document();
        doc.setRootElement(lcdd);

        return doc;
    }

    private void writeGDMLRefs(LCDD lcdd) throws IOException, FileNotFoundException {
        FileCache cache = new FileCache();
        for (URL gdmlFile : getGDMLReferences()) {
            lcdd.mergeGDML(new FileInputStream(cache.getCachedFile(gdmlFile)));
        }
    }

    private void writeFields(LCDD lcdd) throws JDOMException {
        for (Field field : getFields().values()) {
            ((LCDDField) field).addToLCDD(lcdd);
        }
    }

    private void writeSubdetectors(LCDD lcdd, Map<Readout, IDSpec> idMap) throws JDOMException {
        for (Subdetector sub : getSubdetectors().values()) {
            SensitiveDetector sensitiveDetector = null;
            Readout readout = sub.getReadout();
            if (readout != null) {
                if (sub.isTracker()) {
                    sensitiveDetector = new Tracker(sub.getName());
                } else if (sub.isCalorimeter()) {
                    if (readout.getSegmentation() != null && readout.getSegmentation().useForHitPosition()) {
                        // Standard segmented calorimeter.
                        sensitiveDetector = new Calorimeter(sub.getName());
                    } else {
                        // Unsegmented calorimeter for detailed readout studies.
                        // May still have Segmentation but it is not used for the hit positions.
                        sensitiveDetector = new UnsegmentedCalorimeter(sub.getName());
                    }
                }
                
                if (sensitiveDetector != null) {
                    sensitiveDetector.setIDSpec(idMap.get(readout));
                    sensitiveDetector.setHitsCollection(readout.getName());
                    lcdd.addSensitiveDetector(sensitiveDetector);
                }

                
                /* Add the hit_processor elements. */
                for (String processorName : readout.getProcessorNames()) {
                    sensitiveDetector.addContent(new HitProcessor(processorName));
                }
                
                /* Add the hits_collection elements. */
                for (String collectionName : readout.getCollectionNames()) {
                    sensitiveDetector.addContent(new HitsCollection(collectionName));
                }
                
                if (sub.isCalorimeter()) {
                    try {
                        if (readout.getSegmentation() != null) {
                            LCDDSegmentation seg = (LCDDSegmentation) readout.getSegmentation();
                            seg.setSegmentation((Calorimeter) sensitiveDetector);
                        }
                    } catch (Throwable x) {
                        throw new RuntimeException("Readout " + readout.getName() + " is not a valid Segmentation object.", x);
                    }
                }

            }

            // System.out.println("class:" + sub.getClass().getCanonicalName());

            if (sub instanceof LCDDSubdetector) {
                ((LCDDSubdetector) sub).addToLCDD(lcdd, sensitiveDetector);
            } else {
                throw new RuntimeException("Subdetector is not an instanceof LCDDSubdetector.");
            }
        }
    }

    private void writeVisAttribs(LCDD lcdd) {
        // Visualization attributes.
        for (org.lcsim.geometry.compact.VisAttributes vis : getVisAttributes().values()) {
            VisAttributes lcddvis = new VisAttributes(vis.getName());

            float rgba[] = vis.getRGBA();
            lcddvis.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
            lcddvis.setDrawingStyle(vis.getDrawingStyle());
            lcddvis.setLineStyle(vis.getLineStyle());
            lcddvis.setVisible(vis.getVisible());
            lcddvis.setShowDaughters(vis.getShowDaughters());

            lcdd.add(lcddvis);
        }
    }

    private void writeLimits(LCDD lcdd) {
        for (org.lcsim.geometry.compact.LimitSet limitset : getLimits().values()) {
            LimitSet lcddLimitSet = new LimitSet(limitset.getName());
            for (org.lcsim.geometry.compact.Limit limit : limitset.getLimits().values()) {
                Limit lcddLimit = new Limit(limit.getName());

                lcddLimit.setParticles(limit.getParticles());
                lcddLimit.setUnit(limit.getUnit());
                lcddLimit.setValue(limit.getValue());

                lcddLimitSet.addLimit(lcddLimit);
            }
            lcdd.addLimitSet(lcddLimitSet);
        }
    }

    private void writeRegions(LCDD lcdd) {
        for (org.lcsim.geometry.compact.Region region : getRegions().values()) {
            Region lcddRegion = new Region(region.getName());
            lcddRegion.setStoreSecondaries(region.getStoreSecondaries());
            lcddRegion.setKillTracks(region.getKillTracks());
            lcddRegion.setThreshold(region.getEnergyThreshold());
            lcddRegion.setEnergyUnit(region.getEnergyUnit());
            lcddRegion.setLengthUnit(region.getLengthUnit());
            lcddRegion.setCut(region.getRangeCut());
            lcdd.addRegion(lcddRegion);
        }
    }
    
    private void writeMatrices(LCDD lcdd) {
        for (String name : this.getMatrices().keySet()) {
             Matrix matrix = this.getMatrix(name);
             int coldim = matrix.getColumnDimension();             
             org.lcsim.geometry.compact.converter.lcdd.util.Matrix lcddMatrix = 
                     new org.lcsim.geometry.compact.converter.lcdd.util.Matrix(name, coldim);                         
             String values = "";
             for (int i=0, n=matrix.getRowDimension(); i<n; i++) {
                 String row = "";
                 for (int j=0; j<coldim; j++) {
                     row += matrix.get(i, j) + " ";
                 }
                 values += row.trim() + '\n';
             }
             values = values.trim();
             lcddMatrix.setValues(values);
             lcdd.add(lcddMatrix);
        }
    }

    private Map<Readout, IDSpec> writeReadouts(LCDD lcdd) {
        // Loop over the readouts
        Map<Readout, IDSpec> idMap = new HashMap<Readout, IDSpec>();
        for (Readout readout : getReadouts().values()) {            
            /* Setup the IdSpec for this readout. */
            setupIdSpec(lcdd, idMap, readout);            
        }
        return idMap;
    }

    private void setupIdSpec(LCDD lcdd, Map<Readout, IDSpec> idMap, Readout readout) {
        IDDescriptor id = readout.getIDDescriptor();
        IDSpec idspec = new IDSpec(readout.getName());
        idspec.setLength(id.getMaxBit());

        for (int i = 0; i < id.fieldCount(); i++) {
            IDField field = new IDField();
            field.setLabel(id.fieldName(i));
            field.setLength(id.fieldLength(i));
            field.setStart(id.fieldStart(i));
            field.setSigned(id.isSigned(i));
            idspec.addIDField(field);
        }
        lcdd.addIDSpec(idspec);
        idMap.put(readout, idspec);
    }

    private void setupMaterials(LCDD lcdd) throws JDOMException {
        LCDDMaterialHelper helper = new LCDDMaterialHelper(getXMLMaterialManager());
        helper.copyToLCDD(compact, lcdd);
    }
    
    private void writeWorldVolume(LCDD lcdd) {
        Box worldSolid = new Box("world_box");
        worldSolid.setAttribute("x", "world_x");
        worldSolid.setAttribute("y", "world_y");
        worldSolid.setAttribute("z", "world_z");
        lcdd.getSolids().addSolid(worldSolid);

        Volume worldVolume = new Volume("world_volume");
        worldVolume.setSolid(worldSolid);        
        lcdd.getStructure().setWorldVolume(worldVolume);
        
        Element world = new Element("world");
        world.setAttribute("ref", worldVolume.getRefName());
        lcdd.getSetup().addContent(world);
    }
    
    private void writeTrackingVolume(LCDD lcdd) {
        
        Tube trackingSolid = new Tube("tracking_cylinder");
        trackingSolid.setAttribute("rmax", "tracking_region_radius");
        trackingSolid.setAttribute("z", "2*tracking_region_zmax");
        trackingSolid.setAttribute("deltaphi", String.valueOf(2 * Math.PI));
        
        Define define = lcdd.getDefine();        
        Position pos = new Position("tracking_region_pos");
        if (define.getConstant("tracking_region_z") != null) {
            double trackingRegionZ = 0;
            try {
                trackingRegionZ = define.getConstant("tracking_region_z").getConstantValue();
            } catch (DataConversionException e) {
                throw new RuntimeException(e);
            }
            pos.setZ(trackingRegionZ);
        }
        define.addPosition(pos);
                
        lcdd.getSolids().addSolid(trackingSolid);

        Volume trackingVolume = new Volume("tracking_volume");
        trackingVolume.setSolid(trackingSolid);
        lcdd.getStructure().setTrackingVolume(trackingVolume);
        PhysVol trackingPhysVol = new PhysVol(trackingVolume);
        trackingPhysVol.setPosition(pos);
        lcdd.getWorldVolume().addPhysVol(trackingPhysVol);
    }
        
    public String getName() {
        return "lcsim";
    }

    private String getVersion() {
        return "1.0";
    }

    private long getChecksum() {
        return checksum;
    }

    private long calculateChecksum(Element top) throws IOException {
        
        // Write out in canonical format to calculate checksum
        // ignoring comments and whitespace
        Iterator iter = top.getDescendants(new ContentFilter(ContentFilter.COMMENT));
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }

        CRC32 check = new CRC32();
        OutputStream out = new CheckedOutputStream(new NullOutputStream(), check);

        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getCompactFormat());
        outputter.output(top, out);
        out.close();
        return check.getValue();
    }

    private static class NullOutputStream extends OutputStream {
        public void write(byte[] b, int off, int len) throws IOException {
        }

        public void write(byte[] b) throws IOException {
        }

        public void write(int b) throws IOException {
        }
    }
}
