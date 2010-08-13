package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import lpg.runtime.IPrsStream;

import org.eclipse.imp.parser.ISourcePositionLocator;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

/**
 * Identifies the possible sorts of a symbol inserted at
 * a particular offset in an abstract syntax tree.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstSortInspector {
	
	private final RootAstNode ast;

	public AstSortInspector(RootAstNode ast) {
		this.ast = ast;
	}
	
	// TODO: get *all* sorts at cursor by looking at the parse tree? or by storing injections in the AST?
	//       (see AsfixImploder)
	public Set<String> getSortsAtOffset(int startOffset, int endOffset) {
		if (ast == null) return new HashSet<String>();
		ISourcePositionLocator locator = ast.getParseController().getSourcePositionLocator();
		AstNode node = (AstNode) locator.findNode(ast, endOffset);
		if (node == null) node = ast;

		int startToken = getNonLayoutTokenLeftOf(node);
		int endToken = getNonLayoutTokenRightOf(node);
		Set<String> results = getSortsOfOptionalChildren(node, startOffset, endOffset);
		
		boolean isOptNode = false;
		boolean skipNonOptNodes = node.getRightIToken().getEndOffset() > endOffset && !results.isEmpty();

		// Follow the (labeled) injection chain upwards
		do {
			AstNode parent = node.getParent();
			if (!skipNonOptNodes && node.getLeftIToken().getTokenIndex() > startToken) {
				if (node.isList()) {
					isOptNode = true;
					results.add(((ListAstNode) node).getElementSort());
				} else if (node.getSort() != null) {
					if (node.getSort().equals("Some")) isOptNode = true;
					results.add(node.getSort());
				}
			} else if (parent != null && parent.isList()) {
				if (!skipNonOptNodes)
					results.add(((ListAstNode) parent).getElementSort());
			}
			// HACK: include element sort of sibling list
			if (isOptNode && (node = getNextSibling(node)) != null) {
				if (node.isList()) results.add(((ListAstNode) node).getElementSort());
			}
			node = parent;
		} while (node != null && node.getRightIToken().getTokenIndex() < endToken);
		
		if (node != null && node.isList() && !skipNonOptNodes) {
			// Add sort of container list with elements after current node
			results.add(((ListAstNode) node).getElementSort());
		}
		
		results.remove(null);
		return results;
	}

	private Set<String> getSortsOfOptionalChildren(AstNode node, int startOffset, int endOffset) {
		// TODO: detect 'opt' optionals in addition to list children?
		// TODO: somehow behave differently in case the completion resulted in a syntax error?
		//       the skipNonOptNodes thing which mosly addresses this
		IPrsStream tokens = ast.getRightIToken().getIPrsStream();
		Set<String> results = new LinkedHashSet<String>();
		for (AstNode child : node.getChildren()) {
			if (child.isList()) {
				int startToken = getNonLayoutTokenLeftOf(child);
				int endToken = getNonLayoutTokenRightOf(child);
				
				if ((startToken < 0 || tokens.getTokenAt(startToken).getEndOffset() < startOffset)
						&& (endToken >= tokens.getStreamLength() || tokens.getTokenAt(endToken).getStartOffset() >= endOffset)) {
					results.add(((ListAstNode) child).getElementSort());
				}
			}
		}
		return results;
	}

	private int getNonLayoutTokenLeftOf(AstNode node) {
		int result = node.getLeftIToken().getTokenIndex();
		IPrsStream tokens = ast.getRightIToken().getIPrsStream();
		while (--result >= 0) {
			int kind = tokens.getTokenAt(result).getKind();
			if (kind != TokenKind.TK_LAYOUT.ordinal() && kind != TokenKind.TK_ERROR.ordinal())
				break;
		}
		return result;
	}

	private int getNonLayoutTokenRightOf(AstNode node) {
		int result = node.getRightIToken().getTokenIndex();
		IPrsStream tokens = ast.getRightIToken().getIPrsStream();
		while (++result < tokens.getStreamLength()) {
			int kind = tokens.getTokenAt(result).getKind();
			if (kind != TokenKind.TK_LAYOUT.ordinal() && kind != TokenKind.TK_ERROR.ordinal())
				break;
		}
		return result;
	}

	private static AstNode getNextSibling(AstNode node) {
		AstNode parent = node.getParent();
		if (parent == null) return null;
		ArrayList<AstNode> children = parent.getChildren();
		int siblingIndex = children.indexOf(node) + 1;
		if (siblingIndex >= children.size()) return null;
		return children.get(siblingIndex);
	}
}
