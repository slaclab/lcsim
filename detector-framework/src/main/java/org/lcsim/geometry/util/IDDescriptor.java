package org.lcsim.geometry.util;

import java.util.HashMap;
import java.util.Map;

import static org.lcsim.geometry.IDDecoder.INVALID_INDEX;

/**
 * Takes a string of the form:
 *
 * <pre>
 * layer:7,system:3,barrel:3,theta:32:11,phi:11
 * </pre>
 *
 * and generates a parsed description of the fields.
 *
 * @author tonyj
 */
public class IDDescriptor
{
    private int[] start;
    private int[] length;
    private String[] name;
    private Map<String,Integer> nameMap = new HashMap<String,Integer>();
    private int maxBit;
    private int nfields;
    private String description;

    public IDDescriptor(String idDescriptor) throws IDException
    {
        this.description = idDescriptor;
        try
        {
            String[] fields = idDescriptor.split(",");
            //int n = fields.length;
            nfields = fields.length;
            start = new int[nfields];
            length = new int[nfields];
            name = new String[nfields];

            int pos = 0;
            for (int i = 0; i < nfields; i++)
            {
                String[] subFields = fields[i].split(":");
                if (subFields.length < 2 || subFields.length > 3)
                    throw new RuntimeException("Invalid subfield: " + fields[i]);
                name[i] = subFields[0].trim();
                nameMap.put(name[i],i);
                if (subFields.length == 3)
                {
                    start[i] = Integer.parseInt(subFields[1]);
                    if (start[i] < 0)
                        throw new RuntimeException(
                                "Invalid field start position: " + start[i]);
                    length[i] = Integer.parseInt(subFields[2]);
                    if (length[i] == 0)
                        throw new RuntimeException("Invalid field length: "
                                + start[i]);
                }
                else
                {
                    start[i] = pos;
                    length[i] = Integer.parseInt(subFields[1]);
                }
                pos = start[i] + Math.abs(length[i]);
                if (pos > maxBit)
                    maxBit = pos;
            }
        }
        catch (RuntimeException x)
        {
            throw new IDException("Invalid id descriptor: " + idDescriptor, x);
        }
    }

    public int fieldCount()
    {
        return name.length;
    }
    /**
     * Returns the index of the specified field.
     * @throws IllegalArgumentException If the requested field does not exist.
     */
    public int indexOf(String name) throws IllegalArgumentException
    {
        if (nameMap.get(name) != null)
        {
            return nameMap.get(name);
        }
        else
        {
            throw new IllegalArgumentException("Invalid field name: "+name);
        }
    }

    public int fieldStart(int index)
    {
        return start[index];
    }

    public int fieldLength(int index)
    {
        return Math.abs(length[index]);
    }

    public boolean isSigned(int index)
    {
        return length[index] < 0;
    }

    public String fieldName(int index)
    {
        return name[index];
    }

    public int getMaxBit()
    {
        return maxBit;
    }

    public int size()
    {
        return nfields;
    }

    public int maxIndex()
    {
        return nfields - 1;
    }

    public static class IDException extends Exception
    {
        IDException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
    
    public String toString()
    {
        return description;
    }
}
