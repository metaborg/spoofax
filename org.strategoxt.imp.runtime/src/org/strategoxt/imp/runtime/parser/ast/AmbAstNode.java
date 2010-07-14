package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbAstNode extends AstNode {

	public AmbAstNode(ListAstNode children) {
		super(children.getElementSort(), children.getLeftIToken(), children.getRightIToken(),
				"amb", makeList(children));
	}

	public AmbAstNode(ArrayList<AstNode> children) {
		this(new ListAstNode(children.get(0).getSort(), children.get(0).getLeftIToken(),
				children.get(0).getRightIToken(), children));
	}
	
	private static ArrayList<AstNode> makeList(AstNode node) {
		ArrayList<AstNode> result = new ArrayList<AstNode>(1);
		result.add(node);
		return result;
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.APPL;
	}

}
