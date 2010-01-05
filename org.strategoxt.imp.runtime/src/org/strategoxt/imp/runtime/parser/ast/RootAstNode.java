package org.strategoxt.imp.runtime.parser.ast;

import org.eclipse.core.resources.IResource;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

public class RootAstNode extends AstNode {
	
	private final SGLRParseController controller;
	
	private final IResource resource;
	
	@Override
	public IResource getResource() {
		return resource;
	}
	
	@Override
	public SGLRParseController getParseController() {
		return controller;
	}

	protected RootAstNode(AstNode ast, SGLRParseController controller, IResource resource) {
		super(ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(), ast.getConstructor(),
				ast.getChildren());
		
		this.resource = resource;
		this.controller = controller;
		
		overrideReferences(getLeftIToken(), getRightIToken(), getChildren(), ast);
	}
	
	public static RootAstNode makeRoot(AstNode ast, SGLRParseController controller, IResource resource) {
		return new RootAstNode(ast, controller, resource);
	}
	
	@Override
	public RootAstNode cloneIgnoreTokens() {
		return (RootAstNode) super.cloneIgnoreTokens();
	}
}
