/*
 * DisplayHitModules.java
 *
 * Created on December 14, 2007, 1:43 PM
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
import hep.physics.vec.Hep3Vector;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.solids.IPolyhedron;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.geometry.subdetector.SiTrackerEndcap;
import org.lcsim.geometry.subdetector.SiTrackerEndcap2;
import org.lcsim.geometry.subdetector.SiTrackerFixedTarget;
import org.lcsim.geometry.subdetector.SiTrackerFixedTarget2;

/**
 * Converter that draws modules and sensors that have hits in them. 
 * Everything is drawn in the event layer. 
 *
 */
public class DisplayHitModules implements HepRepCollectionConverter{


	/** Creates a new instance of DisplayHitModules */
	public DisplayHitModules() {
	}


	public boolean canHandle(Class k) {
		return (SimTrackerHit.class.isAssignableFrom(k)); 
	}

	public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree) {

		try 
		{
			LCMetaData data = event.getMetaData(collection);
			Subdetector sub = data.getIDDecoder().getSubdetector();
			//ignore older detectors
			if (!(sub instanceof SiTrackerBarrel || sub instanceof SiTrackerEndcap || sub instanceof SiTrackerEndcap2|| sub instanceof SiTrackerFixedTarget|| sub instanceof SiTrackerFixedTarget2))
				return; 
		} 
		// ignore additional SimTrackerHit collections
		catch (RuntimeException x) 
		{
			return;
		} 

		if (DetectorElementStore.getInstance().isEmpty()) 
			return; 

		String collection_name = event.getMetaData(collection).getName();

		//sensor type    
		HepRepType typeS = getSensorType(factory, typeTree, collection_name);

		//module type
		HepRepType typeM = getModuleType(factory, typeTree, collection_name);

		Set<IDetectorElement> alreadyMade = new HashSet<IDetectorElement>(); 

		for (SimTrackerHit h : (List<SimTrackerHit>) collection) 
		{
			// find sensor
			IDetectorElement sensor = h.getDetectorElement(); 

			//make sure we don't have repeats
			if (!alreadyMade.add(sensor)) 
				continue; 

			//get the module from the sensor 
			IDetectorElement module = sensor.getParent(); 

			drawPolyhedron(sensor,typeS,instanceTree,factory); 
			drawPolyhedron(module,typeM,instanceTree,factory); 
		}
	}

	public static void drawPolyhedron(IDetectorElement detelem, HepRepType type, HepRepInstanceTree instanceTree, HepRepFactory factory) {

		ISolid solid = detelem.getGeometry().getLogicalVolume().getSolid(); 
		if (!(solid instanceof IPolyhedron)) 
			return; 
		IPolyhedron poly = (IPolyhedron) solid; 

		List<Point3D> points = poly.getVertices(); 
		int[] point_ordering = poly.getHepRepVertexOrdering();

		HepRepInstance instance = factory.createHepRepInstance(instanceTree,type); 

		for (int i = 0; i< point_ordering.length; i++) {
			Hep3Vector p = detelem.getGeometry().transformLocalToGlobal(points.get(point_ordering[i]));
			factory.createHepRepPoint(instance,p.x(),p.y(),p.z()); 
		}
	}

	public static HepRepType getSensorType(HepRepFactory factory, HepRepTypeTree typeTree, String collection_name){

		HepRepType typeS = factory.createHepRepType(typeTree,"HitSensor"+collection_name);
		typeS.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER); 
		typeS.addAttValue("drawAs","prism"); 
		typeS.addAttValue("color",Color.WHITE);
		return typeS; 
	}

	public static HepRepType getModuleType(HepRepFactory factory, HepRepTypeTree typeTree, String collection_name){

		HepRepType typeM = factory.createHepRepType(typeTree,"HitModule"+collection_name);
		typeM.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
		typeM.addAttValue("drawAs","prism"); 
		typeM.addAttValue("color",Color.WHITE);
		return typeM; 
	}
}
