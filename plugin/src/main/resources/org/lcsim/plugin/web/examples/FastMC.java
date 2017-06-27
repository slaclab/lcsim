import org.lcsim.mc.fast.MCFast;
import org.lcsim.util.Driver;

/**
 * An example that shows how to use the Fast Monte Carlo.
 *
 * @see org.lcsim.mc.fast
 * 
 * @author Norman Graf
 * @version $Id: FastMC.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class FastMC extends Driver
{
   public FastMC()
   {
      // Create MCFast with standard options
      Driver fast = new MCFast();
      // Turn on diagnostic histograms
      fast.setHistogramLevel(HLEVEL_NORMAL);
      // Add as sub-driver
      add(fast);
   }
}
