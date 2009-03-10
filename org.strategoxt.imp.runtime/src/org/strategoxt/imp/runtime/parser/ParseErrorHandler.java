package org.strategoxt.imp.runtime.parser;

import static org.spoofax.jsglr.Term.*;
import lpg.runtime.IToken;
import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
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
	
	private static final String WATER = "WATER";
	
	private static final String INSERT = "INSERTION";
	
	private static final String INSERT_END = "INSERTEND";
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);
	
	private final ISourceInfo sourceInfo;

	private IMessageHandler messages;
	
	private int offset;

	public ParseErrorHandler(ISourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	
	public void clearErrors() {
		messages.clearMessages();
		handler.clearMarkers(sourceInfo.getResource());
	}
	
	public void setMessages (IMessageHandler messages) {
		this.messages = messages;
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
		
		// TODO: Report error for INSERTEND tokens
		
		// TODO: Try using constructor matching to recognize WATER tokens..?
		//       (which wasn't working before...)
		
		ATermAppl prod = termAt(term, 0);
		ATermAppl rhs = termAt(prod, 1);
		ATermAppl attrs = termAt(prod, 2);
		ATermList contents = termAt(term, 1);
		boolean isWaterTerm = isErrorProduction(attrs, WATER);	//isWater(rhs);
		boolean isInsertTerm = isErrorProduction(attrs, INSERT);	
		boolean isEndInsertTerm = isErrorProduction(attrs, INSERT_END);	
		int beginErrorOffSet = 0;		
		
		//pre visit: keep offset as begin of error
		if(isWaterTerm || isInsertTerm || isEndInsertTerm)
        { 
        	beginErrorOffSet = offset;        	
        }
		
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
		if (isWaterTerm) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset - 1);
			reportErrorAtTokens(token, token, "'" + token + "' not expected here");
		}
		if (isEndInsertTerm) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset - 1);
			reportErrorAtTokens(token, token, "Closing of '" + token + "' is expected here");
		}
		if (isInsertTerm) {
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
	 
	public void reportError(SGLRTokenizer tokenizer, Exception exception) {
		String message = "Internal parsing error: " + exception;
		IToken token = tokenizer.makeErrorToken(0);
		
		Environment.logException("Internal parsing error", exception);
		
		reportErrorAtTokens(token, token, message);
	}
	
	private void reportErrorAtTokens(IToken left, IToken right, String message) {
		// UNDONE: Using IMP message handler
		// TODO: Cleanup - remove messages field and related code
		//messages.handleSimpleMessage(
		// 		message, max(0, left.getStartOffset()), max(0, right.getEndOffset()),
		// 		left.getColumn(), right.getEndColumn(), left.getLine(), right.getEndLine());
		handler.addMarker(sourceInfo.getResource(), left, right, message, IMarker.SEVERITY_ERROR);
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
