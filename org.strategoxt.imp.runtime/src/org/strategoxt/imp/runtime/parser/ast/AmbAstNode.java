package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbAstNode extends AstNode {

	public AmbAstNode(ListAstNode children) {
		super("amb", children.getElementSort(), children.getLeftIToken(),
				children.getRightIToken(), makeList(children));
	}

	public AmbAstNode(ArrayList<AstNode> children) {
		this(new ListAstNode(children.get(0).getSort(), children.get(0).getLeftIToken(),
				children.get(0).getRightIToken(), children));
	}
	
	private static ArrayList<AstNode> makeList(AstNode node) {
		ArrayList<AstNode> result = new ArrayList<AstNode>();
		result.add(node);
		return result;
	}

}
