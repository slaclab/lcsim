package org.lcsim.recon.tracking.trflayer;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.trfutil.RandomSimulator;

import org.lcsim.recon.tracking.trfbase.VTrack;

/**
 * A detector simulator contains a detector and a list of layer
 * simulators which match the layers in the detector.
 *<p>
 * The base simulator is constructed from the detector reference.
 * Subclasses add layer simulators either directly or by extraction from
 * other detector simulators.
 *<p>
 * Methods to add or drop clusters invoke the corresponding method on
 * each of the layer simulators.
 *<p>
 * Although this class is not abstract, the constructor is hidden and
 * users must provide subclasses to instantiate.  The expectation is that
 * users will add layers or detectors in the subclass constructor.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class DetectorSimulator extends RandomSimulator
{
    
    // attributes
    
    // Detector.
    private Detector _det;
    
    // Layer simulators.
    private Map _lsims;
    
    // methods
    
    // Constructors from a detector.
    protected DetectorSimulator( Detector det)
    {
        //_lsims = new HashMap();
        _lsims = new TreeMap(); // for ordering of layers
        _det = det;
    }
    
    // Add a layer simulator by name.
    // Name must be known to the detector.
    // Detector and layer simulator must reference the same layer
    // object.
    // Return nonzero for error.
    protected DetSimReturnStatus
            addLayerSimulator(String name, LayerSimulator lsim)
    {
        // Check the name.
        if ( ! _det.isAssigned(name) ) return DetSimReturnStatus.UNKNOWN_NAME;
        
        // Fetch the layers and check for match.
        Layer detlyr = _det.layer(name);
        if(detlyr instanceof InteractingLayer) detlyr = ((InteractingLayer)detlyr).layer();
        Layer simlyr = lsim.layer();
        
        if ( !detlyr.equals(simlyr) ) return DetSimReturnStatus.LAYER_MISMATCH;
        
        _lsims.put(name, lsim);
        
        return DetSimReturnStatus.OK;
        
    }
    
    // Add all the layer simulators from a detector simulator.
    // Each name must be known to the local detector.
    // Detector and each layer simulator must reference the same
    // layer object.
    // Return nonzero for error.
    protected DetSimReturnStatus
            addDetectorSimulator(  DetectorSimulator dsim)
    {
        // Loop over layer simulators.
        for(Iterator i=dsim._lsims.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry e = (Map.Entry) i.next();
            String name = (String) e.getKey();
            LayerSimulator lsim = (LayerSimulator) e.getValue();
            DetSimReturnStatus stat = addLayerSimulator(name,lsim);
            if ( stat != DetSimReturnStatus.OK ) return stat;
        }
        
        return DetSimReturnStatus.OK;
        
    }
    
    // methods
    
    //
    
    /**
     *Return the detector.
     *
     * @return Detector
     */
    public Detector detector()
    {
        return _det;
    }
    
    //
    
    /**
     *Return the list of generators.
     *
     * @return list of HitGenerators
     */
    public List generators()
    {
        List gens = new ArrayList();
        // Loop over layer simulators.
        for(Iterator i=_lsims.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry e = (Map.Entry) i.next();
            
            LayerSimulator lsim = (LayerSimulator) e.getValue();
            List newgens = lsim.generators();
            for ( Iterator igen=newgens.iterator(); igen.hasNext(); )
            {
                gens.add(igen.next());
            }
        }
        return gens;
    }
    
    //
    
    /**
     *Use the specified track to add clusters with each layer simulator.
     *
     * @param   trv VTrack for which to generate clusters
     */
    public void addClusters( VTrack trv )
    {
        addClusters(trv, 0);
    }
    
    
    /**
     *Use the specified track to add clusters with each layer simulator.
     *
     * @param   trv VTrack for which to generate clusters
     * @param   mcid MC track ID to associate with this track
     */
    public void addClusters( VTrack trv, int mcid )
    {
        
        // Loop over layer simulators.
        for (Iterator i=_lsims.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry e = (Map.Entry) i.next();
            LayerSimulator lsim = (LayerSimulator) e.getValue();
            lsim.addClusters(trv, mcid);
        }
        
    }
    
    //
    
    /**
     *Drop clusters from each layer simulator.
     *
     */
    public void dropClusters()
    {
        // Loop over layer simulators.
        for (Iterator i=_lsims.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry e = (Map.Entry) i.next();
            LayerSimulator lsim = (LayerSimulator) e.getValue();
            lsim.dropClusters();
        }
        
    }
    
    //
    
    /**
     *write out the generators
     *
     */
    public void printGenerators()
    {
        
    }
    
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        int size = _lsims.size();
        StringBuffer sb = new StringBuffer(getClass().getName()+" with " + size + " layer generator");
        if ( size != 1 ) sb.append("s");
        if ( size == 0 )
        {
            sb.append(".");
        }
        else
        {
            sb.append(":\n");
            if ( _lsims.size() == 0 )
            {
                sb.append("There are no generators.");
                return sb.toString();
            }
            for (Iterator i=_lsims.entrySet().iterator(); i.hasNext(); )
            {
                Map.Entry e = (Map.Entry) i.next();
                
                sb.append( "\n" + e.getKey()+": "+e.getValue() + "\n");
            }
        }
        return sb.toString();
    }
}

