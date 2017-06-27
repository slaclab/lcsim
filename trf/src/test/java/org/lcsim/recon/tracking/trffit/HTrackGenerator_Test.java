/*
 * HTrackGenerator_Test.java
 *
 * Created on July 24, 2007, 4:57 PM
 *
 * $Id: HTrackGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ClusterTest;
import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trflayer.PropTest;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class HTrackGenerator_Test extends TestCase
{
	private boolean debug;
	/** Creates a new instance of HTrackGenerator_Test */
	public void testHTrackGenerator()
	{
		//**********************************************************************


		String component = "HTrackGenerator";
		String ok_prefix = component + " (I): ";
		String error_prefix = component + " test (E): ";

		if(debug) System.out.println( ok_prefix
				+ "---------- Testing component " + component
				+ ". ----------" );

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Test constructor." );
		// construct VTrack generator
		SurfTest stest = new SurfTest(1);
		TrackVector vec = new TrackVector();
		vec.set(0,  1.0);
		vec.set(1,  2.0);
		vec.set(2,  3.0);
		vec.set(3,  4.0);
		vec.set(4,  5.0);
		VTrack trv = new VTrack( stest.newPureSurface(), vec );
		// Construct list of Hit generators.
		List hgens = new ArrayList();
		hgens.add( new HitGeneratorTest(new SurfTest(2)) );
		hgens.add( new HitGeneratorTest(new SurfTest(4)) );
		hgens.add( new HitGeneratorTest(new SurfTest(6)) );
		hgens.add( new HitGeneratorTest(new SurfTest(8)) );
		// Construct track error matrix.
		TrackError terr = new TrackError();
		terr.set(0,0,  0.01);
		terr.set(1,1,  0.02);
		terr.set(2,2,  0.03);
		terr.set(3,3,  0.04);
		terr.set(4,4,  0.05);
		// Construct propagator.
		PropTest prop = new PropTest();
		// construct generator
		HTrackGenerator gen = new HTrackGenerator(hgens,prop,stest,terr);

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Generate tracks." );
		HTrack trh = gen.newTrack(trv);
		Assert.assertTrue( trh != null );
		if(debug) System.out.println( trh );
		Assert.assertTrue( trh.hits().size() == 4 );
		Assert.assertTrue( trh.newTrack().surface().pureEqual(stest) );

		//********************************************************************

		if(debug) System.out.println( ok_prefix
				+ "------------- All tests passed. -------------" );


		//********************************************************************        
	}

	public static class HitGeneratorTest extends HitGenerator
	{

		private SurfTest _srf;
		private double _min;
		private double _max;

		public HitGeneratorTest(  SurfTest srf)
		{
			_srf = new SurfTest(srf);
		}
		public   Surface surface()
		{
			return _srf;
		}
		public Cluster newCluster(  VTrack trv, int mcid)
		{
			int npred = (int)( flat(1.0,10.0) );
			return new ClusterTest(_srf,npred);
		}

	}
}