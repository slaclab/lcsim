package org.lcsim.detector.identifier;

/**
 * Implementation of {@link IIdentifier}.
 * 
 * @author Jeremy McCormick
 * @version $Id: Identifier.java,v 1.8 2007/11/20 20:30:03 jeremy Exp $
 */
public class Identifier
implements IIdentifier
{    
    long value;
    boolean valid = false;
    boolean garbage = false;
    
    public Identifier()
    {
        garbage = true;
    }
    
    public Identifier(IIdentifier id)
    {
    	value = id.getValue();
    	valid = id.isValid();
    }
    
    public Identifier(Identifier id)
    {
    	value = id.getValue();
    	valid = id.isValid();
    }
    
    public Identifier copy()
    {
    	return new Identifier(this);
    }
    
    public Identifier(long value)
    {
        setValue(value);
    }
    
    public Identifier(int value)
    {
    	setValue(value);
    }
    
    public long getValue()
    {
        return value;
    }
    
    public void setValue( long value )
    {
        this.value = value;       
        valid = true;
    }
    
    public void clear()
    {
         value = 0;
         valid = false;
    }
 
    public String toHexString()
    {
        return Long.toHexString(value);
    }
    
    public String toString()
    {
        return toHexString();
    }
    
    public void fromHexString( String hexRep )
    {
        value = Long.parseLong(hexRep, 16);
    }

    public boolean isValid()
    {
        return valid;
    }

    public int compareTo( Object object )
    {                
        if ( object instanceof IIdentifier )
        {
            return Long.valueOf(((IIdentifier)object).getValue()).compareTo(getValue());
        }
        else if ( object instanceof Long)
        {
            return ((Long)object).compareTo(getValue());
        }
        else if ( object instanceof Integer )
        {
            return ((Long)object).compareTo(getValue());
        }
        else 
        {
            return -1;
        }        
    }    
    
    public int hashCode()
    {
        return Long.valueOf(value).hashCode();
    }
    
    public boolean equals( Object object )
    {
        return compareTo( object ) == 0;
    }
    
    public boolean getGarbage() {
        return garbage;
    }
    
}
