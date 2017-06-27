/*
 * TransformableTrackerHit.java
 *
 * Created on December 4, 2007, 11:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.event.MCParticle;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class BaseTrackerHit implements TrackerHit
{
    
    // Elements of TrackerHit interface
    protected Hep3Vector _position_vector;
    protected SymmetricMatrix _covariance_matrix;
    private double _energy;
    private double _time;
    private List<RawTrackerHit> _raw_hits;
    private int _type;
    private long id;  // FIXME: This ID needs to be set from RawTrackerHit data.
    
    // Cached derived quantities
    private IIdentifierHelper _identifier_helper;
    private IDetectorElement _sensor;
    private Set<SimTrackerHit> _simulated_hits;
    private Set<MCParticle> _mc_particles;
    
    /**
     * Creates a new instance of BaseTrackerHit
     */
    
    // Basic constructor
    public BaseTrackerHit(Hep3Vector position_vector, SymmetricMatrix covariance_matrix, double energy, double time, List<RawTrackerHit> raw_hits, int type)
    {
        _position_vector = position_vector;
        _covariance_matrix = covariance_matrix;
        _energy = energy;
        _time = time;
        _raw_hits = raw_hits;
        _type = type;
    }
    
    // Construct from TrackerHit
    public BaseTrackerHit(TrackerHit hit)
    {
        this(
                new BasicHep3Vector(hit.getPosition()[0],hit.getPosition()[1],hit.getPosition()[2]),
                new SymmetricMatrix( 3, hit.getCovMatrix(), true),
                hit.getdEdx(),
                hit.getTime(),
                hit.getRawHits(),
                hit.getType()
                );
    }
    
    // TrackerHit interface
    //======================
    public double[] getPosition()
    {
        return _position_vector.v();
    }
    
    public double[] getCovMatrix()
    {
        return ((SymmetricMatrix)_covariance_matrix).asPackedArray(true);
    }
    
    public double getdEdx()
    {
        return _energy;
    }
    
    public double getTime()
    {
        return _time;
    }
    
    public List<RawTrackerHit> getRawHits()
    {
        return _raw_hits;
    }
    
    public int getType()
    {
        return _type;
    }
    
    public double getEdepError()
    {
        return 0.;
    }
    
    public int getQuality()
    {
        return 0;
    }
    
    // More refined output types for hit information
    public Hep3Vector getPositionAsVector()
    {
        return _position_vector;
    }
    
    public SymmetricMatrix getCovarianceAsMatrix()
    {
        return _covariance_matrix;
    }

    // Additional information for hits
    //=================================    
    public Set<SimTrackerHit> getSimHits()
    {
        if (_simulated_hits == null)
        {
            _simulated_hits = new HashSet<SimTrackerHit>();
            for (RawTrackerHit raw_hit : _raw_hits)
            {
                _simulated_hits.addAll(raw_hit.getSimTrackerHits());
            }
        }
        return _simulated_hits;
    }
    
    public Set<MCParticle> getMCParticles()
    {
        if (_mc_particles == null)
        {
            _mc_particles = new HashSet<MCParticle>();
            for (SimTrackerHit sim_hit : getSimHits())
            {
                _mc_particles.add(sim_hit.getMCParticle());
            }
        }
        return _mc_particles;
    }
    
    public IDetectorElement getSensor()
    {
        if (_sensor == null)
        {
            _sensor = this.getRawHits().get(0).getDetectorElement();
        }
        return _sensor;
    }
    
    public IIdentifierHelper getIdentifierHelper()
    {
        if (_identifier_helper == null)
        {
            _identifier_helper = getRawHits().get(0).getDetectorElement().getIdentifierHelper();
        }
        return _identifier_helper;
    }
    
    public long getCellID()
    {
        return id;
    }    
}