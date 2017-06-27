package org.lcsim.detector.identifier;


/**
 * 
 * Implementation of {@link IIdentifierHelper}.
 *
 * @author Jeremy McCormick
 * @version $Id: IdentifierHelper.java,v 1.16 2011/02/25 03:09:38 jeremy Exp $
 */

public class IdentifierHelper implements IIdentifierHelper {
    private IIdentifierDictionary iddict = null;

    /*
     * public IExpandedIdentifier unpack( IIdentifier id, int start, int nfields) { return IdentifierUtil.unpack( iddict, id, start, nfields ); }
     * 
     * public IExpandedIdentifier unpack( IIdentifier id, int start) { return IdentifierUtil.unpack( iddict, id, start ); }
     * 
     * public IExpandedIdentifier unpack(IIdentifier compact, List<Integer> indices) { return IdentifierUtil.unpack(iddict, compact, indices); }
     */

    public IdentifierHelper(IIdentifierDictionary iddict) {
        this.iddict = iddict;
    }

    public IIdentifierDictionary getIdentifierDictionary() {
        return iddict;
    }

    public IIdentifier pack(IExpandedIdentifier id) {
        // return IdentifierUtil.pack(getIdentifierDictionary(), id );
        return iddict.pack(id);
    }

    public IExpandedIdentifier unpack(IIdentifier id) {
        // return IdentifierUtil.unpack( iddict, id );
        return iddict.unpack(id);
    }

    /*
     * public int getValue( IIdentifier compact, IIdentifierField desc ) { //return IdentifierUtil.getValue( compact, desc ); return iddict.getFieldValue(compact, desc.getLabel()); }
     */

    public int getValue(IIdentifier compact, int idx) {
        // return IdentifierUtil.getValue( iddict, compact, field );
        return iddict.getFieldValue(compact, idx);
    }

    /*
     * public void setValue(IExpandedIdentifier expid, String field, int value) { expid.setValue(getFieldIndex(field), value); }
     * 
     * public void setValue(IExpandedIdentifier expid, int index, int value) { expid.setValue(index, value); }
     */

    public int getFieldIndex(String fieldName) {
        return iddict.getFieldIndex(fieldName);
    }

    public boolean hasField(String fieldName) {
        return iddict.hasField(fieldName);
    }

    public int getValue(IIdentifier compact, String field) {
        // return IdentifierUtil.getValue( iddict, compact, field );
        return iddict.getField(field).unpack(compact);
    }
    
    public IExpandedIdentifier createExpandedIdentifier() {
        return new ExpandedIdentifier(getIdentifierDictionary().getNumberOfFields());
    }

    /*
     * public IIdentifier pack(IExpandedIdentifier id, int start) { return IdentifierUtil.pack( iddict, id, start ); }
     * 
     * public IIdentifier pack(IExpandedIdentifier id, int start, int nfields) { return IdentifierUtil.pack( iddict, id, start, nfields ); }
     */
}