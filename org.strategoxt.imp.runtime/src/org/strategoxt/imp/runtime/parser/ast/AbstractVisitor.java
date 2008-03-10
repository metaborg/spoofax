package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IAst;
import lpg.runtime.IAstVisitor;

/**
 * A generic "Visitor" class for SGLR ASTs.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class AbstractVisitor implements IAstVisitor {
	public boolean preVisit(AstNode node) {
		return true;
	}

	public void postVisit(AstNode node) {
		
	}
	
	@Override
	public boolean preVisit(IAst arg0) {
		return preVisit((AstNode)arg0);
	}

	@Override
	public void postVisit(IAst arg0) {
		postVisit((AstNode)arg0);
	}

	// TODO: Should AbstractVisitor have special 
	
	public abstract boolean visit(AstNode node);

	public abstract void endVisit(AstNode node);
}
