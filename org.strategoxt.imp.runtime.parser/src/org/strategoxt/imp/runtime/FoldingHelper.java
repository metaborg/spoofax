package org.strategoxt.imp.runtime;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.imp.services.base.FolderBase;
import org.strategoxt.imp.runtime.parser.ast.SGLRAstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym;

// TODO: FoldingHelper should not be in the "parser" plugin (nor should this package be exported)

public class FoldingHelper {
	private final FolderBase folder;
	private final IPrsStream parseStream;
	private final ILexStream lexStream;
	
	public FoldingHelper(FolderBase folder, IPrsStream parseStream) {
		this.folder = folder;
		this.parseStream = parseStream;
		
		lexStream = parseStream.getLexStream();
	}
	
	public void makeCompleteAnnotation(SGLRAstNode node) {
		makeCompleteAnnotation(node.getLeftIToken(), node.getRightIToken());
	}
	
	public void makeCompleteAnnotation(IToken firstToken, IToken lastToken) {
		final int start = firstToken.getEndOffset();
		int end = -1;
		
		if (firstToken.getLine() != lastToken.getLine()) {
			// Consume any layout tokens at the end of our AST node until the next EOL		
			while (parseStream.getStreamLength() >= lastToken.getTokenIndex()) {
				IToken next = parseStream.getTokenAt(lastToken.getTokenIndex() + 1);
				
				if (next.getKind() == SGLRParsersym.TK_LAYOUT) {
					lastToken = next;
					if ((end = getEndOfLinePosition(next)) != -1) break;
				} else {
					// Next AST node starts at the same line!
					break;
				}
			}
			
			if (end == -1) end = lastToken.getEndOffset();
			folder.makeAnnotation(start, end - start + 1);
		}
	}
	
	private int getEndOfLinePosition(IToken token) {
		int end = token.getEndOffset();
		
		for (int i = token.getStartOffset(); i <= end; i++) {
			char c = lexStream.getCharValue(i);
			if (c == '\n' || c == '\r')
				return i;
		}
		
		return -1;
	}
}
