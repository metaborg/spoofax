package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	private static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new SubtermPrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
