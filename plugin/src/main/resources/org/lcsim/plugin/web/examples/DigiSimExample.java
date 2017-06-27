import org.lcsim.recon.cluster.util.CalHitMapDriver;
import org.lcsim.digisim.DigiSimDriver;
import org.lcsim.digisim.CalorimeterHitsDriver;
import org.lcsim.digisim.SimCalorimeterHitsDriver;
import org.lcsim.util.Driver;

/**
 * An example showing how to use the DigiSim package. 
 * 
 * @see org.lcsim.digisim
 *
 * @author Guilherme Lima
 * @version $Id: DigiSimExample.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class DigiSimExample extends Driver
{
   public DigiSimExample()
   {
      // CalHitMapDriver is needed by DigiSim
      add(new CalHitMapDriver());

      // DigiSim: SimCalHits -> RawCalHits
      _digi = new DigiSimDriver();

      // Turn on diagnostic histograms
//       _digi.setHistogramLevel(1);

      // Choose a steering file
//       _digi.setSteeringFile("minimal.steer");

      // Set some modifiers to debug mode.
      // Format is "A:B", where A is digitizer name and B is modifier name
//       _digi.setDebug("EcalBarrDigitizer:EMBDigiIdentity", 1);
//       _digi.setDebug("EcalEndcapDigitizer:EMECDigiIdentity", 1);
//       _digi.setDebug("HcalBarrDigitizer:HBDigiIdentity", 1);
//       _digi.setDebug("HcalEndcapDigitizer:HECDigiIdentity", 1);

      add(_digi);

      // RawCalHits -> CalorimeterHits
//       add( new CalorimeterHitsDriver() );

      // RawCalHits -> SimCalorimeterHits
      add( new SimCalorimeterHitsDriver() );
   }

    public void setSteeringFile(String file) {
	_digi.setSteeringFile(file);
    }

    DigiSimDriver _digi;
}
