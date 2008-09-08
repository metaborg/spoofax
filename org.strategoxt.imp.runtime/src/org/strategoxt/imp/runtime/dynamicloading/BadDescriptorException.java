package org.strategoxt.imp.runtime.dynamicloading;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BadDescriptorException extends Exception {
	private static final long serialVersionUID = 54547354756L;
	
	private static final String DEFAULT_MESSAGE = "Bad editor service descriptor.";
	
	private IStrategoTerm offendingTerm;
	
	public IStrategoTerm getOffendingTerm() {
		return offendingTerm;
	}
	
	public BadDescriptorException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadDescriptorException(String message, IStrategoTerm cause) {
		super(message);
		offendingTerm = cause;
	}
	
	public BadDescriptorException(String message) {
		super(message);
	}

	public BadDescriptorException(Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}
	
	public BadDescriptorException() {
		super(DEFAULT_MESSAGE);
	}
}