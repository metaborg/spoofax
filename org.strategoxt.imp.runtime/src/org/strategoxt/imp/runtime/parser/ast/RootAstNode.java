package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class RootAstNode extends AstNode {
	
	private final IResource resource;
	
	@Override
	public IResource getResource() {
		return resource;
	}
	
	@Override
	public IStrategoAppl getTerm() {
		return (IStrategoAppl) super.getTerm();
	}

	protected RootAstNode(String sort, IToken leftToken, IToken rightToken, String constructor,
			ArrayList<AstNode> children, IResource resource) {
		
		super(sort, leftToken, rightToken, constructor, children);
		
		this.resource = resource;
	}
	
	public static RootAstNode makeRoot(AstNode ast, IResource resource) {
		return new RootAstNode(
				ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(), ast.getConstructor(),
				ast.getChildren(), resource);
	}
}
