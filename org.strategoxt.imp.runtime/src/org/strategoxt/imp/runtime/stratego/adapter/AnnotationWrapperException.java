package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.adapter.aterm.WrapperException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AnnotationWrapperException extends WrapperException {
	private static final long serialVersionUID = 5194285844333201168L;

	public AnnotationWrapperException(String message) {
		super(message);
	}
}
