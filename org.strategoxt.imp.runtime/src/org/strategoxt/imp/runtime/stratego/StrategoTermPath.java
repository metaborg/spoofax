package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * Maintains aterm paths, lists of nodes on the path to the root from a given AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoTermPath {

	private StrategoTermPath() {
		// Constructed by a static constructor
	}
	
	public static IStrategoList createPath(IStrategoAstNode node) {
		LinkedList<IStrategoTerm> results = new LinkedList<IStrategoTerm>();
		
		while (node.getParent() != null) {
			IStrategoAstNode parent = node.getParent();
			ArrayList children = parent.getChildren();
			int index = indexOfIdentical(children, node);
			results.addFirst(Environment.getTermFactory().makeInt(index));
			node = node.getParent();
		}
		
		return Environment.getTermFactory().makeList(results);
	}
	
	public static List<Integer> createPathList(IStrategoAstNode node) {
		LinkedList<Integer> results = new LinkedList<Integer>();
		
		while (node.getParent() != null) {
			IStrategoAstNode parent = node.getParent();
			int index = indexOfIdentical(parent.getChildren(), node);
			results.addFirst(Integer.valueOf(index));
			node = node.getParent();
		}
		
		return results;
	}

	private static int indexOfIdentical(List<?> children, IStrategoAstNode node) {
		int index = 0;
		for (int size = children.size(); index < size; index++) {
			if (children.get(index) == node) break;
		}
		return index;
	}

	/**
	 * Attempts to find a corresponding selection subtree in a new ast.
	 */
	public static IStrategoAstNode findCorrespondingSubtree(IStrategoAstNode newAst, IStrategoAstNode selection) {
		IStrategoAstNode oldAst = getRoot(selection);
		IStrategoAstNode oldParent = oldAst;
		IStrategoAstNode newParent = newAst;
		
		List<Integer> selectionPath = createPathList(selection);
		
		for (int c = 0, size = selectionPath.size(); c < size; c++) {
			int i = selectionPath.get(c);
			if (i >= oldParent.getChildren().size()) { // Shouldn't happen
				Environment.logException("Unable to recover old selection AST in " + oldParent, new ArrayIndexOutOfBoundsException(i));
				return findIdenticalSubtree(oldAst, newAst, selection);
			}
			IStrategoAstNode oldSubtree = (IStrategoAstNode) oldParent.getChildren().get(i);
			IStrategoAstNode newSubtree;
			if (i >= newParent.getChildren().size()) {
				if (i == 0) // fallback
					return findIdenticalSubtree(oldAst, newAst, selection);
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(--i);
			} else if (i > newParent.getChildren().size()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else {
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(i);
			}
			if (!oldSubtree.getConstructor().equals(newSubtree.getConstructor())) {
				// First, try siblings instead
				if (i + 1 < newParent.getChildren().size()) {
					newSubtree = (IStrategoAstNode) newParent.getChildren().get(i + 1);
					if (oldSubtree.getConstructor().equals(newSubtree.getConstructor()))
						continue;
				}
				if (i > 0) {
					newSubtree = (IStrategoAstNode) newParent.getChildren().get(i - 1);
					if (oldSubtree.getConstructor().equals(newSubtree.getConstructor()))
						continue;
				}
				// Fallback
				return findIdenticalSubtree(oldAst, newAst, selection);
			}
			
			if (c == size - 1) { 
				return findCorrespondingSubtreeResult(oldAst, newAst, oldSubtree, newSubtree,
						selection, newParent, i);
			}
			oldParent = oldSubtree;
			newParent = newSubtree;
		}
		
		return newAst;
	}

	private static IStrategoAstNode findCorrespondingSubtreeResult(
			IStrategoAstNode oldAst, IStrategoAstNode newAst,
			IStrategoAstNode oldSubtree, IStrategoAstNode newSubtree,
			IStrategoAstNode selection, IStrategoAstNode newParent, int i) {
		
		if (!newSubtree.equals(oldSubtree)) {
			// First, try siblings instead
			if (i + 1 < newParent.getChildren().size()) {
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(i + 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}
			if (i > 0) {
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(i - 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}

			IStrategoAstNode exactMatch = findIdenticalSubtree(oldAst, newAst, selection);
			if (exactMatch != null) return exactMatch;

			newSubtree = (IStrategoAstNode) newParent.getChildren().get(i);
			if (newSubtree.getConstructor().equals(oldSubtree.getConstructor())
					&& (containsMultipleCopies(newAst, newSubtree)
					    || findSubtree(oldAst, newSubtree, true) == null)) {
				return newSubtree; // meh, close enough
			} else {
				return null;
			}
		}
		return newSubtree;
	}

	private static IStrategoAstNode getRoot(IStrategoAstNode selection) {
		IStrategoAstNode result = selection;
		while (result.getParent() != null)
			result = result.getParent();
		return result;
	}

	/**
	 * Finds a unique, identical 'selection' subtree in 'newAst.'
	 * Returns null in case of multiple candidate subtrees.
	 */
	private static IStrategoAstNode findIdenticalSubtree(IStrategoAstNode oldAst,
			IStrategoAstNode newAst, IStrategoAstNode selection) {
		
		if (containsMultipleCopies(oldAst, selection)) {
			return null;
		} else {
			return findSubtree(newAst, selection, false);
		}
	}

	/**
	 * Finds a single subtree equal to selection in the given ast.
	 */
	private static IStrategoAstNode findSubtree(IStrategoAstNode ast,
			final IStrategoAstNode selection, boolean allowMultipleResults) {
		
		// Visitor for collecting subtrees equal to the old selection  
		class Visitor extends AbstractVisitor {
			IStrategoAstNode result = null;
			boolean isMultiple;
			
			public boolean preVisit(AstNode node) {
				if (node.equals(selection)) {
					if (result == null) result = node;
					else isMultiple = true;
					return false;
				}
				return true;
			}
	
			public void postVisit(AstNode node) {
				// Unused
			}
		}
		
		Visitor visitor = new Visitor();
		ast.accept(visitor);
		return !allowMultipleResults && visitor.isMultiple ? null : visitor.result;
	}

	private static boolean containsMultipleCopies(IStrategoAstNode ast, IStrategoAstNode subtree) {
		if (findSubtree(ast, subtree, false) == null) {
			return findSubtree(ast, subtree, true) != null;
		} else {
			return false;
		}
	}
}
