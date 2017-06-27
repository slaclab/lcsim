package org.lcsim.spacegeom;
import junit.framework.TestCase;

// Test the SpaceXform class.

//**********************************************************************
public class SpaceXformTest extends TestCase
{
	static class TestXform extends SpaceXform
	{	    
	    public static int _check = 0;
	    public SpaceXform inverse()
	    { return this; }
	    public SpacePoint apply( SpacePoint spt)
	    { _check=1; return spt; }
	    public SpacePointVector apply( SpacePointVector svec)
	    { _check=2; return svec; }
	}
	
    //**********************************************************************
    boolean debug = false;
    
    public void testSpaceXForm()
    {
        
        String name = "SpaceXform";
        String ok_prefix = name + " test (I): ";
        String error_prefix = name + " test (E): ";
        
        if (debug) System.out.println( ok_prefix
                + "------- Testing component " + name + ". -------" );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing constructor." );
        TestXform xf = new TestXform();
        
        SpacePoint spt1 = new SpacePoint();
        SpacePointVector svec1 = new SpacePointVector();
        SpacePath spth1 = new SpacePath();
        
        assertTrue(xf._check == 0);
        SpacePoint spt2 = xf.transform(spt1);
        assertTrue(xf._check == 1);
        
        SpacePointVector svec2 = xf.transform(svec1);
        assertTrue(xf._check == 2);
        
        SpacePointVector spth2 = xf.transform(spth1);
        assertTrue(xf._check == 2);
        spt2.x();
        svec2.getStartPoint().x();
        spth2.getStartPoint().x();
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------" );
        
    }
}
