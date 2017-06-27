package org.lcsim.detector;

import junit.framework.TestCase;

public class DetectorElementTest extends TestCase 
{
	IDetectorElement node1 = null;
	IDetectorElement node2 = null;
	IDetectorElement node3 = null;
	
	protected void setUp()
	{
		node1 = new DetectorElement("node1");
		node2 = new DetectorElement("node2",node1);
		node3 = new DetectorElement("node3",node2);
	}
	
	public void test_findDetectorElement()
	{
		IDetectorElement de1 = node1.findDetectorElement("node2");
		assertEquals(de1,node2);
		
		IDetectorElement de2 = de1.findDetectorElement("node3");
		assertEquals(de2,node3);
		
		IDetectorElement de3 = node1.findDetectorElement("node2/node3");
		assertEquals(de3,node3);
	}
}