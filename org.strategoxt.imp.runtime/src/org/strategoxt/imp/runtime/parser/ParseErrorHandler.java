package org.strategoxt.imp.runtime.parser;

import static java.lang.Math.*;
import static org.spoofax.jsglr.Term.*;
import lpg.runtime.IToken;

import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
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
	
	private IMessageHandler messages;
	
	private int offset;
	
	public void clearErrors() {
		messages.clearMessages();
	}
	
	public void setMessages(IMessageHandler messages) {
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
		// TODO: Nicer error messages; merge sequential error tokens etc.
		
		//TODO: use constants in AsfixImploder
		
		ATermAppl prod = termAt(term, 0);
		ATermAppl rhs = termAt(prod, 1);
		ATermAppl attrs = termAt(prod, 2);
		ATermList contents = termAt(term, 1);
		boolean isWaterTerm = isWater(rhs);
		boolean isInsertTerm = isInsert(attrs);		
		int beginErrorOffSet = 0;		
		
		//pre visit: keep offset as begin of error
		if(isWaterTerm || isInsertTerm)
        { 
        	beginErrorOffSet = offset;        	
        }
		
		// Recurse the tree and update the offset
		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);
			if (child.getType() == ATerm.INT){
				offset+=1;				
				}
			else
				reportOnRepairedCode(tokenizer, child);				
		}
		
		//post visit: report error
		if (isWaterTerm) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset - 1);
			reportErrorAtTokens(token, token, "Unexpected token");
		}
		if (isInsertTerm) {
			IToken token = tokenizer.makeErrorToken(beginErrorOffSet, offset);
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
        	: "'" + token.toString() + "' not expected here";

        	reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(SGLRTokenizer tokenizer, Exception exception) {
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
	
	private static boolean isWater(ATermAppl cf) {
		ATermAppl details = applAt(cf, 0);
		
		if (details.getName().equals("sort")) {	
			details = applAt(details, 0);
			return details.getName().equals(WATER);
		}
		return false;
	}
	
	private static boolean isInsert(ATermAppl attrs) {		
		if ("attrs".equals(attrs.getName())) {
			ATermList attrList = termAt(attrs, 0);
		
			ATermAppl term = termAt(attrList, 0);
			if (term.getName().equals("term")) {
				ATermAppl details = applAt(term, 0);
				if (details.getName().equals("cons")) {
					details = applAt(details, 0);					
					return details.getName().equals(INSERT);
				}
			}
		}
		return false;
	}
}
