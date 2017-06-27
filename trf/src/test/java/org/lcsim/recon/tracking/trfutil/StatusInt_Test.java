/*
 * StatusInt_Test.java
 *
 * Created on July 24, 2007, 11:27 AM
 *
 * $Id: StatusInt_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class StatusInt_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of StatusInt_Test */
    public void testStatusInt()
    {
        String component = "StatusInt";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component+ " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "--------------- testing component" + component
                + ". -------------");
        //*******************************************
        if(debug) System.out.println( ok_prefix+" Test constructor");
        
        int value = 12;
        int status = 137;
        
        StatusInt sv = new StatusInt(status, value);
        
        if(debug) System.out.println(sv);
        
        Assert.assertTrue(sv.status() == status);
        Assert.assertTrue(sv.value() == value);
        
        //*******************************************
        if(debug) System.out.println( ok_prefix
                + "-------------------- All tests passed. -------------------");
        //*******************************************
    }
    
}
