package org.strategoxt.imp.runtime.services;

import java.util.ArrayList;
import java.util.Stack;

import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym;

public abstract class OutlinerBase extends org.eclipse.imp.services.base.OutlinerBase {
	/*
	 * A shadow copy of the outline stack maintained in the super class,
	 * holding all pushed AST nodes (rather than outline items).
	 * 
	 * (When not using a strongly typed visitor, this
	 *  should be the most efficient and simple way of determining
	 *  whether an outline item should be popped.)
	 */
	private Stack<AstNode> outlineStack = new Stack<AstNode>();
	
	// Interface implementation
	
	@Override
	protected final void sendVisitorToAST(Object node) {
		sendVisitorToAST((AstNode) node);
	}
	
	protected abstract void sendVisitorToAST(AstNode node);
	
	// Helper methods
	
	// These help select an outline item's caption,
	// in case the outline descriptor didn't include a pattern
	// to find it.
	
	protected void outline(AstNode node) {
		int index = getIdentifierIndex(node);
		if (index != -1) {
			pushSubItem(node.getChildren().get(0).getLeftIToken().toString(), node);
			outlineStack.push(node);
		}
	}
	
	protected void endOutline(AstNode node) {
		if (outlineStack.peek() == node) {
			popSubItem();
			outlineStack.pop();
		}
	}
	
	// TODO2: Optimize - cache getIdentifierIndex
	
	private int getIdentifierIndex(AstNode node) {
		ArrayList<? extends AstNode> children = node.getChildren();
		
		for (int i = 0; i < children.size(); i++) {
			AstNode child = children.get(i);
			if (isIdentifier(child)) return i;
		}
		
		return -1;
	}
	
	private boolean isIdentifier(AstNode node) {
		while (node.getChildren().size() == 1)
			node = node.getChildren().get(0);
		
		return node.getChildren().size() == 0 && node.getLeftIToken().getKind() == SGLRParsersym.TK_IDENTIFIER;
	}
}
