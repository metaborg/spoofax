package org.strategoxt.imp.runtime.services;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NodeMapping<T> {
	private final static int NO_TOKEN_KIND = TokenKind.TK_RESERVED.ordinal();
	
	private final T attribute;
	
	private final String constructor, sort;
	
	private final int tokenKind;
	
	public NodeMapping(String constructor, String sort, TokenKind tokenKind, T attribute) {
		this.attribute = attribute;
		this.constructor = constructor;
		this.sort = sort;
		
		// We use ints for comparison with IMP's integer enum in IToken
		this.tokenKind = tokenKind == null ? NO_TOKEN_KIND : tokenKind.ordinal();
	}
	
	protected NodeMapping(IStrategoTerm pattern, T attribute) throws BadDescriptorException {
		this(termContents(findTerm(pattern, "Constructor")),
			 readSort(pattern),
			 readTokenKind(pattern),
			 attribute);
	}
	
	public static<T> NodeMapping<T> create(IStrategoTerm pattern, T attribute) throws BadDescriptorException {
		return new NodeMapping<T>(pattern, attribute);
	}
	
	private static TokenKind readTokenKind(IStrategoTerm pattern) throws BadDescriptorException {
		String tokenKind = cons(termAt(findTerm(pattern, "Token"), 0));
		try {
			return tokenKind == null ? null : TokenKind.valueOf(tokenKind);
		} catch (IllegalArgumentException e) {
			throw new BadDescriptorException("Could not set the coloring rule for token kind: " + tokenKind, e);
		}
	}
	
	private static String readSort(IStrategoTerm pattern) {
		String result = termContents(findTerm(pattern, "Sort"));
		String listSort = termContents(findTerm(pattern, "Sort"));
		if (listSort != null) result = listSort + "*";
		return result;
	}
	
	public T getAttribute(String constructor, String sort, int tokenKind) {
		if (this.constructor == null || this.constructor.equals(constructor)) {
			if (this.sort == null || this.sort.equals(sort)) {
				if (this.tokenKind == NO_TOKEN_KIND || this.tokenKind == tokenKind) {
					return attribute;
				}
			}
		}
		return null;
	}
}
