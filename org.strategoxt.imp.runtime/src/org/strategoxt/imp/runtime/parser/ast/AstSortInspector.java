package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getElementSort;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.isTermList;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getParseController;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.imp.parser.ISourcePositionLocator;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;

/**
 * Identifies the possible sorts of a symbol inserted at
 * a particular offset in an abstract syntax tree.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstSortInspector {
	
	private final IStrategoTerm ast;

	public AstSortInspector(IStrategoTerm ast) {
		this.ast = ast;
	}
	
	/**
	 * @param startOffset The start offset.
	 * @param endOffset   The end offset (inclusive).
	 */
	public Set<String> getSortsAtOffset(int startOffset, int endOffset) {
		return getSortsAt(startOffset, endOffset, null);
	}
	
	// TODO: get *all* sorts at cursor by looking at the parse tree? or by storing injections in the AST?
	//       (see AsfixImploder)
	/**
	 * @param startOffset The start offset.
	 * @param endOffset   The end offset (inclusive).
	 */
	public Set<String> getSortsAt(int startOffset, int endOffset, IStrategoTerm node) {
		if (ast == null) return new HashSet<String>();

		if (node == null) {
			ISourcePositionLocator locator = getParseController(ast).getSourcePositionLocator();
			node = (IStrategoTerm) locator.findNode(ast, endOffset);
			if (node == null) node = ast;
		}
		
		int startToken = getNonLayoutTokenLeftOf(node);
		int endToken = getNonLayoutTokenRightOf(node);
		Set<String> results = getSortsOfOptionalChildren(node, startOffset, endOffset);
		
		boolean isOptNode = false;
		boolean skipNonOptNodes = getRightToken(node).getEndOffset() > endOffset && !results.isEmpty();

		// Follow the (labeled) injection chain upwards
		do {
			IStrategoTerm parent = getParent(node);
			if (!skipNonOptNodes && getLeftToken(node).getIndex() > startToken) {
				if (node.isList() || (getSort(node) != null) && getSort(node).equals("Some"))
					isOptNode = true;
				results.add(getElementSort(node));
			} else if (parent != null && parent.isList()) {
				if (!skipNonOptNodes)
					results.add(getElementSort(parent));
			}
			// HACK: include element sort of sibling list
			if (isOptNode && (node = getNextSibling(node)) != null) {
				if (node.isList()) results.add(getElementSort(node));
			}
			node = parent;
		} while (node != null && getRightToken(node).getIndex() < endToken);
		
		if (node != null && node.isList() && !skipNonOptNodes) {
			// Add sort of container list with elements after current node
			results.add(getElementSort(node));
		}
		
		results.remove(null);
		return results;
	}

	/**
	 * @param endOffset   The end offset (inclusive).
	 */
	private Set<String> getSortsOfOptionalChildren(IStrategoTerm node, int startOffset, int endOffset) {
		// TODO: detect 'opt' optionals in addition to list children?
		// TODO: somehow behave differently in case the completion resulted in a syntax error?
		//       the skipNonOptNodes thing which mosly addresses this
		ITokenizer tokens = getRightToken(ast).getTokenizer();
		Set<String> results = new LinkedHashSet<String>();
		for (IStrategoTerm child : node.getAllSubterms()) {
			if (child.isList()) {
				int startToken = getNonLayoutTokenLeftOf(child);
				int endToken = getNonLayoutTokenRightOf(child);
				
				if ((startToken < 0 || tokens.getTokenAt(startToken).getEndOffset() < startOffset)
						&& (endToken >= tokens.getTokenCount() || tokens.getTokenAt(endToken).getStartOffset() >= endOffset)) {
					results.add(getElementSort(child));
				}
			}
		}
		return results;
	}

	private int getNonLayoutTokenLeftOf(IStrategoTerm node) {
		int result = getLeftToken(node).getIndex();
		ITokenizer tokens = getRightToken(ast).getTokenizer();
		while (--result >= 0) {
			int kind = tokens.getTokenAt(result).getKind();
			if (kind != IToken.TK_LAYOUT && kind != IToken.TK_ERROR)
				break;
		}
		return result;
	}

	private int getNonLayoutTokenRightOf(IStrategoTerm node) {
		int result = getRightToken(node).getIndex();
		ITokenizer tokens = getRightToken(ast).getTokenizer();
		while (++result < tokens.getTokenCount()) {
			int kind = tokens.getTokenAt(result).getKind();
			if (kind != IToken.TK_LAYOUT && kind != IToken.TK_ERROR)
				break;
		}
		return result;
	}

	private static IStrategoTerm getNextSibling(IStrategoTerm node) {
		IStrategoTerm parent = getParent(node);
		if (parent == null) return null;
		if (isTermList(parent)) {
			IStrategoList tail = ((IStrategoList) parent).tail();
			return tail.isEmpty() ? null : tail.head();
		} else {
			for (int i = 0, max = parent.getSubtermCount() - 1; i < max; i++) {
				if (parent.getSubterm(i) == node) return parent.getSubterm(i + 1);
			}
			return null;
		}
	}
}
