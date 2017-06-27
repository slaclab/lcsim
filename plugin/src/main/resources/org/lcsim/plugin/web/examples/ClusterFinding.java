import java.util.List;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.recon.cluster.nn.NearestNeighborClusterDriver;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * An example that shows how to find clusters and make some simple plots
 * of the results.
 * 
 * @author Norman Graf
 * @version $Id: ClusterFinding.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 * 
 */
public class ClusterFinding extends Driver
{
//
// Use the "convenient" method of generating AIDA plots 
//
   private AIDA aida = AIDA.defaultInstance();
   
   public ClusterFinding()
   {
//
//    Add a cluster Driver with required parameters
//
      int minCells = 5;
      add(new NearestNeighborClusterDriver(minCells));
   }
//
// Process an event
//
   protected void process(EventHeader event)
   {
//
// Make clusters
//
      super.process(event);
//      
// Find all the cluster Lists
//
      List<List<Cluster>> clusterSets = event.get(Cluster.class);
      aida.cloud1D("clusterSets").fill(clusterSets.size());
//
// Loop over all the cluster Lists
//     
      for (List<Cluster> clusters : clusterSets)
      {
//
// Get the ClusterList name
//
         String name = event.getMetaData(clusters).getName() + "/";
//
// Histogram the number of clusters in the List
//
         aida.cloud1D(name+"clusters").fill(clusters.size());
//
// Loop over all the clusters in a List
//
         for (Cluster cluster : clusters)
         {
//
// Histogram the "corrected" energy
//
            aida.cloud1D(name+"energy").fill(cluster.getEnergy());
//
// Histogram the position as R vs Z
//
            double[] pos = cluster.getPosition();
            double R = Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1]);
            aida.cloud2D(name+"Position:R vs Z").fill(pos[2],R);
//
// Histogram the computed direction
//
            aida.cloud1D(name+"Direction: theta").fill(cluster.getITheta());
            aida.cloud1D(name+"Direction: phi").fill(cluster.getIPhi());
//
// Histogram the difference in direction and position theta,phi 
//
            double posphi = Math.atan2(pos[1],pos[0]);
            aida.cloud1D(name+"delta phi").fill(posphi - cluster.getIPhi());
            double postheta = Math.PI/2. - Math.atan2(pos[2],R);
            aida.cloud1D(name+"delta theta").fill(postheta - cluster.getITheta());
         }
      }
   }
}
