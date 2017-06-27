package org.lcsim.geometry.compact.converter.pandora;

import static org.lcsim.geometry.Calorimeter.CalorimeterType.EM_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.EM_ENDCAP;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_ENDCAP;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_ENDCAP;
import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticleType;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.detector.material.BetheBlochCalculator;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.converter.Converter;
import org.lcsim.geometry.field.Solenoid;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.segmentation.AbstractCartesianGrid;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;
import org.lcsim.geometry.subdetector.MultiLayerTracker;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.SamplingFractionManager;

/**
 * This class converts from a compact detector description into slicPandora's
 * geometry input format.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Main.java,v 1.34 2012/02/08 19:59:04 jeremy Exp $
 */
public class Main implements Converter
{
    private final static boolean DEBUG = false;
    
    // ConditionsManager instance.
    private ConditionsManager conditionsManager = ConditionsManager.defaultInstance();
    
    // Numerical formatting.
    static final DecimalFormat xlen = new DecimalFormat("#.########");
    static final DecimalFormat xfrac = new DecimalFormat("#.########");
    static final DecimalFormat xthick = new DecimalFormat("#.######");

    public void convert(String inputFileName, InputStream in, OutputStream out) throws Exception
    {
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);
        String detectorName = det.getDetectorName();
        try
        {
            conditionsManager.setDetector(detectorName, 0);
        }
        catch (ConditionsNotFoundException x)
        {
            throw new RuntimeException("Failed to setup conditions system for detector: " + detectorName, x);
        }
        Document doc = convertDetectorToPandora(det);
        XMLOutputter outputter = new XMLOutputter();
        if (out != null)
        {
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, out);
            out.close();
        }
    }

    public Document convertDetectorToPandora(Detector detector)
    {
        // Setup XML output document.
        Document outputDoc = new Document();
        Element root = new Element("pandoraSetup");
        outputDoc.setRootElement(root);
        Element calorimeters = new Element("calorimeters");
        root.addContent(calorimeters);
        
        // Add basic detector data element.
        Element detectorTag = new Element("detector");
        detectorTag.setAttribute("name", detector.getDetectorName());
        root.addContent(detectorTag);

        // Setup CalorimeterCalibration conditions.
        ConditionsSet calorimeterCalibration = null;
        try
        {
            calorimeterCalibration = conditionsManager.getConditions("CalorimeterCalibration");
        }
        catch (Exception x)
        {
        }
        boolean haveCalCalib = (calorimeterCalibration == null) ? false : true;

        // Process the subdetectors.
        for (Subdetector subdetector : detector.getSubdetectors().values())
        {
            //System.out.println(subdetector.getName());
            // Only handle calorimeters that are planar.
            if (subdetector instanceof AbstractPolyhedraCalorimeter)
            {
                Element calorimeter = new Element("calorimeter");
                AbstractPolyhedraCalorimeter polycal = (AbstractPolyhedraCalorimeter) subdetector;

                // Look for specific calorimeter types in the compact
                // description.
                Calorimeter.CalorimeterType calType = polycal.getCalorimeterType();
                if (calType.equals(HAD_BARREL) || calType.equals(HAD_ENDCAP) || calType.equals(EM_ENDCAP) || calType.equals(EM_BARREL) || calType.equals(MUON_BARREL) || calType.equals(MUON_ENDCAP))
                {
                    // Set basic parameters in pandora calorimeter.
                    calorimeter.setAttribute("type", Calorimeter.CalorimeterType.toString(calType));
                    calorimeter.setAttribute("innerR", Double.toString(polycal.getInnerRadius()));
                    calorimeter.setAttribute("innerZ", Double.toString(polycal.getInnerZ()));
                    calorimeter.setAttribute("innerPhi", Double.toString(polycal.getSectionPhi()));
                    calorimeter.setAttribute("innerSymmetryOrder", Double.toString(polycal.getNumberOfSides()));
                    calorimeter.setAttribute("outerR", Double.toString(polycal.getOuterRadius()));
                    calorimeter.setAttribute("outerZ", Double.toString(polycal.getOuterZ()));
                    calorimeter.setAttribute("outerPhi", Double.toString(polycal.getSectionPhi()));
                    calorimeter.setAttribute("outerSymmetryOrder", Double.toString(polycal.getNumberOfSides()));
                    calorimeter.setAttribute("collection", subdetector.getReadout().getName());

                    // Get the cell sizes from the segmentation.
                    List<Double> cellSizes = getCellSizes(subdetector);
                    
                    // For endcaps, X is U, and Y is V.
                    if (subdetector.isEndcap())
                    {
                        calorimeter.setAttribute("cellSizeU", Double.toString(cellSizes.get(0)));
                        calorimeter.setAttribute("cellSizeV", Double.toString(cellSizes.get(1)));
                    }
                    // The UV mapping is flipped around for barrel.  X is V, and Y is U.
                    else if (subdetector.isBarrel())
                    {
                        calorimeter.setAttribute("cellSizeU", Double.toString(cellSizes.get(1)));
                        calorimeter.setAttribute("cellSizeV", Double.toString(cellSizes.get(0)));
                    }

                    // Create identifier description and add to subdet.
                    calorimeter.addContent(makeIdentifierDescription(polycal));

                    // Add the calorimeter.
                    calorimeters.addContent(calorimeter);

                    LayerStack layers = polycal.getLayering().getLayerStack();

                    Element layersElem = new Element("layers");
                    layersElem.setAttribute("nlayers", Integer.toString(layers.getNumberOfLayers()));

                    calorimeter.addContent(layersElem);

                    double layerD = 0.;

                    if (polycal.isBarrel())
                    {
                        layerD = polycal.getInnerRadius();
                    }
                    else if (polycal.isEndcap())
                    {
                        layerD = polycal.getInnerZ();
                    }

                    CalorimeterConditions subdetectorCalorimeterConditions = null;

                    if (haveCalCalib)
                    {
                        subdetectorCalorimeterConditions = new CalorimeterConditions((Calorimeter) subdetector, calorimeterCalibration);
                    }

                    // Set MIP energy from calibration.
                    if (haveCalCalib)
                    {
                        calorimeter.setAttribute("mipEnergy", xfrac.format(subdetectorCalorimeterConditions.getMipEnergy()));
                        calorimeter.setAttribute("mipSigma", xfrac.format(subdetectorCalorimeterConditions.getMipSigma()));
                        calorimeter.setAttribute("mipCut", xfrac.format(subdetectorCalorimeterConditions.getMipCut()));
                        calorimeter.setAttribute("timeCut", xfrac.format(subdetectorCalorimeterConditions.getTimeCut()));
                    } 
                    // Set MIP energy from Bethe-Bloche calculation.
                    // TODO Check accuracy of this algorithm.
                    else
                    {
                        List<LayerSlice> sensors = subdetector.getLayering().getLayerStack().getLayer(0).getSensors();
                        LayerSlice sensor = sensors.get(0);
                        IMaterial sensorMaterial = MaterialStore.getInstance().get(sensor.getMaterial().getName());

                        ParticleType particleType = ParticlePropertyManager.getParticlePropertyProvider().get(13);

                        Hep3Vector p = new BasicHep3Vector(-6.8641, -7.2721, 1.2168e-7);

                        double emip = BetheBlochCalculator.computeBetheBloch(sensorMaterial, p, particleType.getMass(), particleType.getCharge(), sensor.getThickness());

                        // Set MIP Energy from Bethe Bloche.
                        calorimeter.setAttribute("mipEnergy", xfrac.format(emip));

                        // Set defaults for CalCalib parameters.
                        calorimeter.setAttribute("mipSigma", "0");
                        calorimeter.setAttribute("mipCut", "0");
                        calorimeter.setAttribute("timeCut", xfrac.format(Double.MAX_VALUE));
                    }
                    
                    double totalX0 = 0;

                    for (int i = 0; i < layers.getNumberOfLayers(); i++)
                    {
                        //System.out.println("  layer " + i);
                        Layer layer = layers.getLayer(i);

                        Element layerElem = new Element("layer");
                        layersElem.addContent(layerElem);

                        // Set radiation and interaction lengths.
                        double intLen = 0;
                        double radLen = 0;
                        for (int j = 0; j < layer.getNumberOfSlices(); j++)
                        {
                            LayerSlice slice = layer.getSlice(j);
                            //System.out.println("    slice " + j + " " + slice.getMaterial().getName());                            
                            double x0 = slice.getMaterial().getRadiationLength();
                            //System.out.println("      x0_mat_D="+x0);
                            //System.out.println("      x0_mat="+slice.getMaterial().getRadiationLength());
                            radLen += slice.getThickness() / (x0*10);
                            //System.out.println("      radLen="+radLen);
                            
                            double lambda = slice.getMaterial().getNuclearInteractionLength();
                            intLen += slice.getThickness() / (lambda*10);
                        }
                        //System.out.println("    x0_lyr_tot=" + radLen);
                        
                        
                        totalX0 += radLen;
                        
                        //System.out.println("    layer " + i + " " + radLen);
                        
                        layerElem.setAttribute("radLen", xlen.format(radLen));
                        layerElem.setAttribute("intLen", xlen.format(intLen));

                        // Set distance to IP.
                        double layerD2 = layerD + layer.getThicknessToSensitiveMid();
                        layerElem.setAttribute("distanceToIp", xthick.format(layerD2));

                        // Set cell thickness.
                        layerElem.setAttribute("cellThickness", xthick.format(layer.getThickness()));

                        // Set EM and HAD sampling fractions from
                        // CalorimeterCalibration conditions, if present.
                        if (haveCalCalib)
                        {
                            SamplingLayerRange layerRange = subdetectorCalorimeterConditions.getSamplingLayerRange(i);
                            if (calType == EM_BARREL || calType == EM_ENDCAP)
                            {
                                layerElem.setAttribute("samplingFraction", xfrac.format(layerRange.getEMSampling()));
                            }
                            if (calType == HAD_BARREL || calType == HAD_ENDCAP)
                            {
                                layerElem.setAttribute("samplingFraction", xfrac.format(layerRange.getHADSampling()));
                            }
                            if (calType == MUON_BARREL || calType == MUON_ENDCAP)
                            {
                                layerElem.setAttribute("samplingFraction", xfrac.format(layerRange.getHADSampling()));
                            }
                            layerElem.setAttribute("emSamplingFraction", xfrac.format(layerRange.getEMSampling()));
                            layerElem.setAttribute("hadSamplingFraction", xfrac.format(layerRange.getHADSampling()));
                        }
                        // Set from base SamplingFraction conditions. May throw
                        // an exception if neither CalorimeterCalibration
                        // or SamplingFractions conditions exists.
                        else
                        {
                            double samplingFraction = SamplingFractionManager.defaultInstance().getSamplingFraction(subdetector, i);
                            layerElem.setAttribute("emSamplingFraction", xfrac.format(samplingFraction));
                            layerElem.setAttribute("hadSamplingFraction", xfrac.format(samplingFraction));
                        }

                        // Increment layer distance by thickness of layer.
                        layerD += layer.getThickness();
                    }
                    
                    //System.out.println("    X0 Sum = " + totalX0);
                }

                // Set digital flag.
                try
                {
                    // Set digital attribute from conditions, if present.
                    ConditionsSet conditions = conditionsManager.getConditions("SamplingFractions/" + subdetector.getName());
                    boolean isDigital = conditions.getBoolean("digital");
                    calorimeter.setAttribute("digital", String.valueOf(isDigital));
                }
                catch (Exception x)
                {
                    calorimeter.setAttribute("digital", "false");
                }                
            }                        
        }
                     
        // TODO clean up the hard coded assumptions on coil geometry
        double coilRadLen = 0;
        double coilIntLen = 0;
        int coilLayers = 0;
        double coilInnerR = 0;
        double coilOuterR = 0;
        double bfield = 0;
        double coilMaxZ = 0;
        try 
        {
        	MultiLayerTracker c = (MultiLayerTracker) detector.getSubdetector("SolenoidCoilBarrel");
        	if (c != null) 
        	{
        		coilLayers = c.getNumberOfLayers();
        		coilInnerR = c.getInnerR()[0];
        		coilOuterR = c.getInnerR()[coilLayers-1] + c.getLayerThickness(coilLayers-1);
        		for (int layern = 0; layern != c.getNumberOfLayers(); layern++) 
        		{
        		    for (LayerSlice slice : c.getLayer(layern).getSlices())
        		    {
        		        double x0 = slice.getMaterial().getRadiationLength();
        		        double sliceRadLen = slice.getThickness() / (x0*10);                   
        		        double lambda = slice.getMaterial().getNuclearInteractionLength();
        		        double sliceIntLen = slice.getThickness() / (lambda*10);
        		        
        		        coilRadLen += sliceRadLen;
        		        coilIntLen += sliceIntLen; 
        		    }
        		}
        		//calculate average interaction/radiation length in coil material
        		coilRadLen = coilRadLen/(coilOuterR-coilInnerR);
        		coilIntLen = coilIntLen/(coilOuterR-coilInnerR);
        	}        
        } 
        catch (ClassCastException e) 
        {        
            throw new RuntimeException(e);
        }
        try 
        {
        	Solenoid s = (Solenoid) detector.getFields().get("GlobalSolenoid");
        	if (s != null) 
        	{
        		bfield = s.getField(new BasicHep3Vector(0, 0, 0)).z();
        		coilMaxZ = s.getZMax();
        	}
        } 
        catch (ClassCastException e) 
        {
            throw new RuntimeException(e);
        }
        
        Element coil = new Element("coil");
        coil.setAttribute("radLen", xlen.format(coilRadLen));
        coil.setAttribute("intLen", xlen.format(coilIntLen));
        coil.setAttribute("innerR", Double.toString(coilInnerR));
        coil.setAttribute("outerR", Double.toString(coilOuterR));
        coil.setAttribute("z", Double.toString(coilMaxZ));
        coil.setAttribute("bfield", Double.toString(bfield));
        root.addContent(coil);        

        Tube tube = (Tube) detector.getTrackingVolume().getLogicalVolume().getSolid();
        Element tracking = new Element("tracking");
        tracking.setAttribute("innerR", Double.toString(tube.getInnerRadius()));
        tracking.setAttribute("outerR", Double.toString(tube.getOuterRadius()));
        tracking.setAttribute("z", Double.toString(tube.getZHalfLength()));
        root.addContent(tracking);

        return outputDoc;
    }

    Element makeIdentifierDescription(Subdetector subdet)
    {
        IDDescriptor descr = subdet.getIDDecoder().getIDDescription();
        Element id = new Element("id");
        for (int i = 0, j = descr.fieldCount(); i < j; i++)
        {
            Element field = new Element("field");
            field.setAttribute("name", descr.fieldName(i));
            field.setAttribute("length", Integer.toString(descr.fieldLength(i)));
            field.setAttribute("start", Integer.toString(descr.fieldStart(i)));
            field.setAttribute("signed", Boolean.toString(descr.isSigned(i)));

            id.addContent(field);
        }
        return id;
    }

    private List<Double> getCellSizes(Subdetector subdetector)
    {
        List<Double> cellSizes = new ArrayList<Double>();
        BaseIDDecoder dec = (BaseIDDecoder) subdetector.getReadout().getIDDecoder();
        if (dec instanceof AbstractCartesianGrid)
        {
            AbstractCartesianGrid cgrid = (AbstractCartesianGrid) dec;
            if (cgrid.getGridSizeX() != 0)
            {
                cellSizes.add(cgrid.getGridSizeX());
            }
            if (cgrid.getGridSizeY() != 0)
            {
                cellSizes.add(cgrid.getGridSizeY());
            }
            if (cgrid.getGridSizeZ() != 0)
            {
                cellSizes.add(cgrid.getGridSizeZ());
            }
        }
        if (cellSizes.size() != 2)
            throw new RuntimeException("Only 2 cell dimensions are allowed.");
        return cellSizes;
    }

    public String getOutputFormat()
    {
        return "pandora";
    }

    public FileFilter getFileFilter()
    {
        return new PandoraFileFilter();
    }

    private static class PandoraFileFilter extends FileFilter
    {

        public boolean accept(java.io.File file)
        {
            return file.getName().endsWith(".xml");
        }

        public String getDescription()
        {
            return "Pandora Geometry file (*.xml)";
        }
    }
}