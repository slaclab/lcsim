/*
 * MultiInteractor_Test.java
 *
 * Created on July 24, 2007, 12:11 PM
 *
 * $Id: MultiInteractor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class MultiInteractor_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of MultiInteractor_Test */
    public void testMultiInteractor()
    {
        String component = "MultiInteractor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        List multi_int = new ArrayList();
        
        Interactor inter1 = new InteractorTest(5.0);
        Interactor inter2 = new InteractorTest(5.0);
        
        multi_int.add(inter1);
        multi_int.add(inter2);
        
        if(debug) System.out.println("Test constructor."  );
        
        MultiInteractor mint = new MultiInteractor(multi_int);
        
        if(debug) System.out.println(mint);
        
        if(debug) System.out.println("Fetch the interactors..."  );
        List ints = mint.getInteractors();
        Assert.assertTrue( ints.size()==2);
        Assert.assertTrue( ints.get(0).equals(inter1));
        Assert.assertTrue( ints.get(1).equals(inter2));
        
        //**********************************************************************
        
        if(debug) System.out.println("Interact some tracks..."  );
        // first... refer to tracks passed through MultiInteractor
        // second... refer to tracks passed through inter1 and inter2
        // finally, i should have err1(0,0)=err2(0,0)=25.
        
        TrackError firstError = new TrackError();
        TrackError secondError = new TrackError();
        
        ETrack firstTrack = new ETrack();
        ETrack secondTrack = new ETrack();
        TrackError err = new TrackError();
        
        err.set(0,0 , 1.0);
        firstTrack.setError(err);
        secondTrack.setError(err);
        
        mint.interact(firstTrack);
        
        inter1.interact(secondTrack);
        inter2.interact(secondTrack);
        
        Assert.assertTrue(firstTrack.error(0,0) == 25);
        Assert.assertTrue(secondTrack.error(0,0) == 25);
        
        //**********************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );        
    }
    
}
