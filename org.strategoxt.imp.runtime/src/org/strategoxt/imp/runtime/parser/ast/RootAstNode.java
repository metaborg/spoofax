package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.ISourceInfo;

public class RootAstNode extends AstNode {
	private final ISourceInfo sourceInfo;

	@Override
	public ISourceInfo getSourceInfo() {
		return sourceInfo;
	}
	
	@Override
	public IStrategoAppl getTerm() {
		return (IStrategoAppl) super.getTerm();
	}

	protected RootAstNode(String sort, IToken leftToken, IToken rightToken, String constructor,
			ArrayList<AstNode> children, ISourceInfo sourceInfo) {
		
		super(sort, leftToken, rightToken, constructor, children);
		
		this.sourceInfo = sourceInfo;
	}
	
	public static RootAstNode makeRoot(AstNode ast, ISourceInfo sourceInfo) {
		return new RootAstNode(
				ast.getSort(), ast.getLeftIToken(), ast.getRightIToken(), ast.getConstructor(),
				ast.getChildren(), sourceInfo);
	}
}
