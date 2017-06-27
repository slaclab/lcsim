package org.lcsim.recon.tracking.trflayer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trflayer.Layer;
import org.lcsim.recon.tracking.trfbase.Cluster;

/**
 * Generic layer simulator.  An input track is propagated to each active surface
 * in a layer and an associated hit generator is used to generate a cluster
 * and add it to the layer.  This class takes shared ownership of the layer,
 * the hit generators and the propagator.  The constructor requires that
 * the surfaces associated with the hit generators match those of the layer.
 * Here match means pure_equal.  It is up to the hit generator to decide
 * whether the hit is in bounds.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/


public class LayerSimGeneric extends LayerSimulator
{
    // attributes
    
    // hit generators
    private List _gens;
    
    // propagator
    private Propagator _prop;
    
    // methods
    
    //
    
    /**
     *constructor for one surface
     * Requires that generator surface is same as layer surface.
     *
     * @param   lyr Layer
     * @param   gen HitGenerator
     * @param   prop Propagator
     */
    public LayerSimGeneric(Layer lyr, HitGenerator gen, Propagator prop)
    {
        super(lyr);
        _gens = new ArrayList();
        _gens.add(gen);
        _prop = prop;
        Assert.assertTrue( lyr != null );
        Assert.assertTrue( gen != null );
        Assert.assertTrue( prop != null );
        Assert.assertTrue( checkSurfaces(layer(),_gens) );
    }
    
    //
    
    /**
     *constructor for multiple surfaces
     * Requires that generator surfaces are the same as the layer surface.
     *
     * @param   lyr Layer
     * @param   gens List of HitGenerators
     * @param   prop Propagator
     */
    public LayerSimGeneric(Layer lyr, List gens, Propagator prop)
    {
        super(lyr);
        _gens = new ArrayList();
        _gens.add(gens);
        _prop = prop;
        Assert.assertTrue( lyr != null );
        Assert.assertTrue( gens.size() > 0 );
        Assert.assertTrue( prop != null );
        Assert.assertTrue( checkSurfaces(layer(),_gens) );
    }
    
    //
    
    /**
     *return the list of generators
     *
     * @return List if HitGenerators
     */
    public List generators()
    {
        List gens = new ArrayList();;
        
        for ( Iterator igen=_gens.iterator(); igen.hasNext(); )
            gens.add( igen.next() );
        return gens;
        
    }
    
    //
    
    /**
     *Generate clusters
     *
     * @param   trv0 VTrack for which to generate clusters
     * @param   mcid MC track ID to associate with track
     */
    public void addClusters( VTrack trv0, int mcid)
    {
        
        // Fetch the list of layer surfaces.
        List lsrfs = layer().clusterSurfaces();
        
        // Loop over surfaces.
        Iterator ilsrf = lsrfs.iterator();
        Iterator igen = _gens.iterator();
        while ( ilsrf.hasNext() )
        {
            Surface lsrf = (Surface) ilsrf.next();
            HitGenerator gen = (HitGenerator) igen.next();
            Surface gsrf = gen.surface();
            
            // Propagate track to generator surface.
            VTrack trv = new VTrack(trv0);
            PropStat pstat = _prop.vecProp(trv,gsrf);
            if ( ! pstat.success() ) continue;
            
            // Generate a cluster and add to layer.
            Cluster clu = gen.newCluster(trv, mcid);
            if ( clu != null )
                layer().addCluster(clu,lsrf);
            
        }  // end loop over surfaces
        
    }
    
    
    
    /**
     * output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getName()+" Generic layer simulator.\n");
        sb.append("Layer: \n"+ layer() + "\n");
        sb.append("Propagator: " + _prop + "\n");
        int size = _gens.size();
        sb.append("Hit generators (count is " + size + "):");
        for ( Iterator igen=_gens.iterator(); igen.hasNext(); ) sb.append(igen.next());
        return sb.toString();
    }
    
    //**********************************************************************
    // Helper functions.
    //**********************************************************************
    
    // Return true if the layer and generator surfaces match.
    private static boolean
            checkSurfaces( Layer lyr,
            List gens)
    {
        // Retrieve the active layer surfaces.
        List lsrfs = lyr.clusterSurfaces();
        if ( lsrfs.size() != gens.size() ) return false;
        Iterator ilsrf = lsrfs.iterator();
        Iterator igen = gens.iterator();
        // Require pure surfaces to match.
        while ( igen.hasNext() )
        {
            Surface lsrf = (Surface) ilsrf.next();
            HitGenerator gen = (HitGenerator) igen.next();
            //    if ( ! pgen ) return false;
            Surface gsrf = gen.surface();
            if ( ! lsrf.pureEqual(gsrf) ) return false;
        }
        return true;
    }
    
}

