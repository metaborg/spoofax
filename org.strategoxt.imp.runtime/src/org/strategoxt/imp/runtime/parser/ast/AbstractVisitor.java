package org.strategoxt.imp.runtime.parser.ast;

/**
 * A generic "Visitor" class for SGLR ASTs.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class AbstractVisitor {
	public boolean preVisit(AstNode node) {
		return true;
	}

	public void postVisit(AstNode node) {
		
	}
	
	// TODO: Should AbstractVisitor have special 
	
	public abstract boolean visit(AstNode node);

	public abstract void endVisit(AstNode node);
}
