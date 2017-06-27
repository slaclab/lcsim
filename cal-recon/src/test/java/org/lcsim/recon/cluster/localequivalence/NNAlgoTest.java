/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.cluster.localequivalence;

import hep.physics.vec.Hep3Vector;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 *
 * @author Norman Graf
 */
public class NNAlgoTest extends TestCase
{
    private boolean debug = false;
    /** Creates a new instance of NNAlgoTest */
    public void testNNAlgo()
    {
        Map<Long, CalorimeterHit> hitmap = new HashMap<Long, CalorimeterHit>();
        
        double[] pos = {0.,0.,0.};
        for(int i=0; i<10; ++i)
        {
            double d = i+.1;
            long l = (long) i;
            hitmap.put(l, new CalHit(d, d, l, d, pos));
        }
        
        if(debug) System.out.println(hitmap);
        
        double minValue = 0.15;
        NNAlgo alg = new NNAlgo(minValue);
        if(debug) System.out.println(alg);
        List<NNCluster> clusters = alg.cluster(hitmap);
        if(debug) System.out.println("found "+clusters.size()+ " clusters");
        assertEquals(clusters.size(), 1);
        for(NNCluster clus : clusters)
        {
            if(debug) System.out.println(clus);
            assertEquals(clus.size(), 10);
        }
        
        if(debug) System.out.println("hitmap left with "+hitmap.size());
        assertEquals(hitmap.size(), 0);
        
        //let's try something more creative
        long[] ids =   { 0,  /**/  2,  3,  4,  5,  6,  7, /**/   9};
        double[] vals ={.1,  /**/ .1, .3, .1, .4, .2, .1, /**/  .5};
        hitmap.clear();
        double rE = .1;
        double t = 137.;
        for(int i=0; i<ids.length; ++i)
        {
            hitmap.put(ids[i], new CalHit(rE, vals[i], ids[i], t, pos));
        }
        clusters = alg.cluster(hitmap);
        // should give 3 clusters, since first cluster is below threshhold
        assertEquals(clusters.size(), 3);
        
        // map should have one entry left
        assertEquals(hitmap.size(), 1);
        
        //sort the list of clusters
        Collections.sort(clusters);
        Collections.reverse(clusters);
        if(debug) System.out.println(clusters.get(0).value());
        assertEquals(clusters.get(0).value(), 0.8);
        assertEquals(clusters.get(1).value(), 0.5);
        assertEquals(clusters.get(2).value(), 0.4);
        
        // now let's mix it up a bit by increasing the neighborhood...
        
        NNAlgo alg2 = new NNAlgo(minValue,2,2,2);
        
        hitmap.clear();
        for(int i=0; i<ids.length; ++i)
        {
            hitmap.put(ids[i], new CalHit(rE, vals[i], ids[i], t, pos));
        }
        clusters = alg2.cluster(hitmap);
        // should give 2 clusters, since first cell has same energy as second cell.
        // TODO fix this anomaly
        assertEquals(clusters.size(), 2);
        
        // map should have one entry left
        assertEquals(hitmap.size(), 1);        
        
        //sort the list of clusters
        Collections.sort(clusters);
        Collections.reverse(clusters);
        if(debug) System.out.println(clusters.get(0).value());
        assertEquals(clusters.get(0).value(), 1.1);
        assertEquals(clusters.get(1).value(), 0.6);
        if(debug)
        {
        for(NNCluster clus : clusters)
        {
            System.out.println(clus.value());
        }
        }
//
//
    }
    
}

class CalHit implements CalorimeterHit
{

    @Override
    public double getEnergyError()
    {
        return 0.;
    }

    @Override
    public int getType()
    {
    return 0;    
    }
    private double _rawEnergy;
    private double _correctedEnergy;
    private long _cellID;
    private double _time;
    private double[] _pos;
    
    private Decoder _decoder = new Decoder();
    
    public CalHit(double rE, double cE, long id, double t, double[] pos)
    {
        _rawEnergy = rE;
        _correctedEnergy = cE;
        _cellID = id;
        _time = t;
        _pos = pos;
        _decoder.setID(id);
    }
    /**
     * Raw energy deposited in Calorimeter Cell
     */
    public double getRawEnergy()
    {
        return _rawEnergy;
    }
    /**
     * Corrected energy deposted in Calorimeter Cell.
     */
    public double getCorrectedEnergy()
    {
        return _correctedEnergy;
    }
    /**
     * The ID of the cell. This can be converted to a physical
     * position using a IDDecoder object obtained from the event
     * meta-data or from this hit.
     */
    public long getCellID()
    {
        return _cellID;
    }
    
    /**
     * Get the ID decoder for this hit. Note that all hits in a collection are
     * gauranteed to share the same id decoder, but once hits have been combined
     * into clusters each hit may have its own id decoder.
     */
    public IDDecoder getIDDecoder()
    {
        return _decoder;
    }
    
    /**
     * The subdetector corresponding to this hit.
     */
    public Subdetector getSubdetector()
    {
        return null;
    }
    
    public double getTime()
    {
        return _time;
    }
    
    /**
     * The position of the hit. If the hit position is stored in the source
     * LCIO file this will be returned. Otherwise the IDDecoder is used to get
     * the hit position from the hit ID.
     */
    public double[] getPosition()
    {
        return _pos;
    }
    
    public String toString()
    {
        return "CalHit: value= "+ _correctedEnergy;
    }

    @Override
    public DetectorIdentifierHelper getDetectorIdentifierHelper()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSystemId()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BarrelEndcapFlag getBarrelEndcapFlag()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLayerNumber()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIdentifierFieldValue(String field)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EventHeader.LCMetaData getMetaData()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setMetaData(EventHeader.LCMetaData meta)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDetectorElement getDetectorElement()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDetectorElement(IDetectorElement de)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IIdentifier getIdentifier()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IExpandedIdentifier getExpandedIdentifier()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IIdentifierHelper getIdentifierHelper()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Hep3Vector getPositionVec()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class Decoder implements IDDecoder
{
    private long _id;
    
    
    /** Load the decoder with a 64-bit id value from the hit. */
    public void setID(long id)
    {
        _id = id;
    }
    
    /** Get an expanded identifier that maps strings to integer values. */
    public ExpandedIdentifier getExpandedIdentifier()
    {
        return null;
    }
    
    /** Same as getIDExpanded() except sets id. */
    public ExpandedIdentifier getExpandedIdentifier(long id)
    {
        return null;
    }
    /*/\/\/\ Access to field data /\/\/\ */
    public int getValue(String field)
    {
        return 0;
    }
    public int getValue(int index)
    {
        return 0;
    }
    
    /* /\/\/\ ID description /\/\/\ */
    public int getFieldCount()
    {
        return 0;
    }
    public String getFieldName(int index)
    {
        return null;
    }
    public int getFieldIndex(String name)
    {
        return 0;
    }
    public void setIDDescription(IDDescriptor id)
    {
        
    }
    public IDDescriptor getIDDescription()
    {
        return null;
    }
    
    /** @return layer number */
    public int getLayer()
    {
        return 0;
    }
    
    /* /\/\/\ Position interface /\/\/\ */
    
    // FIXME: change to Hep3Vector and eliminate the X/Y/Z/theta/phi methods
    
    /** @return Hep3Vector representing the position of the current ID. */
    public Hep3Vector getPositionVector()
    {
        return null;
    }
    
    /** @return position as double array of length 3 */
    public double[] getPosition()
    {
        return null;
    }
    
    /** @return X coordinate */
    public double getX()
    {
        return 0.;
    }
    
    /** @return Y coordinate */
    public double getY()
    {
        return 0.;
    }
    
    /** @return Z coordinate */
    public double getZ()
    {
        return 0.;
    }
    
    /** @return phi angle */
    public double getPhi()
    {
        return 0.;
    }
    
    /** @return theta angle */
    public double getTheta()
    {
        return 0.;
    }
    
    /* /\/\/\ Reverse Decoding: Position to Cell /\/\/\ */
    
    public long findCellContainingXYZ(Hep3Vector pos)
    {
        return 0;
    }
    public long findCellContainingXYZ(double[] pos)
    {
        return 0;
    }
    public long findCellContainingXYZ(double x, double y, double z)
    {
        return 0;
    }
    
    /* /\/\/\ Miscellaneous /\/\/\ */
    
    /** Get the flag that indicates barrel or endcap, i.e. the "barrel" field. */
    public BarrelEndcapFlag getBarrelEndcapFlag()
    {
        return null;
    }
    
    /** Get the system ID, i.e. the "system" field. */
    public int getSystemID()
    {
        return 0;
    }
    
    /** @deprecated use getSystemID() instead */
    public int getSystemNumber()
    {
        return 0;
    }
    
    /** Get the Subdetector associated with this IDDecoder, or null if not applicable. */
    public Subdetector getSubdetector()
    {
        return null;
    }
    
    /* /\/\/\ Neighbours /\/\/\ */
    
    public boolean supportsNeighbours()
    {
        return true;
    }
    public long[] getNeighbourIDs()
    {
        long[] n = new long[2];
        n[0] = _id-1;
        n[1] = _id+1;
        return n;
    }
    public long[] getNeighbourIDs(int deltaLayer, int deltaTheta, int deltaPhi)
    {
        long[] n = new long[2*deltaLayer];
        for(int i=0; i< deltaLayer; ++i)
        {
            n[i] = _id+i-deltaLayer;
            n[deltaLayer+i] = _id+i+1;
        }
        return n;
        
    }

    @Override
    public int[] getValues(int[] buffer)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getVLayer()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
