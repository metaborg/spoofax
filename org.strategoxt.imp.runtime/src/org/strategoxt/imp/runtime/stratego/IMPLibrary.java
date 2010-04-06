package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	public static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new SubtermPrimitive());
		add(new ProjectPathPrimitive());
		add(new RefreshResourcePrimitive());
		add(new OriginLocationPrimitive());
		add(new OriginStripPrimitive());
		add(new OriginTermPrimitive());
		add(new OriginTextPrimitive());
		add(new OriginEqualPrimitive());
		add(new OriginSurroundingCommentsPrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
