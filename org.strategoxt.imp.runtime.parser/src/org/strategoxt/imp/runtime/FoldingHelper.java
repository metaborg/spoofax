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
		// Consume any layout tokens that appear at the end of our AST node		
		while (parseStream.getStreamLength() >= lastToken.getTokenIndex()) {
			IToken next = parseStream.getTokenAt(lastToken.getTokenIndex() + 1);
			
			if (next.getKind() == SGLRParsersym.TK_LAYOUT) {
				lastToken = next;
				if (containsEndOfLine(next)) break;
			} else {
				break;
			}
		}
		
		defaultMakeAnnotation(firstToken, lastToken);
	}
	
	private boolean containsEndOfLine(IToken token) {
		int end = token.getEndOffset();
		
		for (int i = token.getStartOffset(); i < end; i++) {
			char c = lexStream.getCharValue(i);
			if (c == '\n' || c == '\r')
				return true;
		}
		
		return false;
	}

	// Default IMP implementation of makeAnnotation (private in generated code...)
	private void defaultMakeAnnotation(IToken firstToken, IToken lastToken) {
	    if (lastToken.getEndLine() > firstToken.getLine()) {
			IToken next_token = parseStream.getIToken(parseStream
					.getNext(lastToken.getTokenIndex()));
			
			IToken[] adjuncts = next_token.getPrecedingAdjuncts();
			IToken gate_token = adjuncts.length == 0 ? next_token : adjuncts[0];
			
			int firstOffset = firstToken.getStartOffset();
			int lastOffset = gate_token
					.getLine() > lastToken.getEndLine() ? parseStream
					.getLexStream().getLineOffset(gate_token.getLine() - 1)
					: lastToken.getEndOffset();
			
			folder.makeAnnotation(firstOffset, lastOffset - firstOffset + 1);
		}
    }
}
