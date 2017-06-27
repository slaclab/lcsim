package org.lcsim.detector.identifier;

/**
 * Implementation of {@link IIdentifierField}.
 * 
 * @author Jeremy McCormick
 * @version $Id: IdentifierField.java,v 1.9 2011/02/25 03:09:38 jeremy Exp $
 */

public class IdentifierField implements IIdentifierField
{
    private String label;
    private int numberOfBits;
    private int offset;
    private boolean isSigned;
    private int maxValue;
    private int minValue;
    private long longMask;
    private int intMask;

    private static final int MAX_OFFSET = 64;
    private static final int MIN_OFFSET = 0;
    private static final int MAX_BITS = 32;
    private static final int MIN_BITS = 1;

    public static class ValueOutOfRangeException extends RuntimeException
    {
        public ValueOutOfRangeException(int value, IIdentifierField field)
        {
            super("The value <" + value + "> is outside range <" + field.getMinValue() + ", " + field.getMaxValue()
                    + "> of field <" + field.getLabel() + "> which has <" + field.getNumberOfBits() + "> bits.");
        }
    }

    public IdentifierField(String label, int numberOfBits, int offset, boolean isSigned)
    {
        if (label == null)
        {
            throw new IllegalArgumentException(label + " - label is null.");
        }

        this.label = label;

        if (numberOfBits > MAX_BITS || numberOfBits < MIN_BITS)
        {
            throw new IllegalArgumentException(label + " - number of bits is not between " + MIN_BITS + " and "
                    + MAX_BITS);
        }

        this.numberOfBits = numberOfBits;

        if (offset > MAX_OFFSET || offset < MIN_OFFSET)
        {
            throw new IllegalArgumentException(label + " - offset <" + offset + "> is not between " + MIN_OFFSET
                    + " and " + MAX_OFFSET);
        }

        if ((offset + numberOfBits) > MAX_OFFSET)
        {
            throw new IllegalArgumentException(label + " - offset + numberOfBits <" + (offset + numberOfBits)
                    + "> is greater than " + MAX_OFFSET);
        }

        if (isSigned && numberOfBits < 2)
        {
            throw new IllegalArgumentException("The signed field " + label + " needs at least 2 bits.");
        }

        this.offset = offset;
        this.isSigned = isSigned;

        this.intMask = (1 << numberOfBits) - 1;
        this.longMask = ((1L << numberOfBits) - 1);

        // Range for unsigned field.
        if (!isSigned())
        {
            maxValue = ((int)Math.pow(2, getNumberOfBits())) - 1;
            minValue = 0;
        }
        // Range for signed field.
        else
        {
            // In a signed field, one bit is reserved for the sign bit.
            maxValue = ((int)Math.pow(2, getNumberOfBits() - 1)) - 1;
            minValue = -maxValue;
        }
    }

    public String getLabel()
    {
        return label;
    }

    public int getNumberOfBits()
    {
        return numberOfBits;
    }

    public int getOffset()
    {
        return offset;
    }

    public boolean isSigned()
    {
        return isSigned;
    }

    public String toString()
    {
        return getLabel() + ":" + getOffset() + ":" + getNumberOfBits() + ":" + isSigned() + "\n";
    }

    // FIXME: Move to IdentifierUtil.
    public int unpack(long value)
    {
        int start = getOffset();
        int length = getNumberOfBits();
        int mask = getIntegerMask();

        int result = (int)((value >> start) & mask);
        if (isSigned())
        {
            int signBit = 1 << (length - 1);
            if ((result & signBit) != 0)
                result -= (1 << length);
        }

        return result;
    }

    // FIXME: Move to IdentifierUtil.
    public int unpack(IIdentifier compact)
    {
        return unpack(compact.getValue());
    }

    // FIXME: Move to IdentifierUtil.
    public IIdentifier pack(int value)
    {
        //if (!isValidValue(value))
        //    throw new ValueOutOfRangeException(value, this);
        long result = 0;
        int start = getOffset();
        long mask = getLongMask();
        result |= (mask & value) << start;
        return new Identifier(result);
    }

    // FIXME: Move to IdentifierUtil.
    public void pack(int value, IIdentifier id)
    {
        //if (!isValidValue(value))
        //    throw new ValueOutOfRangeException(value, this);
        long result = id.getValue();
        int start = getOffset();
        long mask = getLongMask();
        result |= (mask & value) << start;
        id.setValue(result);
    }

    public int getMaxValue()
    {
        return maxValue;
    }

    public int getMinValue()
    {
        return minValue;
    }

    public int getIntegerMask()
    {
        return this.intMask;
    }

    public long getLongMask()
    {
        return this.longMask;
    }

    public boolean isValidValue(int value)
    {
        return value <= getMaxValue() && value >= getMinValue();
    }
}