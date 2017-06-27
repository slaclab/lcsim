/*
 * SiTrackerHitStrip1DConverter.java
 *
 * Created on December 26, 2007, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import java.awt.Color;
import java.util.List;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.TrackerHit;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHitStrip1D;

/**
 *
 * @author cozzy
 *
 *  This is a converter for SiTrackerHit1D objects. It draws the hit segment
 *  for each instance. 
 *
 * Since SiTrackerHitStrip1D implements TrackerHit, TrackerHitConverter will 
 * also fire and draw a marker for each of these. 
 *    
 */
public class SiTrackerHitStrip1DConverter implements HepRepCollectionConverter {
    
    /** Creates a new instance of SiTrackerHitStrip1DConverter */
    public SiTrackerHitStrip1DConverter() {
    }
    
    public boolean canHandle(Class k){
        return TrackerHit.class.isAssignableFrom(k);
    }
    
    public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree) 
    {        
        // Check if the TrackerHit can be cast to a SiTrackerHitStrip1D.
    	if (collection.isEmpty() || !(collection.get(0) instanceof SiTrackerHitStrip1D)) return; 
        
        LCMetaData meta = event.getMetaData(collection);
        String name = meta.getName()+"_Strip1D"; //added string because TrackerHitConverter will also fire on this collection... 
        
        HepRepType parentType = factory.createHepRepType(typeTree,name);
        parentType.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER); 
        parentType.addAttValue("drawAs","Line"); 
        
        parentType.addAttDef("dEdx","Hit dEdx", "physics","");
        parentType.addAttDef("time","Hit time", "physics","");
        parentType.addAttDef("length","Hit Length", "physics", "");
        parentType.addAttDef("numsimhits", "Number of SimTrackerHits","physics","");
        parentType.addAttDef("numrawhits", "Number of contributing raw hits","physics","");
        HepRepType normalType = factory.createHepRepType(parentType,"Normal Hits");
        HepRepType noiseType = factory.createHepRepType(parentType,"Noise Hits");
        
        normalType.addAttValue("color",Color.GREEN);
        noiseType.addAttValue("Color",Color.MAGENTA); 
        
        // Draw modules for noise hits because they don't have associated SimTrackerhits 
        HepRepType moduleType = DisplayHitModules.getModuleType(factory, typeTree, name+"_noiseModules");
        HepRepType sensorType = DisplayHitModules.getSensorType(factory, typeTree, name+"_noiseSensors");
        
        for (SiTrackerHitStrip1D hit : (List<SiTrackerHitStrip1D>) collection) 
        {            
            HepRepInstance instance; 
            
            if (!hit.getSimHits().isEmpty()) {
                instance = factory.createHepRepInstance(instanceTree,normalType); 
            }
            else {
                instance = factory.createHepRepInstance(instanceTree, noiseType);
                IDetectorElement sensor = hit.getSensor(); 
                IDetectorElement module = sensor.getParent(); 
                DisplayHitModules.drawPolyhedron(module, moduleType, instanceTree, factory);
                DisplayHitModules.drawPolyhedron(sensor, sensorType, instanceTree, factory);
            }
            instance.addAttValue("dEdx",hit.getdEdx());
            instance.addAttValue("time",hit.getTime());
            instance.addAttValue("length",hit.getHitLength());
            instance.addAttValue("numsimhits",hit.getSimHits().size());
            instance.addAttValue("numrawhits",hit.getRawHits().size());
            
            for (Point3D p : hit.getHitSegment().getPoints())  {            
                factory.createHepRepPoint(instance,p.x(),p.y(),p.z()); 
            }
        }
    }    
}