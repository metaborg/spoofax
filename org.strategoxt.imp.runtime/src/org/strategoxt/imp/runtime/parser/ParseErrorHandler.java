package org.strategoxt.imp.runtime.parser;

import static org.spoofax.jsglr.Term.*;
import lpg.runtime.IToken;
import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.ISourceInfo;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

/**
 * SGLR parse error reporting for a particular SGLR Parse controller and file. 
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ParseErrorHandler {
	
	public static final String WATER = "WATER";
	
	private static final String INSERT = "INSERTION";
	
	private static final String INSERT_END = "INSERTEND";
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);
	
	private final ISourceInfo sourceInfo;
	
	private boolean isRecoveryEnabled = true;

	private IMessageHandler messages;
	
	private int offset;

	public ParseErrorHandler(ISourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	
	public void clearErrors() {
		try {
			messages.clearMessages();
		} catch (RuntimeException e) {
			// Might happen if editor is closed
			Environment.logException("Exception occurred in clearing error markers", e);
		}
		handler.clearMarkers(sourceInfo.getResource());
	}
	
	public void setMessages(IMessageHandler messages) {
		this.messages = messages;
	}
	
	/**
	 * Informs the parse error handler that recovery is unavailable.
	 * This information is reflected in any parse error messages.
	 */
	public void setRecoveryEnabled(boolean recoveryEnabled) {
		this.isRecoveryEnabled = recoveryEnabled;
	}
	
	/**
	 * Report WATER + INSERT errors from parse tree
	 */
	public void reportNonFatalErrors(SGLRTokenizer tokenizer, ATerm top) {
		try {
			offset=0;
			reportOnRepairedCode(tokenizer, termAt(top, 0));
		} catch (RuntimeException e) {
			reportError(tokenizer, e);
		}
	}

	private void reportOnRepairedCode(SGLRTokenizer tokenizer, ATerm term) {
		// TODO: Nicer error messages; merge consecutive error tokens etc.
		
		if ("amb".equals(((ATermAppl) term).getAFun().getName())) {
			for (ATermList cons = (ATermList) term.getChildAt(0); !cons.isEmpty(); cons = cons.getNext()) {
				reportOnRepairedCode(tokenizer, cons.getFirst());
			}
			return;
		}
		
		ATermAppl prod = termAt(term, 0);
		ATermAppl rhs = termAt(prod, 1);
		ATermAppl attrs = termAt(prod, 2);
		ATermList contents = termAt(term, 1);
		int beginErrorOffSet = offset;
		
		// Recurse the tree and update the offset
		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);
			if (child.getType() == ATerm.INT) {
				offset += 1;				
			} else {
				reportOnRepairedCode(tokenizer, child);
			}
		}
		
		//post visit: report error				
		if (isErrorProduction(attrs, WATER)) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset - 1);
			reportErrorAtTokens(token, token, "'" + token + "' not expected here");
		} else if (isErrorProduction(attrs, INSERT_END)) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset - 1);
			reportErrorAtTokens(token, token, "Closing of '" + token + "' is expected here");
		} else if (isErrorProduction(attrs, INSERT)) {
			IToken token = tokenizer.makeErrorTokenSkipLayout(beginErrorOffSet, offset + 1);
			String inserted = "";
			if (rhs.getName() == "lit") {
				inserted = applAt(rhs, 0).getName();
			}
			reportErrorAtTokens(token, token, "Expected: '" + inserted + "'");
		}
	}
	
		
	public void reportError(SGLRTokenizer tokenizer, TokenExpectedException exception) {
		String message = exception.getShortMessage();
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		
		reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(SGLRTokenizer tokenizer, BadTokenException exception) {
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		String message = exception.isEOFToken()
			? exception.getShortMessage()
			: "'" + token + "' not expected here";
		reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(SGLRTokenizer tokenizer, ParseTimeoutException exception) {
		Environment.logException(exception);
		String message = "Internal parsing error: " + exception.getMessage();
		reportErrorAtFirstLine(message);
	}
	 
	public void reportError(SGLRTokenizer tokenizer, Exception exception) {
		String message = "Internal parsing error: " + exception;
		Environment.logException("Internal parsing error", exception);
		reportErrorAtFirstLine(message);
	}
	
	private void reportErrorAtTokens(IToken left, IToken right, String message) {
		// UNDONE: Using IMP message handler
		// TODO: Cleanup - remove messages field and related code
		//messages.handleSimpleMessage(
		// 		message, max(0, left.getStartOffset()), max(0, right.getEndOffset()),
		// 		left.getColumn(), right.getEndColumn(), left.getLine(), right.getEndLine());
		
		if (!isRecoveryEnabled)
			message += " (recovery unavailable)";
		
		handler.addMarker(sourceInfo.getResource(), left, right, message, IMarker.SEVERITY_ERROR);
	}
	
	private void reportErrorAtFirstLine(String message) {
		if (!isRecoveryEnabled)
			message += " (recovery unavailable)";
		
		handler.addMarkerFirstLine(sourceInfo.getResource(), message, IMarker.SEVERITY_ERROR);
	}	
	
	private static boolean isErrorProduction(ATermAppl attrs, String consName) {		
		if ("attrs".equals(attrs.getName())) {
			ATermList attrList = termAt(attrs, 0);
		
			for (int i=0; i<attrList.getLength(); i++) {							
			ATermAppl term = termAt(attrList, i);
			if (term.getName().equals("term")) {
				ATermAppl details = applAt(term, 0);
				if (details.getName().equals("cons")) {
					details = applAt(details, 0);					
					return details.getName().equals(consName);
				}
			}
			}
		}
		return false;
	}
}
