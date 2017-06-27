package org.lcsim.recon.tracking.trfutil;
/**
 * An AssertException is modelled on the C++ assert macro
 * which is indispensable for testing software components.
 */
public class AssertException extends RuntimeException
{
    /**
     * Constructs an AssertException with no detail message.
     */
    public AssertException()
    {
        super();
    }
    /** Constructs an AssertException with the specified detail message
     * @param str Assertion detail
     */
    public AssertException(String str)
    {
        super( str );
    }
}