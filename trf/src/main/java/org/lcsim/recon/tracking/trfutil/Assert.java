package org.lcsim.recon.tracking.trfutil;
/**
 * Assert is modelled on the C++ assert macro.
 * It throws an AssertException if the boolean result of an
 * expression is false.
 * By making AssertException a RuntimeException the client
 * is not forced to use a try-catch block.
 * Therefore we explicitly print the StackTrace when the exception
 * is thrown.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public final class Assert
{
    /**
     * Assert is a wrapper class for its assert method.
     *
     */
    private Assert()
    {
    }
    
    /**
     * This method throws a runtime exception if its argument is false at runtime.
     *
     * @param   result expression which resolves to boolean
     */
    public static void assertTrue( boolean result )
    {
        if ( !result )
        {
            AssertException ex = new AssertException( "Assertion Failed!" );
            ex.printStackTrace();
            throw ex;
        }
    }
}
