package org.lcsim.recon.tracking.trfbase;
/**
 * A track vector with error.
 *
 * Data includes a surface pointer _srf, 5-vector _vec and 5 x 5
 * symmetric error matrix _err.  All are managed internally.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class ETrack extends VTrack
{
    
    // attributes
    
    private TrackError _err;
    
    // methods
    
    //
    
    /**
     *default constructor
     *
     */
    public ETrack()
    {
        _err = new TrackError();
    }
    
    //
    
    /**
     * constructor from a surface
     *
     * @param  srf  Surface srf at which track is to be defined
     */
    public ETrack( Surface srf)
    {
        super(srf);
        _err = new TrackError();
    }
    
    //
    
    /**
     *constructor from a surface, track and error matrix
     *
     * @param srf   Surface srf at which track is to be defined
     * @param  vec TrackVector vec containing track parameters
     * @param  err  TrackError err  containing track parameter errors
     */
    public ETrack( Surface srf,  TrackVector vec,
    TrackError err)
    {
        super(srf, vec);
        _err = new TrackError(err);
    }
    
    //
    
    /**
     *constructor from a surface, track, error matrix and direction
     *
     * @param srf   Surface srf at which track is to be defined
     * @param  vec  TrackVector vec containing track parameters
     * @param  err TrackError err  containing track parameter errors
     * @param  dir  TrackSurfaceDirection dir direction of track relative to the Surface
     */
    public ETrack( Surface srf,  TrackVector vec,
    TrackError err, TrackSurfaceDirection dir)
    {
        super(srf, vec, dir);
        _err = new TrackError(err);
    }
    
    //
    
    /**
     *Constructor from a VTrack and an error matrix.
     *
     * @param  trv VTrack trv representing track without errors
     * @param  err  TrackError err  containing track parameter errors
     */
    public ETrack( VTrack trv,  TrackError err)
    {
        super(trv);
        _err = new TrackError(err);
    }
    
    //
    
    /**
     *copy constructor
     *
     * @param  tre  ETrack tre  to be copied
     */
    public ETrack( ETrack tre)
    {
        super(tre);
        _err = new TrackError(tre.error());
    }
    
    //
    
    /**
     *set the error matrix for the track
     *
     * @param  newerr  TrackError newerr containg error matrix
     */
    public void setError( TrackError newerr)
    {
        _err = new TrackError(newerr);
    }
    
    
    
    /**
     * String representation of ETrack
     *
     * @return String representation of ETrack
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+" \n"+super.toString());
        //		return "ETrack: \n"+super.toString()+_err;
        
        if ( isValid() )
        {
            //stream << new_line << _err;
            sb.append("Diagonal Sigmas:\n");
            TrackVector derr = new TrackVector();
            for ( int i=0; i<5; ++i )
            {
                derr.set(i, Math.sqrt(_err.get(i,i)));
            }
            sb.append(derr.toString());
            sb.append("Normalized Covariance Matrix:\n");
            //    stream << new_line << derr;
            TrackError nerr = new TrackError(_err);
            nerr.normalize();
            sb.append(" "+nerr);
            //    stream << new_line << nerr;
        }
        
        return sb.toString();
    }
    
    
    //
    
    /**
     *get error matrix
     *
     * @return TrackError error matrix for track parameters
     */
    public  TrackError error()
    {
        return new TrackError(_err);
    }
    
    //
    
    /**
     *get a component of the error matrix
     *
     * @param   i first index
     * @param   j second index
     * @return  value of error matrix element (i,j)
     */
    public double error(int i, int j)
    {
        if( !(i>=0 && i<5) || !(j>=0 && j<5) )
        {
            throw new IllegalArgumentException("Matrix indices must be within [0,4]!");
        }
        return _err.matrix()[i][j];
    }
    
    //
    
    
    /**
     *Return the number of bad entries in the error matrix.
     *
     * @return the number of bad entries in the error matrix
     */
    public int checkError()
    {
        int nbad = 0;
        for ( int i=0; i<5; ++i )
        {
            double eii = _err.matrix()[i][i];
            if ( eii <= 0.0 ) ++nbad;
            for ( int j=0; j<i; ++j )
            {
                double ejj = _err.matrix()[j][j];
                double eij = _err.matrix()[j][i];
                if ( Math.abs(eij*eij) >= eii*ejj ) ++nbad;
            }
        }
        return nbad;
    }
    
    
    /** equality
     *
     * @return true if this equals tre
     * @param tre ETrack to compare
     */
    public boolean equals(ETrack tre)
    {
        if ( !surface().equals(tre.surface()) ) return false;
        if ( !vector().equals(tre.vector()) ) return false;
        if ( !error().equals(tre.error()) ) return false;
        return true;
    }
    
    
    /** Inequality
     *
     * @return true if this does not equal tre
     * @param tre ETrack to compare
     */
    public boolean notEquals(ETrack tre)
    {
        return !equals(tre);
    }
    
    //**********************************************************************
    
    //
    
    /** Return the difference between two track vectors with errors
     * // weighted by their combined error matrix.
     *
     * @return chisquare difference of trv1 and trv2
     * @param trv1 first ETrack
     * @param trv2 second ETrack
     */
    public static double chisqDiff(  ETrack trv1,   ETrack trv2 )
    {
        // If surfaces are not the same, return -1.0.
        if ( !trv1.surface().equals(trv2.surface()) ) return -1.0;
        TrackVector vecdif =
        trv1.surface().vecDiff( trv1.vector(), trv2.vector() );
        TrackError errsum = trv1.error().plus(trv2.error());
        //  if ( invert(errsum) ) return -2.0;
        return vecdif.getMatrix().transpose().times( errsum.inverse().getMatrix().times( vecdif.getMatrix() ) ).get(0,0);
    }
    
    //**********************************************************************
    
    //
    
    /** Get the difference between track vectors with and without errors
     * weighted by the error matrix.
     *
     * @return chisquare difference of trv1 and trv2
     * @param trv1 first ETrack
     * @param trv2 second ETrack
     */
    public static double chisqDiff(  ETrack trv1,   VTrack trv2 )
    {
        // If surfaces are not the same, return -1.0.
        if ( !trv1.surface().equals(trv2.surface()) ) return -1.0;
        TrackVector vecdif =
        trv1.surface().vecDiff( trv1.vector(), trv2.vector() );
        TrackError errsum = trv1.error();
        //  if ( invert(errsum) ) return -2.0;
        //  return chisq_diff(vecdif,errsum);
        return vecdif.getMatrix().transpose().times( errsum.inverse().getMatrix().times( vecdif.getMatrix() ) ).get(0,0);
    }
    
    //**********************************************************************
    
    
    /** Get the difference between track vectors with and without errors
     * weighted by the error matrix.
     *
     * @return chisquare difference of trv1 and trv2
     * @param trv1 first VTrack
     * @param trv2 second VTrack
     */
    public static double chisqDiff(  VTrack trv1,   ETrack trv2 )
    {
        return chisqDiff(trv2,trv1);
    }
    
    
}

/*
// External functions.
 
// equality
bool operator==( ETrack trv1,  ETrack trv2);
 
// inequality
bool operator!=( ETrack trv1,  ETrack trv2);
 
// Get the difference between two track vectors with errors
// weighted by their combined error matrix.
double chisq_diff( ETrack trv1,  ETrack trv2 );
 
// Get the difference between two track vectors with and
// without errors weighted by the error matrix.
double chisq_diff( ETrack trv1,  VTrack trv2 );
 
// Get the difference between two track vectors without and
// with errors weighted by the error matrix.
double chisq_diff( VTrack trv1,  ETrack trv2 );
 
 
 
// equality
bool operator==(const ETrack& trv1, const ETrack& trv2) {
  if ( *trv1.get_surface() != *trv2.get_surface() ) return false;
  if ( trv1.get_vector() != trv2.get_vector() ) return false;
  if ( trv1.get_error() != trv2.get_error() ) return false;
  return true;
}
 
//**********************************************************************
 
// inequality
bool operator!=(const ETrack& trv1, const ETrack& trv2) {
  return ! ( trv1 == trv2 );
}
 */
