package org.strategoxt.imp.runtime.services;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.strategoxt.imp.runtime.parser.ast.AstNode;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;

/**
 * Base class for a folding service. Includes special logic to deal with
 * layout tokens in SGLR token streams.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class FoldingBase extends org.eclipse.imp.services.base.FolderBase {
	private IPrsStream parseStream;

	public void makeCompleteAnnotation(AstNode node) {
		makeCompleteAnnotation(node.getLeftIToken(), node.getRightIToken());
	}

	public void makeCompleteAnnotation(IToken firstToken, IToken lastToken) {
		final int start = firstToken.getEndOffset();
		int end = -1;

		if (firstToken.getLine() != lastToken.getLine()) {
			// Consume any layout tokens at the end of our AST node until the
			// next EOL
			while (parseStream.getStreamLength() >= lastToken.getTokenIndex()) {
				IToken next = parseStream.getTokenAt(lastToken.getTokenIndex() + 1);

				if (next.getKind() == TK_LAYOUT.ordinal()) {
					lastToken = next;
					if ((end = getEndOfLinePosition(next)) != -1)
						break;
				} else {
					// Next AST node starts at the same line!
					break;
				}
			}

			if (end == -1)
				end = lastToken.getEndOffset();
			
			makeAnnotation(start, end - start + 1);
		}
	}

	private int getEndOfLinePosition(IToken token) {
		int end = token.getEndOffset();

		ILexStream lexStream = parseStream.getLexStream();

		for (int i = token.getStartOffset(); i <= end; i++) {
			char c = lexStream.getCharValue(i);
			if (c == '\n' || c == '\r')
				return i;
		}

		return -1;
	}

	@Override
	public void sendVisitorToAST(java.util.HashMap newAnnotations, java.util.List annotations,
			java.lang.Object node) {
		
		AstNode astNode = (AstNode) node;
		parseStream = astNode.getLeftIToken().getPrsStream();
		
		sendVisitorToAST(astNode);
	}
	
	protected abstract void sendVisitorToAST(AstNode node);
}
