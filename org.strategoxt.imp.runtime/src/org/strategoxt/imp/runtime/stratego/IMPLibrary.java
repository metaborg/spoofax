package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;
import org.strategoxt.stratego_xtc.xtc_command_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	private static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new SubtermPrimitive());
	}
	
	public static void init() {
		xtc_command_1_0.instance = new SDFBundleCommand();
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
