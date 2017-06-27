package org.lcsim.recon.tracking.trfbase;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import org.lcsim.recon.tracking.trfutil.Pair;
/** This is propagator dispatcher.  Propagators are registered for each
 * pair of initial and final surface types.  The appropriate propagator
 * is then invoked when any of the usual propagator methods are called.
 *
 * @author Norman A. Graf
 * @version 1.0
 */


public class PropDispatch extends Propagator
{
    
        /*
        private:  // typedefs
         
        // A pair of surface types.
        typedef pair<Type,Type> TypePair;
         
        // Comparator for surface type pairs.
        typedef less<TypePair> Cmp;
         
        // Map of propagators indexed by surface type pairs.
        typedef map<TypePair,PropagatorPtr,Cmp> PropMap;
         */
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return   String representation of this class type
     *Included for completeness with C++ code
     */
    public static String typeName()
    {
        return "PropDispatch";
    }
    
    
    //
    
    /**
     *Return the type.
     *
     * @return  tring representation of this class type
     *Included for completeness with C++ code
     */
    public static String staticType()
    {
        return typeName();
    }
    // attributes
    
    // map of propagators
    private Map _props;
    
    // methods
    
    //
    
    /**
     *output stream
     *
     * @return  String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+"\n");
        Set keys = _props.keySet();
        Set entries = _props.entrySet();
        Iterator keyit;
        Iterator entriesit;
        for(entriesit = entries.iterator(); entriesit.hasNext(); )
        {
            sb.append(entriesit.next()+"\n");
        }
        return sb.toString();
    }
    
    //
    /**
     *constructor
     *
     *
     */
    public PropDispatch()
    {
        _props = new HashMap();
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of this class type
     *Included for completeness with C++ code
     *
     */
    public String  type()
    {
        return staticType();
    }
    
    //
    
    /**
     *register a propagator
     * return nonzero for error (e.g. surface pair already used)
     *
     * @param   stype1  first Surface type
     * @param   stype2  econd Surface type
     * @param   prop  Propagator to use
     * @return  0 if this combination not yet added
     */
    public int addPropagator(String stype1, String stype2, Propagator prop)
    {
        Pair types = new Pair(stype1,stype2);
        if ( _props.containsKey(types) ) return 1;
        _props.put(types, prop);
        return 0;
    }
    
    //
    
    /**
     *Return the number of registrations.
     *
     * @return number of registered propagators
     */
    public int propagatorCount()
    {
        return _props.size();
    }
    
    //
    
    /**
     *Return the propagator for a specified pair of surfaces.
     *
     * @param   stype1  first Surface type
     * @param   stype2  second Surface type
     * @return  Propagator for this pair of Surfaces
     */
    public Propagator propagator(String stype1, String stype2)
    {
        Pair types = new Pair(stype1,stype2);
        if ( !_props.containsKey(types)  ) throw new IllegalArgumentException("Propagator for surface pair not defined!");
        return (Propagator) _props.get(types);
    }
    
    //
    
    /**
     *Clone
     *
     * @return  new copy of this Propagator
     */
    public Propagator newPropagator()
    {
        throw new IllegalArgumentException("Clone not supported!");
        // 	return new PropDispatch();
    }
    
    //
    
    /**
     *propagate a track without error
     *
     * @param   trv  VTrack to propagate
     * @param   srf  Surface to propagate to
     * @return     propagation status
     */
    public PropStat vecProp(VTrack trv, Surface srf)
    {
        TrackDerivative der = null;
        return vecProp(trv,srf,der);
    }
    
    /**
     *propagate a track without error
     *
     * @param   trv  VTrack to propagate
     * @param   srf  Surface to propagate to
     * @param   der  TrackDerivative to update at Surface srf
     * @return   propagation status
     */
    public PropStat vecProp(VTrack trv, Surface srf,
            TrackDerivative der )
    {
        String stype1 = trv.surface().pureType();
        String stype2 = srf.pureType();
        Pair types = new Pair(stype1,stype2);
        if ( !_props.containsKey(types)  ) return new PropStat();
        return ((Propagator) _props.get(types)).vecProp(trv,srf,der);
    }
    
    //
    
    /**
     *propagate a track without error in the specified direction
     *
     * @param   trv VTrack to propagate
     * @param   srf  Surface to propagate to
     * @param   dir  direction in which to propagate
     * @return  propagation status
     */
    public PropStat vecDirProp( VTrack trv, Surface srf, PropDir dir)
    {
        TrackDerivative der = null;
        return vecDirProp(trv,srf,dir,der);
    }
    
    /**
     *propagate a track without error in the specified direction
     *
     * @param   trv VTrack to propagate
     * @param   srf  Surface to propagate to
     * @param   dir  direction in which to propagate
     * @param   der  TrackDerivative to update at Surface srf
     * @return   propagation status
     */
    public PropStat vecDirProp( VTrack trv, Surface srf,
            PropDir dir, TrackDerivative der )
    {
        String stype1 = trv.surface().pureType();
        String stype2 = srf.pureType();
        Pair types = new Pair(stype1,stype2);
        if ( !_props.containsKey(types)  ) return new PropStat();
        return ((Propagator) _props.get(types)).vecDirProp(trv,srf,dir,der);
    }
    
    //
    
    /**
     *propagate a track with error
     *
     * @param   trv  ETrack to propagate
     * @param   srf  Surface to propagate to
     * @return  propagation status
     */
    public PropStat errProp(ETrack trv, Surface srf)
    {
        TrackDerivative der = null;
        return errProp(trv,srf,der);
    }
    
    /**
     *propagate a track with error
     *
     * @param   trv  ETrack to propagate
     * @param   srf  Surface to propagate to
     * @param   der TrackDerivative to update at Surface srf
     * @return   propagation status
     */
    public PropStat errProp(ETrack trv, Surface srf,
            TrackDerivative der )
    {
        String stype1 = trv.surface().pureType();
        String stype2 = srf.pureType();
        Pair types = new Pair(stype1,stype2);
        if ( !_props.containsKey(types)  ) return new PropStat();
        return ((Propagator) _props.get(types)).errProp(trv,srf,der);
    }
    
    //
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   trv  ETrack to propagate
     * @param   srf  Surface to propagate to
     * @param   dir direction in which to propagate
     * @return    propagation status
     */
    public PropStat errDirProp(ETrack trv, Surface srf, PropDir dir)
    {
        TrackDerivative der = null;
        return errDirProp(trv,srf,dir,der);
    }
    
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   trv  ETrack to propagate
     * @param   srf  Surface to propagate to
     * @param   dir direction in which to propagate
     * @param   der TrackDerivative to update at Surface srf
     * @return  propagation status
     */
    public PropStat errDirProp(ETrack trv, Surface srf, PropDir dir,
            TrackDerivative der )
    {
        String stype1 = trv.surface().pureType();
        String stype2 = srf.pureType();
        Pair types = new Pair(stype1,stype2);
        if ( !_props.containsKey(types)  ) return new PropStat();
        return ((Propagator) _props.get(types)).errDirProp(trv,srf,dir,der);
    }
    
}