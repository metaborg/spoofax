package org.strategoxt.imp.runtime.parser;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.AstLocator;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;

public class SGLRAstLocator extends AstLocator {

	@Override
	public Object findNode(Object ast, int startOffset, int endOffset) {
		return super.findNode(((SGLRToken)ast).getAstNode(), startOffset, endOffset);
	}
	
	@Override
	public Object findNode(Object node, int offset) {
		// TODO: Can this happen?
		if (node instanceof SGLRToken)
			node = ((SGLRToken) node).getAstNode();
		
		return super.findNode(node, offset);
	}
	
	@Override
	public int getStartOffset(Object node) {
//		return super.getStartOffset(((SGLRToken)node).getAstNode());
		return ((SGLRToken)node).getStartOffset();
	}
	
	@Override
	public int getEndOffset(Object node) {
//		return super.getEndOffset(((SGLRToken)node).getAstNode());
		return ((SGLRToken)node).getEndOffset();
	}
	
	@Override
	public int getLength(Object node) {
//		return super.getLength(((SGLRToken)node).getAstNode());
		return getEndOffset(node) - getStartOffset(node);
	}
	
	@Override
	public IPath getPath(Object node) {
		return super.getPath(((SGLRToken)node).getAstNode());
	}
	
}
