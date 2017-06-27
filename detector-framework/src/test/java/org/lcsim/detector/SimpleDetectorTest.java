package org.lcsim.detector;

import static org.lcsim.units.clhep.SystemOfUnits.m;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.Inside;
import org.lcsim.detector.solids.Tube;

public class SimpleDetectorTest extends TestCase
{		
	private IPhysicalVolume world = null;
	private static IMaterial dummymat = new MaterialElement("dummymat",1,1,1.0);
	
    public SimpleDetectorTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(SimpleDetectorTest.class);
    }
    
    protected void setUp() throws Exception
    {
    	world = createTestGeometry();
    }
    
    public void testBasicAccess()
    {
    	//System.out.println("pv world = " + world.getName());
    	ILogicalVolume lvWorld = world.getLogicalVolume();
    	//System.out.println("lv world = " + lvWorld.getName());
    	IPhysicalVolume pvDau = lvWorld.getDaughter(0);    	
    	//System.out.println("pv dau = " + pvDau.getName());
    	ILogicalVolume lvDau = pvDau.getLogicalVolume();
    	//System.out.println("lv dau = " + lvDau.getName());
    	Box boxDau = (Box)lvDau.getSolid();
    	//System.out.println("box dau = " + boxDau.getName() + "; halfx halfy halfz = " + boxDau.getXHalfLength() + " " + boxDau.getYHalfLength() + " " + boxDau.getZHalfLength());
    	IMaterial material = lvDau.getMaterial();
    	//System.out.println("mat dau = " + material.getName());
    }

    static double [] xpoints = {10.0,10.1,9.9,5.1,4.9,2.6,2.4,1.1,.9,.6,.4,.2,.1,0};
	private String[] testnames = {"/box1","box1","box1/","/box1"};
    
    /*
    public void testIsInside()
    {
        IPhysicalVolumeNavigator nav = PhysicalVolumeNavigatorStore.getInstance().createDefault(world);
        
        List<Hep3Vector> testpoints = new ArrayList<Hep3Vector>();
        testpoints.add(new BasicHep3Vector(0,0,0));
        testpoints.add(new BasicHep3Vector(50,0,0));
        testpoints.add(new BasicHep3Vector(0,50,0));
        
        for ( Hep3Vector point : testpoints )
        {
            IPhysicalVolumePath path = nav.getPath(point);
        }        
    }*/
    
	public void testNavigator()
    {
    	IPhysicalVolumeNavigator nav = 
            PhysicalVolumeNavigatorStore.getInstance().createDefault(world); 	
    	
    	// The string "/" should give back a reference
    	// to the top volume, which is encoded by a
    	// single "/".
    	assertTrue("/".equals(nav.getPath("/").toString()));
    	
    	// The navigator should normalize all the test names to "/box1".
    	for (String testname : testnames)
    	{    	
    		IPhysicalVolumePath path = nav.getPath(testname);
    		    		
    		assertEquals(path.size(),2);
            
    		assertTrue("/box1".equals(path.toString()                ));
    		assertTrue( "box1".equals(path.getLeafVolume().getName() ));
        	assertTrue("world".equals(path.getTopVolume().getName()  ));
    	}
    	    	    	    
    	IPhysicalVolumePath path = nav.getPath("/box1");
    	    	    	      
        // Check isInside for positive points on "box1". 
    	for (double x : xpoints)
    	{
    		path = nav.getPath(new BasicHep3Vector(x,0,0));
    		
    		if (x<5.0)
    		{
    			assertTrue("/box1".equals(path.toString()));
    		}
    		else {
    			assertTrue("/".equals(path.toString()));
    		}
    	}
    	
        // Check isInside for negative points on "box1".
    	for (double x : xpoints)
    	{
    		IPhysicalVolumePath path3 = nav.getPath(new BasicHep3Vector(-x,0,0));
    		    		
    		if (-x > -5.0)
    		{
    			assertTrue("/box1".equals(path3.toString()));
    		}
    		else {
    			assertTrue("/".equals(path3.toString()));
    		}    		    	
    	}
    	
        // Check isInside for some positive x points on "box2".
    	for (double x : new double[] {44.9,45.1,47.9,48.1,51.9,52.1,49.9,54.9,55.1})
    	{
    		IPhysicalVolumePath path4 = nav.getPath(new BasicHep3Vector(x,0,0));    	
    		if (x>45 && x<55)
    		{
    			assertTrue("/box2".equals(path4.toString()));
    		}
    	}
    	    	
    	// Create a dummy DE that has the "box1" volume as its node.
    	IDetectorElement dummyDE = new DummyDE(nav.getPath("/box1"));
    	IGeometryInfo gi = dummyDE.getGeometry();
    	assertTrue("/box1".equals(gi.getPath().toString()));
    	
    	// Check isInside for positive points on the "box1" DE.
    	for (double x : xpoints)
    	{
    		Hep3Vector thisx = new BasicHep3Vector(x,0,0);
    		Inside inside = gi.inside(thisx);
    		    		    	
    		if (x<5.0)
    		{
    			assertEquals(inside,Inside.INSIDE);
    		}    		
    	}
    	
        // Check isInside for various geometry objects.
    	for (double y : new double[] {44.9,45.1,47.9,48.1,51.9,52.1,49.9,54.9,55.1})
    	{
    		Hep3Vector point = new BasicHep3Vector(0,y,0);
    		IPhysicalVolumePath path5 = nav.getPath(point);
    		
    		if ( y < 45.0 || y > 55.0)
    		{
    			assertTrue(path5.size()==1);
    			assertTrue("/".equals(path5.toString()));
    		}
    		
    		if ((y > 45.0 && y < 48.0) || (y > 52.0 && y < 55.0))
    		{
    			assertTrue("/box3".equals(path5.toString()));
    		}
    		else if (y > 48.0 && y < 52.0)
    		{
    			assertTrue("/box3/box4".equals(path5.toString()));
    		}    		    	
    	}
    	
    	IPhysicalVolumePath path6 = nav.getPath(new BasicHep3Vector(101,0,0));
    	assertTrue("/tube1".equals(path6.toString()));
    	path6 = nav.getPath("/tube1");
    	assertTrue("/tube1".equals(path6.toString()));
    	path6 = nav.getPath(new BasicHep3Vector(111.0,0,0));
    	assertTrue("/tube1/tube2".equals(path6.toString()));
    }
	
	public class VisitorTest 
	implements IPhysicalVolumeVisitor
	{
		public void visit(IPhysicalVolume volume)
		{
			System.out.println("visiting node " + volume.getName());
		}
        
        public boolean isDone()
        {
            return false;
        }
	}
	
    /*
	public void testTraverse()
	{
		IPhysicalVolumeNavigator nav = new PhysicalVolumeNavigator("nav2", world); 	
		VisitorTest visitorTest = new VisitorTest();
		
		System.out.println("---PreOrder Traversal---");
		nav.traversePreOrder(visitorTest);
		System.out.println('\n');
		
		System.out.println("---PostOrder Traversal---");
		nav.traversePostOrder(visitorTest);
	}*/
    
	public IPhysicalVolume createTestGeometry()
	{
		IPhysicalVolume world = createWorld();
		createTestSolids(world);
		return world;
	}
	
	public final void createTestSolids(IPhysicalVolume mom)
	{
		// 10 mm box at 0,0,0 
		Box box = new Box("test_box1",5.0,5.0,5.0);
		LogicalVolume lvTest = new LogicalVolume("lvTest",box,dummymat);
		new PhysicalVolume(
				new Transform3D(),
				"box1",
				lvTest,
				mom.getLogicalVolume(),
				0);

		// 10 mm box at 50,0,0
		Box box2 = new Box("test_box2",5.0,5.0,5.0);
		LogicalVolume lvTest2 = new LogicalVolume("lvTest2",box2,dummymat);
		new PhysicalVolume(
				new Transform3D(new Translation3D(50.0,0,0)),
				"box2",
				lvTest2,
				mom.getLogicalVolume(),
				1);		
		
		// 10 mm box at 0,50,0
		Box box3 = new Box("test_box3",5.0,5.0,5.0);
		LogicalVolume lvTest3 = new LogicalVolume("lvTest3",box3,dummymat);
		new PhysicalVolume(
				new Transform3D(new Translation3D(0,50.0,0)),
				"box3",
				lvTest3,
				mom.getLogicalVolume(),
				2);		
		
		// A 2 mm box inside of box3.
		Box box4 = new Box("test_box4",2.0,2.0,2.0);
		LogicalVolume lvTest4 = new LogicalVolume("lvTest4",box4,dummymat);
		new PhysicalVolume(
				null,
				"box4",
				lvTest4,
				lvTest3,
				0);				
		
		Tube tube1 = new Tube("test_tube1",100.0,200.0,1000.0);
		LogicalVolume lvTest5 = new LogicalVolume("lvTest5",tube1,dummymat);
		new PhysicalVolume(
				null,
				"tube1",
				lvTest5,
				mom.getLogicalVolume(),
				6);
		
		Tube tube2 = new Tube("test_tube2",110.0,120.0,1000.0);
		LogicalVolume lvTest6 = new LogicalVolume("lvTest6",tube2,dummymat);
		new PhysicalVolume(
				null,
				"tube2",
				lvTest6,
				lvTest5,
				7);
	}
	
	public final IPhysicalVolume createWorld()
	{		
		Box boxWorld = new Box(
				"world_box",
				10.0*m,
				10.0*m,
				10.0*m);
		
		LogicalVolume lvWorld = 
			new LogicalVolume(
					"world",
					boxWorld,
					dummymat);
		
		IPhysicalVolume pvTop = 
			new PhysicalVolume(
					null,
					"world",
					lvWorld,
					null,
					0);
		
		return pvTop;
	}   
	
	public class DummyDE
	extends DetectorElement
	{
		DummyDE(IPhysicalVolumePath support)
		{
            super("dummy");
            setSupport(support);
		}
	}
}