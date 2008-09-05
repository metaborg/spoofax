package org.strategoxt.imp.runtime.services;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * A convenience class, subclassing {@link NodeMapping} with an argument
 * of type {@link TextAttributeReference}. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TextAttributeMapping extends NodeMapping<TextAttributeReference> {

	protected TextAttributeMapping(IStrategoTerm pattern, TextAttributeReference attribute)
			throws BadDescriptorException {
		
		super(pattern, attribute);
	}

	public static TextAttributeMapping create(IStrategoTerm pattern, TextAttributeReference attribute)
			throws BadDescriptorException {
		
		return new TextAttributeMapping(pattern, attribute);
	}
}
