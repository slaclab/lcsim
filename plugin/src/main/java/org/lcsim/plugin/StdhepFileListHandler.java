package org.lcsim.plugin;

import org.freehep.application.studio.Studio;
import org.freehep.swing.ExtensionFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.freehep.jas.services.FileHandler;
import org.lcsim.util.loop.FileList;
import org.lcsim.util.loop.StdhepEventSource;

/**
 * A file handler for LCIO file lists.
 * @author tonyj
 * @version $Id: StdhepFileListHandler.java,v 1.1 2007/03/15 06:39:39 tonyj Exp $
 */

class StdhepFileListHandler implements FileHandler
{
   private Studio app;
   StdhepFileListHandler(Studio app)
   {
      this.app = app;
   }
   public boolean accept(File file) throws IOException
   {
      return file.getName().endsWith(".stdhep.filelist");
   }
   
   public FileFilter getFileFilter()
   {
      return new ExtensionFileFilter(".stdhep.filelist","Stdhep File List");
   }

   public void openFile(File file) throws IOException
   {
      Object detectorName = JOptionPane.showInputDialog(app,"Select detector name","Choose detector geometry",JOptionPane.QUESTION_MESSAGE,null,null,"sidaug05");
      FileList fileList = new FileList(file,"Stdhep File List");
      StdhepEventSource source = new StdhepEventSource(fileList,detectorName.toString());
      app.getLookup().add(source);
   }   
}