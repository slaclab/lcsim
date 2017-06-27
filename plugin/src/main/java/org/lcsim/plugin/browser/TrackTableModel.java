package org.lcsim.plugin.browser;
import org.lcsim.event.Track;


/**
 *
 * @author tonyj
 */
class TrackTableModel extends GenericTableModel
{
   private static final String[] columns = {"Type", "D0", "Phi", "Omega", "Z0", "TanLambda", "Track States", "Chi2", "NDF", "dEdx"};
       //, "Momentum"};
   private static Class klass = Track.class;

   TrackTableModel()
   {
       super(klass,columns);
   }

   public Object getValueAt(int row, int column)
   {
       if (column == 0)
       {
           // Track type (???).
           return ((Track)getData(row)).getType();
       }
       else if (column >= 1 && column <= 5)
       {
           // Displays data about first TrackState, only.
           return ((Track)getData(row)).getTrackStates().get(0).getParameter(column-1);
      }
      else if (column == 6)
      {
          // Number of total TrackStates.
          return ((Track)getData(row)).getTrackStates().size();
      }
      else 
      {
          return super.getValueAt(row,column);
      }
   }
   
   public Class getColumnClass(int column)
   {
       if (column > 0 && column <= 5) return Double.class;
       else return super.getColumnClass(column);
   }
}