package org.strategoxt.imp.runtime.parser.tokens;

import static org.spoofax.jsglr.Term.*;

import org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

import static org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym.*;

// TODO2: Token kind heuristic determines the colors, which needs to be migrated to the generator

/**
 * Class that handles producing and printing token kinds.
 * 
 * @note Should be overridden for specific grammars.
 * 
 * @see SGLRParsersym
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRTokenKindManager {
	private static final int RANGE_START = 0;
	
	private static final int RANGE_END = 1;
	
	// General token kind information
	
	public String getName(int kind) {
		return SGLRTokenKindManager.getDefaultName(kind);
	}
	
	public boolean isKeyword(int kind) {
		return kind == TK_KEYWORD || !isGenericToken(kind); // assume all non-default tokens are keywords
	}
	
	/**
	 * Get the token kind for a given sort.
	 */
	public int getTokenKind(ATermList pattern, ATermAppl sort) {
		// TODO2: Optimization - cache default token kinds
		
		if (AsfixAnalyzer.isLayout(sort)) {
			return TK_LAYOUT;
		} else if (sort.getName().equals("lex")) {
			if (isStringLiteral(pattern)) {
				return TK_STRING;
			} else if (isNumberLiteral(pattern)) {
				return TK_NUMBER;
			} else {
				return TK_IDENTIFIER;
			}
		} else if (sort.getName().equals("varsym")) {
			return TK_VAR;
		} else if (isOperator(sort)) {
			return TK_OPERATOR;
		} else {
			return TK_KEYWORD;
		}
	}
	
	static String getDefaultName(int kind) {
    	switch (kind) {
    		case TK_IDENTIFIER:
    			return "TK_IDENTIFIER";
    		case TK_KEYWORD:
    			return "TK_KEYWORD";
    		case TK_OPERATOR:
    			return "TK_OPERATOR";
    		case TK_NUMBER:
    			return "TK_NUMBER";
    		case TK_STRING:
    			return "TK_STRING";
    		case TK_LAYOUT:
    			return "TK_LAYOUT";
    		case TK_VAR:
    			return "TK_VAR";
    		case TK_JUNK:
    			return "TK_JUNK";
    		case TK_EOF:
    			return "TK_EOF";
    		default:
    			return "TK_UNKNOWN";
    	}
    }

	protected static boolean isOperator(ATermAppl sort) {
		if (!AsfixAnalyzer.isLiteral(sort)) return false;
		
		ATermAppl lit = applAt(sort, 0);
		String contents = lit.getName();
		
		for (int i = 0; i < contents.length(); i++) {
			char c = contents.charAt(i);
			if (Character.isLetter(c)) return false;
		}
		
		return true;
	}
	
	protected static boolean isStringLiteral(ATermList pattern) {
		return topdownHasSpaces(pattern);
	}
	
	private static boolean topdownHasSpaces(ATerm term) {
		// Return true if any character range of this contains spaces
		for (int i = 0; i < term.getChildCount(); i++) {
			ATerm child = termAt(term, i);
			if (isAppl(child) && asAppl(child).getName().equals("range")) {
				int start = intAt(child, RANGE_START);
				int end = intAt(child, RANGE_END);
				if (start <= ' ' && ' ' <= end) return true;
			} else {
				if (topdownHasSpaces(child)) return true;
			}
		}
		
		return false;
	}
	
	protected static boolean isNumberLiteral(ATermList pattern) {
		ATerm range = getFirstRange(pattern);
		
		return range != null && intAt(range, RANGE_START) == '0' && intAt(range, RANGE_END) == '9';
	}
	
	private static ATerm getFirstRange(ATerm term) {
		// Get very first character range in this term
		for (int i = 0; i < term.getChildCount(); i++) {
			ATerm child = termAt(term, i);
			if (isAppl(child) && asAppl(child).getName().equals("range")) {
				return child;
			} else {
				child = getFirstRange(child);
				if (child != null) return child;
			}
		}
		
		return null;
	}
}
