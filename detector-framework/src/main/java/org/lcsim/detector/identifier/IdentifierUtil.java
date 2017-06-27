package org.lcsim.detector.identifier;

import java.util.List;

/**
 * Identifier utility methods for packing {@link IIdentifier}s and 
 * unpacking {@link IExpandedIdentifier}s using information from an 
 * {@link IIdentifierDictionary}.
 * 
 * Most of the methods in {@link IIdentifierHelper} use the static 
 * utility functions defined here.
 * 
 * @see org.lcsim.detector.identifier
 * @see IIdentifierDictionary
 * @see IIdentifierField
 * @see IIdentifier
 * @see IExpandedIdentifier
 *
 * @author Jeremy McCormick
 * @version $Id: IdentifierUtil.java,v 1.11 2011/02/25 03:09:38 jeremy Exp $
 */
public final class IdentifierUtil
{    
    /**
     * Class should not be instantiated.  All methods are static.
     */
    private IdentifierUtil()
    {}
               
  /*  public static IExpandedIdentifier unpack( IIdentifierDictionary iddict, IIdentifier compact, int startIndex )
    {     
        return IdentifierUtil.unpack( iddict, compact, startIndex, -1 );
    }
    
    public static IExpandedIdentifier unpack( IIdentifierDictionary iddict, IIdentifier compact )
    {     
        return IdentifierUtil.unpack( iddict, compact, 0 );
    }           
    
    public static IExpandedIdentifier unpack( IIdentifierDictionary iddict, IIdentifier compact, int startIndex, int endIndex )
    {        
        ExpandedIdentifier buffer = new ExpandedIdentifier();
        
        long id = compact.getValue();
        
        int maxIndex = iddict.getMaxIndex();
                
        if ( startIndex > maxIndex )
        {
            throw new RuntimeException("Start index <" + startIndex + "> is not a valid index in <" + iddict.getName() + ">.");
        }
               
        if ( endIndex > iddict.getNumberOfFields() )
        {
            throw new RuntimeException("End index <" + endIndex + "> is not a valid index in <" + iddict.getName() + ">.");
        }
              
        if ( endIndex == -1 )
        {
            endIndex = iddict.getMaxIndex();
        }
        else
        {
            if ( startIndex > endIndex )
            {
                throw new IllegalArgumentException("Start index <" + startIndex + "> is bigger than end index <" + endIndex + ">.");
            }
        }            
                       
        for ( int i=0; i<iddict.getNumberOfFields(); i++)
        {            
            if ( i >= startIndex  && i <= endIndex )
            {                                                
                IIdentifierField field = iddict.getField(i);
                
                int start = field.getOffset();
                int length = field.getNumberOfBits();
                int mask = field.getIntegerMask();
                
                int result = (int) ((id >> start) & mask);
                if (field.isSigned())
                {
                    int signBit = 1<<(length-1);
                    if ((result & signBit) != 0) result -= (1<<length);
                }
                
                buffer.addValue(result);
            }
            else
            {
                buffer.addValue(0);
            }
        }
               
        return buffer;
    }           
    
    public static IExpandedIdentifier unpack(IIdentifierDictionary iddict, IIdentifier compact, List<Integer> indices)
    {        
        ExpandedIdentifier buffer = new ExpandedIdentifier();
        
        long id = compact.getValue();
                                               
        for ( int i=0; i<iddict.getNumberOfFields(); i++)
        {            
            if (indices.contains(i))
            {                                                
                IIdentifierField field = iddict.getField(i);
                
                int start = field.getOffset();
                int length = field.getNumberOfBits();
                int mask = field.getIntegerMask();
                
                int result = (int) ((id >> start) & mask);
                if (field.isSigned())
                {
                    int signBit = 1<<(length-1);
                    if ((result & signBit) != 0) result -= (1<<length);
                }
                
                buffer.addValue(result);
            }
            else
            {
                buffer.addValue(0);
            }
        }
               
        return buffer;
    }
    
    
    
    public static int getValue( IIdentifier compact, IIdentifierField desc )
    {             
        return desc.unpack(compact);
    }
    
    public static int getValue( IIdentifierDictionary iddict, IIdentifier compact, int field )
    {        
        return IdentifierUtil.getValue( compact, iddict.getField( field ) );
    }
    
    public static int getValue( IIdentifierDictionary iddict, IIdentifier compact, String field )
    {
        return IdentifierUtil.getValue( compact, iddict.getField(field) );
    }
    
    public static IIdentifier pack( IIdentifierDictionary iddict, IExpandedIdentifier id, int startIndex, int endIndex )
    {
        long result = 0;
              
        if ( startIndex > iddict.getNumberOfFields() )
        {
            throw new IllegalArgumentException("Start index <" + startIndex + "> is not a valid index in <" + iddict.getName() + ">.");
        }
                                       
        if ( endIndex > iddict.getMaxIndex() )
        {
            throw new IllegalArgumentException("End index <" + endIndex + "> is not a valid index in <" + iddict.getName() + ">.");
        }
        
        if ( endIndex > startIndex )
        {
            throw new IllegalArgumentException("Start index is bigger than end index.");
        }
        
        if ( endIndex == -1 )
        {
            endIndex = iddict.getMaxIndex();
        }
        else
        {
            if ( startIndex > endIndex )
            {
                throw new IllegalArgumentException("Start index <" + startIndex + "> is bigger than end index <" + endIndex + ">.");
            }
        }           
                             
        for ( int i=startIndex; i<=endIndex; i++ )
        {            
            IIdentifierField field = iddict.getField(i);
            
            int value = id.getValue(i);
            if (!field.isValidValue(value))
            {
                throw new IdentifierField.ValueOutOfRangeException( value, field );
            }
            
            int start = field.getOffset(); 
            long mask = field.getLongMask();
            result |= (mask & id.getValue(i)) << start;
        }
        
        return new Identifier( result );
    }
    
    public static IIdentifier pack( IIdentifierDictionary iddict, IExpandedIdentifier id )
    {        
        return IdentifierUtil.pack( iddict, id, 0 );
    }
        
    public static IIdentifier pack( IIdentifierDictionary iddict, IExpandedIdentifier id, int startIndex )
    {
        return IdentifierUtil.pack( iddict, id, startIndex, -1 );
    }    */
}