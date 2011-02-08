package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbAstNode extends IStrategoTerm {

	public AmbAstNode(ListAstNode children) {
		super(children.getElementSort(), getLeftToken(children), getRightToken(children),
				"amb", makeList(children));
	}

	public AmbAstNode(ArrayList<IStrategoTerm> children) {
		this(new ListAstNode(children.get(0).getSort(), children.get(0).getLeftToken(),
				children.get(0).getRightToken(), children));
	}
	
	private static ArrayList<IStrategoTerm> makeList(IStrategoTerm node) {
		ArrayList<IStrategoTerm> result = new ArrayList<IStrategoTerm>(1);
		result.add(node);
		return result;
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.APPL;
	}

}
