package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IAst;
import lpg.runtime.IAstVisitor;

/**
 * A generic "Visitor" class for SGLR ASTs.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class AbstractVisitor implements IAstVisitor {
	
	public abstract boolean preVisit(AstNode node);

	public abstract void postVisit(AstNode node);
	
	@Override
	public final boolean preVisit(IAst arg0) {
		return preVisit((AstNode) arg0);
	}

	@Override
	public final void postVisit(IAst arg0) {
		postVisit((AstNode) arg0);
	}
}
