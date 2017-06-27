package org.lcsim.plugin;

import org.freehep.application.studio.Studio;
import org.freehep.swing.ExtensionFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.freehep.jas.services.FileHandler;

import org.lcsim.util.loop.StdhepEventSource;

/**
 * A file handler for LCIO files.
 * @author tonyj
 * @version $Id: StdhepFileHandler.java,v 1.3 2007/03/15 06:39:39 tonyj Exp $
 */

class StdhepFileHandler implements FileHandler
{
   private Studio app;
   StdhepFileHandler(Studio app)
   {
      this.app = app;
   }

   public boolean accept(File file) throws IOException
   {
      return file.getName().endsWith(".stdhep");
   }

   public FileFilter getFileFilter()
   {
      return new ExtensionFileFilter("stdhep","Stdhep Files");
   }

   public void openFile(File file) throws IOException
   {
      Object detectorName = JOptionPane.showInputDialog(app,"Select detector name","Choose detector geometry",JOptionPane.QUESTION_MESSAGE,null,null,"sidaug05");
      StdhepEventSource source = new StdhepEventSource(file,detectorName.toString());

      app.getLookup().add(source);
   }   
}
