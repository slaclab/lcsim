/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

/**
 *
 * @author cozzy
 */
public class DetectorMismatchException extends RuntimeException {
    public DetectorMismatchException(String expectedName, String actualName) {
        super("Expected detector: "+expectedName+". Actual detector: "+actualName); 
    }
}
