/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.util.xml.ClasspathEntityResolver;

/**
 *LayerWeights are used by SubsetScorer and to disambiguate between Seed and 
 * Confirm layers. 
 * 
 * @author cozzy
 */
public class LayerWeight {
    
        private double default_weight = 1.0;
        private double adjacence_multiplier = 1.0;
        private Map<DumbLayer, Double> weights; 
        private Map<String, Double> readout_efficiencies; 
        private double defaultEfficiency = 1.0; 
        private String targetDetector = "None Specified"; 
        private boolean divideByTwoInTrackerEndcap = false; 
        private boolean divideByTwoInTrackerForward = false; 
        
        public LayerWeight(){
            weights = new HashMap<DumbLayer,Double>(); 
            readout_efficiencies = new HashMap<String,Double>(); 
        } 
        
        public LayerWeight(LayerWeight lw){
            this.default_weight = lw.default_weight; 
            this.weights = lw.weights; 
            this.readout_efficiencies = lw.readout_efficiencies; 
        }
    
        public LayerWeight(DumbLayer[] layers, double[] wghts, String[] readoutNames, double[] readoutEfficiencies){
            if (layers.length!=wghts.length || readoutNames.length!=readoutEfficiencies.length)
                throw new RuntimeException("Array lengths don't match"); 
            
            
            weights = new HashMap<DumbLayer,Double>();
            
            for (int i = 0; i < layers.length; i++)
                setWeight(layers[i], wghts[i]); 
            
            readout_efficiencies = new HashMap<String,Double>(); 
            for (int i = 0 ; i < layers.length; i++)
                setReadoutEfficiency(readoutNames[i],readoutEfficiencies[i]); 
        }
        
        public void setDefaultWeight(double d){
            default_weight = d; 
        }
        
        public void setDefaultReadoutEfficiency(double d){
            checkReadoutEfficiencyValid(d);
            defaultEfficiency = d; 
        }
        
        public void setWeight(DumbLayer lyr, double weight){
            weights.put(lyr, weight); 
        }
        
        public void setReadoutEfficiency(String readoutName, double eff){
            checkReadoutEfficiencyValid(eff);
            readout_efficiencies.put(readoutName, eff); 
            
        }
        public void setTargetDetector(String name){
            targetDetector = name; 
        }
        
        public String getTargetDetector() {
            return targetDetector; 
        }
        
        public double getWeight(DumbLayer layer) {
            if (weights.containsKey(layer)){
                return weights.get(layer).doubleValue(); 
            } else return default_weight; 
        }
        
        public double getReadoutEfficiency(String readoutName) {
            if (readout_efficiencies.containsKey(readoutName)) {
                return readout_efficiencies.get(readoutName); 
            } else return defaultEfficiency; 
        }
        
        public double getWeight(Set<DumbLayer> set) {
            double ret = 1.0;
            
            for (DumbLayer lyr : set) {
                ret*=getWeight(lyr); 
            }
            return ret; 
        }
        
        public double getAdjacenceMultiplier() {
            return adjacence_multiplier;
        }

        public void setAdjacenceMultiplier(double adjacence_multiplier) {
            this.adjacence_multiplier = adjacence_multiplier;
        }

        public boolean isDivideByTwoInTrackerEndcap() {
            return divideByTwoInTrackerEndcap;
        }

        public void setDivideByTwoInTrackerEndcap(boolean divideByTwo) {
            this.divideByTwoInTrackerEndcap = divideByTwo;
        }

        public boolean isDivideByTwoInTrackerForward() {
            return divideByTwoInTrackerForward;
        }

        public void setDivideByTwoInTrackerForward(boolean divideByTwo) {
            this.divideByTwoInTrackerForward = divideByTwo;
        }
                
        /**
         * Returns the prefix of the default resource path to where the 
         * layer weight XML files are stored.
         * @return default resource path to XML weights
         */
        public static String getDefaultResourcePrefix(){
            return "org/lcsim/recon/tracking/seedtracker/strategybuilder/weights/";
        }
        
        /**
         * Loads LayerWeight definitions from the resource with the specified name.
         * The name should be something like /org/lcsim/contrib/seedtracker/strategybuilder/weights/weights.xml
         * 
         * getDefaultResourcePrefix() may be helpful. 
         * 
         * @param resourceName the full name of the resource file to read
         * @return the read LayerWeight definitions
         */
        public static LayerWeight getLayerWeightFromResource(String resourceName) {    
            return getLayerWeightFromInputStream(LayerWeight.class.getClassLoader().getResourceAsStream(resourceName)); 
            
        }
        
        
        public Comparator getComparator(){
            return new Comparator(){
                public int compare(Object o1, Object o2) {
                    DumbLayer dl1 = (DumbLayer) o1;
                    DumbLayer dl2 = (DumbLayer) o2; 
                    double s1 = LayerWeight.this.getWeight(dl1);
                    double s2 = LayerWeight.this.getWeight(dl2);
                    return Double.compare(s1, s2); 
                }
            };
        }
        
        
        /**
         * Loads LayerWeight definitions from the specified input stream
         * @param in an input stream corresponding to a valid XML file 
         * @return the read LayerWeight definitions
         */
        public static LayerWeight getLayerWeightFromInputStream(InputStream in){
            
            Document doc; 
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(true); 
            builder.setFeature("http://apache.org/xml/features/validation/schema", true);
            builder.setEntityResolver(new ClasspathEntityResolver());
            try {
                doc = builder.build(in);
            } catch (JDOMException jdom) {
                jdom.printStackTrace();
                throw new RuntimeException("JDOM exception occurred"); 
            } catch (IOException io ) {
                io.printStackTrace();
                throw new RuntimeException("IO Exception occurred"); 
            }
            
            return getLayerWeightFromDocument(doc);
 
        }
        /**
         * Loads LayerWeight definitions from the specified file. 
         * @param file a valid XML file specifying layer weights
         * @return the read LayerWeight definitions
         */
        public static LayerWeight getLayerWeightFromFile(File file){
            
            if (! file.exists() )
                throw new RuntimeException("File "+file.toString()+" not found"); 
            
            Document doc; 
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(true); 
            builder.setFeature("http://apache.org/xml/features/validation/schema", true);
            builder.setEntityResolver(new ClasspathEntityResolver());
            try {
                doc = builder.build(file);
            } catch (JDOMException jdom) {
                jdom.printStackTrace();
                throw new RuntimeException("JDOM exception occurred"); 
            } catch (IOException io ) {
                io.printStackTrace();
                throw new RuntimeException("IO Exception occurred"); 
            }
            
            return getLayerWeightFromDocument(doc); 
        }
        
        private void checkReadoutEfficiencyValid(double d){
            if (d < 0. || d > 1.00000001)
                throw new RuntimeException("Readout Efficiency must be between 0 and 1"); 
        }
        
        private static LayerWeight getLayerWeightFromDocument(Document doc) {
            Element root = doc.getRootElement(); 
            LayerWeight lw = new LayerWeight(); 
            
            try {
                lw.setDefaultWeight(Double.valueOf(root.getChildText("DefaultWeight")).doubleValue());
                lw.setDefaultReadoutEfficiency(Double.valueOf(root.getChildText("DefaultReadoutEfficiency")));
                try {lw.setAdjacenceMultiplier(Double.valueOf(root.getChildText("AdjacenceMultiplier")).doubleValue());} catch(NullPointerException npe){}
                try {lw.setTargetDetector(root.getChildText("TargetDetector"));} catch(NullPointerException npe){}
                try {lw.setDivideByTwoInTrackerEndcap(Boolean.valueOf(root.getChild("TargetDetector").getAttributeValue("divide_by_two_in_tracker_endcap")));} catch(NullPointerException npe){}
                try {lw.setDivideByTwoInTrackerForward(Boolean.valueOf(root.getChild("TargetDetector").getAttributeValue("divide_by_two_in_tracker_forward")));} catch(NullPointerException npe){}
                Element layers = root.getChild("Layers"); 
                for (Object o : layers.getChildren("Layer")){
                    
                    Element e = (Element) o; 
                    String detName = e.getAttributeValue("detector_name"); 
                    int layer_number = Integer.valueOf(e.getAttributeValue("layer_number")).intValue(); 
                    BarrelEndcapFlag beflag = BarrelEndcapFlag.valueOf(e.getAttributeValue("be_flag")); 
                    DumbLayer dl = new DumbLayer(detName, layer_number, beflag); 
                    lw.setWeight(dl, Double.valueOf(e.getText()));
                } 
                
                Element ro = root.getChild("ReadoutEfficiencies"); 
                
                if (ro!=null) {
                    for (Object o : ro.getChildren("ReadoutEfficiency")){
                        Element e = (Element) o; 
                        String readoutName = e.getAttributeValue("readout"); 
                        lw.setReadoutEfficiency(readoutName, Double.valueOf(e.getText()));
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Something bad happened when parsing"); 
            }
                
            return lw; 
        }
        /**
         * Writes this LayerWeight to an XML file with a location specified
         * by the given file object. 
         * @param file output File object 
         * @return true if succesful, false otherwise. 
         */
        public boolean writeToFile(File file){
            Element root = new Element("LayerWeight"); 
            Document doc = new Document(root); 
            Namespace xs = Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema-instance");
            root.addNamespaceDeclaration(xs);
            root.setAttribute(new Attribute("noNamespaceSchemaLocation","http://lcsim.org/recon/tracking/seedtracker/strategybuilder/strategies.xsd",xs));
            root.addContent(new Element("DefaultWeight").addContent(String.valueOf(default_weight))); 
            root.addContent(new Element("DefaultReadoutEfficiency").addContent(String.valueOf(defaultEfficiency)));          
            root.addContent(new Element("TargetDetector").addContent(String.valueOf(targetDetector)).setAttribute("divide_by_two_in_tracker_endcap", String.valueOf(divideByTwoInTrackerEndcap))); 
            root.addContent(new Element("TargetDetector").addContent(String.valueOf(targetDetector)).setAttribute("divide_by_two_in_tracker_forward", String.valueOf(divideByTwoInTrackerForward))); 
            root.addContent(new Element("AdjacenceMultiplier").addContent(String.valueOf(adjacence_multiplier))); 
            
            Element ro = new Element("ReadoutEfficiencies"); 
            
            for (String readout : readout_efficiencies.keySet()) {
                Element re = new Element("ReadoutEfficiency"); 
                re.setAttribute("readout",readout); 
                re.addContent(String.valueOf(readout_efficiencies.get(readout))); 
                ro.addContent(re);
            }   
            root.addContent(ro); 
            
            Element layers = new Element("Layers"); 
            for (DumbLayer lyr : weights.keySet()) {
                Element layer = new Element("Layer"); 
                layer.setAttribute("layer_number", String.valueOf(lyr.layer)); 
                layer.setAttribute("detector_name", String.valueOf(lyr.detectorName)); 
                layer.setAttribute("be_flag", lyr.be.toString()); 
                layer.addContent(String.valueOf(weights.get(lyr))); 
                layers.addContent(layer); 
            }
            root.addContent(layers); 
            
            try {
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()); 
                FileWriter fw = new FileWriter(file); 
                out.output(doc, fw);
            } catch (Exception e){
                e.printStackTrace();
                return false; 
            }
            return true; 
        }
    }
