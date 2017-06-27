package org.lcsim.plugin.browser;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.IDDecoder;

/**
 * @author tonyj
 */
class SimTrackerHitTableModel extends CellIDTableModel
{
    private static final String[] defaultColumns =
    { "id", "x (mm)", "y (mm)", "z (mm)", "dEdx (GeV)", "time (ns)", "px (GeV)", "py (GeV)", "pz (GeV)", "pathLength (mm)" };

    SimTrackerHitTableModel()
    {
       super(defaultColumns);
    }
    
    public boolean canDisplay(Class c)
    {
        return SimTrackerHit.class.isAssignableFrom(c);
    }

    public Class getColumnClass(int row)
    {
        return row < getFieldCount() ? Integer.class : Double.class;
    }

    public Object getValueAt(int row, int column)
    {
        SimTrackerHit hit = (SimTrackerHit) getHit(row);
        int fieldCount = getFieldCount();

        if (column < fieldCount)
        {
            IDDecoder decoder = getIDDecoder();
            if (decoder != null) decoder.setID(hit.getCellID());
            return decoder == null ? hit.getCellID() : decoder.getValue(column);
        }
        else
            switch (column - fieldCount)
            {
                case 0:
                    return hit.getPoint()[0];
                case 1:
                    return hit.getPoint()[1];
                case 2:
                    return hit.getPoint()[2];
                case 3:
                    return hit.getdEdx();
                case 4:
                    return hit.getTime();
                case 5:
                    return hit.getMomentum()[0];
                case 6:
                    return hit.getMomentum()[1];
                case 7:
                    return hit.getMomentum()[2];
                case 8:
                    return hit.getPathLength();
                default:
                    return " ";
            }
    }
}
