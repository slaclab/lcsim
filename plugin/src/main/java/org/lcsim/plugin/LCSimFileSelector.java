package org.lcsim.plugin;

import java.io.IOException;
import org.freehep.application.studio.Studio;
import org.freehep.swing.ErrorDialog;
import org.freehep.swing.ExtensionFileFilter;
import org.lcsim.util.loop.LCIOEventSource;

/**
 *
 * @author tonyj
 */
class LCSimFileSelector extends FileSelector
{
   private Studio app;
   /** Creates a new instance of LCSimFileSelector */
   public LCSimFileSelector(Studio app)
   {
      super(app,"LCIO","slcio.filelist",new ExtensionFileFilter("slcio","LCIO File"),new ExtensionFileFilter("slcio.filelist","LCIO File List"));
      this.app = app;
   }
   
   public void onFinish()
   {
      try
      {
         LCIOEventSource source = isMultiFileSelected() ? new LCIOEventSource(getSelectedFiles()) : new LCIOEventSource(getSelectedFile());
         app.getLookup().add(source);
         super.onFinish();
      }
      catch (IOException x)
      {
         ErrorDialog.showErrorDialog(this,"Error saving file list",x);
      }
   }
}
