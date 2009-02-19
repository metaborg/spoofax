package org.strategoxt.imp.runtime.parser;

import static java.lang.Math.*;
import lpg.runtime.IToken;

import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;

/**
 * SGLR parse error reporting for a particular SGLR Parse controller and file. 
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ParseErrorHandler {
	
	private static final String WATER = "Water";
	
	private final IMessageHandler messages;
	
	private final SGLRTokenizer tokenizer;
	
	public ParseErrorHandler(IMessageHandler messages, SGLRTokenizer tokenizer) {
		this.messages = messages;
		this.tokenizer = tokenizer;
	}
	
	public void clearErrors() {
		messages.clearMessages();
	}
	
	public void reportNonFatalErrors(AstNode ast) {
		// TODO: Report any insertions using the asfix tree
		new AbstractVisitor() {
			public boolean preVisit(AstNode node) {
				if (WATER.equals(node.getConstructor())) {
					reportErrorAtTokens(node.getLeftIToken(), node.getRightIToken(), "Unexpected text fragment");
				}
				return true;
			}

			public void postVisit(AstNode node) {
				// Nothing to see here; move along.
			}
			
		};
	}
	
	public void reportError(TokenExpectedException exception) {
		String message = exception.getShortMessage();
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		
		reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(BadTokenException exception) {
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		String message = exception.isEOFToken()
        	? exception.getShortMessage()
        	: "'" + token.toString() + "' not expected here";

        	reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(Exception exception) {
		String message = "Internal parsing error: " + exception;
		IToken token = tokenizer.makeErrorToken(0);
		
		Environment.logException("Internal parsing error", exception);
		
		reportErrorAtTokens(token, token, message);
	}
	
	private void reportErrorAtTokens(IToken left, IToken right, String message) {
		messages.handleSimpleMessage(
				message, max(0, left.getStartOffset()), max(0, right.getEndOffset()),
				left.getColumn(), right.getEndColumn(), left.getLine(), right.getEndLine());
		// UNDONE: Using AstMessageHandler
		// parseErrors.addMarker(getProject().getRawProject().getFile(path), token, token, message, IMarker.SEVERITY_ERROR);
	}
}
