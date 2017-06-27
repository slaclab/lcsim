package org.lcsim.geometry.subdetector;

/**
 * 
 * @author Jeremy McCormick
 * @version $Id: BarrelEndcapFlag.java,v 1.5 2010/11/30 00:16:29 jeremy Exp $
 */
public enum BarrelEndcapFlag
{
    UNKNOWN( -1 ), BARREL( 0 ), ENDCAP_NORTH( 1 ), ENDCAP_SOUTH( 2 ), ENDCAP( 3 );

    private int flag;

    BarrelEndcapFlag( int flag )
    {
        if ( flag < -1 || flag > 3 )
        {
            throw new IllegalArgumentException( "Bad BarrelEndcapFlag value: " + flag );
        }

        this.flag = flag;
    }

    public boolean isBarrel()
    {
        return this == BARREL;
    }

    public boolean isEndcap()
    {
        return flag > 0;
    }

    public boolean isEndcapNorth()
    {
        return this == ENDCAP_NORTH;
    }

    public boolean isEndcapSouth()
    {
        return this == ENDCAP_SOUTH;
    }

    public int getFlag()
    {
        return flag;
    }

    public static BarrelEndcapFlag createBarrelEndcapFlag( int flag )
    {
        if ( flag == 0 )
        {
            return BARREL;
        }
        else if ( flag == 1 )
        {
            return ENDCAP_NORTH;
        }
        else if ( flag == 2 )
        {
            return ENDCAP_SOUTH;
        }
        else if ( flag == -1 )
        {
            return UNKNOWN;
        }
        else
        {
            throw new IllegalArgumentException( "Bad flag value: " + flag );
        }
    }
};
