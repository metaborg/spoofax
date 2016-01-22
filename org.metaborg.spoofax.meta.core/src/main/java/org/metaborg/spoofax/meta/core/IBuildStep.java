package org.metaborg.spoofax.meta.core;

public interface IBuildStep {
	/**
	 * Pre-Java compilation
	 */
	void compilePreJava(LanguageSpecBuildInput input);
	
	/**
	 * Post-Java compilation
	 */
	void compilePostJava(LanguageSpecBuildInput input);
}