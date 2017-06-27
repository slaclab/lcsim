/*
 * StatusValue_Test.java
 *
 * Created on July 24, 2007, 11:26 AM
 *
 * $Id: StatusValue_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class StatusValue_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of StatusValue_Test */
    public void testStatusValue()
    {
        String component = "StatusValue";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component "+ component
                + ". ----------" );
        //***********************************************
        if(debug) System.out.println( ok_prefix+ "Test constructor");
        
        String value = "Value";
        int status = 137;
        
        StatusValue sv = new StatusValue(status, value);
        
        if(debug) System.out.println(sv);
        
        Assert.assertTrue(sv.status() == status);
        Assert.assertTrue(sv.value() == value);
        
        
        //***********************************************
        if(debug) System.out.println( ok_prefix
                + "----------------------- All tests passed. --------------");
        //***********************************************
    }
    
}
