package org.strategoxt.imp.runtime.stratego;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.generator.position_of_term_1_0;
import org.strategoxt.imp.generator.term_at_position_0_1;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;
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
	
	public static IStrategoList createPath(IStrategoAstNode node) {
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
	
	public static List<Integer> createPathList(IStrategoAstNode node) {
		if (node instanceof SubListAstNode)
			node = ((SubListAstNode) node).getCompleteList();
		LinkedList<Integer> results = new LinkedList<Integer>();
		
		while (node.getParent() != null) {
			IStrategoAstNode parent = node.getParent();
			int index = indexOfIdentical(parent.getChildren(), node);
			results.addFirst(Integer.valueOf(index));
			node = node.getParent();
		}
		return results;
	}
	
	/**
	 * Creates a term path given the AST Node of a parsed ATerm file.
	 * The resulting path relates to the actual AST, ignoring the 'appl' etc constructors
	 * of the ATerm syntax.
	 */
	public static IStrategoList createPathFromParsedATerm(final IStrategoAstNode node, Context context) {
		IStrategoTerm top = node.getRoot().getTerm();
		final IStrategoTerm marker = context.getFactory().makeString(ContentProposer.COMPLETION_TOKEN);
		top = oncetd_1_0.instance.invoke(context, top, new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (current instanceof IWrappedAstNode && ((IWrappedAstNode) current).getNode() == node) {
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
	public static IStrategoList getTermPathWithOrigin(Context context, IStrategoTerm ast, IStrategoAstNode origin) {
		if (ast == null)
			return null;
		
		if (origin instanceof ListAstNode) {
			// Lists have no origin information; don't try to find the node.
			return null;
		}
		
		final IStrategoAstNode originChild = origin.getChildren().size() == 0
				? null
				: (IStrategoAstNode) origin.getChildren().get(0);
		
		class TestOrigin extends Strategy {
			IStrategoAstNode origin;
			IStrategoAstNode nextBest;
			
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (current instanceof IWrappedAstNode) {
					IStrategoAstNode currentOrigin = ((IWrappedAstNode) current).getNode();
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

	private static int indexOfIdentical(List<?> children, IStrategoAstNode node) {
		int index = 0;
		for (int size = children.size(); index < size; index++) {
			if (children.get(index) == node) break;
		}
		return index;
	}

	/**
	 * Find the common ancestor of two AST nodes, creating a SubListAstNode if they are in the same list ancestor.
	 */
	public static IStrategoAstNode findCommonAncestor(IStrategoAstNode node1, IStrategoAstNode node2) {
		if (node1 == null) return node2;
		if (node2 == null) return node1;
		
		List<IStrategoAstNode> node1Ancestors = new ArrayList<IStrategoAstNode>();
		for (IStrategoAstNode n = node1; n != null; n = n.getParent())
			node1Ancestors.add(n);
		
		for (IStrategoAstNode n = node2, n2Child = node2; n != null; n2Child = n, n = n.getParent())
			if (node1Ancestors.contains(n)) {
				if(node1Ancestors.get(node1Ancestors.indexOf(n))==n)
					return tryCreateListCommonAncestor(n, node1Ancestors, n2Child);
			}
		
		throw new IllegalStateException("Could not find common ancestor for nodes: " + node1 + "," + node2);
	}
	
	private static IStrategoAstNode tryCreateListCommonAncestor(IStrategoAstNode commonAncestor, List<IStrategoAstNode> ancestors1List, IStrategoAstNode child2) {
		if (commonAncestor != child2 && ((AstNode) commonAncestor).isList()) {
			int i = ancestors1List.indexOf(commonAncestor);
			if (i == 0)
				return commonAncestor;
			IStrategoAstNode child1 = ancestors1List.get(i - 1);
			return new AstNodeFactory().createSublist((ListAstNode) commonAncestor, child1, child2, true); 
		} else {
			return commonAncestor;
		}
	}

	/**
	 * Attempts to find a corresponding selection subtree in a new ast.
	 */
	public static IStrategoAstNode findCorrespondingSubtree(IStrategoAstNode newAst, IStrategoAstNode selection) {
		if (selection instanceof SubListAstNode)
			return findCorrespondingSublist(newAst, (SubListAstNode) selection);
		
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
			if (i > newParent.getChildren().size()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else if (oldParent.getChildren().size() > newParent.getChildren().size()) {
				i = max(0, i - (oldParent.getChildren().size() - newParent.getChildren().size()));
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(i);
			} else if (i > newParent.getChildren().size()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else {
				newSubtree = (IStrategoAstNode) newParent.getChildren().get(i);
			}
			if (!constructorEquals(oldSubtree, newSubtree)) {
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

	private static IStrategoAstNode findCorrespondingSublist(IStrategoAstNode newAst, SubListAstNode selection) {
		IStrategoAstNode start = findCorrespondingSubtree(newAst, selection.getFirstChild());
		IStrategoAstNode end = findCorrespondingSubtree(newAst, selection.getLastChild());
		return findCommonAncestor(start, end);
	}

	private static boolean constructorEquals(IStrategoAstNode first, IStrategoAstNode second) {
		return first.getConstructor() == null
				? second.getConstructor() == null
				: first.getConstructor().equals(second.getConstructor());
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
	
	/**
	 * Gets the node furthest up the ancestor chain that
	 * has either the same character offsets or has only one
	 * child with the same character offsets as the node given.
	 */
	public static final IStrategoAstNode getMatchingAncestor(IStrategoAstNode oNode) {
		IStrategoAstNode result = oNode;
		int startOffset = result.getLeftIToken().getStartOffset();
		int endOffset = result.getRightIToken().getEndOffset();
		while (result.getParent() != null
				&& (result.getParent().getChildren().size() <= 1 
						|| (result.getParent().getLeftIToken().getStartOffset() >= startOffset
							&& result.getParent().getRightIToken().getEndOffset() <= endOffset)))
			result = result.getParent();
		return result;
	}
}
