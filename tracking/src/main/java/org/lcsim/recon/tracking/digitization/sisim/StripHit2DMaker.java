/*
 * StripHit2DMaker.java
 *
 * Created on December 20, 2007, 11:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IReadout;
import org.lcsim.detector.solids.GeomOp3D;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;

/**
 *
 * @author tknelson
 */
public class StripHit2DMaker implements StripHitCombiner
{
    
    private static String _NAME = "RawTrackerHitMaker";
    
    /**
     * Creates a new instance of StripHit2DMaker
     */
    public StripHit2DMaker()
    {
    }
    
    public String getName()
    {
        return _NAME;
    }
    
    public List<SiTrackerHitStrip2D> makeHits(IDetectorElement detector)
    {
        
        List<SiTrackerHitStrip2D> hits_2D = new ArrayList<SiTrackerHitStrip2D>();
        
        List<SiTrackerModule> modules = detector.findDescendants(SiTrackerModule.class);
        
        // Loop over all modules
        for (SiTrackerModule module : modules)
        {
            List<SiSensor> sensors = module.findDescendants(SiSensor.class);
            
            boolean doublesided_sensors = false;
            boolean pixel_sensors = false;
            
            // Process double-sided sensors first
            for (SiSensor sensor : sensors)
            {
                if (sensor.isDoubleSided())
                {
                    doublesided_sensors = true;
//                    if (sensor.hasPixels())
//                    {
//                        pixel_sensors = true;
//                    }
//                    {
                        hits_2D.addAll(this.makeHits(sensor));
//                    }
                }
            }
            
            // If have two sensors and neither is double-sided then make 2d hits from single-sided sensor hits
            if (sensors.size() == 2 && !doublesided_sensors && !pixel_sensors)
            {
                hits_2D.addAll(this.makeHits(module));
            }
        }
        return hits_2D;
    }
    
    public List<SiTrackerHitStrip2D> makeHits(SiSensor sensor)
    {
        List<SiTrackerHitStrip2D> hits_2D = new ArrayList<SiTrackerHitStrip2D>();
        
        if (sensor.isDoubleSided() && !sensor.hasPixels())
        {
            // Get hits for this sensor
            IReadout ro = sensor.getReadout();
            List<SiTrackerHitStrip1D> hits = ro.getHits(SiTrackerHitStrip1D.class);
            
            // Make map from electrodes to lists of hits
            Map<SiSensorElectrodes,List<SiTrackerHitStrip1D>> side_hitlists = new HashMap<SiSensorElectrodes,List<SiTrackerHitStrip1D>>();
            for (SiTrackerHitStrip1D hit : hits)
            {
                SiSensorElectrodes electrodes = hit.getReadoutElectrodes();
                if (side_hitlists.get(electrodes) == null)
                {
                    side_hitlists.put(electrodes,new ArrayList<SiTrackerHitStrip1D>());
                }
                
                side_hitlists.get(electrodes).add(hit);
            }
            
            // If have hits on both sides, make 2d strip hits
            if (side_hitlists.keySet().size() == 2)
            {
                
//                System.out.println("Found sensor with hits on both sides!");
                
                List<List<SiTrackerHitStrip1D>> hitlists = new ArrayList<List<SiTrackerHitStrip1D>>(side_hitlists.values());
                
                for (SiTrackerHitStrip1D hit1: hitlists.get(0))
                {
                    for (SiTrackerHitStrip1D hit2: hitlists.get(1))
                    {
                        List<SiTrackerHitStrip1D> hitpair = new ArrayList<SiTrackerHitStrip1D>();
                        hitpair.add(hit1);
                        hitpair.add(hit2);
                        hits_2D.add(makeTrackerHit2D(hitpair));
                    }
                }
            }
        }
        
        return hits_2D;
        
    }
    
    
    public List<SiTrackerHitStrip2D> makeHits(SiTrackerModule module)
    {
        List<SiTrackerHitStrip2D> hits_2D = new ArrayList<SiTrackerHitStrip2D>();
        List<SiSensor> sensors = module.findDescendants(SiSensor.class);
        
        if (sensors.size() == 2)
        {
            
            // Map to lists of hits for both sensors
            Map<SiSensor,List<SiTrackerHitStrip1D>> sensor_hitlists = new HashMap<SiSensor,List<SiTrackerHitStrip1D>>();
            
            // Loop over sensors
            for (SiSensor sensor : sensors)
            {
                // bail out if we find a double-sided sensor
                if (sensor.isDoubleSided() || sensor.hasPixels())
                {
                    return hits_2D;
                }
                
                // Get hits for this sensor
                IReadout ro = sensor.getReadout();
                List<SiTrackerHitStrip1D> hits = ro.getHits(SiTrackerHitStrip1D.class);
                
                // Add hits to map
                if (hits.size() != 0)
                {
                    sensor_hitlists.put(sensor,new ArrayList<SiTrackerHitStrip1D>());
                    sensor_hitlists.get(sensor).addAll(hits);
                }
                
            }
            
            // If we have hits on both sensors, make 2D hits
            if (sensor_hitlists.keySet().size() == 2)
            {
//                System.out.println("Found module with hits on both sides. Module: "+module.getName());
                
                List<List<SiTrackerHitStrip1D>> hitlists = new ArrayList<List<SiTrackerHitStrip1D>>(sensor_hitlists.values());
                
                for (SiTrackerHitStrip1D hit1: hitlists.get(0))
                {
                    for (SiTrackerHitStrip1D hit2: hitlists.get(1))
                    {
                        List<SiTrackerHitStrip1D> hitpair = new ArrayList<SiTrackerHitStrip1D>();
                        hitpair.add(hit1);
                        hitpair.add(hit2);
                        hits_2D.add(makeTrackerHit2D(hitpair));
                    }
                }
            }
            
        }
        
        return hits_2D;
    }
    
    
    private SiTrackerHitStrip2D makeTrackerHit2D(List<SiTrackerHitStrip1D> hits_1D)
    {
        double energy = 0;
        double time = 0;
        List<RawTrackerHit> raw_hits = new ArrayList<RawTrackerHit>();
        
        for (SiTrackerHitStrip1D hit_1D : hits_1D)
        {
            energy += hit_1D.getdEdx();
            time += hit_1D.getdEdx()*hit_1D.getTime();
            raw_hits.addAll(hit_1D.getRawHits());
        }
        time /= energy;
        
        TrackerHitType decoded_type = new TrackerHitType(TrackerHitType.CoordinateSystem.GLOBAL,
                TrackerHitType.MeasurementType.STRIP_2D);
        
        return new SiTrackerHitStrip2D(calculateGlobalCovariance(hits_1D), energy, time,
                raw_hits, decoded_type, hits_1D);
        
    }
    
    SymmetricMatrix calculateGlobalCovariance(List<SiTrackerHitStrip1D> hits_1D)
    {
        double[] covariance_inverted_sum = new double[6];
        
        for (SiTrackerHitStrip1D hit_1D : hits_1D)
        {
        	
        	//System.out.println("proc hit: " + ((SimTrackerHit)hit_1D.getSimHits().toArray()[0]).getDetectorElement().getName());
            
//            SymmetricMatrix covariance = hit_1D.getTransformedHit(TrackerHitType.CoordinateSystem.GLOBAL).getCovarianceAsMatrix();
//            SymmetricMatrix covariance_inverted = new SymmetricMatrix(3);
//
//            for (int i = 0; i<3; i++)
//            {
//                System.out.println("covariance.diagonal(" +i+ "): "+covariance.diagonal(i));
//
//                if (covariance.diagonal(i) == 0)
//                {
//                    System.out.println("Found zero element for i: "+i);
//                    covariance.setElement(i,i,1);
//                }
//            }
//
//            System.out.println("Covariance matrix: "+covariance);
//
//            inverse(covariance,covariance_inverted);
//
//            System.out.println("Covariance matrix inverted: "+covariance_inverted);
            
            
            // Get covariance and invert to get matrices that add linearly in contrbuting to chisquared
            // FIXME for now we cheat and attribute a negligible measurement error to the third coordinate
            // to avoid an indeteminate (non-invertable) matrix.  
            // Ideally, we should treat only the 2X2 submatrix
            
            SymmetricMatrix covariance_inverted = hit_1D.getTransformedHit(TrackerHitType.CoordinateSystem.GLOBAL).getCovarianceAsMatrix();
            for (int i = 0; i<covariance_inverted.getNRows(); i++)
            {
                if (covariance_inverted.diagonal(i) < GeomOp3D.DISTANCE_TOLERANCE*GeomOp3D.DISTANCE_TOLERANCE)
                {
//                    System.out.println("Found zero element for i: "+i);
                    covariance_inverted.setElement(i,i,GeomOp3D.DISTANCE_TOLERANCE*GeomOp3D.DISTANCE_TOLERANCE);
                }
            }
            
            //System.out.println("Covariance matrix: "+covariance_inverted);
            
            covariance_inverted.invert();
            
//            System.out.println("Covariance matrix inverted: "+covariance_inverted);
            
            
            
            
            // Sum the matrices
            for (int i = 0; i < covariance_inverted_sum.length; i++)
            {
                covariance_inverted_sum[i] += covariance_inverted.asPackedArray(true)[i];
            }
            
        }
        
        SymmetricMatrix covariance = new SymmetricMatrix(3,covariance_inverted_sum,true);
        covariance.invert();
        
        //System.out.println("Covariance matrix after: "+covariance);
        
        return covariance;
    }
    
    
//    public static void inverse(Matrix mIn, MutableMatrix mOut) throws InvalidMatrixException
//    {
//        int order = mIn.getNRows();
//        if (order != mIn.getNColumns()) throw new InvalidMatrixException("Matrix.inverse only supports square matrices");
//        if (order != mOut.getNColumns() && order != mOut.getNRows()) throw new InvalidMatrixException("mOut must be same size as mIn");
//
//        int[] ik = new int[order];
//        int[] jk = new int[order];
//        double[][] array = new double[order][order];
//        for (int i=0;i<order;i++)
//        {
//            System.out.println(" i: "+i);
//            for (int j=0; j<order; j++)
//            {
//                System.out.println(" j: "+j);
//                array[i][j] = mIn.e(i,j);
//                System.out.println("array(i,j): "+array[i][j]);
//            }
//        }
//
//        for (int k=0; k<order; k++)
//        {
//            // Find largest element in rest of matrix
//            double amax = 0;
//            for (int i=k; i<order; i++)
//            {
//                for (int j=k; j<order; j++)
//                {
//
//                    double test = array[i][j];
//
//                    for (int i2=0;i2<order;i2++)
//                    {
//                        System.out.println(" i2: "+i2);
//                        for (int j2=0; j2<order; j2++)
//                        {
//                            System.out.println(" j2: "+j2);
//                            System.out.println("array(i2,j2): "+array[i2][j2]);
//                        }
//                    }
//
//
//                    System.out.println("k: "+k+" i: "+i+"  j:"+j);
//                    System.out.println("test "+test);
//                    System.out.println("array[i][j]: "+array[i][j]);
//                    System.out.println("Math.abs(array[i][j]): "+Math.abs(array[i][j]));
//                    System.out.println("Math.abs(amax): "+Math.abs(amax));
//
//                    if (Math.abs(array[i][j]) > Math.abs(amax))
//                    {
//                        amax = array[i][j];
//                        ik[k] = i;
//                        jk[k] = j;
//                    }
//                }
//            }
//
//            // Interchange rows and columns to put max in array[k][k]
//
//            System.out.println("FINAL amax: "+amax);
//
//            if (amax == 0)
//            {
//                throw new IndeterminateMatrixException();
//            }
//
//            {
//                int i = ik[k];
//                assert(k <= i);
//                if (i > k)
//                {
//                    for (int j=0; j<order; j++)
//                    {
//                        double save = array[k][j];
//                        array[k][j] = array[i][j];
//                        array[i][j] = -save;
//                    }
//                }
//            }
//            {
//                int j = jk[k];
//                assert(k <= j);
//                if (j > k)
//                {
//                    for (int i=0; i<order; i++)
//                    {
//                        double save = array[i][k];
//                        array[i][k] = array[i][j];
//                        array[i][j] = -save;
//                    }
//                }
//            }
//
//            // Accumulate elements of inverse matrix
//
//            for (int i=0; i<order; i++)
//            {
//                if (i == k) continue;
//                array[i][k] = -array[i][k]/amax;
//            }
//            for (int i=0; i<order; i++)
//            {
//                if (i == k) continue;
//                for (int j=0; j<order; j++)
//                {
//                    if (j == k) continue;
//                    array[i][j] += array[i][k]*array[k][j];
//                }
//            }
//            for (int j=0; j<order; j++)
//            {
//                if (j == k) continue;
//                array[k][j] = array[k][j]/amax;
//            }
//            array[k][k] = 1/amax;
//        }
//
//        // restore ordering of matrix
//
//        for (int l=0; l<order; l++)
//        {
//            int k = order - l - 1;
//            {
//                int j = ik[k];
//                if (j>k)
//                {
//                    for (int i=0; i<order; i++)
//                    {
//                        double save = array[i][k];
//                        array[i][k] = -array[i][j];
//                        array[i][j] = save;
//                    }
//                }
//            }
//            {
//                int i = jk[k];
//                if (i>k)
//                {
//                    for (int j=0; j<order; j++)
//                    {
//                        double save = array[k][j];
//                        array[k][j] = -array[i][j];
//                        array[i][j] = save;
//                    }
//                }
//            }
//        }
//        for (int i=0;i<order;i++)
//        {
//            for (int j=0; j<order; j++)
//            {
//                mOut.setElement(i,j,array[i][j]);
//            }
//        }
//    }
    
    
}


//                    // Get hits for this sensor
//                    IReadout ro = sensor.getReadout();
//                    List<SiTrackerHitStrip1D> hits = ro.getHits(SiTrackerHitStrip1D.class);
//
//                    // Make map from electrodes to lists of hits
//                    Map<SiSensorElectrodes,List<SiTrackerHitStrip1D>> side_hitlists = new HashMap<SiSensorElectrodes,List<SiTrackerHitStrip1D>>();
//                    for (SiTrackerHitStrip1D hit : hits)
//                    {
//                        SiSensorElectrodes electrodes = hit.getReadoutElectrodes();
//                        if (side_hitlists.get(electrodes) == null)
//                        {
//                            side_hitlists.put(electrodes,new ArrayList<SiTrackerHitStrip1D>());
//                        }
//
//                        side_hitlists.get(electrodes).add(hit);
//                    }
//
//                    // If have hits on both sides, make 2d strip hits
//                    if (side_hitlists.keySet().size() == 2)
//                    {
//                        List<List<SiTrackerHitStrip1D>> hitlists = new ArrayList<List<SiTrackerHitStrip1D>>(side_hitlists.values());
//
//                        for (SiTrackerHitStrip1D hit1: hitlists.get(0))
//                        {
//                            for (SiTrackerHitStrip1D hit2: hitlists.get(1))
//                            {
//                                List<SiTrackerHitStrip1D> hitpair = new ArrayList<SiTrackerHitStrip1D>();
//                                hitpair.add(hit1);
//                                hitpair.add(hit2);
//                                hits_2D.add(makeTrackerHit2D(hitpair));
//                            }
//                        }
//                    }


//                Map<SiSensor,List<SiTrackerHitStrip1D>> sensor_hitlists = new HashMap<SiSensor,List<SiTrackerHitStrip1D>>();
//
//                for (SiSensor sensor : sensors)
//                {
//                    // Get hits for this sensor
//                    IReadout ro = sensor.getReadout();
//                    List<SiTrackerHitStrip1D> hits = ro.getHits(SiTrackerHitStrip1D.class);
//
//                    // If sensor is single-sided strip sensor, add hits to map
//                    if (hits.size() != 0)
//                    {
//                        sensor_hitlists.put(sensor,new ArrayList<SiTrackerHitStrip1D>());
//                        sensor_hitlists.get(sensor).addAll(hits);
//                    }
//
//                }
//
//                if (sensor_hitlists.keySet().size() == 2)
//                {
//                    List<List<SiTrackerHitStrip1D>> hitlists = new ArrayList<List<SiTrackerHitStrip1D>>(sensor_hitlists.values());
//
//                    for (SiTrackerHitStrip1D hit1: hitlists.get(0))
//                    {
//                        for (SiTrackerHitStrip1D hit2: hitlists.get(1))
//                        {
//                            List<SiTrackerHitStrip1D> hitpair = new ArrayList<SiTrackerHitStrip1D>();
//                            hitpair.add(hit1);
//                            hitpair.add(hit2);
//                            hits_2D.add(makeTrackerHit2D(hitpair));
//                        }
//                    }
//                }
