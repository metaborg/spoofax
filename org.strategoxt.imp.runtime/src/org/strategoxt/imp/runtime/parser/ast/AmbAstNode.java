package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbAstNode extends AstNode {

	public AmbAstNode(ArrayList<AstNode> children) {

		super("amb", "amb", children.get(0).getLeftIToken(),
				children.get(children.size() - 1).getRightIToken(), children);
	}

}
