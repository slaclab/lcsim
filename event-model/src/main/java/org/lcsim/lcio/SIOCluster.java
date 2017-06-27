package org.lcsim.lcio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;

/**
 * 
 * @author tonyj
 */
public class SIOCluster implements Cluster
{
    private final static double[] dummy = new double[6];
    private int type;
    private float energy;
    private float energyError;
    private double raw_energy;
    private double[] position;
    private double[] positionError;
    private float theta;
    private float phi;
    private double[] directionError;
    private double[] shape;
    private List<Cluster> clusters;
    private List<CalorimeterHit> calorimeterHits;
    private double[] hitContributions;
    private double[] subdetectorEnergies;
    private List<SIORef> tempHits;
    private List<SIORef> tempClusters;

    SIOCluster(SIOInputStream in, int flag, int version) throws IOException
    {
        type = in.readInt();
        energy = in.readFloat();
        if (version > 1051)
        {
            energyError = in.readFloat();
        }
        position = new double[3];
        for (int i = 0; i < 3; i++)
            position[i] = in.readFloat();
        positionError = new double[6];
        for (int i = 0; i < 6; i++)
            positionError[i] = in.readFloat();

        theta = in.readFloat();
        phi = in.readFloat();
        directionError = new double[3];
        for (int i = 0; i < 3; i++)
            directionError[i] = in.readFloat();

        int nShape;
        if (version > 1002)
        {
            nShape = in.readInt();
        }
        else
        {
            nShape = 6;
        }
        shape = new double[nShape];
        for (int i = 0; i < nShape; i++)
        {
            shape[i] = in.readFloat();
        }

        if (version > 1002)
        {
            int nPid = in.readInt();
            // this.particleIDs = new ArrayList(nPid);
            // for (int i=0; i<nPid; i++)
            // {
            // ParticleID id = new SIOParticleID(in,owner,flag,major,minor);
            // particleIDs.add(id);
            // }
        }
        else
        {
            // read 3 dummy floats
            float[] particleType = new float[3];
            particleType[0] = in.readFloat();
            particleType[1] = in.readFloat();
            particleType[2] = in.readFloat();
        }

        int nClust = in.readInt();
        tempClusters = new ArrayList<SIORef>(nClust);
        clusters = null;
        for (int i = 0; i < nClust; i++)
        {
            tempClusters.add(in.readPntr());
        }
        if ((flag & (1 << LCIOConstants.CLBIT_HITS)) != 0)
        {
            int n = in.readInt();
            tempHits = new ArrayList<SIORef>(n);
            calorimeterHits = null;
            this.hitContributions = new double[n];
            for (int i = 0; i < n; i++)
            {
                tempHits.add(in.readPntr());
                hitContributions[i] = in.readFloat();
            }
        }
        int nEnergies = in.readInt();
        subdetectorEnergies = new double[nEnergies];
        for (int i = 0; i < nEnergies; i++)
        {
            subdetectorEnergies[i] = in.readFloat();
        }
        in.readPTag(this);
    }

    public List<CalorimeterHit> getCalorimeterHits()
    {
        if (calorimeterHits == null && tempHits != null)
        {
            calorimeterHits = new ArrayList(tempHits.size());
            for (SIORef ref : tempHits)
            {
                calorimeterHits.add((CalorimeterHit) ref.getObject());
            }
            tempHits.clear();
            tempHits = null;
        }
        if (calorimeterHits == null)
            return Collections.EMPTY_LIST;
        else
            return calorimeterHits;
    }

    public List<Cluster> getClusters()
    {
        if (clusters == null && tempClusters != null)
        {
            clusters = new ArrayList(tempClusters.size());
            for (SIORef ref : tempClusters)
            {
                clusters.add((Cluster) ref.getObject());
            }
            tempClusters.clear();
            tempClusters = null;
        }
        return clusters;
    }

    public double[] getDirectionError()
    {
        return directionError;
    }

    /** Return corrected cluster energy */
    public double getEnergy()
    {
        return energy;
    }

    public double getEnergyError()
    {
        return energyError;
    }

    public double[] getHitContributions()
    {
        return hitContributions;
    }

    public double getIPhi()
    {
        return phi;
    }

    public double getITheta()
    {
        return theta;
    }

    public double[] getPosition()
    {
        return position;
    }

    public double[] getPositionError()
    {
        return positionError;
    }

    public double[] getShape()
    {
        return shape;
    }

    public double[] getSubdetectorEnergies()
    {
        return subdetectorEnergies;
    }

    /**
     * Set the subdetector energy contributions
     * @param energies
     */
    public void setSubdetectorEnergies(double[] energies)
    {
    	subdetectorEnergies = energies;
    }

    public int getType()
    {
        return type;
    }

    static void write(Cluster cluster, SIOOutputStream out, int flag) throws IOException
    {
        out.writeInt(cluster.getType());
        out.writeFloat((float) cluster.getEnergy());
        out.writeFloat((float) cluster.getEnergyError());
        double[] p = cluster.getPosition();
        if (p == null)
            p = dummy;
        for (int i = 0; i < 3; i++)
            out.writeFloat((float) p[i]);
        p = cluster.getPositionError();
        if (p == null)
            p = dummy;
        for (int i = 0; i < 6; i++)
            out.writeFloat((float) p[i]);
        out.writeFloat((float) cluster.getITheta());
        out.writeFloat((float) cluster.getIPhi());
        p = cluster.getDirectionError();
        if (p == null)
            p = dummy;
        for (int i = 0; i < 3; i++)
            out.writeFloat((float) p[i]);

        p = cluster.getShape();
        if (p == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(p.length);
            for (int i = 0; i < p.length; i++)
            {
                out.writeFloat((float) p[i]);
            }
        }

        out.writeInt(0);
        // List pids = cluster.getParticleIDs() ;
        // out.writeInt( pids.size());
        // for (Iterator iter = pids.iterator(); iter.hasNext();)
        // {
        // ParticleID pid = (ParticleID) iter.next();
        // SIOParticleID.write(pid,out);
        // }

        List<Cluster> clusters = cluster.getClusters();
        out.writeInt(clusters.size());
        for (Cluster c : clusters)
        {
            out.writePntr(c);
        }

        if ((flag & (1 << LCIOConstants.CLBIT_HITS)) != 0)
        {
            List<CalorimeterHit> calorimeterHits = cluster.getCalorimeterHits();
            double[] hitContributions = cluster.getHitContributions();
            out.writeInt(calorimeterHits.size());
            int ii = 0;
            for (CalorimeterHit hit : calorimeterHits)
            {
                out.writePntr(hit);
                out.writeFloat((float) hitContributions[ii++]);
            }
        }
        p = cluster.getSubdetectorEnergies();
        if (p == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(p.length);
            for (int i = 0; i < p.length; i++)
            {
                out.writeFloat((float) p[i]);
            }
        }
        out.writePTag(cluster);
    }

    /**
     * Return the number of hits comprising the cluster, including hits in subclusters, as per
     * interface. Hits belonging to more than one cluster/subcluster should be counted only once.
     */
    public int getSize()
    {
        Set<CalorimeterHit> hitSet = new HashSet<CalorimeterHit>(this.getCalorimeterHits());
        for (Cluster clus : this.getClusters())
        {
            hitSet.addAll(clus.getCalorimeterHits());
        }

        // cleanup and return
        int size = hitSet.size();
        hitSet.clear();
        return size;
    }

    /**
     * Return the sum of the raw energies from the hits in the cluster
     */
    public double getRawEnergy()
    {
        if (raw_energy > 0)
            return raw_energy;

        Set<CalorimeterHit> hitSet = new HashSet<CalorimeterHit>(this.getCalorimeterHits());
        for (Cluster clus : this.getClusters())
        {
            hitSet.addAll(clus.getCalorimeterHits());
        }

        for (CalorimeterHit hit : hitSet)
        {
            raw_energy += hit.getRawEnergy();
        }
        return raw_energy;
    }

    public int getParticleId() {
        return 0;
    }
}
