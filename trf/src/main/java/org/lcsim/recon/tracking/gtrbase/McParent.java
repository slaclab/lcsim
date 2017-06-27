package org.lcsim.recon.tracking.gtrbase;
// McParent

import java.util.Set;
import java.util.HashSet;
/**
 * This class describes the parents of a Monte Carlo
 * particle. To save space, only important parenthood
 * is preserved. This information is stored by setting
 * bits in the parentword. Users can either query the
 * McParent object for its pedigree through predefined
 * methods (e.g. boolean from_z() ), or request a set
 * of all parents registered.
 * Note that we use the PDG numbering scheme, and deal
 * with int values. This is not typesafe and it is up
 * to the user to ensure that the input and/or output
 * values are used correctly.
 * A better design would define an McId object which would
 * encapsulate the particular numbering scheme, ensure
 * compile-time integrity, and provide users flexibility
 * in their particular choice of numbering schemes.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class McParent
{
    
    // attributes
    
    // Version number
    private int _version;
    
    // Particle Parentage
    private int _parentword;
    
    //enums
    
    private static final int UDS=1;
    private static final int CHARM=2;
    private static final int CBAR=4;
    private static final int BOTTOM=8;
    private static final int BBAR=16;
    private static final int TOP=32;
    private static final int TBAR=64;
    private static final int ZZERO=128;
    private static final int WMINUS=256;
    private static final int WPLUS=512;
    private static final int PSI=1024;
    private static final int UPSILON=2048;
    private static final int TAUMINUS=4096;
    private static final int TAUPLUS=8192;
    
    /**
     * Construct a default instance.
     * Leaves object in an invalid state.
     *
     */
    public McParent()
    {
        _parentword = 0;
    }
    
    /**
     * Construct an instance from Parent word.
     *
     * @param   parentword The packed parent word.
     */
    public McParent( int parentword )
    {
        _parentword = parentword;
    }
    
    /**
     * Set the parentage, given the PDG ID of a parent
     * We currently support:
     *
     *          PDG Id
     * Z          23
     * W+         24
     * W-        -24
     * t           6
     * tbar       -6
     * b           5
     * bbar       -5
     * c           4
     * cbar       -4
     * uds         1,2,3
     * psi         443
     * upsilon     553
     * tau+       -15
     * tau-        15
     *
     *
     *
     * @param   pdgid The pdg id of the particles ancestor.
     */
    public void setParent(int pdgid)
    {
        switch(pdgid)
        {
            case 1:     // u
                _parentword = _parentword | UDS;
                break;
            case -1:     // ubar
                _parentword = _parentword | UDS;
                break;
            case 2:     // d
                _parentword = _parentword | UDS;
                break;
            case -2:     // dbar
                _parentword = _parentword | UDS;
                break;
            case 3:     // s
                _parentword = _parentword | UDS;
                break;
            case -3:     // sbar
                _parentword = _parentword | UDS;
                break;
            case 4:     // c
                _parentword = _parentword | CHARM;
                break;
            case -4:     // cbar
                _parentword = _parentword | CBAR;
                break;
            case 5:     // b
                _parentword = _parentword | BOTTOM;
                break;
            case -5:     // bbar
                _parentword = _parentword | BBAR;
                break;
            case 6:     // t
                _parentword = _parentword | TOP;
                break;
            case -6:     // tbar
                _parentword = _parentword | TBAR;
                break;
            case 23:     // Z
                _parentword = _parentword | ZZERO;
                break;
            case 24:     // W+
                _parentword = _parentword | WPLUS;
                break;
            case -24:     // W-
                _parentword = _parentword | WMINUS;
                break;
            case 443:     // psi
                _parentword = _parentword | PSI;
                break;
            case 553:     // upsilon
                _parentword = _parentword | UPSILON;
                break;
            case 15:     // tau-
                _parentword = _parentword | TAUMINUS;
                break;
            case -15:     // tau+
                _parentword = _parentword | TAUPLUS;
                break;
        }
    }
    
    /**
     *Return the parentword
     *
     * @return The integer parent word.
     */
    public int parentWord()
    {
        return _parentword;
    }
    
    /**
     * Return the list of parents for this track as
     * a set of integers representing the PDG particle ID.
     * Note that uds will return an ID=1 for brevity
     * Use a set to allow a quick "find" of a parent type.
     *
     * @return The list of this particles parents.
     */
    public Set parents()
    {
        Set s = new HashSet();
        if((_parentword & UDS)!=0) s.add(new Integer(1));
        if((_parentword & CHARM)!=0) s.add(new Integer(4));
        if((_parentword & CBAR)!=0) s.add(new Integer(-4));
        if((_parentword & BOTTOM)!=0) s.add(new Integer(5));
        if((_parentword & BBAR)!=0) s.add(new Integer(-5));
        if((_parentword & TOP)!=0) s.add(new Integer(6));
        if((_parentword & TBAR)!=0) s.add(new Integer(-6));
        if((_parentword & ZZERO)!=0) s.add(new Integer(23));
        if((_parentword & WMINUS)!=0) s.add(new Integer(24));
        if((_parentword & WPLUS)!=0) s.add(new Integer(-24));
        if((_parentword & PSI)!=0) s.add(new Integer(443));
        if((_parentword & UPSILON)!=0) s.add(new Integer(553));
        if((_parentword & TAUMINUS)!=0) s.add(new Integer(15));
        if((_parentword & TAUPLUS)!=0) s.add(new Integer(-15));
        
        return s;
        
    }
    
    /**
     *Is particle from Z?
     *
     * @return true if a Z boson is in this particles history.
     */
    public boolean from_z()
    {
        return (_parentword & ZZERO)!=0;
    }
    
    /**
     *Is particle from W+?
     *
     * @return true if a W+ boson is in this particles history.
     */
    public boolean from_w_plus()
    {
        return (_parentword & WPLUS)!=0;
    }
    
    /**
     *Is particle from W-?
     *
     * @return true if a W- boson is in this particles history.
     */
    public boolean from_w_minus()
    {
        return (_parentword & WMINUS)!=0;
    }
    
    /**
     *Is particle from tau+?
     *
     * @return true if a tau+ is in this particles history.
     */
    public boolean from_tau_plus()
    {
        return (_parentword & TAUPLUS)!=0;
    }
    
    /**
     *Is particle from tau-?
     *
     * @return true if a tau- is in this particles history.
     */
    public boolean from_tau_minus()
    {
        return (_parentword & TAUMINUS)!=0;
    }
    
    /**
     *Is particle from psi?
     *
     * @return true if a psi is in this particles history.
     */
    public boolean from_psi()
    {
        return (_parentword & PSI)!=0;
    }
    
    /**
     *Is particle from upsilon?
     *
     * @return true if a upsilon is in this particles history.
     */
    public boolean from_upsilon()
    {
        return (_parentword & UPSILON)!=0;
    }
    
    /**
     *Is particle from top quark?
     *
     * @return true if a top quark is in this particles history.
     */
    public boolean from_t()
    {
        return (_parentword & TOP)!=0;
    }
    
    /**
     *Is particle from anti-top quark?
     *
     * @return  true if a top antiquark is in this particles history.
     */
    public boolean from_t_bar()
    {
        return (_parentword & TBAR)!=0;
    }
    
    /**
     *Is particle from bottom quark?
     *
     * @return true if a bottom quark is in this particles history.
     */
    public boolean from_b()
    {
        return (_parentword & BOTTOM)!=0;
    }
    
    /**
     *Is particle from anti-bottom quark?
     *
     * @return true if a bottom antiquark is in this particles history.
     */
    public boolean from_b_bar()
    {
        return (_parentword & BBAR)!=0;
    }
    
    /**
     *Is particle from charm quark?
     *
     * @return true if a charm quark is in this particles history.
     */
    public boolean from_c()
    {
        return (_parentword & CHARM)!=0;
    }
    
    /**
     *Is particle from anti-charm quark?
     *
     * @return true if a charm antiquark is in this particles history.
     */
    public boolean from_c_bar()
    {
        return (_parentword & CBAR)!=0;
    }
    
    /**
     * Is particle from u,d or s quark?
     *
     * @return true if a light quark is in this particles history.
     */
    public boolean from_uds()
    {
        return (_parentword & UDS)!=0;
    }
    
}

