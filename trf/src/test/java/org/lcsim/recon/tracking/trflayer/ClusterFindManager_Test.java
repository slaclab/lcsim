/*
 * ClusterFindManager_Test.java
 *
 * Created on July 24, 2007, 4:34 PM
 *
 * $Id: ClusterFindManager_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class ClusterFindManager_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ClusterFindManager_Test */
    public void testClusterFindManager()
    {
         String component = "ClusterFindManager";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        if(debug) System.out.println( ok_prefix
                + "No tests. abstract class");
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
