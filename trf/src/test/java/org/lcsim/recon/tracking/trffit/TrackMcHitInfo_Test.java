/*
 * TrackMcHitInfo_Test.java
 *
 * Created on July 24, 2007, 4:42 PM
 *
 * $Id: TrackMcHitInfo_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class TrackMcHitInfo_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of TrackMcHitInfo_Test */
    public void testTrackMcHitInfo()
    {
        String component = "TrackMcHitInfo";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("-------- Testing component " + component
                + ". --------" );
        
        
        //********************************************************************
        
        
        // make some data
        
        int nhits = 16;
        Map idmap = new HashMap();
        idmap.put(new Integer(2),new Integer(15));
        idmap.put(new Integer(13),new Integer(11));
        idmap.put(new Integer(137),new Integer(1));
        
        
        if(debug) System.out.println("Test constructors" );
        
        TrackMcHitInfo tmcinfo = new TrackMcHitInfo(nhits,idmap);
        
        if(debug) System.out.println("Test best_id " + tmcinfo.bestMcId() );
        Assert.assertTrue( tmcinfo.bestMcId() == 2 );
        
        if(debug) System.out.println("Test purity " +  tmcinfo.purity() );
        Assert.assertTrue( tmcinfo.purity() == 15./16. );
        
        if(debug) System.out.println("Test number of hits " + tmcinfo.numHits() );
        Assert.assertTrue( tmcinfo.numHits() == 16 );
        
        if(debug) System.out.println("Test full list of Mc Ids, there are " + tmcinfo.mc_idlist().size()+ " ids.");
        
        Assert.assertTrue( tmcinfo.mc_idlist().size() == 3 );
        for(int i = 0; i<tmcinfo.mc_idlist().size(); ++i)
        {
            if(debug) System.out.println("Mc Id " + i +" " + ((Integer)tmcinfo.mc_idlist().get(i)).intValue());
        }
        
        if(debug) System.out.println("Test McIdMap " +  tmcinfo.mcHitNumber().size() );
        Assert.assertTrue( tmcinfo.mcHitNumber().size() == 3 );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
