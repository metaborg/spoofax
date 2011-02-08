package org.strategoxt.imp.runtime.parser.tokens;

import static org.spoofax.terms.Term.*;

import org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer;

import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;
import org.spoofax.interpreter.terms.IStrategoList;

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
	public int getTokenKind(IStrategoList pattern, ATermAppl sort) {
		// TODO2: Optimization - cache default token kinds?
		
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
	
	/**
	 * Determines whether the given string could possibly 
	 * be a keyword (as opposed to an operator).
	 */
	public static boolean isKeyword(String literal) {
		for (int i = 0, end = literal.length(); i < end; i++) {
			char c = literal.charAt(i);
			if (!isKeywordChar(c))
				return false;
		}
		return true;
	}
	
	/**
	 * Determines whether the given character could possibly 
	 * be part of a keyword (as opposed to an operator).
	 */
	public static boolean isKeywordChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
	
	protected static boolean isStringLiteral(IStrategoList pattern) {
		return topdownHasSpaces(pattern);
	}
	
	private static boolean topdownHasSpaces(IStrategoTerm term) {
		// Return true if any character range of this contains spaces
		for (int i = 0; i < term.getChildCount(); i++) {
			IStrategoTerm child = termAt(term, i);
			if (isTermAppl(child) && asAppl(child).getName().equals("range")) {
				int start = intAt(child, RANGE_START);
				int end = intAt(child, RANGE_END);
				if (start <= ' ' && ' ' <= end) return true;
			} else {
				if (topdownHasSpaces(child)) return true;
			}
		}
		
		return false;
	}
	
	protected static boolean isNumberLiteral(IStrategoList pattern) {
		IStrategoTerm range = getFirstRange(pattern);
		
		return range != null && intAt(range, RANGE_START) == '0' && intAt(range, RANGE_END) == '9';
	}
	
	private static IStrategoTerm getFirstRange(IStrategoTerm term) {
		// Get very first character range in this term
		for (int i = 0; i < term.getChildCount(); i++) {
			IStrategoTerm child = termAt(term, i);
			if (isTermAppl(child) && asAppl(child).getName().equals("range")) {
				return child;
			} else {
				child = getFirstRange(child);
				if (child != null) return child;
			}
		}
		
		return null;
	}
}
