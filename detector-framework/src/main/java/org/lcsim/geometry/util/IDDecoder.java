package org.lcsim.geometry.util;

/**
 *
 * @author tonyj
 */
public class IDDecoder
{
    private IDDescriptor desc;
    private long id;
    
    /** Creates a new instance of IIDecoder */
    public IDDecoder(IDDescriptor desc)
    {
        this.desc = desc;
    }
    
    public void setID(long id)
    {
        this.id = id;
    }
    
    public long getID()
    {
    	return this.id;
    }
        
    public int getFieldIndex(String name)
    {
        return desc.indexOf(name);
    }
    
    public int getValue(String name)
    {
        return getValue(desc.indexOf(name));
    }
    
    public int getValue(int index)
    {
        int start = desc.fieldStart(index);
        int length = desc.fieldLength(index);
        int mask = (1<<length) - 1;
        
        int result = (int) ((id >> start) & mask);
        if (desc.isSigned(index))
        {
            int signBit = 1<<(length-1);
            if ((result & signBit) != 0) result -= (1<<length);
        }
        return result;
    }
    
    public int[] getValues(int[] buffer)
    {
        if (buffer.length != desc.fieldCount()) throw new IllegalArgumentException("Invalid buffer length");
        for (int i=0; i<buffer.length; i++)
        {
            buffer[i] = getValue(i);
        }
        return buffer;
    }
    
    public int getFieldCount()
    {
        return desc.fieldCount();
    }
    
    public String getFieldName(int index)
    {
        return desc.fieldName(index);
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<desc.fieldCount();)
        {
            sb.append(desc.fieldName(i));
            sb.append(':');
            sb.append(getValue(i));
            if (++i >= desc.fieldCount()) break;
            sb.append(',');
        }
        return sb.toString();
    }
}