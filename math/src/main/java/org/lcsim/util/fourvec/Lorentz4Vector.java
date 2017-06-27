package org.lcsim.util.fourvec;
/**
 * A Lorentz Four-vector class interface
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public interface Lorentz4Vector
{
    /**
     * Add a four-vector and returns the result.
     * @param y2 Four-vector to add.
     * @return  Resulting four-vector sum.
     */
    public Lorentz4Vector plus(Lorentz4Vector y2);
    
    // sum in place operator
    /**
     * Add a four-vector to this four-vector.
     * @param y1 Four-vector to add.
     */
    public void plusEquals(Lorentz4Vector y1);
    
    /**
     * Add the constituents of a four-vector to this four-vector.
     * @param f1 first element 
     * @param f2 second element
     * @param f3 third element
     * @param f4 fourth element
     */
    public void plusEquals(double f1,double f2,double f3, double f4);
    
    /**
     * Subtract a four-vector and return the result.
     * @param y1  Four-vector to add.
     * @param y2 
     * @return Resulting four-vector difference.
     */
    public Lorentz4Vector minus(Lorentz4Vector y1,Lorentz4Vector y2);
    
    //               multiply by constant

    public Lorentz4Vector times(double a,Lorentz4Vector y);
    

    public Lorentz4Vector times(Lorentz4Vector y,double a);
    
    //               divide by constant

    public Lorentz4Vector divide(Lorentz4Vector y, double a);
    
    //               output operator

    public String toString();
    
    //               dot product

    public double dot(Lorentz4Vector y1);
    
    //             space components  dot product
    public double vec3dot(Lorentz4Vector y1);
    
    public double px();
    
    public double py();

    public double pz();

    public double E();

    public double pT();

    public double p();

    public double phi();

    public double theta();

    public double mass2();
    
    public double mass();
    

    public Lorentz4Vector boost(Lorentz4Vector prest);
}

