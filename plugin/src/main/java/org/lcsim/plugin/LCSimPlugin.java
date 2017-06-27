package org.lcsim.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import org.freehep.application.mdi.PageContext;
import org.freehep.application.mdi.PageEvent;
import org.freehep.application.mdi.PageListener;
import org.freehep.application.mdi.PageManager;
import org.freehep.application.studio.Plugin;
import org.freehep.application.studio.Studio;
import org.freehep.application.studio.StudioListener;
import org.freehep.jas.event.ClassLoadEvent;
import org.freehep.jas.event.ClassLoadedEvent;
import org.freehep.jas.event.ClassUnloadEvent;
import org.freehep.jas.event.ScriptEvent;
import org.freehep.jas.plugin.console.ConsoleOutputStream;
import org.freehep.jas.plugin.console.ConsoleService;
import org.freehep.jas.services.ScriptEngine;
import org.freehep.jas.services.WebBrowser;
import org.freehep.record.loop.LoopEvent;
import org.freehep.record.loop.LoopListener;
import org.freehep.record.loop.RecordLoop;
import org.freehep.util.FreeHEPLookup;
import org.freehep.util.commanddispatcher.CommandProcessor;
import org.freehep.util.commanddispatcher.CommandState;
import org.freehep.util.template.Template;
import org.freehep.xml.menus.XMLMenuBuilder;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.event.EventHeader;
import org.lcsim.event.util.LCSimEventGenerator;
import org.lcsim.plugin.conditions.InteractiveConditionsManagerImplementation;
import org.lcsim.plugin.browser.LCSimEventBrowser;
import org.lcsim.util.Driver;
import org.lcsim.util.DriverAdapter;
import org.lcsim.util.loop.EventGeneratorRecordSource;
import org.xml.sax.SAXException;

/**
 *
 * @author tonyj
 * @version $Id: LCSimPlugin.java,v 1.15 2012/06/15 05:24:20 onoprien Exp $
 */

public class LCSimPlugin extends Plugin implements StudioListener, PageListener
{
    private LCSim lcsim;
    private final LCSimCommands commands = new LCSimCommands();
    private JToolBar toolbar;
    private ConsoleOutputStream out;
    private ConsoleService cs;
    private static final Logger logger = Logger.getLogger("org.lcsim.plugin");
    
    protected void init() throws SAXException, IOException
    {        
        Studio app = getApplication();
        FreeHEPLookup lookup = app.getLookup();
        lcsim = new LCSim(lookup);
        
        XMLMenuBuilder builder = app.getXMLMenuBuilder();
        URL xml = getClass().getResource("LCSim.menus");
        builder.build(xml);
        
        toolbar = builder.getToolBar("lcsim");
        app.addToolBar(toolbar, toolbar.getName());
                
        lookup.add(new LCSimFileHandler(app),"org.lcsim Plugin");
        lookup.add(new LCSimFileListHandler(app),"org.lcsim Plugin");
        lookup.add(new LCSimDataSource(app));
        lookup.add(new StdhepFileHandler(app),"org.lcsim Plugin");
        lookup.add(new StdhepFileListHandler(app),"org.lcsim Plugin");
        lookup.add(new StdhepDataSource(app));
        
        // Listen for any drivers to be loaded
        app.getEventSender().addEventListener(this,ClassLoadEvent.class);
        // Register to receive scriptEvents
        app.getEventSender().addEventListener(this, ScriptEvent.class);
        
        // Register the command processsor
        app.getCommandTargetManager().add(commands);
        
        LCSimEventBrowser.registerTableModels(app.getLookup());
        
        Template map = new Template();
        map.set("title","org.lcsim Examples");
        map.set("url","classpath:/org/lcsim/plugin/web/examples.html");
        map.set("description","Examples of using the org.lcsim package");
        lookup.add(map,"examples");
        
        ConditionsManager.setDefaultConditionsManager(new InteractiveConditionsManagerImplementation(app));
        
        logger.finest("LCSim plugin created");
    }
    public boolean canBeShutDown()
    {
        return true;
    }

    protected void shutdown()
    {
        Studio app = getApplication();
        FreeHEPLookup lookup = app.getLookup();
        
        lookup.remove(lookup.lookup(LCSimFileHandler.class));
        lookup.remove(lookup.lookup(StdhepFileHandler.class));
        
        app.getEventSender().removeEventListener(this,ClassLoadEvent.class);
        app.getEventSender().removeEventListener(this,ClassLoadEvent.class);   
        
        app.getCommandTargetManager().remove(commands);
        lookup.remove(lookup.lookup(Template.class));
        
        app.removeToolBar(toolbar);
        
        RecordLoop loop = (RecordLoop) getApplication().getLookup().lookup(RecordLoop.class);
        loop.removeLoopListener(commands);
    }
    protected void postInit()
    {
        try
        {
            Class converterClass = Class.forName("org.lcsim.util.heprep.LCSimHepRepConverter");
            Object converter = converterClass.newInstance();
            getApplication().getLookup().add(converter);
        }
        // This block will catch NoClassDefFoundError (which is not an Exception) as well as other reflection errors that may occur.  --JM
        catch (Throwable t)        
        {        	
        	t.printStackTrace(System.err);
            System.err.println("WARNING: Unable to create LCSimHepRepConverter.  The HepRep plugin is probably not installed.");
        }

        RecordLoop loop = (RecordLoop) getApplication().getLookup().lookup(RecordLoop.class);
        loop.addLoopListener(commands);
        
        try
        {
           Studio app = getApplication();
           FreeHEPLookup lookup = app.getLookup();
           cs = (ConsoleService) lookup.lookup(ConsoleService.class);
           if (cs != null) out = cs.getConsoleOutputStream("Record Loop", null);
        }
        catch (IOException x)
        {
           System.err.println("Warning: Unable to create console output stream");
        }
    }
    public void handleEvent(EventObject event)
    {
        if (event instanceof ClassLoadedEvent)
        {
            Class x = ((ClassLoadedEvent) event).getLoadedClass();
            try
            {
                if (Driver.class.isAssignableFrom(x))
                {
                   logger.fine("Creating new instance of Driver "+x.getName());
                   if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
                   Driver driver = (Driver) x.newInstance();
                   if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),null);
                   
                   logger.fine("Creating Driver adapter for driver "+x.getName());
                   DriverAdapter listener = new DriverAdapter(driver, cs);
                   getApplication().getLookup().add(listener);
                }
                else if (LCSimEventGenerator.class.isAssignableFrom(x))
                {
                    logger.fine("Creating new instance of LCSimEventGenerator "+x.getName());
                    if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);
                    LCSimEventGenerator gen = (LCSimEventGenerator) x.newInstance();
                    if (cs != null) cs.redirectStandardOutputOnThreadToConsole(Thread.currentThread(),out);

                    String name = x.getName();
                    int pos = name.lastIndexOf('.');
                    if (pos >= 0) name = name.substring(pos+1);
                    logger.fine("Creating EventGeneratorRecordSource for generator "+x.getName());
                    EventGeneratorRecordSource source = new EventGeneratorRecordSource(gen,name);
                    getApplication().getLookup().add(source);
                }
            }
            catch (InstantiationException xx)
            {
                getApplication().error("Error instantiating "+x.getName(),xx);
            }
            catch (ExceptionInInitializerError xx)
            {
                getApplication().error("Error instantiating "+x.getName(),xx.getException());
            }
            catch (IllegalAccessException xx)
            {
                getApplication().error("Could not create class "+x.getName()+", missing public constructor?",xx);
            }
            catch (Exception xx)
            {
                getApplication().error("Error instantiating "+x.getName(),xx);
            }
        }
        else if (event instanceof ClassUnloadEvent)
        {
            // Record loop takes care of this
        }
        else if (event instanceof  ScriptEvent)
        {
            ScriptEngine engine = ((ScriptEvent) event).getScriptEngine();
            engine.registerVariable("lcsim", lcsim);
        }
    }
    public void pageChanged(PageEvent e)
    {
        PageContext page = (PageContext) e.getSource();        
        if (e.getID() == e.PAGESELECTED)
        {
            if ( page.getPage() instanceof LCSimEventBrowser )
            {
                getApplication().getCommandTargetManager().add(((LCSimEventBrowser) page.getPage()).getCommands());
            }
        }
        else if (e.getID() == e.PAGEDESELECTED)
        {
            if ( page.getPage() instanceof LCSimEventBrowser )
            {
                getApplication().getCommandTargetManager().remove(((LCSimEventBrowser) page.getPage()).getCommands());
            }
        }
    }
    
    class LCSimCommands extends CommandProcessor implements LoopListener
    {
        public void onLCSimEventBrowser()
        {
            createNewEventBrowser();
            setChanged();
        }
        private void createNewEventBrowser()
        {
            RecordLoop loop = (RecordLoop) getApplication().getLookup().lookup(RecordLoop.class);
            LCSimEventBrowser tree  = new LCSimEventBrowser(getApplication(),loop);
            getApplication().getPageManager().openPage(tree,"LCSim Event",null);           
        }
        
        private void createNewDriverTree()
        {
            RecordLoop loop = (RecordLoop) getApplication().getLookup().lookup(RecordLoop.class);
            LCSimDriverTree tree  = new LCSimDriverTree(getApplication(),loop);
            getApplication().getPageManager().openPage(tree,"LCSim Drivers",null);           
        }
        
        public void enableLCSimEventBrowserButton(CommandState state)
        {
            RecordLoop loop = (RecordLoop) getApplication().getLookup().lookup(RecordLoop.class);
            try
            {
               Object record = loop.getRecordSource().getCurrentRecord();
               state.setEnabled(record instanceof EventHeader);
            }
            catch (Exception x)
            {
               state.setEnabled(false);
            }
        }
        
        public void onLCSimEventBrowserButton()
        {
            PageManager manager = getApplication().getPageManager();
            PageContext selected = manager.getSelectedPage(); 
            if (selected != null && selected.getPage() instanceof LCSimEventBrowser)
            {
               createNewEventBrowser();
            }
            else
            {
               boolean found = false;
               List<PageContext> pages = manager.pages();
               for (PageContext page : pages)
               {
                  if (page.getPage() instanceof LCSimEventBrowser)
                  {
                     page.requestShow();
                     found = true;
                     break;
                  }
               }
               if (!found) createNewEventBrowser();
            }
            setChanged();
        }
        
        public void onLCSimDriverTree()
        {
            PageManager manager = getApplication().getPageManager();
            PageContext selected = manager.getSelectedPage(); 
            if (selected != null && selected.getPage() instanceof LCSimDriverTree)
            {
               createNewDriverTree();
            }
            else
            {
               boolean found = false;
               List<PageContext> pages = manager.pages();
               for (PageContext page : pages)
               {
                  if (page.getPage() instanceof LCSimDriverTree)
                  {
                     page.requestShow();
                     found = true;
                     break;
                  }
               }
               if (!found) createNewDriverTree();
            }
            setChanged();
        }
        
        public void onLCSimHelp()
        {
            URL help = getClass().getResource("web/index.html");
            WebBrowser wb = (WebBrowser) getApplication().getLookup().lookup(WebBrowser.class);
            if (wb != null && help != null) wb.showURL(help);
        }

      public void process(LoopEvent event) {
        switch (event.getEventType()) {
          case SUSPEND:
          case RESET:
            setChanged();
          default:
        }
      }

//      public void loopEnded(LoopEvent loopEvent)
//      {
//          setChanged();
//      }
//
//      public void loopReset(LoopEvent loopEvent)
//      {
//          setChanged();
//      }

    }
}