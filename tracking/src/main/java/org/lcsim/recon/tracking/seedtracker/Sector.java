package org.lcsim.recon.tracking.seedtracker;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.fit.helicaltrack.HelicalTrack2DHit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 *
 * @author Richard Partridge
 */
public class Sector {
    private String _identifier;
    private String _layerID;
    private List<HelicalTrackHit> _hitlist;
    private int _phisector;
    private int _zsector;
    private double _rmin;
    private double _rmax;
    private double _phimin;
    private double _phimax;
    private double _zmin;
    private double _zmax;
    private double _philo;
    private double _phihi;
    private double _zlo;
    private double _zhi;

    public Sector(String identifier, String layerID, int phisector, int zsector,
            double philo, double phihi, double zlo, double zhi) {

        //  Save the identifier, indices, and limits for this sector
        _identifier = identifier;
        _layerID = layerID;
        _phisector = phisector;
        _zsector = zsector;
        _philo = philo;
        _phihi = phihi;
        _zlo = zlo;
        _zhi = zhi;

        //  Initialize the hit list
        _hitlist = new ArrayList<HelicalTrackHit>();

        //  Initialize the geographic region encompassed by hits in this sector
        _rmin = 99999.;
        _rmax = 0.;
        _phimin = 2.0 * Math.PI;
        _phimax = 0.0;
        _zmin = 99999.;
        _zmax = -99999.;
    }

    public void addHit(HelicalTrackHit hit) {

        //  Check the layerID matches
        if (!hit.getLayerIdentifier().equals(_layerID)) SectorError();

        //  Check that phi is within limits
        double phi = hit.phi();
        if (phi < _philo || phi > _phihi) SectorError();

        //  Check that z is within limits
        double z;
        if (hit.BarrelEndcapFlag() == BarrelEndcapFlag.BARREL) {
            z = hit.z();
        } else {
            z = hit.r();
        }
        if (z < _zlo || z > _zhi) SectorError();

        //  Add the hit to the list for this sector
        _hitlist.add(hit);

        //  Get the location of this hit
        double r = hit.r();
        double zmin = hit.z();
        double zmax = zmin;
        if (hit instanceof HelicalTrack2DHit) {
            HelicalTrack2DHit hit2d = (HelicalTrack2DHit) hit;
            zmin = hit2d.zmin();
            zmax = hit2d.zmax();
        }

        //  Update the span in location for hits in this sector
        _rmin = Math.min(_rmin, r);
        _rmax = Math.max(_rmax, r);
        _phimin = Math.min(_phimin, phi);
        _phimax = Math.max(_phimax, phi);
        _zmin = Math.min(_zmin, zmin);
        _zmax = Math.max(_zmax, zmax);
    }

    public String Identifier() {
        return _identifier;
    }

    public String LayerID() {
        return _layerID;
    }

    public List<HelicalTrackHit> Hits() {
        return _hitlist;
    }

    public double rmin() {
        return _rmin;
    }

    public double rmax() {
        return _rmax;
    }

    public double phimin() {
        return _phimin;
    }

    public double phimax() {
        return _phimax;
    }

    public double zmin() {
        return _zmin;
    }

    public double zmax() {
        return _zmax;
    }

    public int phiSector() {
        return _phisector;
    }
    
    public int zSector() {
        return _zsector;
    }
    
    public double philo() {
        return _philo;
    }
    
    public double phihi() {
        return _phihi;
    }
    
    public double zlo() {
        return _zlo;
    }
    
    public double zhi() {
        return _zhi;
    }

    private void SectorError() {
        throw new RuntimeException("Hit Sectoring error");
    }
}