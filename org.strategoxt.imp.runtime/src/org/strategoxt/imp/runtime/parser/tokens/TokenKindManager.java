package org.strategoxt.imp.runtime.parser.tokens;

import static org.spoofax.jsglr.Term.*;

import org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;

/**
 * Class that handles producing and printing token kinds.
 * 
 * @note May be overridden for specific grammars.
 * 
 * @see TokenKind
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class TokenKindManager {
	private static final int RANGE_START = 0;
	
	private static final int RANGE_END = 1;
	
	// General token kind information
	
	/**
	 * Get the token kind for a given sort.
	 */
	public TokenKind getTokenKind(ATermList pattern, ATermAppl sort) {
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
