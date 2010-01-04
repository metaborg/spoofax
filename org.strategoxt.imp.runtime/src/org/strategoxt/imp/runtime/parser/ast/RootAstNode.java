package org.strategoxt.imp.runtime.parser.ast;

import org.eclipse.core.resources.IResource;

public class RootAstNode extends AstNode {
	
	private final IResource resource;
	
	@Override
	public IResource getResource() {
		return resource;
	}

	protected RootAstNode(AstNode ast, IResource resource) {
		
		super(ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(), ast.getConstructor(),
				ast.getChildren());
		
		this.resource = resource;
		
		overrideReferences(getLeftIToken(), getRightIToken(), getChildren(), ast);
	}
	
	public static RootAstNode makeRoot(AstNode ast, IResource resource) {
		return new RootAstNode(ast, resource);
	}
	
	@Override
	@Deprecated
	public RootAstNode clone() {
		return (RootAstNode) super.clone();
	}
}
