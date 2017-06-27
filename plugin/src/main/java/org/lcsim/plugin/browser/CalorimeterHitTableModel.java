package org.lcsim.plugin.browser;



import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.geometry.IDDecoder;

/**
 * @author tonyj
 */
class CalorimeterHitTableModel extends CellIDTableModel
{
    private static final String[] defaultColumns =
    { "id", "type", "raw E (GeV)", "corr E (GeV)", "E error", "X (mm)", "Y (mm)", "Z (mm)", "time (ns)" };

    CalorimeterHitTableModel()
    {
       super(defaultColumns);
    }
    
    public boolean canDisplay(Class c)
    {
        if(SimCalorimeterHit.class.isAssignableFrom(c)) return false;
        return CalorimeterHit.class.isAssignableFrom(c);
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
                    return hit.getType();
                case 1:
                    try {
                        return hit.getRawEnergy();
                    } catch (Exception e) {
                        return 0.;
                    }
                case 2:
                    return hit.getCorrectedEnergy();
                case 3:
                    return hit.getEnergyError();
                case 4:
                    return hit.getPosition()[0];
                case 5:
                    return hit.getPosition()[1];
                case 6:
                    return hit.getPosition()[2];
                case 7:
                    return hit.getTime();
                default:
                    return " ";
            }
        }
    }
}
