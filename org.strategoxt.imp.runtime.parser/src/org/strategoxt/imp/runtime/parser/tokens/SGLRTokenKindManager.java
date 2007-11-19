package org.strategoxt.imp.runtime.parser.tokens;

import static org.spoofax.jsglr.Term.*;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

import static org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym.*;

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
		
		if (isLayoutSort(sort)) {
			return TK_LAYOUT;
		} else if (sort.getName().equals("lex")) {
			if (isStringLiteral(pattern)) {
				return TK_STRING;
			} else if (isNumberLiteral(pattern)) {
				return TK_NUMBER;
			} else {
				return TK_IDENTIFIER;
			}
		} else if (isOperator(sort)) {
			return TK_OPERATOR;
		} else {
			return TK_KEYWORD;
		}
	}
	
	static String getDefaultName(int kind) {
    	switch (kind) {
    		case SGLRParsersym.TK_IDENTIFIER:
    			return "TK_IDENTIFIER";
    		case SGLRParsersym.TK_KEYWORD:
    			return "TK_KEYWORD";
    		case SGLRParsersym.TK_OPERATOR:
    			return "TK_OPERATOR";
    		case SGLRParsersym.TK_NUMBER:
    			return "TK_NUMBER";
    		case SGLRParsersym.TK_STRING:
    			return "TK_STRING";
    		case SGLRParsersym.TK_LAYOUT:
    			return "TK_LAYOUT";
    		case SGLRParsersym.TK_JUNK:
    			return "TK_JUNK";
    		case SGLRParsersym.TK_EOF:
    			return "TK_EOF";
    		default:
    			return "TK_UNKNOWN";
    	}
    }

	protected static boolean isLayoutSort(ATermAppl sort) {
		ATermAppl details = (ATermAppl) sort.getChildAt(0);
	
		if (details.getName().equals("opt"))
			details = (ATermAppl) details.getChildAt(0);
			
		return details.getName().equals("layout");
	}
	
	protected static boolean isOperator(ATermAppl sort) {
		if (sort.getName() != "lit") return false;
		
		ATermAppl lit = (ATermAppl) sort.getChildAt(0);
		String contents = lit.getName();
		
		for (int i = 0; i < contents.length(); i++) {
			char c = contents.charAt(i);
			if (Character.isLetterOrDigit(c)) return false;
		}
		
		return true;
	}
	
	protected static boolean isStringLiteral(ATermList pattern) {
		return topdownHasSpaces(pattern);
	}
	
	private static boolean topdownHasSpaces(ATerm term) {
		// Return true if any character range of this contains spaces
		for (int i = 0; i < term.getChildCount(); i++) {
			ATerm child = (ATerm) term.getChildAt(i);
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
			ATerm child = (ATerm) term.getChildAt(i);
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
