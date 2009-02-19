package org.strategoxt.imp.runtime.parser.tokens;

/**
 * Class containing constant values for default SGLR parser token kinds.
 * 
 * @see TokenKindManager	Determines token kinds and prints token kind names.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public enum TokenKind {	
	/** Token kind for a generic identifier. */
	TK_IDENTIFIER,
	
	/** Token kind for a generic numeric value. */
	TK_NUMBER,
	
	/** Token kind for a generic string literal. */
	TK_STRING,
	
	/** Token kind for a generic keyword token. */
	TK_KEYWORD,
	
	/** Token kind for a generic keyword token. */
	TK_OPERATOR,
	
	/** Token kind for a meta-variable. */
	TK_VAR,
	
	/** Token kind for a layout (or comment) token. */
	TK_LAYOUT,
	
	/** Token kind for an EOF token. */
	TK_EOF,
	
	/** Token kind for an erroneous token. */
	TK_ERROR,

	/** Unknown token kind. */
	TK_UNKNOWN,
	
	TK_RESERVED,
	
	TK_NO_TOKEN_KIND;
	
	public static TokenKind valueOf(int ordinal) {
		if (0 <= ordinal && ordinal < values().length)
			return values()[ordinal];
		else
			return TK_UNKNOWN;
	}
}
