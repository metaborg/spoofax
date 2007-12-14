package org.strategoxt.imp.runtime.parser.ast;

/**
 * A generic "Visitor" class for SGLR ASTs.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class AbstractVisitor {
	public boolean preVisit(SGLRAstNode node) {
		return true;
	}

	public void postVisit(SGLRAstNode node) {
		
	}
	
	// TODO: Should AbstractVisitor have special 
	
	public abstract boolean visit(SGLRAstNode node);

	public abstract void endVisit(SGLRAstNode node);
}
