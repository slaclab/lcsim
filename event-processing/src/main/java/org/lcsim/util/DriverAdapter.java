package org.lcsim.util;

import java.io.IOException;
import org.freehep.jas.plugin.console.ConsoleOutputStream;
import org.freehep.jas.plugin.console.ConsoleService;
import org.freehep.record.loop.AbstractLoopListener;
import org.freehep.record.loop.LoopEvent;
import org.freehep.record.loop.RecordEvent;
import org.freehep.record.loop.RecordListener;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.Detector;


/**
 * Drive a Driver from a Record loop
 * @author Tony Johnson
 * @version $Id: DriverAdapter.java,v 1.8 2012/06/15 05:24:20 onoprien Exp $
 */
public class DriverAdapter extends AbstractLoopListener implements RecordListener
{
   private Driver driver;
   // The console stuff is here to fix LCSMI-128. More logically this stuff should
   // probably be part of the JAS3 record loop adapter (which currently handles
   // redirection of the recordSupplied method)
   private ConsoleService cs;
   private ConsoleOutputStream out;
   private Detector detector;
   
   public DriverAdapter(Driver driver)
   {
      this.driver = driver;
   }
   public DriverAdapter(Driver driver, ConsoleService cs) throws IOException
   {
      this.driver = driver;
      this.cs = cs;
      if (cs != null) out = cs.getConsoleOutputStream("Record Loop", null);
   }
   
   public void finish(LoopEvent event)
   {
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
      driver.endOfData();
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),null);
   }
   
   public void suspend(LoopEvent event)
   {
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
      driver.suspend();
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),null);
   }
   
   public void resume(LoopEvent event)
   {
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
      driver.resume();
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),null);
   }
   
   public void recordSupplied(RecordEvent rse)
   {
      try
      {
         Object event = rse.getRecord();
         if (event instanceof EventHeader)
         {
            EventHeader evt = (EventHeader) event;
            
            if (detector != evt.getDetector())
            {
               detectorChanged(evt.getDetector());
            }
            driver.process(evt);
         }
      }
      catch (Driver.NextEventException x)
      {
         // OK, just continue with next event.
      }
   }
   
   private void detectorChanged(Detector detector)
   {
      this.detector = detector;
      driver.detectorChanged(detector);
   }
   
   public void start(LoopEvent event)
   {
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
      driver.startOfData();
      if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),null);
   }
   
   public Driver getDriver()
   {
      return driver;
   }
}