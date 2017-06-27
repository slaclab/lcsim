package org.lcsim.plugin.conditions;

import java.awt.Frame;
import javax.swing.SwingUtilities;
import org.freehep.application.Application;
import org.freehep.application.studio.Studio;
import org.freehep.swing.wizard.WizardDialog;
import org.freehep.swing.wizard.WizardPage;
import org.lcsim.conditions.*;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.util.loop.LCSimConditionsManagerImplementation;

/**
 * Extends the default ConditionsManager to add interactive prompting for detector conditions
 * @author tonyj
 * @version $Id: InteractiveConditionsManagerImplementation.java,v 1.3 2007/09/11 00:21:02 tonyj Exp $
 */
public class InteractiveConditionsManagerImplementation extends LCSimConditionsManagerImplementation
{
   private Studio app;
   private int run;
   private boolean newDetectorSet = false;
   
   /** Creates a new instance of InteractiveConditionsManagerImplementation */
   public InteractiveConditionsManagerImplementation(Studio app)
   {
      super();
      this.app = app;
   }
   
   public void setDetector(String name, int run) throws ConditionsManager.ConditionsNotFoundException
   {
      try
      {
         super.setDetector(name, run);
      }
      catch (ConditionsManager.ConditionsNotFoundException x)
      {
         this.run = run;
         displayWizardPage(name);
         if (!newDetectorSet) throw x;
      }
   }
   private void displayWizardPage(String name)
   {
      Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class,app);
      WizardDialog wizard = new AppWizardDialog(frame,"Open Data Source...", new ConditionsWizardPage(this,name));
      wizard.pack();
      wizard.setLocationRelativeTo(app);
      wizard.setVisible(true);
   }
   void setDetectorFound(boolean found)
   {
      this.newDetectorSet = found;
   }
   void addAlias(String alias, String target) throws ConditionsNotFoundException
   {
      ConditionsReader.addAlias(alias,target);
      super.setDetector(alias,run);
      setDetectorFound(true);
   }

   Studio getStudio()
   {
      return app;
   }

   private class AppWizardDialog extends WizardDialog
   {
      AppWizardDialog(Frame frame, String title, WizardPage firstPage)
      {
         super(frame,title,firstPage);
      }
      
      protected void handleError(String message, Throwable t)
      {
         Application.error(this,message,t);
      }
      
   }
}
