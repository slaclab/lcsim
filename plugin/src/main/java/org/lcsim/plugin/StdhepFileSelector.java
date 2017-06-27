package org.lcsim.plugin;

import java.io.IOException;
import javax.swing.JOptionPane;
import org.freehep.application.studio.Studio;
import org.freehep.swing.ErrorDialog;
import org.freehep.swing.ExtensionFileFilter;
import org.lcsim.util.loop.StdhepEventSource;

/**
 *
 * @author tonyj
 */
class StdhepFileSelector extends FileSelector
{
   private Studio app;
   /** Creates a new instance of LCSimFileSelector */
   public StdhepFileSelector(Studio app)
   {
      super(app,"Stdhep","stdhep.filelist",new ExtensionFileFilter("stdhep","Stdhep File"),new ExtensionFileFilter("stdhep.filelist","Stdhep File List"));
      this.app = app;
   }
   
   public void onFinish()
   {
      try
      {
         Object detectorName = JOptionPane.showInputDialog(app,"Select detector name","Choose detector geometry",JOptionPane.QUESTION_MESSAGE,null,null,"sidaug05");
         StdhepEventSource source = isMultiFileSelected() ? new StdhepEventSource(getSelectedFiles(),detectorName.toString()) : new StdhepEventSource(getSelectedFile(),detectorName.toString());
         app.getLookup().add(source);
         super.onFinish();
      }
      catch (IOException x)
      {
         ErrorDialog.showErrorDialog(this,"Error saving file list",x);
      }
   }
}
