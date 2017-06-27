package org.lcsim.plugin.browser;

import org.lcsim.event.Cluster;


/**
 *
 * @author tonyj
 */
class ClusterTableModel extends GenericTableModel
{
   private static final String[] columns = {"Type","Energy","Position","ITheta","IPhi","Size"};
   private static Class klass = Cluster.class;

   ClusterTableModel()
   {
      super(klass,columns);
   }
}
