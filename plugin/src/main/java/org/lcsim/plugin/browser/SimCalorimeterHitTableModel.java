package org.lcsim.plugin.browser;



import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.geometry.IDDecoder;

/**
 * @author tonyj
 */
class SimCalorimeterHitTableModel extends CellIDTableModel
{
    private static final String[] defaultColumns =
    { "ID", "raw energy (GeV)", "corrected energy (GeV)", "X (mm)", "Y (mm)", "Z (mm)", "time (ns)" };

    SimCalorimeterHitTableModel()
    {
       super(defaultColumns);
    }
    
    public boolean canDisplay(Class c)
    {
        return SimCalorimeterHit.class.isAssignableFrom(c);
    }

    public Class getColumnClass(int row)
    {
        return row < getFieldCount() ? (getIDDecoder() == null) ? Long.class : Integer.class : Double.class;
    }

    public Object getValueAt(int row, int column)
    {
        CalorimeterHit hit = (CalorimeterHit) getHit(row);
        int fieldCount = getFieldCount();

        if (column < fieldCount)
        {
            IDDecoder decoder = getIDDecoder();
            if (decoder != null) decoder.setID(hit.getCellID());
            return decoder == null ? hit.getCellID() : decoder.getValue(column);
        }
        else
        {
            switch (column - fieldCount)
            {
                case 0:
                    return hit.getRawEnergy();
                case 1:
                    double cE = java.lang.Double.NaN;
                    try
                    {
                        cE = hit.getCorrectedEnergy();
                    }
                    catch (Exception e)
                    {
                    }
                    return cE;
                case 2:
                    return hit.getPosition()[0];
                case 3:
                    return hit.getPosition()[1];
                case 4:
                    return hit.getPosition()[2];
                case 5:
                    return hit.getTime();
                default:
                    return " ";
            }
        }
    }
}
