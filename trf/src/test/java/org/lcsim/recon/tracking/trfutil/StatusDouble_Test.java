/*
 * StatusDouble_Test.java
 *
 * Created on July 24, 2007, 11:32 AM
 *
 * $Id: StatusDouble_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class StatusDouble_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of StatusDouble_Test */
    public void testStatusDouble()
    {
        String component = "StatusDouble";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component+ " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "--------------- testing component" + component
                + ". -------------");
        //*******************************************
        if(debug) System.out.println( ok_prefix+" Test constructor");
        
        double value = 12;
        int status = 137;
        
        StatusDouble sv = new StatusDouble(status, value);
        
        if(debug) System.out.println(sv);
        
        Assert.assertTrue(sv.status() == status);
        Assert.assertTrue(sv.value() == value);
        
        //*******************************************
        if(debug) System.out.println( ok_prefix
                + "-------------------- All tests passed. -------------------");
        //*******************************************
    }
    
}
