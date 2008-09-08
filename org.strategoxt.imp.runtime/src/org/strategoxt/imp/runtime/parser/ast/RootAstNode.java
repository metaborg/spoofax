package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

import lpg.runtime.IToken;

public class RootAstNode extends AstNode {
	private final SGLRParseController parseController;
	
	// This field provides an object reference for the key to stay alive in a WeakHashMap
	@SuppressWarnings("unused")
	private final Object cachingKey;

	public Language getLanguage() {
		return parseController.getLanguage();
	}

	public SGLRParseController getParseController() {
		return parseController;
	}

	protected RootAstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children, SGLRParseController parseController, Object cachingKey) {
		
		super(sort, constructor, leftToken, rightToken, children);
		
		this.parseController = parseController;
		this.cachingKey = cachingKey;
	}
	
	public static RootAstNode makeRoot(AstNode ast, SGLRParseController parseController, Object cachingKey) {
		return new RootAstNode(
				ast.getSort(), ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(),
				ast.getChildren(), parseController, cachingKey);
	}
}
