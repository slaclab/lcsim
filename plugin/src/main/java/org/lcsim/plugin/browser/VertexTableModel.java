/*
 * VertexTableModel.java
 *
 * Created on September 20, 2007, 6:58 AM
 *
 * $Id: VertexTableModel.java,v 1.1 2007/09/24 21:03:00 ngraf Exp $
 */

package org.lcsim.plugin.browser;

import org.lcsim.event.Vertex;

/**
 *
 * @author Norman Graf
 */
public class VertexTableModel extends GenericTableModel
{
    private static final String[] columns ={  "Position", "Chi2", "Probability", "AlgorithmType"};
    private static Class klass = Vertex.class;
    /** Creates a new instance of VertexTableModel */
    public VertexTableModel()
    {
        super(klass,columns);
    }
}
