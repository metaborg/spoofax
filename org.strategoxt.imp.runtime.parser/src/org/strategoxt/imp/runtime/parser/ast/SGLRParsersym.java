package org.strategoxt.imp.runtime.parser.ast;

/**
 * Class containing constant values for default SGLR parser token kinds.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRParsersym {
	/** Token kind for a generic identifier. */
	public static final int
		TK_IDENTIFIER = -1;
	
	/** Token kind for a generic keyword token. */
	public static final int
		TK_KEYWORD = -2;
	
	/** Token kind for a layout token. */
	public static final int
		TK_LAYOUT = -3;
	
	/** Token kind for an EOF token. */
	public static final int
		TK_EOF = -4;

	/** Unknown token kind. */
	public static final int
		TK_UNKNOWN = 0;
}
