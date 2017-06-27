/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.event.EventHeader;
import org.lcsim.recon.tracking.seedtracker.SeedLayer.SeedType;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.util.xml.ClasspathEntityResolver;

/**
 *
 * @author cozzy
 */
public class StrategyXMLUtils {

    /**
     * Returns the default location in the resource hierarchy of the strategy xml files. 
     * @return
     */
    public static String getDefaultStrategiesPrefix(){
        return "org/lcsim/recon/tracking/seedtracker/strategies/"; 
    }
    
    /**
     * Returns a strategy list from an xml file in the default location
     * in the resource hierarchy given the full path to the resource of the file
     * (i.e. org/lcsim/contrib/seedtracker/strategybuilder/strategies/strategy.xml)
     * 
     * getDefaultStrategiesPrefix() may be helpful here. 
     * @param resourceName the full name of the resource
     * @return a list of strategies based on the resource xml file
     */
    public static List<SeedStrategy> getStrategyListFromResource(String resourceName) {
        
        return getStrategyListFromInputStream(StrategyXMLUtils.class.getClassLoader().
                getResourceAsStream(resourceName)); 
    }
    
    /**
     * Attempts to parse the given file as an XML file containing a well-formed
     * list of strategies. If successful, a list of SeedStrategies corresponding
     * to the strategies defined in the file is generated. 
     * 
     * @param file A File object corresponding to the XML file. 
     * @return
     */
    public static List<SeedStrategy> getStrategyListFromFile(File file) {
        
        if (! file.exists() ){
            throw new RuntimeException("File "+file.toString()+" not found"); 
        }
        
        Document doc; 
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(true); 
        builder.setFeature("http://apache.org/xml/features/validation/schema", true);
        builder.setEntityResolver(new ClasspathEntityResolver());
        try {
            doc = builder.build(file);
        } catch (JDOMException jdom) {
            jdom.printStackTrace();
            throw new RuntimeException("JDOM exception occurred. "+jdom.getMessage()); 
        } catch (IOException io ) {
            io.printStackTrace();
            throw new RuntimeException("IO Exception occurred"); 
        }
       
        return getStrategyListFromDocument(doc); 
        
    }
    
     /**
     * Attempts to parse the given input stream as an XML file containing a well-formed
     * list of strategies. If successful, a list of SeedStrategies corresponding
     * to the strategies defined in the file is generated. This method will 
     * be useful if you are attempting to load resources using getResourceAsStream(). 
     * 
     * @param stream An InputStream object corresponding to the XML file. 
     * @return the list of strategies corresponding to the input stream
     */
    public static List<SeedStrategy> getStrategyListFromInputStream(InputStream stream){
        
        Document doc; 
        SAXBuilder builder = new SAXBuilder(); 
        builder.setValidation(true); 
        builder.setFeature("http://apache.org/xml/features/validation/schema", true);
        builder.setEntityResolver(new ClasspathEntityResolver());
        try {
            doc = builder.build(stream);
            
        } catch (JDOMException jdom) {
            jdom.printStackTrace();
            throw new RuntimeException("JDOM exception occurred"); 
        } catch (IOException io ) {
            io.printStackTrace();
            throw new RuntimeException("IO Exception occurred"); 
        }
       
        return getStrategyListFromDocument(doc); 
    }
    
    private static List<SeedStrategy> getStrategyListFromDocument(Document doc) {
        
        Element listElement = doc.getRootElement(); 
        List<SeedStrategy> ret = new ArrayList<SeedStrategy>(); 
        
        try {
            for (Object o : listElement.getChildren("Strategy")){
                Element e = (Element) o; 
                SeedStrategy strat = new SeedStrategy(e.getAttributeValue("name")); 
                strat.putBadHitChisq(Double.valueOf(e.getChild("BadHitChisq").getText()));
                strat.putMaxChisq(Double.valueOf(e.getChild("MaxChisq").getText()));
                strat.putMaxDCA(Double.valueOf(e.getChild("MaxDCA").getText()));
                strat.putMaxZ0(Double.valueOf(e.getChild("MaxZ0").getText()));
                strat.putMinConfirm(Integer.valueOf(e.getChild("MinConfirm").getText()));
                strat.putMinHits(Integer.valueOf(e.getChild("MinHits").getText()));
                strat.putMinPT(Double.valueOf(e.getChild("MinPT").getText()));
                Element layers = e.getChild("Layers"); 
                for (Object ol : layers.getChildren("Layer")){
                    Element l = (Element) ol; 
                    String detName = l.getAttributeValue("detector_name"); 
                    int layer_number = Integer.valueOf(l.getAttributeValue("layer_number")).intValue(); 
                    SeedType type = SeedType.valueOf(l.getAttributeValue("type")); 
                    BarrelEndcapFlag beflag = BarrelEndcapFlag.valueOf(l.getAttributeValue("be_flag")); 
                    SeedLayer lyr = new SeedLayer(detName, layer_number, beflag, type); 
                    strat.addLayer(lyr);
                }
                ret.add(strat);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new RuntimeException("NullPointerException thrown. See output for details. This probably means the XML is malformed"); 
        }
        
        return ret; 
    }
    
    
    
       /**
     * Generates an XML file representing the given strategy list. 
     * @param strategyList
     * @param file file object where xml will be written
     * @return a boolean denoting the success of the operation
     */
    
    public static boolean writeStrategyListToFile(List<SeedStrategy> strategyList, File file){
        return writeStrategyListToFile(strategyList, file, null);
    }
    
    
    /**
     * Generates an XML file representing the given strategy list along with a top level comment. 
     * @param strategyList
     * @param file file object where xml will be written
     * @param comment top-level comment 
     * @return a boolean denoting the success of the operation
     */
    public static boolean writeStrategyListToFile(List<SeedStrategy> strategyList, File file, StrategyXMLMetadata meta) {
        
        Element listElement = new Element("StrategyList");
        Document doc = new Document(listElement); 
        Namespace xs = Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema-instance");
        listElement.addNamespaceDeclaration(xs);
        listElement.setAttribute(new Attribute("noNamespaceSchemaLocation","http://lcsim.org/recon/tracking/seedtracker/strategybuilder/strategies.xsd",xs));
        if (meta!=null){
            if (meta.comment!=null){
                listElement.addContent(new Comment(meta.comment)); 
            }
            if (meta.targetDetector!=null){
                listElement.addContent(new Element("TargetDetector").addContent(meta.targetDetector)); 
            } else listElement.addContent(new Element("TargetDetector").addContent("None specified")); 
        }  else listElement.addContent(new Element("TargetDetector").addContent("None specified")); 
        
        int counter = 1; 
        for (SeedStrategy strat : strategyList){
            listElement.addContent("\n");
            listElement.addContent(new Comment(String.valueOf(counter++)));
            Element strategy = new Element("Strategy");
            
            if (meta!=null && meta.strategyComments.containsKey(strat)){
                strategy.addContent(new Comment(meta.strategyComments.get(strat))); 
            }
            
            strategy.setAttribute("name",strat.getName()); 
            strategy.addContent(new Comment("Cutoffs"));
            strategy.addContent(new Element("MinPT").addContent(String.valueOf(strat.getMinPT()))); 
            strategy.addContent(new Element("MinHits").addContent(String.valueOf(strat.getMinHits())));
            strategy.addContent(new Element("MinConfirm").addContent(String.valueOf(strat.getMinConfirm())));
            strategy.addContent(new Element("MaxDCA").addContent(String.valueOf(strat.getMaxDCA()))); 
            strategy.addContent(new Element("MaxZ0").addContent(String.valueOf(strat.getMaxZ0()))); 
            strategy.addContent(new Element("MaxChisq").addContent(String.valueOf(strat.getMaxChisq())));
            strategy.addContent(new Element("BadHitChisq").addContent(String.valueOf(strat.getBadHitChisq()))); 
            
            strategy.addContent(new Comment("Layers"));
            Element layers = new Element("Layers"); 
            for (SeedLayer lyr : strat.getLayerList()){
                Element layer = new Element("Layer"); 
                layer.setAttribute("type", lyr.getType().toString()); 
                layer.setAttribute("layer_number", String.valueOf(lyr.getLayer())); 
                layer.setAttribute("detector_name", lyr.getDetName()); 
                layer.setAttribute("be_flag", lyr.getBarrelEndcapFlag().toString()); 
                layers.addContent(layer); 
            }
            strategy.addContent(layers); 
            listElement.addContent(strategy); 
        }
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
