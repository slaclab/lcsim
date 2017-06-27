package org.lcsim.material;

import junit.framework.TestCase;

public class NuclearInteractionLengthTest extends TestCase
{
    String materialLookup[] = {"Tungsten"};
    //, "Lead", "Silicon", "Boron"};
    double lamKey[] = {191.9};
    double lamDensKey[] = {9.946};
    public void testRadLen()
    {
        MaterialManager mgr = MaterialManager.instance();
        System.out.println("Material radLen, radLenDens");
        for (int i=0, n=materialLookup.length; i<n; i++)
        {
            Material material = mgr.getMaterial(materialLookup[i]);
            String materialName = material.getName();
            System.out.println(materialName + " " 
                    + material.getNuclearInteractionLength() + ", " 
                    + material.getNuclearInteractionLengthWithDensity());
            System.out.println("key: " + lamKey[i] + ", " + lamDensKey[i]);
            assertEquals(material.getNuclearInteractionLength(), lamKey[i], 0.1);
            assertEquals(material.getNuclearInteractionLengthWithDensity(), lamDensKey[i], 0.01);
        }
    }
}