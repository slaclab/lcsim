package org.lcsim.plugin;

import org.freehep.application.studio.Studio;
import org.freehep.swing.ExtensionFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import org.freehep.jas.services.FileHandler;
import org.lcsim.util.loop.LCIOEventSource;

/**
 * A file handler for LCIO files.
 * @author tonyj
 * @version $Id: LCSimFileHandler.java,v 1.2 2005/06/20 23:23:08 tonyj Exp $
 */

class LCSimFileHandler implements FileHandler
{
   private Studio app;
   LCSimFileHandler(Studio app)
   {
      this.app = app;
   }
   public boolean accept(File file) throws IOException
   {
      return file.getName().endsWith(".slcio");
   }
   
   public FileFilter getFileFilter()
   {
      return new ExtensionFileFilter("slcio","LCIO Files");
   }

   public void openFile(File file) throws IOException
   {
      LCIOEventSource source = new LCIOEventSource(file);
      app.getLookup().add(source);
   }   
}