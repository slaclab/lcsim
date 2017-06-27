package org.lcsim.recon.cluster.localequivalence;

import static java.lang.Math.abs;
import java.util.List;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.event.Cluster;
import org.lcsim.event.base.BaseCluster;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.math.moments.CentralMomentsCalculator;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * Simplest Analysis Driver showing how to access list of clusters 
 * from the event.
 * 
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class NNAlgoClusterAnalysisDriver extends Driver
{

    private AIDA _aida = AIDA.defaultInstance();

    @Override
    protected void process(EventHeader event)
    {
        try
        {
            List<Cluster> clusters = event.get(Cluster.class, "BeamCalHitsEMClusters");
            if (clusters != null)
            {
                for (Cluster c : clusters)
                {
                    _aida.cloud1D("Cluster Energy").fill(c.getEnergy());
                }
            }
        } catch (Exception e)
        {

        }
    }
}
