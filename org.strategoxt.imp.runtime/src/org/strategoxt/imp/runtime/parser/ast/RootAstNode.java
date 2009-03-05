package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.ISourceInfo;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

public class RootAstNode extends AstNode {
	private final ISourceInfo lsourceInfo;

	@Override
	public ISourceInfo getSourceInfo() {
		return lsourceInfo;
	}
	
	@Override
	public IStrategoAppl getTerm() {
		return (IStrategoAppl) super.getTerm();
	}

	protected RootAstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children, ISourceInfo sourceInfo) {
		
		super(sort, constructor, leftToken, rightToken, children);
		
		this.lsourceInfo = sourceInfo;
	}
	
	public static RootAstNode makeRoot(AstNode ast, ISourceInfo sourceInfo) {
		return new RootAstNode(
				ast.getSort(), ast.getConstructor(), ast.getLeftIToken(), ast.getRightIToken(),
				ast.getChildren(), sourceInfo);
	}
}
