package org.lcsim.event.base;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import java.util.Collections;
import java.util.Map;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Vertex;

/**
 * Default implementation of Vertex
 * @author Norman Graf
 * @version $Id: BaseVertex.java,v 1.2 2007/09/25 21:49:44 tonyj Exp $
 */
public class BaseVertex implements Vertex
{
    protected boolean _isPrimary ;
    protected String _type ;
    protected double _chi2 ;
    protected double _probability ;
    protected SymmetricMatrix _covarianceMatrix;
    protected Hep3Vector _position;
    protected ReconstructedParticle _aParticle ;   
    
    /** Creates a new instance of BaseVertex */
    // TODO decide whether to allow default constructor and setters, or limit to a fully qualified constructor.
    protected  BaseVertex()
    {
    }    
    public BaseVertex(boolean isPrimary, String type, double chi2, double prob, SymmetricMatrix cov, Hep3Vector pos, ReconstructedParticle rp)
    {
        _isPrimary = isPrimary;
        _type = type;
        _chi2 = chi2;
        _probability = prob;
        _covarianceMatrix = cov;
        _position = pos;
        _aParticle = rp;
    }
    
    public double getProbability()
    {
        return  _probability;
    }
    
    public ReconstructedParticle getAssociatedParticle()
    {
        return _aParticle;
    }
    
    public Map<String, Double> getParameters()
    {
        return Collections.<String, Double>emptyMap();
    }
    
    public Hep3Vector getPosition()
    {
        return _position;
    }
    
    public String getAlgorithmType()
    {
        return _type;
    }
    
//    public String toString()
//    {
//        String retValue;
//        
//        retValue = super.toString();
//        return retValue;
//    }
    
    public SymmetricMatrix getCovMatrix()
    {
        return _covarianceMatrix;
    }
    
    public boolean isPrimary()
    {
        return _isPrimary;
    }
    
    public double getChi2()
    {
        return _chi2;
    }
    
    // setters
    // TODO decide whether to allow separate setters.
//    public void setPrimary( boolean primary )
//    {
//        _isPrimary = primary;
//    }
//    //public void setAlgorithmType( int type ) ;
//    public void setAlgorithmType( String type )
//    {
//        _type = type;
//    }
//    public void setChi2( double chi2 )
//    {
//        _chi2 = chi2;
//    }
//    public void setProbability( double probability )
//    {
//        _probability = probability;
//    }
//    public void setPosition( Hep3Vector position )
//    {
//        _position = position;
//    }
//    public void setPosition( double x, double y, double z )
//    {
//        _position = new BasicHep3Vector(x, y, z);
//    }
//    public void setCovMatrix( SymmetricMatrix cov )
//    {
//        _covarianceMatrix = cov;
//    }
//    
//    public void setAssociatedParticle( ReconstructedParticle  rp )
//    {
//        _aParticle = rp;
//    }
}