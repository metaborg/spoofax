package org.strategoxt.imp.runtime.services;

import java.util.List;
import java.util.Stack;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;

/**
 * Base class for an outliner.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class TreeModelBuilder extends TreeModelBuilderBase {
	
	/**
	 * A shadow copy of the outline stack maintained in the super class,
	 * holding all pushed AST nodes (rather than outline items).
	 * 
	 * (When not using a strongly typed visitor, this
	 *  should be the most efficient and simple way of determining
	 *  whether an outline item should be popped.)
	 */
	private final Stack<AstNode> treeStack = new Stack<AstNode>();
	
	private final List<NodeMapping> rules;
	
	private final ILabelProvider labelProvider;
	
	public TreeModelBuilder(List<NodeMapping> rules, ILabelProvider labelProvider) {
		this.rules = rules;
		this.labelProvider = labelProvider;
	}	
	
    private class TreeModelVisitor extends AbstractVisitor {
		public boolean preVisit(AstNode node) {
			if (NodeMapping.hasAttribute(rules, node.getConstructor(), node.getSort(), 0))
				startItem(node);
			
			return true;
		}

		public void postVisit(AstNode node) {
			endItem(node);
		}
	}
	
	// Interface implementation
	
	@Override
	public void visitTree(Object node) {
		((AstNode) node).accept(new TreeModelVisitor());
	}
	
	void startItem(AstNode node) {
		String label = labelProvider.getText(node);
		if (treeStack.isEmpty() && node instanceof RootAstNode) {
			// Skip the top node: already added by TreeModelBuilderBase
		} else if (label == null || label.length() == 0) {
			// Skip empty-label nodes
		} else {
			pushSubItem(node);
			treeStack.push(node);
		}
	}
	
	void endItem(AstNode node) {
		if (!treeStack.isEmpty() && treeStack.peek() == node) {
			popSubItem();
			treeStack.pop();
		}
	}
}
