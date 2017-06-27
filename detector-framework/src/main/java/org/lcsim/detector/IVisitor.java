package org.lcsim.detector;

/**
 * Generic interface for the Visitor pattern.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu> 
 */
public interface IVisitor<T>
{
    /**
     * Perform some action on a node.
     * 
     * @param object The object to be visited.
     */
	public void visit(T object);
    
    /**
     * True if the traversal should stop.
     * 
     * @return True if the traversal should stop.
     */
    public boolean isDone();
}
