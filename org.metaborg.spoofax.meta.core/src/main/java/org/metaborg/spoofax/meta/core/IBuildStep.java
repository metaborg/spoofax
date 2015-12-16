package org.metaborg.spoofax.meta.core;

public interface IBuildStep {
	/**
	 * Pre-Java compilation
	 */
	void compilePreJava(MetaBuildInput input);
	
	/**
	 * Post-Java compilation
	 */
	void compilePostJava(MetaBuildInput input);
}