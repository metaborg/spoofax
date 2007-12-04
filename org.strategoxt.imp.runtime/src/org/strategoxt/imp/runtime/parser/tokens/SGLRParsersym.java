package org.strategoxt.imp.runtime.parser.tokens;

/**
 * Class containing constant values for default SGLR parser token kinds.
 * 
 * @see SGLRTokenKindManager	Determines token kinds and prints token kind names.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRParsersym {	
	/** Token kind for a generic identifier. */
	public static final int
		TK_IDENTIFIER = -1;
	
	/** Token kind for a generic numeric value. */
	public static final int
		TK_NUMBER = -2;
	
	/**
	 * Token kind for a generic number literal.
	 * 
	 * @deprecated	Use {@link #TK_NUMBER} instead.
	 */
	@Deprecated // used in colorer_simple template
	public static final int
		TK_DoubleLiteral = TK_NUMBER;
	
	/** Token kind for a generic string literal. */
	public static final int
		TK_STRING = -3;
	
	/**
	 * Token kind for a generic string literal.
	 * 
	 * @deprecated	Use {@link #TK_STRING} instead.
	 */
	@Deprecated // commented in colorer_simple template
	public static final int
		TK_StringLiteral = TK_STRING;
	
	/** Token kind for a generic keyword token. */
	public static final int
		TK_KEYWORD = -4;
	
	/** Token kind for a generic keyword token. */
	public static final int
		TK_OPERATOR = -5;
	
	/** Token kind for a meta-variable. */
	public static final int
		TK_VAR = -6;
	
	/** Token kind for a layout (or comment) token. */
	public static final int
		TK_LAYOUT = -7;
	
	/** Token kind for an EOF token. */
	public static final int
		TK_EOF = -8;
	
	/** Token kind for an junk (invalid) token. */
	public static final int
		TK_JUNK = -9;

	/** Unknown token kind. */
	public static final int
		TK_UNKNOWN = 0;
	
	static boolean isGenericToken(int kind) {
		return kind <= 0;
	}
}
