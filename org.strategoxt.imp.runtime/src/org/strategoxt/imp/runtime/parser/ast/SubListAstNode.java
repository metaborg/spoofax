package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

import lpg.runtime.IToken;

/**
 * An artificial partial list AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SubListAstNode extends ListAstNode {

	private final ListAstNode completeList;

	private int indexStart;

	public int getIndexStart() {
		return indexStart;
	}

	public int getIndexEnd() {
		return indexStart + this.getChildren().size()-1;
	}

	public SubListAstNode(ListAstNode completeList, String elementSort, IToken leftToken,
			IToken rightToken, ArrayList<AstNode> children, int indexStart) {
		super(elementSort, leftToken, rightToken, children);
		this.completeList = completeList;
		this.indexStart = indexStart;
	}

	public ListAstNode getCompleteList() {
		return completeList;
	}
}
