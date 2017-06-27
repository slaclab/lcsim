package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepInstanceTree;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;

/**
 *
 * @author tonyj
 * @version $Id: ClusterConverter.java,v 1.8 2010/05/05 20:36:35 ngraf Exp $
 */
class ClusterConverter implements HepRepCollectionConverter
{
    private Color[] colors;
    
    ClusterConverter()
    {
        ColorMap cm = new RainbowColorMap();
        colors = new Color[20];
        for (int i=0; i<colors.length; i++) colors[i] = cm.getColor(((double) i)/colors.length,1);
        // Shuffle the elements in the array
        Collections.shuffle(Arrays.asList(colors));
    }
    public boolean canHandle(Class k)
    {
        return Cluster.class.isAssignableFrom(k);
    }
    public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
    {
        LCMetaData meta = event.getMetaData(collection);
        String name = meta.getName();
        int flags = meta.getFlags();
        
        HepRepType typeX = factory.createHepRepType(typeTree, name);
        typeX.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
        typeX.addAttValue("drawAs","Point");
        typeX.addAttValue("color",Color.RED);
        typeX.addAttValue("fill",true);
        typeX.addAttValue("fillColor",Color.RED);
        typeX.addAttValue("MarkName","Box");
        typeX.addAttDef("energy", "Hit Energy", "physics", "");
        typeX.addAttDef("cluster", "Cluster Energy", "physics", "");
        
        int i = 0;
        
        for (Cluster cluster : (List<Cluster>) collection)
        {
            Color clusterColor = colors[i];
            i = (i+1) % colors.length;
            // Some clusters (for example created by the FastMC) may not have hits
            // now draw cluster centroid as star
            double[]  pos = cluster.getPosition();
            HepRepInstance instanceC = factory.createHepRepInstance(instanceTree, typeX);
            HepRepPoint cp = factory.createHepRepPoint(instanceC, pos[0],pos[1],pos[2]);
            
            SpacePoint point = new CartesianPoint(pos);
            instanceC.addAttValue("drawAs", "Ellipsoid");
            // TODO Change from fixed size to reflect true cluster dimensions.
            instanceC.addAttValue("Radius", 5);
            instanceC.addAttValue("Radius2", 5);
            instanceC.addAttValue("Radius3", 20);
            //TODO Change to use cluster direction instead of connecting origin to centroid.
            double theta = point.theta(); 
            double phi = point.phi(); 
            instanceC.addAttValue("Phi", phi);
            instanceC.addAttValue("Theta", theta);            
            
            instanceC.addAttValue("MarkName","Star");
            instanceC.addAttValue("cluster",cluster.getEnergy());
            instanceC.addAttValue("color",clusterColor);
            instanceC.addAttValue("MarkSize",10);
            
            List<CalorimeterHit> hits = cluster.getCalorimeterHits();
            if (hits != null)
            {
                for (CalorimeterHit hit : hits)
                {
                    // FixMe: What if hit doesn't have position?
                	double hitpos[] = null;
                	try {
                		hitpos = hit.getPosition();
                	}
                	catch (Exception x)
                	{}
                	
                	if (hitpos != null)
                	{
                                double e = 0.;
                                if(hit instanceof SimCalorimeterHit)
                                {
                                    e = hit.getRawEnergy();
                                }
                                else
                                {
                                    e = hit.getCorrectedEnergy();
                                }
                		pos = hit.getPosition();
                		HepRepInstance instanceX = factory.createHepRepInstance(instanceC, typeX);
                		instanceX.addAttValue("energy",e);
                		instanceX.addAttValue("MarkSize",5);
                		//instanceX.addAttValue("cluster",cluster.getEnergy());
                		instanceX.addAttValue("color",clusterColor);
                		instanceX.addAttValue("showparentattributes", true);
                		instanceX.addAttValue("pickparent", true);
                		HepRepPoint pp = factory.createHepRepPoint(instanceX,pos[0],pos[1],pos[2]);
                	}
                }
            }
        }
    }
}
