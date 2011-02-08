package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.IToken.TK_NO_TOKEN_KIND;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.List;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.Token;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NodeMapping<T> {
	
	private final T attribute;
	
	private final String constructor, sort;
	
	private final int tokenKind;
	
	public NodeMapping(String constructor, String sort, int tokenKind, T attribute) {
		this.attribute = attribute;
		this.constructor = constructor;
		this.sort = sort;
		this.tokenKind = tokenKind;
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
	
	private static int readTokenKind(IStrategoTerm pattern) throws BadDescriptorException {
		IStrategoAppl tokenTerm = findTerm(pattern, "Token");
		String tokenKind = tokenTerm == null ? null : cons(termAt(tokenTerm, 0));
		try {
			return Token.valueOf(tokenKind);
		} catch (IllegalArgumentException e) {
			throw new BadDescriptorException("Could not set the coloring rule for token kind: " + tokenKind, e);
		}
	}
	
	private static String readSort(IStrategoTerm pattern) {
		String result = termContents(findTerm(pattern, "Sort"));
		String listSort = termContents(findTerm(pattern, "ListSort"));
		if (listSort != null) result = listSort + "*";
		return result;
	}
	
	public T getAttribute(String constructor, String sort, int tokenKind) {
		if (this.constructor == null || this.constructor.equals(constructor)) {
			if (this.sort == null || this.sort.equals(sort)) {
				if (this.tokenKind == TK_NO_TOKEN_KIND || this.tokenKind == tokenKind) {
					return attribute;
				}
			}
		}
		return null;
	}
	
	public T getAttribute() {
		return attribute;
	}
	
	public static<T> T getFirstAttribute(List<NodeMapping<T>> mappings, String constructor, String sort, int tokenKind) {
		for (int i = 0; i < mappings.size(); i++) {
			T result = mappings.get(i).getAttribute(constructor, sort, tokenKind);
			if (result != null) return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static<T> boolean hasAttribute(List<? extends NodeMapping> mappings, String constructor, String sort, int tokenKind) {
		return getFirstAttribute((List<NodeMapping<T>>) mappings, constructor, sort, tokenKind) != null;
	}
	
	@Override
	public String toString() {
		return "(" + constructor + "," + sort + "," + Token.tokenKindToString(tokenKind).toString()
				+ " => " + attribute + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeMapping)) return false;
		NodeMapping o = (NodeMapping) obj;
		return equals(attribute, o.attribute)
			&& equals(constructor, o.constructor)
			&& equals(sort, o.sort)
			&& tokenKind == o.tokenKind;
	}
	
	private static boolean equals(Object o1, Object o2) {
		if (o1 == null) return o2 == null;
		else return o1.equals(o2);
	}
	
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
