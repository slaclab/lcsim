/*
 *  Class Erf
 * 
 */
package org.lcsim.math.probability;

/**
 *
 * Calculates the following probability integrals:
 *<p>
 *   erf(x) <br>
 *   erfc(x) = 1 - erf(x) <br>
 *   phi(x) = 0.5 * erfc(-x/sqrt(2)) <br>
 *   phic(x) = 0.5 * erfc(x/sqrt(2))
 *<p>
 * Note that phi(x) gives the probability for an observation smaller than x for
 * a Gaussian probability distribution with zero mean and unit standard
 * deviation, while phic(x) gives the probability for an observation larger
 * than x.
 *<p>
 * The algorithms for erf(x) and erfc(x) are based on Schonfelder's work.
 * See J.L. Schonfelder, "Chebyshev Expansions for the Error and Related
 * Functions", Math. Comp. 32, 1232 (1978).  The calculations of phi(x)
 * and phic(x) are trivially calculated using the erfc(x) algorithm.
 *<p>
 * Schonfelder's algorithms are advertised to provide "30 digit" accurracy.
 * Since this level of accuracy exceeds the machine precision for doubles,
 * summation terms whose relative weight is below machine precision are
 * dropped.
 *<p>
 * In this algorithm, we calculate
 *<p>
 *   erf(x) = x* y(t) for |x| < 2 <br>
 *   erf(x) = 1 - exp(-x*x) * y(t) / x for x >= 2 <br>
 *   erfc(|x|) = exp(-x*x)*y(|x|) <br>
 *<p>
 * The functions y(x) are expanded in terms of Chebyshev polynomials, where
 * there is a different set of coefficients a[r] for each of the above 3 cases.
 *<p>
 *   y(x) = Sum'( a[r] * T(r, t) )
 *<p>
 * The notation Sum' indicates that the r = 0 term is divided by 2.
 *<p>
 * The variable t is defined as
 *<p>
 *   t = ( x*x - 2 ) / 2   for erf(x) with x < 2 <br>
 *   t = ( 21 - 2*x*x ) / (5 + 2*x*x)   for erf(x) with x >= 2 <br>
 *   t = ( 4*|x| - 15 ) / ( 4*|x| + 15 )   for erfc(x)
 *<p>
 * The code and implementation are based on Alan Genz's FORTRAN source code
 * that can be found at http://www.math.wsu.edu/faculty/genz/homepage.
 *<p>
 * Genz's code was a bit tricky to "reverse engineer", so we go through the
 * way these calculations are performed in some detail.  Rather than calculate
 * y(x) directly,  he calculates
 *<p>
 *   bm = Sum( a[r] * U(r, t) )    r = 0 : N <br>
 *   bp = Sum( a[r] * U(r-2, t) )  r = 2 : N
 *<p>
 * where U(r, t) are Chebyshev polynomials of the second kind.  The coefficients
 * a[r] decrease with r, and the value of N is chosen where a[N] / a[0] is
 * ~10^-16, reflecting the machine precision for doubles.
 *<p>
 * The Chebyshev polynomials of the second kind U(r, t) are calculated using the
 * recursion relation:
 *<p>
 *   U(r, t) = 2 * t * U(r-1, t) - U(r-2, t)
 *<p>
 * Genz uses the identity
 *<p>
 *   T(r, t) = ( U(r, t) - U(r-2, t) ) / 2
 *<p>
 * to calculate y(x)
 *<p>
 *   y(x) = ( bm - bp ) / 2.
 *<p>
 * Note that we get the correct contributions for the r = 0 and r = 1 terms by
 * ignoring these terms in the bp sum, including getting the desired factor
 * of 1/2 in the contribution from the r = 0 term.
 *
 * @author Richard Partridge
 */
public class Erf {

    private static double rtwo = 1.414213562373095048801688724209e0;

    //  Coefficients for the erf(x) calculation with |x| < 2
    private static double[] a1 = {
        1.483110564084803581889448079057e0,
        -3.01071073386594942470731046311e-1,
        6.8994830689831566246603180718e-2,
        -1.3916271264722187682546525687e-2,
        2.420799522433463662891678239e-3,
        -3.65863968584808644649382577e-4,
        4.8620984432319048282887568e-5,
        -5.749256558035684835054215e-6,
        6.11324357843476469706758e-7,
        -5.8991015312958434390846e-8,
        5.207009092068648240455e-9,
        -4.23297587996554326810e-10,
        3.1881135066491749748e-11,
        -2.236155018832684273e-12,
        1.46732984799108492e-13,
        -9.044001985381747e-15,
        5.25481371547092e-16};

    //  Coefficients for the err(x) calculation with x > 2
    private static double[] a2 = {
      1.077977852072383151168335910348e0,
      -2.6559890409148673372146500904e-2,
      -1.487073146698099509605046333e-3,
      -1.38040145414143859607708920e-4,
      -1.1280303332287491498507366e-5,
      -1.172869842743725224053739e-6,
      -1.03476150393304615537382e-7,
      -1.1899114085892438254447e-8,
      -1.016222544989498640476e-9,
      -1.37895716146965692169e-10,
      -9.369613033737303335e-12,
      -1.918809583959525349e-12,
      -3.7573017201993707e-14,
      -3.7053726026983357e-14,
      2.627565423490371e-15,
      -1.121322876437933e-15,
      1.84136028922538e-16};

    //  Coefficients for the erfc(x) calculation
    private static double[] a3 = {
        6.10143081923200417926465815756e-1,
        -4.34841272712577471828182820888e-1,
        1.76351193643605501125840298123e-1,
        -6.0710795609249414860051215825e-2,
        1.7712068995694114486147141191e-2,
        -4.321119385567293818599864968e-3,
        8.54216676887098678819832055e-4,
        -1.27155090609162742628893940e-4,
        1.1248167243671189468847072e-5,
        3.13063885421820972630152e-7,
        -2.70988068537762022009086e-7,
        3.0737622701407688440959e-8,
        2.515620384817622937314e-9,
        -1.028929921320319127590e-9,
        2.9944052119949939363e-11,
        2.6051789687266936290e-11,
        -2.634839924171969386e-12,
        -6.43404509890636443e-13,
        1.12457401801663447e-13,
        1.7281533389986098e-14,
        -4.264101694942375e-15,
        -5.45371977880191e-16,
        1.58697607761671e-16,
        2.0899837844334e-17,
        -5.900526869409e-18};

    /**
     * Calculate the error function
     * 
     * @param x argument
     * @return error function
     */
    public static double erf(double x) {

        //  Initialize
        double xa = Math.abs(x);
        double erf;

        //  Case 1: |x| < 2
        if (xa < 2.) {

            //  First calculate 2*t
            double tt = x*x - 2.;

            //  Initialize the recursion variables.
            double bm = 0.;
            double b = 0.;
            double bp = 0.;

            //  Calculate bm and bp as defined above
            for (int i = 16; i >= 0; i--) {
                bp = b;
                b = bm;
                bm = tt * b - bp + a1[i];
            }

            //  Finally, calculate erfc using the Chebyshev polynomial identity
            erf = x * (bm - bp) / 2.;

        } else {

            //  Case 2: |x| >= 2

            //  First calculate 2*t
            double tt = (42. - 4 * xa*xa) / (5. + 2 * xa*xa);

            //  Initialize the recursion variables.
            double bm = 0.;
            double b = 0.;
            double bp = 0.;

            //  Calculate bm and bp as defined above
            for (int i = 16; i >= 0; i--) {
                bp = b;
                b = bm;
                bm = tt * b - bp + a2[i];
            }

            //  Finally, calculate erfc using the Chebyshev polynomial identity
            erf = 1. - Math.exp(-x * x) * (bm - bp) / (2. * xa);

            //  Take care of negative argument for case 2
            if (x < 0.) erf = -erf;
        }

        //  Finished both cases!
        return erf;
    }

    /**
     * Calculate the error function complement
     * @param x argument
     * @return error function complement
     */
    public static double erfc(double x) {

        //  Initialize
        double xa = Math.abs(x);
        double erfc;

        //  Set phi to 0 when the argument is too big
        if (xa > 100.) {
            erfc = 0.;
        } else {

            //  First calculate 2*t
            double tt = (8. * xa - 30.) / (4. * xa + 15.);

            //  Initialize the recursion variables.
            double bm = 0.;
            double b = 0.;
            double bp = 0.;

            //  Calculate bm and bp as defined above
            for (int i = 24; i >= 0; i--) {
                bp = b;
                b = bm;
                bm = tt * b - bp + a3[i];
            }

            //  Finally, calculate erfc using the Chebyshev polynomial identity
            erfc = Math.exp(-x * x) * (bm - bp) / 2.;
        }

        //  Cacluate erfc for negative arguments
        if (x < 0.) erfc = 2. - erfc;

        return erfc;
    }

    /**
     * Calcualate the probability for an observation smaller than x for a
     * Gaussian probability distribution with zero mean and unit standard
     * deviation
     * 
     * @param x argument
     * @return probability integral
     */
    public static double phi(double x) {
        return 0.5 * erfc( -x / rtwo);
    }

    /**
     * Calculate the probability for an observation larger than x for a
     * Gaussian probability distribution with zero mean and unit standard
     * deviation
     *
     * @param x argument
     * @return probability integral
     */
    public static double phic(double x) {
        return 0.5 * erfc(x / rtwo);
    }
}

