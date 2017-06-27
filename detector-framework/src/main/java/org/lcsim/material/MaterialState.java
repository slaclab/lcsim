package org.lcsim.material;

/**
 * An enumeration class providing MaterialState enums.
 * @author jeremym
 * @version $Id: MaterialState.java,v 1.5 2011/03/11 19:22:20 jeremy Exp $
 */
public class MaterialState
{
    private String _state;

    public final static MaterialState UNKNOWN = new MaterialState("unknown");
    public final static MaterialState GAS = new MaterialState("gas");
    public final static MaterialState LIQUID = new MaterialState("liquid");
    public final static MaterialState SOLID = new MaterialState("solid");

    private MaterialState(String state)
    {
        _state = state;
    }

    /**
     * String representation of MaterialState.
     * @return String representation of this instance.
     */
    public String toString()
    {
        return _state;
    }

    public static MaterialState fromString(String stateStr)
    {
        MaterialState state = MaterialState.UNKNOWN;
        if (stateStr != null)
        {
            if (stateStr.toLowerCase().equals("solid"))
            {
                state = MaterialState.SOLID;
            }
            else if (stateStr.toLowerCase().equals("gas"))
            {
                state = MaterialState.GAS;
            }
            else if (stateStr.toLowerCase().equals("liquid"))
            {
                state = MaterialState.LIQUID;
            }
        }
        return state;
    }
}