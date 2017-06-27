package org.lcsim.material;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticlePropertyProvider;
import hep.physics.particle.properties.ParticleType;

/**
 *
 * @author jeremym
 */
public class BetheBlockTest extends TestCase
{    
    // change to true if debugging this componen
    private boolean _debug = false;
    private static ParticlePropertyProvider pinfo = 
    	ParticlePropertyManager.getParticlePropertyProvider();
    
    /** Creates a new instance of MaterialManagerTest */
    public BetheBlockTest()
    {}
    
    public BetheBlockTest(String testName)
    {
        super(testName);   
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(BetheBlockTest.class);
    }
    
    public void test_All()
    {
    	MaterialManager.instance();
    	double[] p = {10,50,100,200,500,1000,2000,5000,10000};
    	String[] materials = {"Copper","Iron","Silicon","Lead","Hydrogen","Helium","Carbon"};
    	int[] pdgids = {11,13,211,321,2212};
    	
    	for (double pp : p)
    	{
    		for (String m : materials)
    		{
    			for (int pdg : pdgids)
    			{
    				ParticleType ptype = pinfo.get(pdg);
    				double mass = ptype.getMass() * 1000;
    				double charge = ptype.getCharge();
    				double[] parr = {pp, 0, 0};
    				String pname = ptype.getName();
    				this.run(m, pname, mass, charge, parr);
    			}
    		}
    	}
    }
    
    private void run(String materialName, String particle, double mass,
            double charge, double[] p)
    {
        Material material = MaterialManager.instance().materials().get(materialName);
        assert (material != null);
        double dEdx = MaterialCalculator.computeBetheBloch(material,
                p, mass, charge, 1.0);
        if (_debug) System.out.println(particle + " " + p[0] + " " + materialName + " " + dEdx);
    }
}
