package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.vec.Hep3Vector;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.solids.LineSegment3D;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiStrips;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.RawTrackerHit;

/**
 * Convert RawTrackerHit objects into HepRep line segments.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: RawTrackerHitConverter.java,v 1.2 2012/06/14 00:02:39 jeremy Exp $
 */
class RawTrackerHitConverter implements HepRepCollectionConverter
{
   public boolean canHandle(Class k) {
      return RawTrackerHit.class.isAssignableFrom(k);
   }
   
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree) {      
      LCMetaData meta = event.getMetaData(collection);
      String name = meta.getName();           

      HepRepType typeX = factory.createHepRepType(typeTree, name);
      typeX.addAttValue("layer", LCSimHepRepConverter.HITS_LAYER);
      typeX.addAttValue("drawAs", "Line");
      typeX.addAttValue("color", Color.GREEN);
      
      try {      
          // Setup links to SiSensor objects in detector geometry.
          setSensors(meta, (List<RawTrackerHit>)collection);
            
          // Loop over hits and draw them as lines.
          for (RawTrackerHit hit : (List<RawTrackerHit>)collection) {
              try {
                  if (hit.getDetectorElement() != null) {
                      SiSensor sensor = (SiSensor)hit.getDetectorElement();
                      if (sensor != null) {
                          Collection<SiSensorElectrodes> trodes = sensor.getReadoutElectrodes();
                          SiStrips strips = (SiStrips)trodes.toArray()[0];
                          LineSegment3D line = strips.getStrip(hit.getIdentifierFieldValue("strip"));
                          ITransform3D transform = strips.getLocalToGlobal();
                          Hep3Vector startPoint = transform.transformed(line.getStartPoint());
                          Hep3Vector endPoint = transform.transformed(line.getEndPoint());
                          HepRepInstance instance = factory.createHepRepInstance(instanceTree, typeX);
                          factory.createHepRepPoint(instance, startPoint.x(), startPoint.y(), startPoint.z());
                          factory.createHepRepPoint(instance, endPoint.x(), endPoint.y(), endPoint.z());
                      }
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      }
      catch (Exception e) {
          e.printStackTrace();
      }
   }
   
   private void setSensors(LCMetaData meta, List<RawTrackerHit> hits) {

       // Get the ID dictionary and field information.
       IIdentifierDictionary dict = meta.getIDDecoder().getSubdetector().getDetectorElement().getIdentifierHelper().getIdentifierDictionary();
       
       if (dict != null && dict.hasField("side") && dict.hasField("strip")) {
           int fieldIdx = dict.getFieldIndex("side");
           int sideIdx = dict.getFieldIndex("strip");
       
           for (RawTrackerHit hit : hits) {

               // The "side" and "strip" fields needs to be stripped from the ID for sensor lookup.
               IExpandedIdentifier expId = dict.unpack(hit.getIdentifier());
               expId.setValue(fieldIdx, 0);
               expId.setValue(sideIdx, 0);
               IIdentifier strippedId = dict.pack(expId);

               // Find the sensor DetectorElement.
               List<IDetectorElement> des = DetectorElementStore.getInstance().find(strippedId);
               if (des == null || des.size() == 0) {
                   throw new RuntimeException("Failed to find any DetectorElements with stripped ID <0x" + Long.toHexString(strippedId.getValue()) + ">.");
               }
               else if (des.size() == 1) {
                   hit.setDetectorElement((SiSensor)des.get(0));
               }
               else {
                   // Use first sensor found, which should work unless there are sensors with duplicate IDs.
                   for (IDetectorElement de : des) {
                       if (de instanceof SiSensor) {
                           hit.setDetectorElement((SiSensor)de);
                           break;
                       }
                   }
               }   

               // No sensor was found.
               if (hit.getDetectorElement() == null) {
                   throw new RuntimeException("No sensor was found for hit with stripped ID <0x" + Long.toHexString(strippedId.getValue()) + ">.");
               }
           }
       }
   }
}
