import java.io.File;
import org.lcsim.mc.fast.MCFast;
import org.lcsim.util.Driver;
import org.lcsim.util.loop.LCIODriver;

/**
 * A example of writing LCIO output.
 * 
 * @see org.lcsim.util.loop.LCIODriver
 * 
 * @author Tony Johnson
 * @version $Id: LCIOOutput.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class LCIOOutput extends Driver
{
   public LCIOOutput()
   {
      // Create MCFast with standard options
      add(new MCFast());
      // Write the file in users home directory
      File output = new File(System.getProperty("user.home"),"fastmc.slcio");
      add(new LCIODriver(output));
   }
}
