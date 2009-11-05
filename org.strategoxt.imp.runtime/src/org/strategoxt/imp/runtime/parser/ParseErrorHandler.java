package org.strategoxt.imp.runtime.parser;

import static org.spoofax.jsglr.Term.*;

import java.util.ArrayList;
import java.util.List;

import lpg.runtime.IToken;

import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.RecoveryConnector;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
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
	
	/**
	 * The parse stream character that indicates EOF was unexpected.
	 */
	public static final char UNEXPECTED_EOF_CHAR = (char) -2;
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);
	
	private final SGLRParseController source;
	
	private boolean isRecoveryAvailable = true;

	private IMessageHandler messages;
	
	private int offset;
	
	private boolean inLexicalContext;
	
	private List<Runnable> errorReports = new ArrayList<Runnable>();

	public ParseErrorHandler(SGLRParseController source) {
		this.source = source;
	}
	
	public void clearErrors() {
		try {
			messages.clearMessages();
		} catch (RuntimeException e) {
			// Might happen if editor is closed
			Environment.logException("Exception occurred in clearing error markers", e);
		}
		handler.clearMarkers(source.getResource());
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
	public void gatherNonFatalErrors(SGLRTokenizer tokenizer, ATerm top) {
		try {
			errorReports.clear();
			offset = 0;
			reportSkippedFragments(tokenizer);
			ATermAppl asfix = termAt(top, 0);
			reportRecoveredErrors(tokenizer, asfix, 0, 0);
		} catch (RuntimeException e) {
			reportError(tokenizer, e);
		}
	}
	
	public void applyMarkers() {
		for (Runnable marker : errorReports) {
			marker.run();
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
			tokenizer.changeTokenKinds(startOffset, offset - 1, TokenKind.TK_ERROR);
			reportErrorAtTokens(token, token, "Syntax error, '" + token + "' not expected here");
		} else if (isErrorProduction(attrs, INSERT_END)) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			tokenizer.changeTokenKinds(startOffset, offset - 1, TokenKind.TK_ERROR);
			reportErrorAtTokens(token, token, "Syntax error, Closing of '" + token + "' is expected here");
		} else if (isErrorProduction(attrs, INSERT)) {
			IToken token = tokenizer.makeErrorTokenSkipLayout(startOffset, offset + 1, outerStartOffset2);
			String inserted = "token";
			if (rhs.getName().equals("lit")) {
				inserted = applAt(rhs, 0).getName();
			} else if (rhs.getName().equals("char-class")) {
				inserted = toString(listAt(rhs, 0));
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
	
	private static String toString(ATermList chars) {
		// TODO: move to SSL_implode_string.call() ?
        StringBuilder result = new StringBuilder(chars.getLength());

        while (chars.getFirst() != null) {
        	ATermInt v = (ATermInt) chars.getFirst();
            result.append((char) v.getInt());
            chars = chars.getNext();
        }
        
        return result.toString();
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
				// Recovered by skipping a region
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
				tokenizer.changeTokenKinds(beginSkipped, endSkipped, TokenKind.TK_ERROR);
				reportErrorAtTokens(token, token, "Could not parse this fragment");
			} else if (c == UNEXPECTED_EOF_CHAR) {
				// Recovered using a forced reduction
				IToken token = tokenizer.makeErrorTokenBackwards(i);
				if (token.getStartOffset() == 0) break; // be less complainy about single-token files
				reportErrorAtTokens(token, token, "End of file unexpected");
			}
		}
		
		// Report forced reductions
		int treeEnd = tokenizer.getParseStream().getTokenAt(tokenizer.getParseStream().getStreamLength() - 1).getEndOffset();
		if (treeEnd < inputChars.length) {
			IToken token = tokenizer.makeErrorToken(treeEnd + 1, inputChars.length);
			reportErrorAtTokens(token, token, "Could not parse the remainder of this file");
			tokenizer.changeTokenKinds(treeEnd + 1, inputChars.length, TokenKind.TK_ERROR);
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
		Environment.logException("Error while reporting parse errors", exception);
		reportErrorAtFirstLine(message);
	}
	
	private void reportErrorAtTokens(final IToken left, final IToken right, String message) {
		// UNDONE: Using IMP message handler
		// TODO: Cleanup - remove messages field and related code
		//messages.handleSimpleMessage(
		// 		message, max(0, left.getStartOffset()), max(0, right.getEndOffset()),
		// 		left.getColumn(), right.getEndColumn(), left.getLine(), right.getEndLine());
		
		final String message2 = isRecoveryAvailable ? message : message + " (recovery unavailable)";
		
		errorReports.add(new Runnable() {
			public void run() {
				handler.addMarker(source.getResource(), left, right, message2, IMarker.SEVERITY_ERROR);
			}
		});
	}
	
	private void reportErrorAtFirstLine(String message) {
		final String message2 = isRecoveryAvailable ? message : message + " (recovery unavailable)";
		
		errorReports.add(new Runnable() {
			public void run() {
				handler.addMarkerFirstLine(source.getResource(), message2, IMarker.SEVERITY_ERROR);
			}
		});
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
