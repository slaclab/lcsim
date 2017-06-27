package org.lcsim.recon.util;

/**
 * Stores often needed Calorimeter information for easy access
 * @author cassell
 */
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.detector.material.BetheBlochCalculator;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.IMaterialStore;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;
import org.lcsim.geometry.subdetector.CylindricalBarrelCalorimeter;
import org.lcsim.geometry.subdetector.CylindricalEndcapCalorimeter;
import org.lcsim.geometry.subdetector.EcalBarrel;
import org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter;
import org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter2;
import org.lcsim.geometry.subdetector.PolyhedraEndcapCalorimeter;
import org.lcsim.geometry.subdetector.PolyhedraEndcapCalorimeter2;

public class CalorimeterInformation
{
    static CalorimeterInformation theCalorimeterInformation;
    private final boolean debug = true;
    private int ncal = 0;
    private List<Subdetector> sublist;
    private Subdetector[] subdetector;
    private IDDecoder[] idd;
    private String[] subtype;
    private String[] name;
    private String[] collname;
    private String[] digicollname;
    private int[] sysid;
    private int[] nlayers;
    private int[] nsides;
    private double[] rmin;
    private double[] rmax;
    private double[] zmin;
    private double[] zmax;
    private List[] de;
    private List[] nrad;
    private List[] nlam;
    private Map<Calorimeter.CalorimeterType,Integer> indexmap;
    private int index;
    private BetheBlochCalculator bbc;
    private IMaterialStore ms;
    public CalorimeterInformation()
    {
        theCalorimeterInformation = this;
    }
    public static CalorimeterInformation instance()
    {
        return theCalorimeterInformation;
    }
    /**
     * Initialize values from the Detector
     * @param Detector
     */
    protected void init(Detector d)
    {
        bbc = new BetheBlochCalculator();
        ms = MaterialStore.getInstance();
        sublist = new ArrayList<Subdetector>();
        for(Subdetector s:d.getSubdetectors().values())
        {
            if(s.isCalorimeter())
            {
                if(s.getReadout() == null)
                {
                    if (debug)
                        System.out.println("Subdetector "+s.getName()+" isCalorimeter but has null Readout");
                }
                else
                {
                    sublist.add( s );
                    if (debug)
                        System.out.println("Adding subdetector "+s.getName());
                }
            }
        }
        ncal = sublist.size();
        subdetector = new Subdetector[ncal];
        idd = new IDDecoder[ncal];
        subtype = new String[ncal];
        name = new String[ncal];
        collname = new String[ncal];
        digicollname = new String[ncal];
        sysid = new int[ncal];
        nlayers = new int[ncal];
        nsides = new int[ncal];
        rmin = new double[ncal];
        rmax = new double[ncal];
        zmin = new double[ncal];
        zmax = new double[ncal];
        de = new List[ncal];
        nrad = new List[ncal];
        nlam = new List[ncal];
        indexmap = new HashMap<Calorimeter.CalorimeterType,Integer>();
        Hep3Vector p = new BasicHep3Vector(0.,0.,100.);
        Map<String,Double> dedxmap = new HashMap<String,Double>();
        for(int i=0;i<ncal;i++)
        {
            Subdetector s = sublist.get(i);
            subdetector[i] = s;
            name[i] = s.getName();
            sysid[i] = s.getSystemID();
            nlayers[i] = s.getLayering().getNumberOfLayers();
            Calorimeter c = (Calorimeter) s;
            indexmap.put(c.getCalorimeterType(),i);
            idd[i] = subdetector[i].getIDDecoder();
            if(idd[i] == null)System.out.println("null IDDecoder for subdector "+subdetector[i].getName());
            collname[i] = subdetector[i].getReadout().getName();
            digicollname[i] = new String(collname[i]).replace("Hits","DigiHits");
            nrad[i] = new ArrayList<Double>();
            nlam[i] = new ArrayList<Double>();
            de[i] = new ArrayList<Double>();
            for(int j=0;j<nlayers[i];j++)
            {
                Layer layer = s.getLayering().getLayer(j);
                double xrad = 0.;
                double xlam = 0.;
                double xde = 0.;
                for(LayerSlice slice:layer.getSlices())
                {
                    IMaterial m = ms.get(slice.getMaterial().getName());
                    double dedx;
                    if(dedxmap.containsKey(slice.getMaterial().getName()))dedx = dedxmap.get(slice.getMaterial().getName()).doubleValue();
                    else
                    {
                        dedx = bbc.computeBetheBloch(m, p, 105., 1., .01)/10000.;
                        dedxmap.put(slice.getMaterial().getName(),new Double(dedx));
                    }
                    double dx = slice.getThickness();
                    xrad += dx/m.getRadiationLengthWithDensity();
                    xlam += dx/m.getNuclearInteractionLengthWithDensity();
                    xde += dx*dedx;
                }
                nrad[i].add(new Double(xrad/10.));
                nlam[i].add(new Double(xlam/10.));
                de[i].add(new Double(xde));
            }
            nsides[i] = 1;
            if(s.isBarrel())
            {
                if(s instanceof CylindricalBarrelCalorimeter)
                {
                    subtype[i] = "CylindricalBarrelCalorimeter";
                    CylindricalBarrelCalorimeter b = (CylindricalBarrelCalorimeter) s;
                    rmin[i] = b.getInnerRadius();
                    rmax[i] = b.getOuterRadius();
                    zmin[i] = b.getInnerZ();
                    zmax[i] = b.getOuterZ();
                }
                else if(s instanceof PolyhedraBarrelCalorimeter)
                {
                    subtype[i] = "PolyhedraBarrelCalorimeter";
                    PolyhedraBarrelCalorimeter b = (PolyhedraBarrelCalorimeter) s;
                    rmin[i] = b.getInnerRadius();
                    rmax[i] = b.getOuterRadius();
                    zmin[i] = b.getInnerZ();
                    zmax[i] = b.getOuterZ();
                    nsides[i] = ((AbstractPolyhedraCalorimeter) s).getNumberOfSides();
                }
                else if(s instanceof PolyhedraBarrelCalorimeter2)
                {
                    subtype[i] = "PolyhedraBarrelCalorimeter";
                    PolyhedraBarrelCalorimeter2 b = (PolyhedraBarrelCalorimeter2) s;
                    rmin[i] = b.getInnerRadius();
                    rmax[i] = b.getOuterRadius();
                    zmin[i] = b.getInnerZ();
                    zmax[i] = b.getOuterZ();
                    nsides[i] = ((AbstractPolyhedraCalorimeter) s).getNumberOfSides();
                }
                else if(s instanceof EcalBarrel)
                {
                    subtype[i] = "EcalBarrel";
                    EcalBarrel b = (EcalBarrel) s;
                    rmin[i] = b.getInnerRadius();
                    rmax[i] = b.getOuterRadius();
                    zmin[i] = b.getInnerZ();
                    zmax[i] = b.getOuterZ();
                    nsides[i] = ((AbstractPolyhedraCalorimeter) s).getNumberOfSides();
                }
                else
                {
                    System.out.println("Barrel calorimeter "+name[i]+" is unknown type");
                    subtype[i] = "unknown";
                }
            }
            else if(c.isEndcap())
            {
                if(s instanceof CylindricalEndcapCalorimeter)
                {
                    subtype[i] = "CylindricalEndcapCalorimeter";
                    CylindricalEndcapCalorimeter e = (CylindricalEndcapCalorimeter) s;
                    rmin[i] = e.getInnerRadius();
                    rmax[i] = e.getOuterRadius();
                    zmin[i] = e.getInnerZ();
                    zmax[i] = e.getOuterZ();
                }
                else if(s instanceof PolyhedraEndcapCalorimeter)
                {
                    subtype[i] = "PolyhedraEndcapCalorimeter";
                    PolyhedraEndcapCalorimeter e = (PolyhedraEndcapCalorimeter) s;
                    rmin[i] = e.getInnerRadius();
                    rmax[i] = e.getOuterRadius();
                    zmin[i] = e.getInnerZ();
                    zmax[i] = e.getOuterZ();
                    nsides[i] = ((AbstractPolyhedraCalorimeter) s).getNumberOfSides();
                }
                else if(s instanceof PolyhedraEndcapCalorimeter2)
                {
                    subtype[i] = "PolyhedraEndcapCalorimeter2";
                    PolyhedraEndcapCalorimeter2 e = (PolyhedraEndcapCalorimeter2) s;
                    rmin[i] = e.getInnerRadius();
                    rmax[i] = e.getOuterRadius();
                    zmin[i] = e.getInnerZ();
                    zmax[i] = e.getOuterZ();
                    nsides[i] = ((AbstractPolyhedraCalorimeter) s).getNumberOfSides();
                }
                else
                {
                    System.out.println("Endcap calorimeter "+name[i]+" is unknown type");
                    subtype[i] = "unknown";

                }
            }
            else
            {
                System.out.println("Calorimeter "+name[i]+" is not barrel or endcap: Information lost");
                subtype[i] = "unknown";
            }
        }
        
        if (debug)
            printOut(System.out);
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return Subdetector
     */
    public Subdetector getSubdetector(String s)
    {
        return getSubdetector(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return Subdetector
     */
    public Subdetector getSubdetector(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return subdetector[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return IDDecoder for this calorimeter
     */
    public IDDecoder getIDDecoder(String s)
    {
        return getIDDecoder(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return IDDecoder for this calorimeter
     */
    public IDDecoder getIDDecoder(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return idd[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the calorimeter name
     */
    public String getName(String s)
    {
        return getName(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the calorimeter name
     */
    public String getName(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return name[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the collection name for this calorimeter
     */
    public String getCollectionName(String s)
    {
        return getCollectionName(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the collection name for this calorimeter
     */
    public String getCollectionName(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return collname[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the DigiSim output collection name
     */
    public String getDigiCollectionName(String s)
    {
        return getDigiCollectionName(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the DigiSim output collection name
     */
    public String getDigiCollectionName(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return digicollname[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the segmentation type
     */
    public String getCalorimeterType(String s)
    {
        return getCalorimeterType(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the segmentation type
     */
    public String getCalorimeterType(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return subtype[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return integer system ID
     */
    public int getSystemID(String s)
    {
        return getSystemID(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return integer system ID
     */
    public int getSystemID(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1;
        }
        index = ind.intValue();
        return sysid[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return # layers
     */
    public int getNLayers(String s)
    {
        return getNLayers(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return # layers
     */
    public int getNLayers(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1;
        }
        index = ind.intValue();
        return nlayers[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return # sides
     */
    public int getNSides(String s)
    {
        return getNSides(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return # sides
     */
    public int getNSides(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1;
        }
        index = ind.intValue();
        return nsides[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the maximum radius
     */
    public double getRMax(String s)
    {
        return getRMax(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the maximum radius
     */
    public double getRMax(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1.;
        }
        index = ind.intValue();
        return rmax[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the Minimum radius
     */
    public double getRMin(String s)
    {
        return getRMin(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the minimum radius
     */
    public double getRMin(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1.;
        }
        index = ind.intValue();
        return rmin[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the minimum z
     */
    public double getZMin(String s)
    {
        return getZMin(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the minimum z
     */
    public double getZMin(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1.;
        }
        index = ind.intValue();
        return zmin[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the maximum z
     */
    public double getZMax(String s)
    {
        return getZMax(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the maximum z
     */
    public double getZMax(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return -1.;
        }
        index = ind.intValue();
        return zmax[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the mean minI energy loss at normal incidence
     *         for each layer
     */
    public List<Double> getMeanDe(String s)
    {
        return getMeanDe(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the mean minI energy loss at normal incidence
     *         for each layer
     */
    public List<Double> getMeanDe(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return de[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the number of radiation lengths at normal incidence
     *         for each layer
     */
    public List<Double> getNRad(String s)
    {
        return getNRad(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the number of radiation lengths at normal incidence
     *         for each layer
     */
    public List<Double> getNRad(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return nrad[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @return the number of interaction lengths at normal incidence
     *         for each layer
     */
    public List<Double> getNLam(String s)
    {
        return getNLam(Calorimeter.CalorimeterType.fromString(s));
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @return the number of interaction lengths at normal incidence
     *         for each layer
     */
    public List<Double> getNLam(Calorimeter.CalorimeterType s)
    {
        Integer ind = indexmap.get(s);
        if(ind == null)
        {
            System.out.println("Type "+s+" did not map to a CalorimeterType");
            return null;
        }
        index = ind.intValue();
        return nlam[index];
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @param int layer
     * @return the mean minI energy loss at normal incidence
     *         for a layer
     */
    public double getMeanDe(String s, int l)
    {
        return getMeanDe(Calorimeter.CalorimeterType.fromString(s),l);
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @param int layer
     * @return the mean minI energy loss at normal incidence
     *         for a layer
     */
    public double getMeanDe(Calorimeter.CalorimeterType s, int l)
    {
        List<Double> t = getMeanDe(s);
        if( (l < 0)||(l >= t.size()) )
        {
            System.out.println("Layer "+l+" out of range for "+s);
            return 0.;
        }
        return t.get(l).doubleValue();
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @param int layer
     * @return the number of radiation lengths at normal incidence
     *         for a layer
     */
    public double getNRad(String s, int l)
    {
        return getNRad(Calorimeter.CalorimeterType.fromString(s),l);
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @param int layer
     * @return the number of radiation lengths at normal incidence
     *         for a layer
     */
    public double getNRad(Calorimeter.CalorimeterType s, int l)
    {
        List<Double> t = getNRad(s);
        if( (l < 0)||(l >= t.size()) )
        {
            System.out.println("Layer "+l+" out of range for "+s);
            return 0.;
        }
        return t.get(l).doubleValue();
    }
    /**
     *
     * @param String representation of Calorimeter type
     * @param int layer
     * @return the number of interaction lengths at normal incidence
     *         for a layer
     */
    public double getNLam(String s, int l)
    {
        return getNLam(Calorimeter.CalorimeterType.fromString(s),l);
    }
    /**
     *
     * @param CalorimetType representation from
     * Calorimeter.CalorimeterType
     * @param int layer
     * @return the number of interaction lengths at normal incidence
     *         for a layer
     */
    public double getNLam(Calorimeter.CalorimeterType s, int l)
    {
        List<Double> t = getNLam(s);
        if( (l < 0)||(l >= t.size()) )
        {
            System.out.println("Layer "+l+" out of range for "+s);
            return 0.;
        }
        return t.get(l).doubleValue();
    }
    
    public void printOut(PrintStream ps)
    {
        ps.println();
        ps.println("---- CalorimeterInformation ----");
        ps.println();
        for ( int i = 0; i < ncal; i++)
        {
            Calorimeter cal = (Calorimeter)sublist.get(i);
            ps.println(cal.getCalorimeterType().toString() + " : " + cal.getName());
            
            /*
            ps.println("    sysid = " + sysid);
            ps.println("    nlayers = " + nlayers);
            ps.println("    nsides = " + nsides);
            ps.println("    digicoll = " + digicollname[i]);
            ps.println("    collname = " + digicollname[i]);
            */
            
            ps.println("    rmin = " + rmin[i]);
            ps.println("    rmax = " + rmax[i]);
            ps.println("    zmin = " + zmin[i]);
            ps.println("    zmax = " + zmax[i]);                       
            ps.println();
        }        
    }
}
