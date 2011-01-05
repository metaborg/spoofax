package org.strategoxt.imp.runtime.stratego;

import static java.lang.Math.max;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.SimpleTermVisitor;
import org.spoofax.terms.attachments.ParentAttachment;
import org.strategoxt.imp.generator.position_of_term_1_0;
import org.strategoxt.imp.generator.term_at_position_0_1;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_aterm.explode_aterm_0_0;
import org.strategoxt.stratego_aterm.implode_aterm_0_0;
import org.strategoxt.stratego_lib.oncetd_1_0;

/**
 * Maintains aterm paths, lists of nodes on the path to the root from a given AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoTermPath {

	private StrategoTermPath() {
		// Constructed by a static constructor
	}
	
	public static IStrategoList createPath(ISimpleTerm node) {
		List<Integer> pathInts=createPathList(node);
		return toStrategoPath(pathInts);
	}

	public static IStrategoList toStrategoPath(List<Integer> pathInts) {
		LinkedList<IStrategoTerm> results = new LinkedList<IStrategoTerm>();
		for (int i = 0; i < pathInts.size(); i++) {
			results.add(Environment.getTermFactory().makeInt(pathInts.get(i)));
		}
		return Environment.getTermFactory().makeList(results);
	}
	
	public static List<Integer> createPathList(ISimpleTerm node) {
		if (node instanceof SubListAstNode)
			node = ((SubListAstNode) node).getCompleteList();
		LinkedList<Integer> results = new LinkedList<Integer>();
		
		while (getParent(node) != null) {
			ISimpleTerm parent = getParent(node);
			int index = indexOfIdentical(parent.getChildren(), node);
			results.addFirst(Integer.valueOf(index));
			node = getParent(node);
		}
		return results;
	}
	
	/**
	 * Creates a term path given the AST Node of a parsed IStrategoTerm file.
	 * The resulting path relates to the actual AST, ignoring the 'appl' etc constructors
	 * of the IStrategoTerm syntax.
	 */
	public static IStrategoList createPathFromParsedIStrategoTerm(final ISimpleTerm node, Context context) {
		IStrategoTerm top = ParentAttachment.getRoot(node);
		final IStrategoTerm marker = context.getFactory().makeString(ContentProposer.COMPLETION_TOKEN);
		top = oncetd_1_0.instance.invoke(context, top, new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (current instanceof IStrategoTerm && ((IStrategoTerm) current).getNode() == node) {
					return explode_aterm_0_0.instance.invoke(context, marker);
				} else {
					return null;
				}
			}
		});
		top = implode_aterm_0_0.instance.invoke(context, top);
		return (IStrategoList) position_of_term_1_0.instance.invoke(context, top, new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				return marker.equals(current) ? current : null;
			}
		});
	}
	
	/**
	 * Determine the path to a term in 'ast' with origin 'origin'.
	 */
	public static IStrategoList getTermPathWithOrigin(Context context, IStrategoTerm ast, ISimpleTerm origin) {
		if (ast == null)
			return null;
		
		if (isTermList(origin)) {
			// Lists have no origin information; don't try to find the node.
			return null;
		}
		
		final ISimpleTerm originChild = origin.getSubtermCount() == 0
				? null
				: (ISimpleTerm) origin.getSubterm(0);
		
		class TestOrigin extends Strategy {
			ISimpleTerm origin;
			ISimpleTerm nextBest;
			
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (current instanceof IStrategoTerm) {
					ISimpleTerm currentOrigin = ((IStrategoTerm) current).getNode();
					if (currentOrigin == origin) return current;
					List children = currentOrigin.getChildren();
					if (nextBest == null && originChild != null) {
						for (int i = 0; i < children.size(); i++)
							if (children.get(i) == originChild)
								nextBest = currentOrigin;
					}
				}
				return null;
			}
		}
		TestOrigin testOrigin = new TestOrigin();
		testOrigin.origin = origin;
		
		IStrategoTerm perfectMatch = position_of_term_1_0.instance.invoke(context, ast, testOrigin);
		if (perfectMatch != null) {
			return (IStrategoList) perfectMatch;
		} else if (testOrigin.nextBest != null) {
			Environment.logWarning("Could not determine term corresponding to " + origin.toString() + " in resulting AST; using next best match " + testOrigin.nextBest);
			testOrigin.origin = testOrigin.nextBest;
			return (IStrategoList) position_of_term_1_0.instance.invoke(context, ast, testOrigin);
		} else {
			Environment.logWarning("Could not determine term corresponding to " + origin.toString() + " in resulting AST");
			return null;
		}
	}

	public static IStrategoTerm getTermAtPath(Context context, IStrategoTerm term, IStrategoList path) {
		return term_at_position_0_1.instance.invoke(context, term, path);
	}

	private static int indexOfIdentical(List<?> children, ISimpleTerm node) {
		int index = 0;
		for (int size = children.size(); index < size; index++) {
			if (children.get(index) == node) break;
		}
		return index;
	}

	/**
	 * Find the common ancestor of two AST nodes, creating a SubListAstNode if they are in the same list ancestor.
	 */
	public static ISimpleTerm findCommonAncestor(ISimpleTerm node1, ISimpleTerm node2) {
		if (node1 == null) return node2;
		if (node2 == null) return node1;
		
		List<ISimpleTerm> node1Ancestors = new ArrayList<ISimpleTerm>();
		for (ISimpleTerm n = node1; n != null; n = getParent(n))
			node1Ancestors.add(n);
		
		for (ISimpleTerm n = node2, n2Child = node2; n != null; n2Child = n, n = getParent(n))
			if (node1Ancestors.contains(n)) {
				if(node1Ancestors.get(node1Ancestors.indexOf(n))==n)
					return tryCreateListCommonAncestor(n, node1Ancestors, n2Child);
			}
		
		throw new IllegalStateException("Could not find common ancestor for nodes: " + node1 + "," + node2);
	}
	
	private static ISimpleTerm tryCreateListCommonAncestor(ISimpleTerm commonAncestor, List<ISimpleTerm> ancestors1List, ISimpleTerm child2) {
		if (commonAncestor != child2 && ((IStrategoTerm) commonAncestor).isList()) {
			int i = ancestors1List.indexOf(commonAncestor);
			if (i == 0)
				return commonAncestor;
			ISimpleTerm child1 = ancestors1List.get(i - 1);
			return SubListAstNode.createSublist((ListAstNode) commonAncestor, child1, child2, true); 
		} else {
			return commonAncestor;
		}
	}

	/**
	 * Attempts to find a corresponding selection subtree in a new ast.
	 */
	public static ISimpleTerm findCorrespondingSubtree(ISimpleTerm newAst, ISimpleTerm selection) {
		if (selection instanceof SubListAstNode)
			return findCorrespondingSublist(newAst, (SubListAstNode) selection);
		
		ISimpleTerm oldAst = getRoot(selection);
		ISimpleTerm oldParent = oldAst;
		ISimpleTerm newParent = newAst;
		
		List<Integer> selectionPath = createPathList(selection);
		
		for (int c = 0, size = selectionPath.size(); c < size; c++) {
			int i = selectionPath.get(c);
			if (i >= oldParent.getSubtermCount()) { // Shouldn't happen
				Environment.logException("Unable to recover old selection AST in " + oldParent, new ArrayIndexOutOfBoundsException(i));
				return findIdenticalSubtree(oldAst, newAst, selection);
			}
			ISimpleTerm oldSubtree = (ISimpleTerm) oldParent.getSubterm(i);
			ISimpleTerm newSubtree;
			if (i > newParent.getSubtermCount()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else if (oldParent.getSubtermCount() > newParent.getSubtermCount()) {
				i = max(0, i - (oldParent.getSubtermCount() - newParent.getSubtermCount()));
				newSubtree = (ISimpleTerm) newParent.getSubterm(i);
			} else if (i > newParent.getSubtermCount()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else {
				newSubtree = (ISimpleTerm) newParent.getSubterm(i);
			}
			if (!constructorEquals(oldSubtree, newSubtree)) {
				// First, try siblings instead
				if (i + 1 < newParent.getSubtermCount()) {
					newSubtree = (ISimpleTerm) newParent.getSubterm(i + 1);
					if (oldSubtree.getConstructor().equals(newSubtree.getConstructor()))
						continue;
				}
				if (i > 0) {
					newSubtree = (ISimpleTerm) newParent.getSubterm(i - 1);
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

	private static ISimpleTerm findCorrespondingSublist(ISimpleTerm newAst, SubListAstNode selection) {
		ISimpleTerm start = findCorrespondingSubtree(newAst, selection.getFirstChild());
		ISimpleTerm end = findCorrespondingSubtree(newAst, selection.getLastChild());
		return findCommonAncestor(start, end);
	}

	private static boolean constructorEquals(ISimpleTerm first, ISimpleTerm second) {
		return first.getConstructor() == null
				? second.getConstructor() == null
				: first.getConstructor().equals(second.getConstructor());
	}

	private static ISimpleTerm findCorrespondingSubtreeResult(
			ISimpleTerm oldAst, ISimpleTerm newAst,
			ISimpleTerm oldSubtree, ISimpleTerm newSubtree,
			ISimpleTerm selection, ISimpleTerm newParent, int i) {
		
		if (!newSubtree.equals(oldSubtree)) {
			// First, try siblings instead
			if (i + 1 < newParent.getSubtermCount()) {
				newSubtree = (ISimpleTerm) newParent.getSubterm(i + 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}
			if (i > 0) {
				newSubtree = (ISimpleTerm) newParent.getSubterm(i - 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}

			ISimpleTerm exactMatch = findIdenticalSubtree(oldAst, newAst, selection);
			if (exactMatch != null) return exactMatch;

			newSubtree = (ISimpleTerm) newParent.getSubterm(i);
			if (constructorEquals(newSubtree, oldSubtree)
					&& (containsMultipleCopies(newAst, newSubtree)
					    || findSubtree(oldAst, newSubtree, true) == null)) {
				return newSubtree; // meh, close enough
			} else {
				return null;
			}
		}
		return newSubtree;
	}

	private static ISimpleTerm getRoot(ISimpleTerm selection) {
		ISimpleTerm result = selection;
		while (getParent(result) != null)
			result = getParent(result);
		return result;
	}

	/**
	 * Finds a unique, identical 'selection' subtree in 'newAst.'
	 * Returns null in case of multiple candidate subtrees.
	 */
	private static ISimpleTerm findIdenticalSubtree(ISimpleTerm oldAst,
			ISimpleTerm newAst, ISimpleTerm selection) {
		
		if (containsMultipleCopies(oldAst, selection)) {
			return null;
		} else {
			return findSubtree(newAst, selection, false);
		}
	}

	/**
	 * Finds a single subtree equal to selection in the given ast.
	 */
	private static ISimpleTerm findSubtree(ISimpleTerm ast,
			final ISimpleTerm selection, boolean allowMultipleResults) {
		
		// Visitor for collecting subtrees equal to the old selection  
		class Visitor extends SimpleTermVisitor {
			ISimpleTerm result = null;
			boolean isMultiple;
			
			public void preVisit(ISimpleTerm node) {
				if (node.equals(selection)) {
					if (result == null) result = node;
					else isMultiple = true;
					// TODO: Optimize - set isDone() to true!
					// return false;
				}
			}
		}
		
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		return !allowMultipleResults && visitor.isMultiple ? null : visitor.result;
	}

	private static boolean containsMultipleCopies(ISimpleTerm ast, ISimpleTerm subtree) {
		if (findSubtree(ast, subtree, false) == null) {
			return findSubtree(ast, subtree, true) != null;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the node furthest up the ancestor chain that
	 * has either the same character offsets or has only one
	 * child with the same character offsets as the node given.
	 * 
	 * @param allowMultiChildParent
	 *             Also fetch the first parent if it has multiple children (e.g., Call("foo", "bar")).
	 */
	public static final ISimpleTerm getMatchingAncestor(ISimpleTerm oNode, boolean allowMultiChildParent) {
		if (allowMultiChildParent && oNode.getConstructor() == null && getParent(oNode) != null)
			return getParent(oNode);
		
		ISimpleTerm result = oNode;
		int startOffset = getLeftToken(result).getStartOffset();
		int endOffset = getRightToken(result).getEndOffset();
		while (getParent(result) != null
				&& (getParent(result).getSubtermCount() <= 1 
						|| (getLeftToken(getParent(result)).getStartOffset() >= startOffset
							&& getRightToken(getParent(result)).getEndOffset() <= endOffset)))
			result = getParent(result);
		return result;
	}
}
