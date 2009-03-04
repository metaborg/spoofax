package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.ISourceInfo;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

public class RootAstNode extends AstNode {
	private final ISourceInfo locationInfo;
	
	@SuppressWarnings("unused")
	private Object cachingKey;

	@Override
	public ISourceInfo getSourceInfo() {
		return locationInfo;
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
		
		this.locationInfo = parseController;
	}
	
	public static RootAstNode makeRoot(AstNode ast, SGLRParseController parseController) {
		return new RootAstNode(
				ast.getSort(), ast.getConstructor(), ast.getLeftIToken(), ast.getRightIToken(),
				ast.getChildren(), parseController);
	}
}
