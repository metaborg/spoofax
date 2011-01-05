package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.List;
import java.util.Stack;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;

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
	private final Stack<IStrategoTerm> treeStack = new Stack<IStrategoTerm>();
	
	private final List<NodeMapping> rules;
	
	private final ILabelProvider labelProvider;
	
	public TreeModelBuilder(List<NodeMapping> rules, ILabelProvider labelProvider) {
		this.rules = rules;
		this.labelProvider = labelProvider;
	}	
	
    private class TreeModelVisitor extends TermVisitor {
		public void preVisit(IStrategoTerm node) {
			IStrategoConstructor cons = tryGetConstructor(node);
			if (cons != null) {
				String consName = cons.getName();
				if (NodeMapping.hasAttribute(rules, consName, getSort(node), 0))
					startItem(node);
			}
		}

		@Override
		public void postVisit(IStrategoTerm node) {
			endItem(node);
		}
	}
	
	// Interface implementation
	
	@Override
	public void visitTree(Object node) {
		new TreeModelVisitor().visit((IStrategoTerm) node);
	}
	
	void startItem(IStrategoTerm node) {
		String label = labelProvider.getText(node);
		if (treeStack.isEmpty() && getParent(node) == null) {
			// Skip the top node: already added by TreeModelBuilderBase
		} else if (label == null || label.length() == 0) {
			// Skip empty-label nodes
		} else {
			pushSubItem(node);
			treeStack.push(node);
		}
	}
	
	void endItem(IStrategoTerm node) {
		if (!treeStack.isEmpty() && treeStack.peek() == node) {
			popSubItem();
			treeStack.pop();
		}
	}
}
