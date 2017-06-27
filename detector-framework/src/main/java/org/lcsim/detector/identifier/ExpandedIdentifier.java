package org.lcsim.detector.identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IExpandedIdentifier}.
 *
 * @author Jeremy McCormick
 * @version $Id: ExpandedIdentifier.java,v 1.9 2007/11/20 20:30:03 jeremy Exp $
 */

public class ExpandedIdentifier
implements IExpandedIdentifier
{
    List<Integer> values = new ArrayList<Integer>();
    
    public ExpandedIdentifier()
    {}
    
    public ExpandedIdentifier(IExpandedIdentifier id)
    {
    	values.addAll(id.getValues());
    }
    
    public ExpandedIdentifier(ExpandedIdentifier id)
    {
    	values.addAll(id.getValues());
    }
    
    public ExpandedIdentifier copy()
    {
    	return new ExpandedIdentifier(this);
    }
    
    public ExpandedIdentifier( int reserve )
    {
    	if (reserve > 64)
    	{
    		throw new RuntimeException("cannot reserve more than 64 fields");
    	}
    	
    	if (reserve > 0)
    	{
    		for (int i=0; i<reserve; i++)
    		{
    			addValue(0);
    		}
    	}
    }
    
    public ExpandedIdentifier( int[] values )
    {
        for ( int value : values )
        {
            this.values.add( value );
        }
    }
    
    public ExpandedIdentifier( int[] values, int start )
    {
        if ( start >= values.length )
        {
            throw new IllegalArgumentException("Start index <" + start + "> is invalid.");
        }
        
        for ( int i=start; i<values.length; i++ )
        {
            this.values.add( values[start] );            
        }        
    }
    
    public ExpandedIdentifier( List<Integer> values )
    {
        this.values.addAll( values );
    }
    
    public ExpandedIdentifier( List<Integer> values, int start )
    {
        if ( start >= values.size() )
        {
            throw new IllegalArgumentException("Start index<" + start + "> is invalid.");
        }
        
        for ( int i=start; i<values.size(); i++ )
        {
            this.values.add( values.get( i ) );
        }
    }
    
    public ExpandedIdentifier( String values )
    {
        String[] buffer = values.split("/");
        for ( String value : buffer )
        {
            if ( !value.equals("")) 
            {
                addValue( Integer.parseInt( value ) );
            }
        }
    }

    public void addValue( int value)
    {
        values.add(value);        
    }

    public void setValue(int idx, int value)
    {
        values.set(idx,value);
    }

    public void clear()
    {
        values.clear();
    }

    public int size()
    {
        return values.size();
    }

    public int getValue( int index )
    {
        return values.get( index );
    }

    public List<Integer> getValues()
    {
        return values;
    }

    public boolean isValid()
    {
        return values.size() != 0;        
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        for ( Integer value : values )
        {
            buff.append("/" + value);
        }
        return buff.toString();
    }   
    
    public boolean equals(IExpandedIdentifier id)
    {
        if ( id.size() != this.size() )
        {
            return false;
        }
        
        for ( int i=0; i<this.size(); i++)
        {
            if ( id.getValue(i) != this.getValue(i))
            {
                return false;
            }            
        }
        return true;   
    }
    
    public int hashCode()
    {
        return values.hashCode();
    }
    
    public boolean equals(Object object)
    {                
        if (object instanceof IExpandedIdentifier)
        {
            return equals((IExpandedIdentifier)object);
        }
        else
        {
            return false;
        }
    }

    public int match( IExpandedIdentifier id )
    {
        int maxSize = ( size() > id.size() ? id.size() : size() );
        for ( int i=0; i<maxSize; i++ )
        {
            int compare = Integer.valueOf( getValue( i ) ).compareTo( id.getValue( i ) );
            if ( compare != 0 )
            {
                return compare;         
            }
        }
        return 0;
    }

    public int getMaxIndex()
    {
        return size() - 1;
    }
    
    public boolean isValidIndex(int i)
    {
        return i <= getMaxIndex();
    }
     
    public int compareField( IExpandedIdentifier id, int idx )
    {
        if ( idx > getMaxIndex() || idx > id.getMaxIndex() )
        {
            throw new IllegalArgumentException( "The index argument <" + idx + "> is out of range.");
        }
        return Integer.valueOf( getValue( idx ) ).compareTo( id.getValue( idx ) );
    }    
}
