package org.lcsim.math.chisq;
/** A utility class to return the probability that a value of chi-squared <b> x </b> measured
 * for a system with <b> n </b>degrees of freedom would be exceeded by chance. Based on routines
 * in the book <em> "Numerical Recipes: The Art of Scientific Computing"</em>.
 * @author Norman Graf
 * @version $Id: ChisqProb.java,v 1.1.1.1 2010/11/30 21:32:00 jeremy Exp $
 *
 */

public class ChisqProb
{
    
    /**Returns the incomplete gamma function P(a, x).
     *
     * <br clear="all" /><table border="0" width="100%"><tr><td>
     * <table align="center"><tr><td nowrap="nowrap" align="center">
     * <i>P</i>(<i>a</i>,<i>x</i>)  <font face="symbol">�</font
     * > </td><td nowrap="nowrap" align="center">
     * <font face="symbol">g</font
     * >(<i>a</i>,<i>x</i>)<hr noshade="noshade" /><font face="symbol">G</font
     * >(<i>a</i>)<br /></td><td nowrap="nowrap" align="center">
     * <font face="symbol">�</font
     * > </td><td nowrap="nowrap" align="center">
     * 1<hr noshade="noshade" /><font face="symbol">G</font
     * >(<i>a</i>)<br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * <font size="-1"><i>x</i></font><!--sup
     * --><br /><font face="symbol">�<br />�<br /></font><font size="-1">0</font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
     * <i>e</i><sup> <font face="symbol">-</font
     * > <i>t</i></sup> <i>t</i><sup><i>a</i> <font face="symbol">-</font
     * > 1</sup> <i>dt</i> </td></tr></table>
     * </td></tr></table>
     *
     * This is the probability that the observed chi-square for a correct
     * model should be less than a value of chi-square.
     *  @param a the number of degrees of freedom
     *  @param x the value of chi-squared
     *  @return The incomplete gamma function P(a, x).
     */
    public static double gammp(double a, double x)
    {
        if (x < 0.0 || a <= 0.0) System.out.println("Invalid arguments in routine gammp");
        if (x < (a+1.0))
        { //Use the series representation.
            return gser(a/2.,x/2.);
        }
        else
        { //Use the continued fraction representation and take its complement.
            return 1.0-gcf(a/2.,x/2.);
        }
    }
    
    /**Returns the incomplete gamma function Q(a, x)= 1 - P(a, x).
     *
     * <br clear="all" /><table border="0" width="100%"><tr><td>
     * <table align="center"><tr><td nowrap="nowrap" align="center">
     * <i>Q</i>(<i>a</i>,<i>x</i>)  <font face="symbol">�</font
     * > 1 <font face="symbol">-</font
     * > <i>P</i>(<i>a</i>,<i>x</i>)  <font face="symbol">�</font
     * > </td><td nowrap="nowrap" align="center">
     * <font face="symbol">G</font
     * >(<i>a</i>,<i>x</i>)<hr noshade="noshade" /><font face="symbol">G</font
     * >(<i>a</i>)<br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * <font size="-1"><font face="symbol">�</font
     * ></font><!--sup
     * --><br /><font face="symbol">�<br />�<br /></font><font size="-1"><i>x</i></font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
     * <i>e</i><sup> <font face="symbol">-</font
     * > <i>t</i></sup> <i>t</i><sup><i>a</i> <font face="symbol">-</font
     * > 1</sup> <i>dt</i> </td></tr></table>
     * </td></tr></table>
     *
     * This is the probability that the observed chi-square
     * will exceed the value of chi-square by chance even for a correct model.
     *  @param a the number of degrees of freedom
     *  @param x the value of chi-squared
     *  @return The incomplete gamma function Q(a, x)= 1 - P(a, x).
     */
    public static double gammq(double a, double x)
    {
        
        if (x < 0.0 || a <= 0.0) System.out.println("Invalid arguments in routine gammq");
        if (x < (a+1.0))
        { //Use the series representation and take its complement.
            return 1.0-gser(a/2.,x/2.);
        }
        else
        { //Use the continued fraction representation.
            return gcf(a/2.,x/2.);
        }
    }
    
    /**Returns the incomplete gamma function P(a, x) evaluated by its series representation.
     *
     * <br clear="all" /><table border="0" width="100%"><tr><td>
     * <table align="center"><tr><td nowrap="nowrap" align="center">
     * <font face="symbol">g</font
     * >(<i>a</i>,<i>x</i>) = <i>e</i><sup> <font face="symbol">-</font
     * > <i>x</i></sup> <i>x</i><sup><i>a</i></sup> </td><td nowrap="nowrap" align="center">
     * <font size="-1"><font face="symbol">�</font
     * ></font><!--sup
     * --><br /><font size="+3"><font face="symbol">�<br />
     * </font></font><font size="-1"><i>n</i> = 0</font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * &nbsp;<font face="symbol">G</font
     * >(<i>a</i>)
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div><font face="symbol">G</font
     * >(<i>a</i> + 1 + <i>n</i>)<br /></td><td nowrap="nowrap" align="center">
     * <i>x</i><sup><i>n</i></sup></td></tr></table>
     * </td></tr></table>
     *
     *  @param a the number of degrees of freedom/2
     *  @param x the value of chi-squared/2
     *  @return The incomplete gamma P(a, x) evaluated by its series representation.
     */
    public static double gser(double a, double x)
    {
        int ITMAX=100;
        double EPS=3.0e-7;
        
        int n;
        double sum,del,ap;
        double gln=gammln(a);
        if (x <= 0.0)
        {
            if (x < 0.0) System.out.println("x less than 0 in routine gser");
            return 0.0;
        }
        else
        {
            ap=a;
            del=sum=1.0/a;
            for (n=1;n<=ITMAX;n++)
            {
                ++ap;
                del *= x/ap;
                sum += del;
                if (Math.abs(del) < Math.abs(sum)*EPS)
                {
                    return sum*Math.exp(-x+a*Math.log(x)-(gln));
                }
            }
            System.out.println("a too large, ITMAX too small in routine gser");
            return 0.;
        }
    }
    
    /**Returns the incomplete gamma function Q(a, x) evaluated by its continued fraction representation.
     *
     * <br clear="all" /><table border="0" width="100%"><tr><td>
     * <table align="center"><tr><td nowrap="nowrap" align="center">
     * <font face="symbol">G</font
     * >(<i>a</i>,<i>x</i>) = <i>e</i><sup> <font face="symbol">-</font
     * > <i>x</i></sup> <i>x</i><sup><i>a</i></sup> </td><td align="left" class="cl"><font face="symbol">
     * �<br />�
     * </font> </td><td nowrap="nowrap" align="center">
     * &nbsp;1
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div><i>x</i> + <br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * &nbsp;1 <font face="symbol">-</font
     * > <i>a</i>
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>1 + <br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * &nbsp;1
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div><i>x</i> + <br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * &nbsp;2 <font face="symbol">-</font
     * > <i>a</i>
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>1 + <br /></td><td nowrap="nowrap" align="center">
     * </td><td nowrap="nowrap" align="center">
     * &nbsp;2
     * <div class="hrcomp"><hr noshade="noshade" size="1"/></div><i>x</i> + <br /></td><td nowrap="nowrap" align="center">
     * <font face="symbol">�</font
     * > </td><td align="left" class="cl"><font face="symbol">
     * �<br />�
     * </font></td><td nowrap="nowrap" align="center">
     * &nbsp;&nbsp;&nbsp;(<i>x</i>  &gt;  0)</td></tr></table>
     * </td></tr></table>
     *
     *  @param a the number of degrees of freedom/2
     *  @param x the value of chi-squared/2
     *  @return The incomplete gamma Q(a, x) evaluated by its continued fraction representation.
     */
    public static double gcf(double a, double x)
    {
        int ITMAX=100; //Maximum allowed number of iteration
        double EPS=3.0e-7; //Relative accuracy.
        double FPMIN=1.0e-30; //Number near the smallest representable doubleing-point number.
        {
            int i;
            double an,b,c,d,del,h;
            double gln=gammln(a);
            b=x+1.0-a; //Set up for evaluating continued fraction by modified Lentz's method with b0 = 0.
            c=1.0/FPMIN;
            d=1.0/b;
            h=d;
            for (i=1;i<=ITMAX;i++)
            { //Iterate to convergence.
                an = -i*(i-a);
                b += 2.0;
                d=an*d+b;
                if (Math.abs(d) < FPMIN) d=FPMIN;
                c=b+an/c;
                if (Math.abs(c) < FPMIN) c=FPMIN;
                d=1.0/d;
                del=d*c;
                h *= del;
                if (Math.abs(del-1.0) < EPS) break;
            }
            if (i > ITMAX) System.out.println("a too large, ITMAX too small in gcf");
            return Math.exp(-x+a*Math.log(x)-(gln))*h; //Put factors in front.
        }
    }
    
    /**Returns the logarithm of the Gamma function of x, for x>0.
     *
     * <br clear="all" /><table border="0" width="100%"><tr><td>
     * <table align="center"><tr><td nowrap="nowrap" align="center">
     * <font face="symbol">G</font
     * >(<i>z</i>) = </td><td nowrap="nowrap" align="center">
     * <font size="-1"><font face="symbol">�</font
     * ></font><!--sup
     * --><br /><font face="symbol">�<br />�<br /></font><font size="-1">0</font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
     * <i>t</i><sup><i>z</i> <font face="symbol">-</font
     * > 1</sup> <i>e</i><sup> <font face="symbol">-</font
     * > <i>t</i></sup> <i>dt</i> </td></tr></table>
     * </td></tr></table>
     *
     *@param x
     *@return The value ln(Gamma(x))
     */
    public static double gammln(double x)
    {
        double y,tmp,ser;
        double[] cof=
        {76.18009172947146,-86.50532032941677,
         24.01409824083091,-1.231739572450155,
         0.1208650973866179e-2,-0.5395239384953e-5};
         int j;
         y=x;
         tmp=x+5.5;
         tmp -= (x+0.5)*Math.log(tmp);
         ser=1.000000000190015;
         for (j=0;j<=5;j++) ser += cof[j]/++y;
         return -tmp+Math.log(2.5066282746310005*ser/x);
    }
}