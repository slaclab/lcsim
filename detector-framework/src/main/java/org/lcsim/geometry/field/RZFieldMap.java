package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.jdom.Element;
import org.jdom.JDOMException;
import java.net.URL;
import org.lcsim.util.cache.FileCache;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 *
 * @author jeremym
 */
public class RZFieldMap extends AbstractFieldMap
{
    private int numBinsR;
    private int numBinsZ;
    
    private double gridSizeR;
    private double gridSizeZ;
    
    private double maxZ;
    private double maxR;
    private double maxRSquared;
    
    private String location;
    
    private double[][] BrArray;
    private double[][] BzArray;
    
    public RZFieldMap(Element node) throws JDOMException
    {
        super(node);       
        
        numBinsR = node.getAttribute("numBinsR").getIntValue();
        
        if (numBinsR < 2)
        {
            throw new JDOMException("numBinsR is invalid: " + numBinsR);
        }
        
        numBinsZ = node.getAttribute("numBinsZ").getIntValue();
        
        if (numBinsZ < 2)
        {
            throw new JDOMException("numBinsZ is invalid: " + numBinsZ);
        }
       
        /* FIXME: Hard-coded conversion of cm to mm. */
        gridSizeR = node.getAttribute("gridSizeR").getDoubleValue() * 10;
        gridSizeZ = node.getAttribute("gridSizeZ").getDoubleValue() * 10;
        
        maxR = ( numBinsR - 1 ) * gridSizeR;
        maxZ = ( numBinsZ - 1 ) * gridSizeZ;
        maxRSquared = maxR*maxR;
        
        BrArray = new double[numBinsZ][numBinsR];
        BzArray = new double[numBinsZ][numBinsR];
               
        location = node.getAttribute("url").getValue();
        
        try
        {
            readFieldMap();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error reading field map from " + location, e);
        }
    }
    
    private void readFieldMap() throws IOException
    {
        readFieldMap(this.location);
    }
    
    private void readFieldMap(String location) throws IOException
    {
        FileCache cache = new FileCache();
        File file = cache.getCachedFile(new URL(location));
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        for (;;)
        {
            String line = reader.readLine();
            if (line == null) break;
            String[] chunks = line.trim().split(" +");
            
            if ( chunks.length > 0 )
            {
                if ( chunks.length != 4 )
                {
                    throw new IOException("Invalid RZ field map line: " + line);
                }
                
                /* FIXME: Hard-coded unit conversion of cm to mm. */
                double z = Double.parseDouble(chunks[0]) * 10;
                double r = Double.parseDouble(chunks[1]) * 10;
                
                /* FIXME: Hard-coded unit conversion of kilogauss to tesla. */
                double Bz = Double.parseDouble(chunks[2]) / 10;
                double Br = Double.parseDouble(chunks[3]) / 10;
                
                int iz= (int) ((z + 0.0001)/gridSizeZ);
                int ir=(int) ((r + 0.0001)/gridSizeR);
                
                if ( iz > ( numBinsZ - 1) )
                {
                    throw new IOException("z bin out of range: " + iz);
                }
                
                if ( ir > ( numBinsR - 1) )
                {
                    throw new IOException("r bin out of range:" + ir);
                }
                
                BzArray[iz][ir] = Bz;
                BrArray[iz][ir] = Br;
            }
        }
    }
    
    void getField(double x, double y, double z, BasicHep3Vector field)
    {
        double rSquared = x*x + y*y;
        
        double hz = 0;
        double hr = 0;
        
        if(abs(z)>maxZ || rSquared>maxRSquared)
        {
            field.setV(0,0,0);
            return;
        }
        double r = sqrt(rSquared);
        // FIXME: .001?
        int iz = (int) ((abs(z)+0.001)/gridSizeZ);
        int ir = (int) ((r+0.001)/gridSizeR);
        
        // outside
        if(iz<0 || ir>numBinsR)
        {
            field.setV(0,0,0);
            return;
        }
        
        int izfar = 0;
        if(iz>=numBinsZ)
        {
            izfar = 1;
            iz = numBinsZ;
        }
        
        double bz0 = BzArray[iz][ir];
        double br0 = BrArray[iz][ir];
        
        double delz = 0.;
        double delr = 0.;
        
        double brdz = 0.;
        double brdr = 0.;
        
        if(r>0.0)
        {
            delr = r - ((float)ir) * gridSizeR;
            brdz = (BrArray[iz+1][ir]-br0)/gridSizeZ;
            brdr = (BrArray[iz][ir+1]-br0)/gridSizeR;
        }
        
        delz = abs(z) - ((float)iz) * gridSizeZ;
        
        double bzdz = (BzArray[iz+1][ir]-bz0)/gridSizeZ;
        double bzdr = (BzArray[iz][ir+1]-bz0)/gridSizeR;
        
        if(izfar==1)
        {
            hz = bz0+bzdr*delr;
            hr = br0+brdr*delr;
        }
        else
        {
            hz = bz0+bzdz*delz+bzdr*delr;
            hr = br0+brdz*delz+brdr*delr;
        }
        
        if(z<0.0) hr = -hr;
        
        double theta = atan2(y, x);
        double hx = hr * cos(theta);
        double hy = hr * sin(theta);
        
        field.setV(hx,hy,hz);
    }
        
    public final int getNumBinsR()
    {
        return numBinsR;
    }
    
    public final int getNumBinsZ()
    {
        return numBinsZ;
    }
    
    public final double getGridSizeR()
    {
        return gridSizeR;
    }
    
    public final double getGridSizeZ()
    {
        return gridSizeZ;
    }
    
    public final double getMaxZ()
    {
        return maxZ;
    }
    
    public final double getMaxR()
    {
        return maxR;
    }
}