package org.lcsim.recon.emid.hmatrix;

import java.io.InputStream;
import org.lcsim.conditions.ConditionsConverter;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.RawConditions;
import org.lcsim.geometry.GeometryReader;

/**
 * A Class to manage HMatrices under possibly different run conditions.
 * @author Norman A. Graf
 * @version 1.0
 */
public class HMatrixConditionsConverter implements ConditionsConverter<HMatrix>
{
    public HMatrix getData(ConditionsManager manager, String name)
    {
        RawConditions conditions = manager.getRawConditions(name);
        try
        {
            InputStream in = conditions.getInputStream();
            HMatrix hm = HMatrix.create(in);
            return hm;
        }
        catch (Exception x)
        {
            throw new RuntimeException("Error reading HMatrix "+name,x);
        }
    }
    
    public Class<HMatrix> getType()
    {
        return HMatrix.class;
    }
}
