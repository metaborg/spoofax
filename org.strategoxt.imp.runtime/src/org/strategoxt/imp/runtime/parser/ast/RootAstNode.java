package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.strategoxt.imp.runtime.parser.SGLRParseController;

import lpg.runtime.IToken;

public class RootAstNode extends AstNode {
	private final SGLRParseController parseController;
	
	private final String grammar;

	public String getGrammarName() {
		return grammar;
	}

	public SGLRParseController getParseController() {
		return parseController;
	}

	protected RootAstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children, SGLRParseController parseController) {
		
		super(sort, constructor, leftToken, rightToken, children);
		
		this.parseController = parseController;
		this.grammar = parseController.getActiveGrammarName();
	}
	
	public static RootAstNode makeRoot(AstNode ast, SGLRParseController parseController) {
		assert ast.getSort().equals("<START>");
		
		return new RootAstNode(
				ast.getSort(), ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(),
				ast.getChildren(), parseController);
	}
}
