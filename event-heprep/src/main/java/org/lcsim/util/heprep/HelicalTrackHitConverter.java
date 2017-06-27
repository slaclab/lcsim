/*
 * To change this template, choose Tools | Templates
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
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.fit.helicaltrack.HelicalTrack2DHit;
import org.lcsim.fit.helicaltrack.HelicalTrack3DHit;
import org.lcsim.fit.helicaltrack.HelicalTrackCross;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 * HepRepConverter for display of the hits of Richard Partridge's framework. 
 * @author cozzy
 */
public class HelicalTrackHitConverter implements HepRepCollectionConverter{
    
    public boolean canHandle(Class k){
        return HelicalTrackHit.class.isAssignableFrom(k);
    }
    
    public void convert (EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree){
        
        LCMetaData meta = event.getMetaData(collection);
        String name = meta.getName();
        
        HepRepType type3d = factory.createHepRepType(typeTree,name+"3d");
        type3d.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
        type3d.addAttValue("drawAs","Point");
        type3d.addAttValue("color",Color.ORANGE);
        type3d.addAttValue("fill",true);
        type3d.addAttValue("fillColor",Color.ORANGE);
        type3d.addAttValue("MarkName","Box");
        type3d.addAttDef("dEdx", "Hit dEdx", "physics", "");
        type3d.addAttDef("time", "Hit time", "physics", "");
        type3d.addAttDef("flagValue", "BarrelEndcapFlag value","physics","");
        type3d.addAttDef("drphi", "Hit drphi", "physics",""); 
        type3d.addAttDef("dr", "Hit dr", "physics",""); 
        type3d.addAttDef("hit identifier", "Hit identifier", "physics", ""); 
        
        HepRepType type2d = factory.createHepRepType(typeTree,name+"2d");
        type2d.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
        type2d.addAttValue("drawAs","Line");
        type2d.addAttValue("color",Color.ORANGE);
        type2d.addAttDef("dEdx", "Hit dEdx", "physics", "");
        type2d.addAttDef("time", "Hit time", "physics", "");
        type2d.addAttDef("zmin", "Z min", "physics", "");
        type2d.addAttDef("zmax", "Z max", "physics", "");
        type2d.addAttDef("drphi", "Hit drphi", "physics",""); 
        type2d.addAttDef("hit identifier", "Hit identifier", "physics", ""); 
        
        HepRepType typeCenter = factory.createHepRepType(typeTree,name+"2dSegmentCenters");
        typeCenter.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
        typeCenter.addAttValue("drawAs","Point");
        typeCenter.addAttValue("color",Color.ORANGE);
        typeCenter.addAttValue("fill",true);
        typeCenter.addAttValue("fillColor",Color.ORANGE);
        typeCenter.addAttValue("MarkName","Box");
        typeCenter.addAttDef("dEdx", "Hit dEdx", "physics", "");
        typeCenter.addAttDef("time", "Hit time", "physics", "");
        typeCenter.addAttDef("drphi", "Hit drphi", "physics",""); 
        
        
        for (HelicalTrackHit hit : (List<HelicalTrackHit>)collection){
            
            double[] pos = hit.getPosition();
            
            if (hit instanceof HelicalTrack3DHit || hit instanceof HelicalTrackCross) {
                HepRepInstance instance = factory.createHepRepInstance(instanceTree,type3d);
                instance.addAttValue("dEdx",hit.getdEdx());
                instance.addAttValue("time",hit.getTime());
                String flagValue;
                if (hit instanceof HelicalTrack3DHit)
                    flagValue=((HelicalTrack3DHit)hit).BarrelEndcapFlag().toString();
                else
                    flagValue=((HelicalTrackCross)hit).BarrelEndcapFlag().toString(); 
                instance.addAttValue("flagValue",flagValue);
                instance.addAttValue("drphi",hit.drphi()); 
                instance.addAttValue("dr",hit.dr()); 
                instance.addAttValue("hit identifier",hit.getLayerIdentifier()); 
                factory.createHepRepPoint(instance, pos[0], pos[1], pos[2]);        
            }
            
            else if (hit instanceof HelicalTrack2DHit){
                
                HepRepInstance instance2d = factory.createHepRepInstance(instanceTree,type2d);
                instance2d.addAttValue("dEdx",hit.getdEdx());
                instance2d.addAttValue("time",hit.getTime());
                HelicalTrack2DHit hit2d = (HelicalTrack2DHit) hit;
                instance2d.addAttValue("zmin", hit2d.zmin());
                instance2d.addAttValue("zmax", hit2d.zmax());
                instance2d.addAttValue("drphi",hit.drphi()); 
                instance2d.addAttValue("hit identifier",hit.getLayerIdentifier()); 
                HelicalTrack2DHit h = (HelicalTrack2DHit) hit;
                factory.createHepRepPoint(instance2d, pos[0], pos[1], h.zmin());
                factory.createHepRepPoint(instance2d, pos[0], pos[1], h.zmax());
                
                HepRepInstance instanceCenter = factory.createHepRepInstance(instanceTree,typeCenter); 
                instanceCenter.addAttValue("dEdx",hit.getdEdx()); 
                instanceCenter.addAttValue("time",hit.getTime());
                instanceCenter.addAttValue("drphi",hit.drphi()); 
                factory.createHepRepPoint(instanceCenter,pos[0],pos[1],pos[2]);
            }
        }
        
    }

}
