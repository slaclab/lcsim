package org.lcsim.lcio;

/**
 * Allows block handlers to register to be called back at end of event.
 * @author tonyj
 */
interface LCIOCallback
{
   void callback();   
}
