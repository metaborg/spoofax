package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

/**
 * An artificial partial list AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SubListAstNode extends ListAstNode {

	private final ListAstNode completeList;

	public SubListAstNode(ListAstNode completeList, String elementSort, IToken leftToken, IToken rightToken, ArrayList<AstNode> children) {
		super(elementSort, leftToken, rightToken, children);
		this.completeList = completeList;
	}

	public ListAstNode getCompleteList() {
		return completeList;
	}
	
	public AstNode getFirstChild() {
		return getChildren().get(0);
	}
	
	public AstNode getLastChild() {
		return getChildren().get(getChildren().size() - 1);
	}
}
