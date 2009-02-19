package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IAst;

/**
 * A generic "Visitor" class for SGLR ASTs.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class AbstractVisitor implements IAstVisitor {
	
	public final boolean preVisit(IAst arg0) {
		return preVisit((AstNode) arg0);
	}

	public final void postVisit(IAst arg0) {
		postVisit((AstNode) arg0);
	}
}

// Local interface avoids abstract methods and subsequent @Override annotations

interface IAstVisitor extends lpg.runtime.IAstVisitor {
	boolean preVisit(AstNode node);

	void postVisit(AstNode node);
}