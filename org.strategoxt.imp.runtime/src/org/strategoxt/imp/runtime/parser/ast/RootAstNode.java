package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

import lpg.runtime.IToken;

public class RootAstNode extends AstNode {
	private final SGLRParseController parseController;
	
	@SuppressWarnings("unused")
	private Object cachingKey;

	@Override
	public Language getLanguage() {
		return parseController.getLanguage();
	}

	@Override
	public SGLRParseController getParseController() {
		return parseController;
	}
	
	@Override
	public IStrategoAppl getTerm() {
		return (IStrategoAppl) super.getTerm();
	}
	
	/**
	 * Sets an object reference associated with this object.
	 * This can be used for caching schemes with a WeakHashMap,
	 * ensuring a life reference to the specified object exists.
	 * 
	 * @param cachingKey
	 */
	public void setCachingKey(Object cachingKey) {
		this.cachingKey = cachingKey;
	}

	protected RootAstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children, SGLRParseController parseController) {
		
		super(sort, constructor, leftToken, rightToken, children);
		
		this.parseController = parseController;
	}
	
	public static RootAstNode makeRoot(AstNode ast, SGLRParseController parseController) {
		return new RootAstNode(
				ast.getSort(), ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(),
				ast.getChildren(), parseController);
	}
}
