package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import lpg.runtime.IPrsStream;

import org.eclipse.imp.parser.ISourcePositionLocator;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstSortInspector {
	
	private final RootAstNode ast;

	public AstSortInspector(RootAstNode ast) {
		this.ast = ast;
	}

	
	// TODO: get *all* sorts at cursor by looking at the parse tree? or by storing injections in the AST?
	//       (see AsfixImploder)
	public Set<String> getSortsAtOffset(int offset) {
		//if (node.getConstructor().equals(COMPLETION_UNKNOWN))
		//	return Collections.emptySet();
		
		if (ast == null) return Collections.emptySet();
		ISourcePositionLocator locator = ast.getParseController().getSourcePositionLocator();
		AstNode node = (AstNode) locator.findNode(ast, offset);
		if (node == null) node = ast;

		int startToken = getLayoutStartTokenOffset(node);
		int endToken = getLayoutEndTokenOffset(node);
		Set<String> results = getSortsOfOptionalChildren(node, offset);
		boolean isOptNode = false;

		// Follow the (labeled) injection chain upwards
		do {
			AstNode parent = node.getParent();
			if (node.getLeftIToken().getTokenIndex() > startToken) {
				if (node.isList()) {
					isOptNode = true;
					results.add(((ListAstNode) node).getElementSort());
				} else if (node.getSort() != null) {
					if (node.getSort().equals("Some")) isOptNode = true;
					results.add(node.getSort());
				}
			} else if (parent != null && parent.isList()) {
				results.add(((ListAstNode) parent).getElementSort());
			}
			// HACK: include element sort of sibling list
			if (isOptNode && (node = getNextSibling(node)) != null) {
				if (node.isList()) results.add(((ListAstNode) node).getElementSort());
			}
			node = parent;
		} while (node != null && node.getRightIToken().getTokenIndex() < endToken);
		
		if (node != null && node.isList()) {
			// Add sort of container list with elements after current node
			results.add(((ListAstNode) node).getElementSort());
		}
		
		results.remove(null);
		return results;
	}

	private Set<String> getSortsOfOptionalChildren(AstNode node, int offset) {
		// TODO: detect 'opt' optionals in addition to list children?
		// TODO: somehow behave differently in case the completion resulted in a syntax error?
		IPrsStream tokens = ast.getRightIToken().getIPrsStream();
		Set<String> results = new LinkedHashSet<String>();
		for (AstNode child : node.getChildren()) {
			if (child.isList()) {
				int startToken = getLayoutStartTokenOffset(child);
				int endToken = getLayoutEndTokenOffset(child);
				
				if ((startToken < 0 || tokens.getTokenAt(startToken).getEndOffset() <= offset)
						&& (endToken >= tokens.getStreamLength() || tokens.getTokenAt(endToken).getStartOffset() >= offset)) {
					results.add(((ListAstNode) child).getElementSort());
				}
			}
		}
		return results;
	}

	private int getLayoutStartTokenOffset(AstNode node) {
		int result = node.getLeftIToken().getTokenIndex();
		IPrsStream tokens = ast.getRightIToken().getIPrsStream();
		while (--result >= 0) {
			int kind = tokens.getTokenAt(result).getKind();
			if (kind != TokenKind.TK_LAYOUT.ordinal() && kind != TokenKind.TK_ERROR.ordinal())
				break;
		}
		return result;
	}

	private int getLayoutEndTokenOffset(AstNode node) {
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
