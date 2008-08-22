package org.strategoxt.imp.runtime.services;

import org.eclipse.jface.text.TextAttribute;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ColorMapping {
	private final static int NO_TOKEN_KIND = TokenKind.TK_RESERVED.ordinal();
	
	private final TextAttributeReference attribute;
	
	private final String constructor, sort;
	
	private final int tokenKind;
	
	public ColorMapping(String constructor, String sort, TokenKind tokenKind, TextAttributeReference attribute) {
		this.attribute = attribute;
		this.constructor = constructor;
		this.sort = sort;
		
		// We use ints for comparison with IMP's integer enum in IToken
		this.tokenKind = tokenKind == null ? NO_TOKEN_KIND : tokenKind.ordinal();
	}
	
	public TextAttribute getAttribute(String constructor, String sort, int tokenKind) {
		if (this.constructor == null || this.constructor.equals(constructor)) {
			if (this.sort == null || this.sort.equals(sort)) {
				if (this.tokenKind == NO_TOKEN_KIND || this.tokenKind == tokenKind) {
					return attribute.get();
				}
			}
		}
		return null;
	}
}
