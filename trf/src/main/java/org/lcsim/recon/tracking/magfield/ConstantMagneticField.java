package org.lcsim.recon.tracking.magfield;

import org.lcsim.recon.tracking.spacegeom.CartesianPointVector;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointTensor;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

/**
 *
 * @author Norman A Graf
 * 
 *  @version $Id:
 */
public class ConstantMagneticField extends AbstractMagneticField
{

       private double _Bx;
       private double _By;
       private double _Bz;
    
   public ConstantMagneticField(double Bx, double By, double Bz)
    {
        _Bx = Bx;
        _By = By;
        _Bz = Bz;
    } 
   
    @Override
    public SpacePointVector field(SpacePoint p)
    {
        return new CartesianPointVector(p,_Bx, _By, _Bz);
    }

    @Override
    public SpacePointVector field(SpacePoint p, SpacePointTensor g)
    {
        // Set gradient to zero.
          if(g != null) g = new SpacePointTensor(p);
          return field(p);
    }
    
    public String toString()
    {
        return "Constant magnetic field with Bx= "+_Bx+" By= "+_By+" Bz= "+_Bz;
    }

}
