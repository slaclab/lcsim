package org.lcsim.recon.tracking.trfbase;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import org.lcsim.recon.tracking.trfutil.Assert;

/** Class used to add an interaction when fitting tracks defined
 * only by hits on Surfaces. Recall that Interactors are currently
 * held by Layers, which are not accessible from Surfaces.
 * Instead of modifying Surface to hold the Interactors, we introduce
 * this registry.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public class InteractorRegistry extends Interactor
{
    
    // map of Interactors keyed by Surface
    private Map _imap;
    
    //
    
    /**
     *default Constructor
     *
     */
    public InteractorRegistry()
    {
        _imap = new HashMap();
    }
    
    //
    
    /**
     * copy Constructor
     *
     * @param   reg  InteractorRegistry to replicate
     */
    public InteractorRegistry(InteractorRegistry reg)
    {
        throw new IllegalArgumentException("Not yet implemented!");
    }
    
    //
    
    /** associate this surface with some kind of interaction
     *
     * @param srf   Surface to associate with  Interactor inter
     * @param inter Interactor for Surface srf
     */
    public void registerInteractor( Surface srf, Interactor inter)
    {
        if(_imap.containsKey(srf)) throw new IllegalArgumentException("This surface already has a Interactor associated with it");
        _imap.put( srf, inter );
    }
    
    //
    
    /**
     *modify VTrack with the appropriate interaction
     *
     * @param   etrk  ETrack to interact
     */
    public void interact( ETrack etrk)
    {
        
        Surface thisSurf = bsurf( etrk);
        // Does the surface have some kind of interactor registered?
        boolean found = _imap.containsKey(thisSurf);
        if(!found) System.out.println(thisSurf+" not found!");
        // If not, return
        if(!found) return;
        // If so, interact according to the Interactor registered
        // Note that this could be a mulitInteractor.
        Interactor inter = (Interactor) _imap.get(thisSurf);
        inter.interact( etrk );
    }
    
    //
    
    /**
     *return the bounded surface of the VTrack.  This is needed because
     * VTracks typically have pure surfaces
     *
     * @param   vtrk  VTrack for which to retrieve the bounded Surface
     * @return bounded Surface for the VTrack
     */
    public Surface bsurf( VTrack vtrk)
    {
        Surface thisSurf = vtrk.surface();
        boolean found = false;
        Set keys = _imap.keySet();
        for( Iterator it = keys.iterator(); it.hasNext(); )
        {
            Surface surf = (Surface) it.next();
            if( surf.pureEqual(thisSurf) )
            {
                if(surf.status(vtrk).inBounds())
                {
                    found = true;
                    return surf;
                }
            }
        }
        if(!found)
        {
            System.out.println("surf "+thisSurf+" not found!");
            for( Iterator it = keys.iterator(); it.hasNext(); )
            {
                Surface surf = (Surface) it.next();
                if( surf.pureEqual(thisSurf) )
                {
                    System.out.println(surf.status(vtrk));
                    System.out.println("vtrk:"+vtrk);
                    System.out.println(surf);
                }
            }
        }
        Assert.assertTrue(found);
        return thisSurf;
    }
    
    //
    
    /**
     *Copy
     *
     * @return  new copy of this Interactor
     */
    public Interactor newCopy()
    {
        return new InteractorRegistry(this);
    }
    
    // output stream
    
    /**
     * String representation of InteractorRegistry
     *
     * @return  String representation of InteractorRegistry
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+"\n");
        sb.append(_imap);
        return sb.toString();
    }
}
