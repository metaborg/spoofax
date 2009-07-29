package org.strategoxt.imp.runtime.parser;

import static org.spoofax.jsglr.Term.*;
import lpg.runtime.IToken;
import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.RecoveryConnector;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.ISourceInfo;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
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
	
	/**
	 * The constructor used for "water" recovery rules.
	 */
	public static final String WATER = "WATER";
	
	/**
	 * The constructor used for "insertion" recovery rules.
	 */
	public static final String INSERT = "INSERTION";
	
	/**
	 * The constructor used for "end insertion" recovery rules.
	 */
	public static final String INSERT_END = "INSERTEND";
	
	/**
	 * The parse stream character that indicates a character has
	 * been skipped by the parser.
	 */
	public static final char SKIPPED_CHAR = (char) -1;
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);
	
	private final ISourceInfo sourceInfo;
	
	private boolean isRecoveryAvailable = true;

	private IMessageHandler messages;
	
	private int offset;
	
	private boolean inLexicalContext;

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
	public void setRecoveryAvailable(boolean recoveryAvailable) {
		this.isRecoveryAvailable = recoveryAvailable;
	}
	
	/**
	 * Report WATER + INSERT errors from parse tree
	 */
	public void reportNonFatalErrors(SGLRTokenizer tokenizer, ATerm top) {
		try {
			offset = 0;
			reportSkippedFragments(tokenizer);
			ATermAppl asfix = termAt(top, 0);
			reportRecoveredErrors(tokenizer, asfix, 0, 0);
		} catch (RuntimeException e) {
			reportError(tokenizer, e);
		}
	}

    /**
     * Report recoverable errors (e.g., inserted brackets).
     * 
	 * @param outerBeginOffset  The begin offset of the enclosing construct.
     */
	private void reportRecoveredErrors(SGLRTokenizer tokenizer, ATermAppl term, int outerStartOffset, int outerStartOffset2) {
		// TODO: Nicer error messages; merge consecutive error tokens etc.
		int startOffset = offset;
		
		if ("amb".equals(term.getAFun().getName())) {
			// Report errors in first ambiguous branch and update offset
			ATermList ambs = termAt(term, 0);
			reportRecoveredErrors(tokenizer, (ATermAppl) ambs.getFirst(), startOffset, outerStartOffset);
			
			reportAmbiguity(tokenizer, term, startOffset);
			return;
		}
		
		ATermAppl prod = termAt(term, 0);
		ATermAppl rhs = termAt(prod, 1);
		ATermAppl attrs = termAt(prod, 2);
		ATermList contents = termAt(term, 1);
		boolean lexicalStart = false;
		
		if (!inLexicalContext && AsfixImploder.isLexicalNode(rhs) || AsfixImploder.isVariableNode(rhs)) {
			inLexicalContext = lexicalStart = true;
		}
		
		// Recursively visit the subtree and update the offset
		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);
			if (child.getType() == ATerm.INT) {
				offset += 1;				
			} else {
				reportRecoveredErrors(tokenizer, (ATermAppl) child, startOffset, outerStartOffset);
			}
		}
		
		//post visit: report error				
		if (isErrorProduction(attrs, WATER)) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			reportErrorAtTokens(token, token, "Syntax error, '" + token + "' not expected here");
		} else if (isErrorProduction(attrs, INSERT_END)) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			reportErrorAtTokens(token, token, "Syntax error, Closing of '" + token + "' is expected here");
		} else if (isErrorProduction(attrs, INSERT)) {
			IToken token = tokenizer.makeErrorTokenSkipLayout(startOffset, offset + 1, outerStartOffset2);
			String inserted = "token";
			if (rhs.getName() == "lit") {
				inserted = applAt(rhs, 0).getName();
			}
			if (token.getLine() == tokenizer.getLexStream().getLine(outerStartOffset2) && !token.toString().equals(inserted)) {
				reportErrorAtTokens(token, token, "Syntax error, expected: '" + inserted + "'");
			} else {
				// Had to backtrack to the last token of the current line,
				// (or reporting a missing } at another })
				reportErrorAtTokens(token, token, "Syntax error, insert '" + inserted + "' to complete construct");
			}
		}
		
		if (lexicalStart) inLexicalContext = false;
	}
	
	private void reportAmbiguity(SGLRTokenizer tokenizer, ATermAppl amb, int startOffset) {
		if (!inLexicalContext && hasContextFreeNode(amb)) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			reportErrorAtTokens(token, token, "Fragment is ambiguous");
		}
	}
	
	private static boolean hasContextFreeNode(ATermAppl term) {
		if ("lit".equals(term.getAFun().getName()))
			return true;
		
		for (int i = 0; i < term.getChildCount(); i++) {
			ATerm child = termAt(term, i);
			if (child.getType() == ATerm.AFUN) {
				boolean success = hasContextFreeNode((ATermAppl) child);
				if (success) return true;
			}
		}
		
		return false;
	}
	
	private void reportSkippedFragments(SGLRTokenizer tokenizer) {
		char[] inputChars = tokenizer.getLexStream().getInputChars();
		for (int i = 0; i < inputChars.length; i++) {
			char c = inputChars[i];
			if (c == SKIPPED_CHAR) {
				int beginSkipped = i;
				int endSkipped = i;
				while (++i < inputChars.length) {
					c = inputChars[i];
					if (c == SKIPPED_CHAR)
						endSkipped = i;
					else if (!RecoveryConnector.isLayoutCharacter(c))
						break;
				}
				IToken token = tokenizer.makeErrorToken(beginSkipped, endSkipped);
				reportErrorAtTokens(token, token, "Could not parse this fragment");
			}
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
		
		if (!isRecoveryAvailable)
			message += " (recovery unavailable)";
		
		handler.addMarker(sourceInfo.getResource(), left, right, message, IMarker.SEVERITY_ERROR);
	}
	
	private void reportErrorAtFirstLine(String message) {
		if (!isRecoveryAvailable)
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
