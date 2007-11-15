package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;
import aterm.ATerm;

/**
 * Default ATermAstNode factory.
 * Should be overridden to supply specialized AstNode classes for a specific grammar.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ATermAstNodeFactory {
	public ATermAstNode create(ATerm aterm, ArrayList<ATermAstNode> children,
			IToken leftToken, IToken rightToken) {
		return new ATermAstNode(aterm, children, leftToken, rightToken);
	}
}
