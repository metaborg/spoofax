package org.strategoxt.imp.runtime.stratego;

import static java.lang.Math.max;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.Term.isTermList;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.terms.StrategoSubList;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.ParentAttachment;
import org.strategoxt.imp.generator.generator;
import org.strategoxt.imp.generator.position_of_term_1_0;
import org.strategoxt.imp.generator.term_at_position_0_1;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.ContentProposerSemantic;
import org.strategoxt.imp.runtime.services.StrategoObserver;
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
	
	public static IStrategoList createPath(IStrategoTerm node) {
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
	
	public static List<Integer> createPathList(IStrategoTerm node) {
		if (node instanceof StrategoSubList)
			node = ((StrategoSubList) node).getCompleteList();
		LinkedList<Integer> results = new LinkedList<Integer>();
		
		while (getParent(node) != null) {
			IStrategoTerm parent = getParent(node);
			int index = indexOfIdentical(parent, node);
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
	public static IStrategoList createPathFromParsedIStrategoTerm(final IStrategoTerm node, StrategoObserver observer) {
		observer.getLock().lock();
		try {
			Context context = observer.getRuntime().getCompiledContext();
			IStrategoTerm top = ParentAttachment.getRoot(node);
			final IStrategoTerm marker = context.getFactory().makeString(ContentProposerSemantic.COMPLETION_TOKEN);
			top = oncetd_1_0.instance.invoke(context, top, new Strategy() {
				@Override
				public IStrategoTerm invoke(Context context, IStrategoTerm current) {
					if (hasImploderOrigin(current) && tryGetOrigin(current) == node) {
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
		finally {
			observer.getLock().unlock();
		}
	}
	
	/**
	 * Determine the path to a term in 'ast' with origin 'origin'.
	 */
	public static IStrategoList getTermPathWithOrigin(StrategoObserver observer, IStrategoTerm ast, IStrategoTerm origin) {
		if (ast == null)
			return null;
		if (isTermList(origin)) {
			// Lists have no origin information, try to find the node by its first child.
			if (origin.getSubtermCount() > 0) {
				IStrategoList subtermPath = getTermPathWithOrigin(observer, ast, origin.getSubterm(0));
				if (subtermPath != null){
					// Arrays.copyOf is Java 1.6
					//IStrategoTerm[] originPath = Arrays.copyOf(subtermPath.getAllSubterms(), subtermPath.getSubtermCount()-1);
					IStrategoTerm[] allSubterms = subtermPath.getAllSubterms();
					IStrategoTerm[] originPath = new IStrategoTerm[subtermPath.getSubtermCount()-1];
					System.arraycopy(allSubterms, 0, originPath, 0, originPath.length);
					TermFactory factory = new TermFactory();
					return factory.makeList(originPath);
				}
			}
			return null;
		}
		
		final IStrategoTerm originChild = origin.getSubtermCount() == 0
				? null
				: (IStrategoTerm) origin.getSubterm(0);
		
		class TestOrigin extends Strategy {
			IStrategoTerm origin1;
			IStrategoTerm nextBest;
			
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (hasImploderOrigin(current)) {
					IStrategoTerm currentOrigin = tryGetOrigin(current);
					if (currentOrigin == origin1) return current;
					IStrategoTerm currentImploderOrigin = ImploderAttachment.getImploderOrigin(currentOrigin);
					IStrategoTerm imploderOrigin1 = ImploderAttachment.getImploderOrigin(origin1);
					if (	
							currentImploderOrigin != null &&
							imploderOrigin1 != null &&
							ImploderAttachment.getLeftToken(currentImploderOrigin).getStartOffset() == ImploderAttachment.getLeftToken(imploderOrigin1).getStartOffset() &&
							ImploderAttachment.getRightToken(currentImploderOrigin).getEndOffset() == ImploderAttachment.getRightToken(imploderOrigin1).getEndOffset()
					){
						if(currentOrigin.equals(origin1))
							return current; 
						if(current.getTermType() == origin1.getTermType()){
							if(current.getTermType() == IStrategoTerm.APPL){
								IStrategoAppl currentAppl = (IStrategoAppl)current;
								IStrategoAppl origin1Appl = (IStrategoAppl)origin1;
								if(currentAppl.getName().equals(origin1Appl.getName()) && currentAppl.getSubtermCount() == origin1Appl.getSubtermCount())
									return current;
							}
							nextBest = current;							
						}
					}
					// sets a term as 'nextBest' if one of the subterms of its origin-term is the originChild
					if (nextBest == null && originChild != null) {
						for (int i = 0, max = currentOrigin.getSubtermCount(); i < max; i++)
							if (currentOrigin.getSubterm(i) == originChild)
								nextBest = currentOrigin;
					}
				}
				else { // sets a term as 'nextBest' in case no origin term exists, but one of its subterms is origin-related to the originChild
					if (current == origin1) return current;
					if (nextBest == null && originChild != null) {
						for (int i = 0, max = current.getSubtermCount(); i < max; i++)
							if (tryGetOrigin(current.getSubterm(i)) == originChild)
								nextBest = current;
					}					
				}
				return null;
			}
		}
		TestOrigin testOrigin = new TestOrigin();
		testOrigin.origin1 = origin;
		
		observer.getLock().lock();
		try {
			Context context = observer.getRuntime().getCompiledContext();
			
			generator.init(context);
			IStrategoTerm perfectMatch = position_of_term_1_0.instance.invoke(context, ast, testOrigin);

			if (perfectMatch != null) {
				return (IStrategoList) perfectMatch;
			} else if (testOrigin.nextBest != null) {
				testOrigin.origin1 = testOrigin.nextBest;
				return (IStrategoList) position_of_term_1_0.instance.invoke(context, ast, testOrigin);
			} else {
				return null;
			}
		}
		finally {
			observer.getLock().unlock();
		}
	}

	public static IStrategoTerm getTermAtPath(StrategoObserver observer, IStrategoTerm term, IStrategoList path) {
		observer.getLock().lock();
		try {
			return term_at_position_0_1.instance.invoke(observer.getRuntime().getCompiledContext(), term, path);
		}
		finally {
			observer.getLock().unlock();
		}
	}

	private static int indexOfIdentical(IStrategoTerm parent, IStrategoTerm node) {
		int index = 0;
		for (int size = parent.getSubtermCount(); index < size; index++) {
			if (parent.getSubterm(index) == node) break;
		}
		return index;
	}

	/**
	 * Find the common ancestor of two AST nodes, creating a SubListAstNode if they are in the same list ancestor.
	 */
	public static IStrategoTerm findCommonAncestor(IStrategoTerm node1, IStrategoTerm node2) {
		if (node1 == null) return node2;
		if (node2 == null) return node1;
		
		List<IStrategoTerm> node1Ancestors = new ArrayList<IStrategoTerm>();
		for (IStrategoTerm n = node1; n != null; n = getParent(n))
			node1Ancestors.add(n);
		
		for (IStrategoTerm n = node2, n2Child = node2; n != null; n2Child = n, n = getParent(n)) {
			int node1Index = node1Ancestors.indexOf(n);
			if (node1Index != -1 && node1Ancestors.get(node1Index) == n) // common ancestor w/ reference equality
				return tryCreateListCommonAncestor(n, node1Ancestors, n2Child);
		}
		
		Environment.logWarning("Could not find common ancestor for nodes: " + node1 + "," + node2);
		assert false : "Could not find common ancestor for nodes: " + node1 + "," + node2;
		return getRoot(node1);
	}
	
	private static IStrategoTerm tryCreateListCommonAncestor(IStrategoTerm commonAncestor, List<IStrategoTerm> ancestors1List, IStrategoTerm child2) {
		if (commonAncestor != child2 && commonAncestor.isList()) {
			int i = ancestors1List.indexOf(commonAncestor);
			if (i == 0)
				return commonAncestor;
			IStrategoTerm child1 = ancestors1List.get(i - 1);
			return new TermTreeFactory(Environment.getTermFactory()).createSublist((IStrategoList) commonAncestor, child1, child2); 
		} else {
			return commonAncestor;
		}
	}

	/**
	 * Attempts to find a corresponding selection subtree in a new ast.
	 */
	public static IStrategoTerm findCorrespondingSubtree(IStrategoTerm newAst, IStrategoTerm selection) {
		if (selection instanceof StrategoSubList)
			return findCorrespondingSublist(newAst, (StrategoSubList) selection);
		
		IStrategoTerm oldAst = getRoot(selection);
		IStrategoTerm oldParent = oldAst;
		IStrategoTerm newParent = newAst;
		
		List<Integer> selectionPath = createPathList(selection);
		
		for (int c = 0, size = selectionPath.size(); c < size; c++) {
			int i = selectionPath.get(c);
			if (i >= oldParent.getSubtermCount()) { // Shouldn't happen
				Environment.logException("Unable to recover old selection AST in " + oldParent, new ArrayIndexOutOfBoundsException(i));
				return findIdenticalSubtree(oldAst, newAst, selection);
			}
			IStrategoTerm oldSubtree = oldParent.getSubterm(i);
			IStrategoTerm newSubtree;
			if (i > newParent.getSubtermCount()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else if (oldParent.getSubtermCount() > newParent.getSubtermCount()) {
				i = max(0, i - (oldParent.getSubtermCount() - newParent.getSubtermCount()));
				newSubtree = newParent.getSubterm(i);
			} else if (i > newParent.getSubtermCount()) {
				return findIdenticalSubtree(oldAst, newAst, selection);
			} else {
				newSubtree = newParent.getSubterm(i);
			}
			if (!constructorEqual(oldSubtree, newSubtree)) {
				// First, try siblings instead
				if (i + 1 < newParent.getSubtermCount()) {
					newSubtree = newParent.getSubterm(i + 1);
					if (constructorEqual(oldSubtree, newSubtree))
						continue;
				}
				if (i > 0) {
					newSubtree = newParent.getSubterm(i - 1);
					if (constructorEqual(oldSubtree, newSubtree))
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

	private static IStrategoTerm findCorrespondingSublist(IStrategoTerm newAst, StrategoSubList selection) {
		IStrategoTerm start = findCorrespondingSubtree(newAst, selection.getFirstChild());
		IStrategoTerm end = findCorrespondingSubtree(newAst, selection.getLastChild());
		return findCommonAncestor(start, end);
	}

	private static boolean constructorEqual(IStrategoTerm first, IStrategoTerm second) {
		IStrategoConstructor firstCons = tryGetConstructor(first);
		return firstCons == null
				? tryGetConstructor(second) == null
				: firstCons.equals(tryGetConstructor(second));
	}

	private static IStrategoTerm findCorrespondingSubtreeResult(
			IStrategoTerm oldAst, IStrategoTerm newAst,
			IStrategoTerm oldSubtree, IStrategoTerm newSubtree,
			IStrategoTerm selection, IStrategoTerm newParent, int i) {
		
		if (!newSubtree.equals(oldSubtree)) {
			// First, try siblings instead
			if (i + 1 < newParent.getSubtermCount()) {
				newSubtree = newParent.getSubterm(i + 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}
			if (i > 0) {
				newSubtree = newParent.getSubterm(i - 1);
				if (newSubtree.equals(oldSubtree)) return newSubtree;
			}

			IStrategoTerm exactMatch = findIdenticalSubtree(oldAst, newAst, selection);
			if (exactMatch != null) return exactMatch;

			newSubtree = newParent.getSubterm(i);
			if (constructorEqual(newSubtree, oldSubtree)
					&& (containsMultipleCopies(newAst, newSubtree)
					    || findSubtree(oldAst, newSubtree, true) == null)) {
				return newSubtree; // meh, close enough
			} else {
				return null;
			}
		}
		return newSubtree;
	}

	private static IStrategoTerm getRoot(IStrategoTerm selection) {
		IStrategoTerm result = selection;
		while (getParent(result) != null)
			result = getParent(result);
		return result;
	}

	/**
	 * Finds a unique, identical 'selection' subtree in 'newAst.'
	 * Returns null in case of multiple candidate subtrees.
	 */
	private static IStrategoTerm findIdenticalSubtree(IStrategoTerm oldAst,
			IStrategoTerm newAst, IStrategoTerm selection) {
		
		if (containsMultipleCopies(oldAst, selection)) {
			return null;
		} else {
			return findSubtree(newAst, selection, false);
		}
	}

	/**
	 * Finds a single subtree equal to selection in the given ast.
	 */
	private static IStrategoTerm findSubtree(IStrategoTerm ast,
			final IStrategoTerm selection, boolean allowMultipleResults) {
		
		// Visitor for collecting subtrees equal to the old selection  
		class Visitor extends TermVisitor {
			IStrategoTerm result = null;
			boolean isMultiple;
			
			public void preVisit(IStrategoTerm node) {
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

	private static boolean containsMultipleCopies(IStrategoTerm ast, IStrategoTerm subtree) {
		if (findSubtree(ast, subtree, false) == null) {
			return findSubtree(ast, subtree, true) != null;
		} else {
			return false;
		}
	}
}
