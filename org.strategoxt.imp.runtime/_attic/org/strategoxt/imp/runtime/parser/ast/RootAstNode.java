package org.strategoxt.imp.runtime.parser.ast;

import org.eclipse.core.resources.IResource;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

public class IStrategoTerm extends IStrategoTerm {
	
	private final SGLRParseController controller;
	
	private IResource resource;
	
	@Override
	public IResource getResource() {
		return resource;
	}
	
	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
	@Override
	public SGLRParseController getParseController() {
		return controller;
	}

	protected IStrategoTerm(IStrategoTerm ast, SGLRParseController controller, IResource resource) {
		super(getSort(ast), getLeftToken(ast), getRightToken(ast), ast.getConstructor(),
				ast.getChildren());
		
		this.resource = resource;
		this.controller = controller;
		
		overrideReferences(getLeftIToken(), getRightIToken(), getChildren(), ast);
	}
	
	public static IStrategoTerm makeRoot(IStrategoTerm ast, SGLRParseController controller, IResource resource) {
		return new IStrategoTerm(ast, controller, resource);
	}
	
	@Override
	public IStrategoTerm cloneIgnoreTokens() {
		return (IStrategoTerm) super.cloneIgnoreTokens();
	}
}
