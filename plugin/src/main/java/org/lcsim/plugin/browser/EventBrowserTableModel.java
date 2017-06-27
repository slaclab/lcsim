package org.lcsim.plugin.browser;

import java.util.List;
import javax.swing.table.TableModel;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * An interface to be implement by object that can display 
 * collections in the event browser.
 * @author tonyj
 */
public interface EventBrowserTableModel extends TableModel
{
   boolean canDisplay(Class c);
   void setData(LCMetaData meta, List data);   
}
