import org.lcsim.recon.cheater.ReconCheater;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

public class Cheater extends Driver
{
    public Cheater()
    {
        add(new ReconCheater());
    }
    
    protected void process(EventHeader event)
    {
        super.process(event);
    }
}
