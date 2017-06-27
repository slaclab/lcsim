package org.lcsim.plugin;

import org.freehep.application.studio.Studio;
import org.freehep.swing.ExtensionFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import org.freehep.jas.services.FileHandler;
import org.lcsim.util.loop.FileList;
import org.lcsim.util.loop.LCIOEventSource;

/**
 * A file handler for LCIO file lists.
 * @author tonyj
 * @version $Id: LCSimFileListHandler.java,v 1.1 2007/03/15 05:24:30 tonyj Exp $
 */

class LCSimFileListHandler implements FileHandler
{
   private Studio app;
   LCSimFileListHandler(Studio app)
   {
      this.app = app;
   }
   public boolean accept(File file) throws IOException
   {
      return file.getName().endsWith(".slcio.filelist");
   }
   
   public FileFilter getFileFilter()
   {
      return new ExtensionFileFilter(".slcio.filelist","LCIO File List");
   }

   public void openFile(File file) throws IOException
   {
      FileList fileList = new FileList(file,"LCIO File List");
      LCIOEventSource source = new LCIOEventSource(fileList);
      app.getLookup().add(source);
   }   
}