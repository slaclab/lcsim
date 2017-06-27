package org.lcsim.detector;

import junit.framework.TestCase;

public class ReadoutTest extends TestCase 
{
	public interface TestInterface
	{}
	
	public class TestClass implements TestInterface
	{}
	
	public class TestClass2 extends TestClass
	{}
	
	public class TestClass3
	{}
	
	public void testHits()
	{
		Readout readout = new Readout();
		readout.addHit(new TestClass2());
		assertTrue(readout.getHits(TestInterface.class).size() == 1);
		assertTrue(readout.getHits(TestClass.class).size() == 1);
		assertTrue(readout.getHits(TestClass2.class).size() == 1);
		assertTrue(readout.getHits(TestClass3.class).size() == 0);
	}
}
