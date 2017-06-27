package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepConverter;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTreeID;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.geometry.Detector;

/**
 *
 * @author tonyj
 */
public class LCSimHepRepConverter implements HepRepConverter
{
    private HepRepFactory factory;

    public final static String HITS_LAYER = "Hits";
    public final static String PARTICLES_LAYER = "Particles";
    public final static String AXIS_LAYER = "Axis";

    private List<HepRepCollectionConverter> converters = new LinkedList();

    public LCSimHepRepConverter()
    {
        try
        {
            factory = HepRepFactory.create();
            register(new CalorimeterHitConverter());
            register(new ClusterConverter());
            register(new MCParticleConverter());
            register(new SimTrackerHitConverter());
            register(new TrackerHitConverter());
            register(new TrackConverter());
            register(new ReconstructedParticleConverterNew());
            register(new VertexConverter());
            register(new Hep3VectorConverter());    
            register(new DisplayHitModules());
            register(new SiTrackerHitStrip1DConverter()); 
            register(new SiTrackerHitStrip2DConverter());
            register(new HelicalTrackHitConverter());
            register(new RawTrackerHitConverter());
        }
        catch (Exception x)
        {
            throw new RuntimeException("Could not create heprep factory",x);
        }
    }
    public void register(HepRepCollectionConverter converter)
    {
        converters.add(0, converter);
    }
    public void deregister(HepRepCollectionConverter converter)
    {
        converters.remove(converter);
    }
    public boolean canHandle(Class objectClass)
    {
        return EventHeader.class.isAssignableFrom(objectClass);
    }

    public HepRep convert(Object object)
    {
        EventHeader event = (EventHeader) object;
        HepRep root = factory.createHepRep();

        // Lets start with the detector
        try
        {
            Detector detector = event.getDetector();
            detector.appendHepRep(factory,root);
        }
        catch (Exception e)
        {
        }

        // Now on to deal with the event data

        root.addLayer(PARTICLES_LAYER);
        root.addLayer(HITS_LAYER);

        HepRepTreeID treeID = factory.createHepRepTreeID("EventType", "1.0");
        HepRepTypeTree typeTree = factory.createHepRepTypeTree(treeID);
        root.addTypeTree(typeTree);

        HepRepInstanceTree instanceTree = factory.createHepRepInstanceTree("Event", "1.0", typeTree);
        root.addInstanceTree(instanceTree);

        List<List<Object>> data = event.get(Object.class);

        for (List collection : data)
        {
            LCMetaData meta = event.getMetaData(collection);
            if(meta==null) continue;
            Class colType = meta.getType();

            for (HepRepCollectionConverter converter : converters)
            {
                if (converter.canHandle(colType))
                {
                    converter.convert(event,collection,factory,typeTree,instanceTree);
                }
            }
        }

        createAxisLines(factory, root, typeTree, instanceTree);

        return root;
    }

    private void createAxisLines(HepRepFactory factory, HepRep root, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
    {
        root.addLayer(AXIS_LAYER);
        
        HepRepType axis = factory.createHepRepType(typeTree, "axis");
        axis.addAttValue("drawAs","Line");
        axis.addAttValue("layer", "Axis");
        axis.addAttValue("lineWidth", 20);

        HepRepType xaxis = factory.createHepRepType(axis, "xaxis");
        xaxis.addAttValue("color",Color.RED);
        xaxis.addAttValue("fill",true);
        xaxis.addAttValue("fillColor",Color.RED);
        HepRepInstance x = factory.createHepRepInstance(instanceTree, xaxis);
        factory.createHepRepPoint(x,0,0,0);
        factory.createHepRepPoint(x,2000,0,0);

        HepRepType yaxis = factory.createHepRepType(axis, "yaxis");
        yaxis.addAttValue("color",Color.GREEN);
        yaxis.addAttValue("fill",true);
        yaxis.addAttValue("fillColor",Color.GREEN);
        HepRepInstance y = factory.createHepRepInstance(instanceTree, yaxis);
        factory.createHepRepPoint(y,0,0,0);
        factory.createHepRepPoint(y,0,2000,0);

        HepRepType zaxis = factory.createHepRepType(axis, "zaxis");
        zaxis.addAttValue("color",Color.BLUE);
        zaxis.addAttValue("fill",true);
        zaxis.addAttValue("fillColor",Color.BLUE);
        HepRepInstance z = factory.createHepRepInstance(instanceTree, zaxis);
        factory.createHepRepPoint(z,0,0,0);
        factory.createHepRepPoint(z,0,0,2000);                            
    }
    
    /**
     * Find a converter for a class.
     * @param colType the class to convert to heprep
     * @return The converter or null if does not exist.
     */
    public HepRepCollectionConverter findConverter(Class<?> colType) {
        for (HepRepCollectionConverter converter : converters) {
            if (converter.canHandle(colType)) {
                return converter;
            }
        }
        return null;
    }
}
