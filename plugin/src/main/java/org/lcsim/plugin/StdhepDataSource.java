package org.lcsim.plugin;

import org.freehep.application.studio.Studio;
import org.freehep.jas.services.DataSource;
import org.freehep.swing.wizard.WizardPage;

/**
 * A data source which allows reading a single file or a set of files.
 * @author tonyj
 */

class StdhepDataSource implements DataSource
{
   private Studio app;
   
   StdhepDataSource(Studio app)
   {
      this.app = app;
   }
   
   public WizardPage getWizardPage()
   {
      return new StdhepFileSelector(app);
   }
   
   public String getName()
   {
      return "Stdhep Files (.stdhep, .stdhep.filelist)";
   }
}
