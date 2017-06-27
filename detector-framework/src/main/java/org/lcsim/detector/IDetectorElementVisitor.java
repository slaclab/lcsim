package org.lcsim.detector;

/**
 * Visitor pattern for {@link org.lcsim.IDetectorElement}, used for 
 * performing an action on nodes in the detector tree.
 *
 * @author Jeremy McCormick
 * @version $Id: IDetectorElementVisitor.java,v 1.2 2010/04/14 17:52:31 jeremy Exp $
 */

public interface IDetectorElementVisitor
extends IVisitor<IDetectorElement>
{}
