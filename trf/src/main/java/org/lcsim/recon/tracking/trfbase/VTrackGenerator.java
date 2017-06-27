package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfutil.RandomGenerator;
import org.lcsim.recon.tracking.trfutil.Assert;

/**Generates VTrack objects with parameters chosen randomly.
* The track surface and the ranges for the parameters are
* specified in the constructor
 *
* @author Norman A. Graf
* @version 1.0
*/

public class VTrackGenerator extends RandomGenerator {

  // attributes

  // min and max values for each parameter.
  private TrackVector _min;
  private TrackVector _max;

  // surface.
  private Surface _srf;
 
//methods

  // 

	/**
	 *constructor
	 *
	 * @param   srf surface at which to generate track 
	 * @param   min minimum values for random track parameters 
	 * @param   max maximum values for random track parameters 
	 */
  public VTrackGenerator(  Surface srf,   TrackVector min,
                                        TrackVector max)
                                        {
											_srf = srf.newPureSurface();
											 _min = min;
											  _max = max;
  for ( int i=0; i<5; ++i ) Assert.assertTrue( _min.get(i) <= _max.get(i) );
                                        }

  // 

	/**
	 *Copy constructor.
	 *
	 * @param   vtg  VTrackGenerator to replicate
	 */
  public VTrackGenerator(  VTrackGenerator vtg)
  {
  	super(vtg);
	_min = vtg._min; 
	_max = vtg._max; 
	_srf = vtg._srf;
  }

 
  //

	/**
	 * Return the surface.
	 *
	 * @return Surface at which tracks are to be generated    
	 */
    public Surface surface()   { return _srf; }

  //  

	/**
	 *Generate a new track. 
	 *
	 * @return new random VTrack with track parameters distributed 
	 * evenly between minimum and maximum TrackVector values 
	 */
  public VTrack newTrack()
  {
  TrackVector vec = new TrackVector();
  for ( int i=0; i<5; ++i ) vec.set(i, flat( _min.get(i), _max.get(i) ) );
  VTrack trv = new VTrack(_srf,vec);
  // If the direction is not set, make it forward
//  if ( trv.is_forward() ) return trv;
//  if ( trv.is_backward() ) return trv;
  trv.setForward();
  return trv;  
  }
  

	/**
	 * String representation of VTrackGenerator
	 *
	 * @return  String representation of VTrackGenerator   
	 */
  public String toString()
  {
  String className = getClass().getName();
  int lastDot = className.lastIndexOf('.');
  if(lastDot!=-1)className = className.substring(lastDot+1);
  
	return className+" Lower limit: " +_min +
   "\nUpper limit: " + _max +  
    "\nSurface: " + _srf; 
  }

} 
