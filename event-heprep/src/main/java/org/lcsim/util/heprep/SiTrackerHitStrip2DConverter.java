/*
 * SiTrackerHitStrip2DConverter.java
 *
 * Created on January 16, 2008, 1:20 PM
 *
 */

package org.lcsim.util.heprep; 

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import java.awt.Color;
import java.util.List;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHitStrip2D;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.EventHeader;
import org.lcsim.event.TrackerHit;
import org.lcsim.util.heprep.HepRepCollectionConverter;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.util.heprep.LCSimHepRepConverter;

/**
 *
 * @author cozzy <cozzyd@stanford.edu>
 */
public class SiTrackerHitStrip2DConverter implements HepRepCollectionConverter{
    
    public SiTrackerHitStrip2DConverter() {
    }
    
    public boolean canHandle(Class k){
        return TrackerHit.class.isAssignableFrom(k);
    }

    public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree) {
        
        //make sure we have the right type of object... 
        if (collection.isEmpty() || !(collection.get(0) instanceof SiTrackerHitStrip2D)) return; 
        
        
        LCMetaData meta = event.getMetaData(collection); 
        String name = meta.getName()+"_Strip2D"; //add string because TrackerHitConverter will also fire on this collection... 
        
        HepRepType ptType = factory.createHepRepType(typeTree,name); 
        ptType.addAttValue("layer", LCSimHepRepConverter.HITS_LAYER); 
        ptType.addAttValue("drawAs", "Point"); 
        
        ptType.addAttDef("dEdx","Hit dEdx", "physics","");
        ptType.addAttDef("time","Hit time", "physics","");
        ptType.addAttDef("isGhost","Whether or not the hit is a ghost","physics",""); 
        ptType.addAttDef("numrawhits", "Number of contributing raw hits","physics","");
        ptType.addAttDef("numsimhits", "Number of SimTrackerHits","physics","");
        
        HepRepType normalType = factory.createHepRepType(ptType,"Normal Hits");
        HepRepType noiseType = factory.createHepRepType(ptType,"Noise Hits");
        
        normalType.addAttValue("color",Color.RED);
        noiseType.addAttValue("Color",Color.MAGENTA); 
        
         // Draw modules for noise hits because they don't have associated SimTrackerhits 
        HepRepType moduleType = DisplayHitModules.getModuleType(factory, typeTree, name+"_noiseModules");
        HepRepType sensorType = DisplayHitModules.getSensorType(factory, typeTree, name+"_noiseSensors");
        
        
        for (SiTrackerHitStrip2D hit : (List<SiTrackerHitStrip2D>) collection) {
            
            HepRepInstance instance; 
            
            if (!hit.getSimHits().isEmpty()) {
                instance = factory.createHepRepInstance(instanceTree, normalType);
            }
            else {
                instance = factory.createHepRepInstance(instanceTree, noiseType);
                IDetectorElement sensor = hit.getSensor(); 
                IDetectorElement module = sensor.getParent(); 
                DisplayHitModules.drawPolyhedron(module, moduleType, instanceTree, factory);
                DisplayHitModules.drawPolyhedron(sensor, sensorType, instanceTree, factory);
            }
            instance.addAttValue("dEdx", hit.getdEdx());
            instance.addAttValue("time", hit.getTime());
            instance.addAttValue("isGhost", hit.isGhost());
            instance.addAttValue("numrawhits", hit.getRawHits().size());
            instance.addAttValue("numsimhits", hit.getSimHits().size()); 
            
            
            double[] p = hit.getPosition();
            
            factory.createHepRepPoint(instance,p[0],p[1],p[2]); 
        }   
    }
}
